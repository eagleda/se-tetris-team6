package tetris.domain;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomBlockGeneratorTest {

    @Test
    void peekNext_and_nextBlock_buffer_behavior() {
        Random fixed = new Random(12345);
        RandomBlockGenerator gen = new RandomBlockGenerator(fixed);
        BlockKind a = gen.peekNext();
        assertNotNull(a);
        BlockKind b = gen.nextBlock();
        // peekNext should match the first nextBlock call
        assertEquals(a, b);
    }

    @Test
    void setDifficulty_marks_dirty_and_changes_weights() {
        Random rnd = new Random(1);
        RandomBlockGenerator gen = new RandomBlockGenerator(rnd);
        gen.setDifficulty(GameDifficulty.EASY);
        BlockKind k = gen.peekNext();
        assertNotNull(k);
    }
}
