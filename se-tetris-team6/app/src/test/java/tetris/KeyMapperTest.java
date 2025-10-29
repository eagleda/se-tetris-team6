package tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.Test;

import tetris.util.KeyMapper;

class KeyMapperTest {

    @Test
    void nameToKeyCode_and_back() {
        assertEquals(KeyEvent.VK_LEFT, KeyMapper.nameToKeyCode("LEFT"));
        assertEquals(KeyEvent.VK_RIGHT, KeyMapper.nameToKeyCode("Right"));
        assertEquals(KeyEvent.VK_SPACE, KeyMapper.nameToKeyCode("SPACE"));
        assertEquals(KeyEvent.VK_A, KeyMapper.nameToKeyCode("A"));

        // reverse
        assertEquals("Left", KeyMapper.keyCodeToName(KeyEvent.VK_LEFT));
        assertTrue(KeyMapper.keyCodeToName(KeyEvent.VK_A).matches("(?i)a"));
    }
}
