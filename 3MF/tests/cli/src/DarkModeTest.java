package com.qualitylogic.x3mf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.io.File;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.JCheckBoxMenuItem;

public class DarkModeTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                boolean editorSmoke = args.length > 0 && args[0].equals("editor");
                StartUp.brunCli = !editorSmoke;
                MainFrame frame = new MainFrame();
                frame.add(frame.mfPane);
                frame.add(frame.xmlPane);
                frame.add(frame.statPane);
                frame.add(frame.consolePane);
                Color original = frame.xmlEd.getBackground();
                frame.xmlEd.setText("<model unit=\"millimeter\">plain<!--comment--><![CDATA[data]]></model>");
                Document document = frame.xmlEd.getDocument();
                document.addUndoableEditListener(frame.undoManager);
                frame.xmlEd.getDocument().insertString(0, " ", null);
                int length = document.getLength();
                MainFrame.txtConsole.setText("ordinary <font color='red'>error</font>"
                    + "<font color='green'>success</font><font color='DAA520'>warning</font><a href='1'>line</a>");
                String lightReport = MainFrame.txtConsole.getText();
                StartUp.brunCli = true;
                frame.setDarkMode(true);
                check(original.equals(frame.xmlEd.getBackground()), "CLI ignores theme requests");
                StartUp.brunCli = false;
                if (editorSmoke) {
                    JFrame window = (JFrame) Window.getWindows()[0];
                    JCheckBoxMenuItem toggle = (JCheckBoxMenuItem) window.getJMenuBar().getMenu(2).getItem(2);
                    check(toggle.getText().equals("Dark Mode"), "View menu toggle");
                    toggle.doClick();
                    check(toggle.isSelected(), "menu selected state");
                } else frame.setDarkMode(true);
                check(frame.xmlEd.getBackground().equals(EditorTheme.BACKGROUND), "XML surface");
                check(frame.txtStat.getForeground().equals(EditorTheme.FOREGROUND), "statistics text");
                check(MainFrame.txtConsole.getText().contains("#ff9292"), "HTML error palette");
                check(MainFrame.txtConsole.getText().contains("#8cdda0"), "HTML success palette");
                check(MainFrame.txtConsole.getText().contains("#f1cf75"), "HTML warning palette");
                DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) frame.getTree().getCellRenderer();
                check(renderer.getTextNonSelectionColor().equals(EditorTheme.FOREGROUND), "replacement tree");
                check(!renderer.getTextSelectionColor().equals(renderer.getBackgroundSelectionColor()), "tree selection");
                check(frame.xmlEd.getDocument() == document && document.getLength() == length, "document preserved");
                check(frame.undoManager.canUndo(), "undo preserved");
                frame.xmlEd.setSize(850, 100);
                BufferedImage image = new BufferedImage(850, 100, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                frame.xmlEd.paint(graphics);
                graphics.dispose();
                int ordinaryPixels = 0;
                for (int y = 0; y < image.getHeight(); y++)
                    for (int x = 0; x < image.getWidth(); x++)
                        if (image.getRGB(x, y) == EditorTheme.FOREGROUND.getRGB()) ordinaryPixels++;
                check(ordinaryPixels > 10, "custom XML view paints ordinary text in dark foreground");
                ImageIO.write(image, "png", new File("dark-xml.png"));
                frame.setDarkMode(false);
                check(frame.xmlEd.getBackground().equals(original), "light colors restored");
                check(MainFrame.txtConsole.getText().equals(lightReport), "light HTML restored exactly");
                MainFrame.txtConsole.setText("");
                frame.setDarkMode(true);
                MainFrame.txtConsole.setText("new <font color='red'>error</font>");
                check(MainFrame.txtConsole.getText().contains("#ff9292"), "new console updates themed");
                frame.setDarkMode(false);
                IniData settings = new IniData();
                check(!settings.darkMode, "default light");
                settings.darkMode = true;
                settings.writeIni();
                settings = new IniData();
                settings.readIni();
                check(settings.darkMode, "dark preference round trip");
                settings.darkMode = false;
                settings.writeIni();
                settings = new IniData();
                settings.readIni();
                check(!settings.darkMode, "light preference round trip");
                System.out.println("PASS: dark mode surfaces, tree replacement, HTML updates, XML painting, undo, restoration, persistence and CLI isolation");
                for (Window window : Window.getWindows()) window.dispose();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
