package com.qualitylogic.x3mf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.SwingUtilities;

/** Exercise the real GUI report presenter with an actual placement fixture, headlessly. */
public final class StructureWarningTest {
    private static Field field(String name) throws Exception {
        Field field = MainFrame.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
    private static FileStructureCheck validate(String path, String schema) {
        StartUp.brunCli = true;
        FileStructureCheck result = new FileStructureCheck();
        result.fCurrentFile = path;
        result.fSchemaLoc = schema;
        result.iErrMaxStr = 100;
        result.keyinfo = new ArrayList<>();
        result.checkFile();
        return result;
    }
    private static void require(boolean condition, String text) {
        if (!condition) throw new AssertionError(text);
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        FileStructureCheck negative = validate(args[0], args[3]);
        require(negative.fErrorReturn.isEmpty(), "Placement policy must remain a warning");
        require(negative.fxmlWarningReturn.contains("WR_028"), "Missing placement diagnostic");
        require(negative.fWarningReturn.contains("-10.1, -10.1, 30.1"), "Unexpected transformed minimum");
        FileStructureCheck positive = validate(args[1], args[3]);
        FileStructureCheck error = validate(args[2], args[3]);
        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame();
                int cases = 0;
                for (boolean cli : new boolean[]{false, true})
                    for (boolean guiHide : new boolean[]{false, true})
                        for (boolean cliHide : new boolean[]{false, true}) {
                            StartUp.brunCli = cli;
                            StartUp.bHideWarnCLI = cliHide;
                            field("bdoWarn").setBoolean(frame, guiHide);
                            field("saveForConsole").set(frame, "");
                            frame.reportStructureResult(negative);
                            String html = MainFrame.txtConsole.getText();
                            String xml = (String)field("fxmlWarningReturn").get(frame);
                            boolean hidden = cli ? cliHide : guiHide;
                            require(html.contains("WR_028"), "Missing warning code in visible status");
                            require(!html.contains("No structural errors"), "False green success with warnings");
                            require(xml.contains("WR_028"), "Presentation preference suppressed XML diagnostic");
                            require(((String)field("fxmlErrorReturn").get(frame)).isEmpty(), "Warning promoted to error");
                            require(field("bStructError").getBoolean(frame), "Warning state lost");
                            require(html.contains("Warning details are hidden") == hidden, "Wrong warning preference selected");
                            require(html.contains("minimum coordinates") != hidden, "Warning detail filtering failed");
                            cases++;
                        }
                field("saveForConsole").set(frame, "");
                frame.reportStructureResult(positive);
                require(MainFrame.txtConsole.getText().contains("No structural errors"), "Positive control lost success status");
                require(((String)field("fxmlWarningReturn").get(frame)).isEmpty(), "Stale warning after positive result");
                require(!field("bStructError").getBoolean(frame), "Stale warning status after positive result");
                field("saveForConsole").set(frame, "");
                frame.reportStructureResult(error);
                require(MainFrame.txtConsole.getText().contains("ER_179"), "Error control lost failure diagnostic");
                require(!MainFrame.txtConsole.getText().contains("No structural errors"), "False green success after error");
                System.out.println("Report visibility checks passed: " + (cases + 2));
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
}
