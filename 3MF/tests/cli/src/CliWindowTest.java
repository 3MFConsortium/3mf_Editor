import com.qualitylogic.x3mf.MainFrame;
import com.qualitylogic.x3mf.StartUp;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Checks CLI window isolation, including when AWT initially defaults to desktop mode. */
public class CliWindowTest {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("editor")) {
            SwingUtilities.invokeAndWait(() -> {
                MainFrame editor = new MainFrame();
                editor.start();
                Window[] windows = Window.getWindows();
                if (windows.length != 1 || !(windows[0] instanceof JFrame))
                    throw new AssertionError("Expected one editor window");
                JFrame frame = (JFrame) windows[0];
                if (!frame.isVisible() || frame.getContentPane() != editor
                        || frame.getJMenuBar() == null || !frame.getTitle().equals("3MF Edit and Test"))
                    throw new AssertionError("Editor window setup failed");
                frame.dispose();
            });
            System.exit(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!GraphicsEnvironment.isHeadless() || Window.getWindows().length != 0) {
                System.err.println("CLI initialized desktop AWT or created a window");
                Runtime.getRuntime().halt(1);
            }
        }));
        StartUp.main(args);
    }
}
