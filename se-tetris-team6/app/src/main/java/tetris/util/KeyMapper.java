package tetris.util;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

/**
 * Utility to map human-friendly key names (e.g. "LEFT", "A", "SPACE") to
 * KeyEvent VK_* integer codes and vice-versa.
 */
public final class KeyMapper {

    private KeyMapper() {}

    public static int nameToKeyCode(String name) {
        if (name == null) return -1;
        String n = name.trim().toUpperCase();
        // allow direct number or single char
        if (n.length() == 1) {
            char c = n.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                try {
                    Field f = KeyEvent.class.getField("VK_" + c);
                    return f.getInt(null);
                } catch (Exception ex) {
                    // fallthrough
                }
            }
            if (c >= '0' && c <= '9') {
                try {
                    Field f = KeyEvent.class.getField("VK_" + c);
                    return f.getInt(null);
                } catch (Exception ex) {
                }
            }
        }

        // try common names directly
        String fieldName = "VK_" + n.replaceAll("\\s+", "_");
        try {
            Field f = KeyEvent.class.getField(fieldName);
            return f.getInt(null);
        } catch (Exception e) {
            // allow some synonyms
            switch (n) {
                case "LEFT": return KeyEvent.VK_LEFT;
                case "RIGHT": return KeyEvent.VK_RIGHT;
                case "UP": return KeyEvent.VK_UP;
                case "DOWN": return KeyEvent.VK_DOWN;
                case "SPACE": return KeyEvent.VK_SPACE;
                case "ENTER": return KeyEvent.VK_ENTER;
                case "ESC":
                case "ESCAPE": return KeyEvent.VK_ESCAPE;
                case "CTRL":
                case "CONTROL": return KeyEvent.VK_CONTROL;
                case "SHIFT": return KeyEvent.VK_SHIFT;
                case "ALT": return KeyEvent.VK_ALT;
                default:
                    return -1;
            }
        }
    }

    public static String keyCodeToName(int keyCode) {
        return KeyEvent.getKeyText(keyCode);
    }
}
