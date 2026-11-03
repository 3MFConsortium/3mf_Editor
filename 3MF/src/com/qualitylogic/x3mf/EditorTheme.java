package com.qualitylogic.x3mf;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellRenderer;

/** Per-editor colors; never changes Swing defaults or CLI report presentation. */
final class EditorTheme {
    static final Color BACKGROUND = new Color(30, 32, 36);
    static final Color FOREGROUND = new Color(225, 228, 232);
    private static final Color SELECTION = new Color(55, 86, 125);
    private final Map<Component, Color[]> originals = new WeakHashMap<>();

    void apply(Component component, boolean dark) {
        Color[] saved = originals.get(component);
        if (saved == null) {
            saved = new Color[] {component.getBackground(), component.getForeground()};
            originals.put(component, saved);
        }
        component.setBackground(dark ? BACKGROUND : saved[0]);
        component.setForeground(dark ? FOREGROUND : saved[1]);
        if (component instanceof JTextComponent) {
            JTextComponent text = (JTextComponent) component;
            if (saved.length == 2) {
                saved = new Color[] {saved[0], saved[1], text.getCaretColor(),
                    text.getSelectionColor(), text.getSelectedTextColor()};
                originals.put(component, saved);
            }
            text.setCaretColor(dark ? FOREGROUND : saved[2]);
            text.setSelectionColor(dark ? SELECTION : saved[3]);
            text.setSelectedTextColor(dark ? Color.WHITE : saved[4]);
            text.putClientProperty("3mf.darkMode", dark);
        }
        if (component instanceof JTree) {
            JTree tree = (JTree) component;
            if (tree.getCellRenderer() instanceof DefaultTreeCellRenderer) {
                DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
                Color[] colors = originals.get(renderer);
                if (colors == null) {
                    colors = new Color[] {renderer.getBackgroundNonSelectionColor(),
                        renderer.getTextNonSelectionColor(), renderer.getBackgroundSelectionColor(),
                        renderer.getTextSelectionColor(), renderer.getBorderSelectionColor()};
                    originals.put(renderer, colors);
                }
                renderer.setBackgroundNonSelectionColor(dark ? BACKGROUND : colors[0]);
                renderer.setTextNonSelectionColor(dark ? FOREGROUND : colors[1]);
                renderer.setBackgroundSelectionColor(dark ? SELECTION : colors[2]);
                renderer.setTextSelectionColor(dark ? Color.WHITE : colors[3]);
                renderer.setBorderSelectionColor(dark ? FOREGROUND : colors[4]);
            }
            // Renderer colors are managed above; it is not a normal child surface.
            tree.repaint();
            return;
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) apply(child, dark);
        }
        component.repaint();
    }

    static final class ConsolePane extends JEditorPane {
        private static final long serialVersionUID = 1L;
        private static final Pattern FONT = Pattern.compile(
            "(?i)(<font\\s+color=['\"]?)(red|green|DAA520)(['\"]?\\s*>)");
        private String source = "";
        private boolean dark;

        void setDark(boolean dark) {
            this.dark = dark;
            setText(source);
        }

        @Override public void setText(String text) {
            source = text == null ? "" : text;
            // HTMLDocument retains style rules across setText calls. Discard the
            // console document so dark rules cannot leak into light mode.
            if (getEditorKit() != null) setDocument(getEditorKit().createDefaultDocument());
            if (!dark) {
                super.setText(source);
                return;
            }
            Matcher matcher = FONT.matcher(source);
            StringBuffer result = new StringBuffer();
            while (matcher.find()) {
                String color = matcher.group(2);
                String replacement = color.equalsIgnoreCase("red") ? "#ff9292"
                    : color.equalsIgnoreCase("green") ? "#8cdda0" : "#f1cf75";
                matcher.appendReplacement(result, Matcher.quoteReplacement(
                    matcher.group(1) + replacement + matcher.group(3)));
            }
            matcher.appendTail(result);
            super.setText("<html><head><style>body { color: #e1e4e8; background-color: #1e2024; }"
                + "a { color: #91caff; }</style></head><body>" + result + "</body></html>");
        }
    }
}
