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
}
