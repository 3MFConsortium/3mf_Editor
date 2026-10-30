import com.qualitylogic.x3mf.XmlTextPane;
import java.awt.image.BufferedImage;
import java.util.Locale;
import javax.swing.SwingUtilities;

/** Isolated display probe for issue #14; never opens or rewrites a package. */
public class LongLineProbe {
    private static volatile String stage = "generate";
    public static void main(String[] args) throws Exception {
        double megabytes = Double.parseDouble(args[0]);
        boolean lines = Boolean.parseBoolean(args[1]);
        int timeout = Integer.parseInt(args[2]);
        boolean bulk = args.length > 3 && Boolean.parseBoolean(args[3]);
        Thread watchdog = new Thread(() -> {
            try { Thread.sleep(timeout * 1000L); } catch (InterruptedException e) { return; }
            System.err.println("TIMEOUT stage=" + stage);
            Thread.getAllStackTraces().forEach((thread, stack) -> {
                if (thread.getName().startsWith("AWT-EventQueue")) {
                    System.err.println(thread.getName());
                    for (StackTraceElement frame : stack) System.err.println("  " + frame);
                }
            });
            Runtime.getRuntime().halt(124);
        }, "probe-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
        String vertex = "<vertex x=\"1.25\" y=\"2.5\" z=\"3.75\"/>" + (lines ? "\n" : "");
        StringBuilder xml = new StringBuilder("<model><resources><object id=\"1\"><mesh><vertices>");
        while (xml.length() < megabytes * 1_000_000) xml.append(vertex);
        xml.append("</vertices></mesh></object></resources></model>");
        String text = xml.toString();
        System.out.printf(Locale.ROOT, "bytes=%d lineBreaks=%s%n", text.length(), lines);
        SwingUtilities.invokeAndWait(() -> {
            stage = "construct";
            XmlTextPane pane = new XmlTextPane();
            pane.setSize(1000, 700);
            stage = "setText";
            long start = System.nanoTime();
            if (bulk) {
                // Investigation control: populate before attaching a measuring view.
                javax.swing.text.Document document = pane.getEditorKit().createDefaultDocument();
                try { document.insertString(0, text, null); }
                catch (javax.swing.text.BadLocationException e) { throw new RuntimeException(e); }
                pane.setDocument(document);
            } else {
                pane.setText(text);
            }
            System.out.printf(Locale.ROOT, "setText_ms=%.1f%n", (System.nanoTime() - start) / 1e6);
            stage = "paint";
            BufferedImage image = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D graphics = image.createGraphics();
            try {
                start = System.nanoTime();
                pane.paint(graphics);
                System.out.printf(Locale.ROOT, "paint_ms=%.1f%n", (System.nanoTime() - start) / 1e6);
            } finally { graphics.dispose(); }
            stage = "done";
        });
        System.exit(0);
    }
}
