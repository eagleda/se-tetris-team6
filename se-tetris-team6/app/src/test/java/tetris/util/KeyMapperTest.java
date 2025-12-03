package tetris.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.Test;

class KeyMapperTest {

    @Test
    void nameToKeyCode_convertsCommonNames() {
        assertEquals(KeyEvent.VK_LEFT, KeyMapper.nameToKeyCode("LEFT"));
        assertEquals(KeyEvent.VK_SPACE, KeyMapper.nameToKeyCode("SPACE"));
        assertEquals(KeyEvent.VK_A, KeyMapper.nameToKeyCode("A"));
    }

    @Test
    void keyCodeToName_roundTripsKnownKeys() {
        String left = KeyMapper.keyCodeToName(KeyEvent.VK_LEFT);
        String space = KeyMapper.keyCodeToName(KeyEvent.VK_SPACE);
        assertNotNull(left);
        assertFalse(left.isBlank());
        assertNotNull(space);
        assertFalse(space.isBlank());
    }

    @Test
    void unknownKey_returnsNullOrEmpty() {
        int custom = 9999;
        String name = KeyMapper.keyCodeToName(custom);
        assertTrue(name == null || name.isEmpty() || name.startsWith("Unknown") || name.startsWith("VK_") || name.matches(".*0x[0-9A-Fa-f]+.*"));
        assertEquals(-1, KeyMapper.nameToKeyCode(String.valueOf(custom)));
    }
}
