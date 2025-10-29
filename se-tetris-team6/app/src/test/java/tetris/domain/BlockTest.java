package tetris.domain;

import org.junit.jupiter.api.Test;
import tetris.domain.model.Block;
import tetris.domain.BlockShape;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {

    @Test
    void spawn_and_move_and_copy_work() {
        Block b = Block.spawn(BlockKind.I, 3, 0);
        assertEquals(BlockKind.I, b.getKind());
        int oldX = b.getX();
        b.moveBy(1,2);
        assertEquals(oldX + 1, b.getX());
        Block copy = b.copy();
        assertEquals(b.getX(), copy.getX());
    }

    @Test
    void rotate_updates_shape() {
        Block b = Block.spawn(BlockKind.T, 0,0);
        BlockShape before = b.getShape();
        b.rotateCW();
        assertNotEquals(before, b.getShape());
    }
}
