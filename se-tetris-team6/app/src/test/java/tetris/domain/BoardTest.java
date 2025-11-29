package tetris.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void canPlace_and_place_and_clearLines_work() {
        Board b = new Board();
    ShapeView small = BlockShape.of(BlockKind.O);
    assertTrue(b.canPlace(small, 0,0));
    b.place(small, 0, 0, 1);
        assertFalse(b.canPlace(small, 0,0));

        // fill a row
        for (int x = 0; x < Board.W; x++) b.setCell(x, Board.H - 1, 1);
        int cleared = b.clearLines();
        assertTrue(cleared >= 1);
    }

    @Test
    void clearArea_and_clear_are_defensive() {
        Board b = new Board();
        b.clearArea(-5, -5, 0, 0); // should be no-op
        b.clear();
        assertNotNull(b.gridView());
        b.setCell(-1, -1, 5); // no exception
    }
}
