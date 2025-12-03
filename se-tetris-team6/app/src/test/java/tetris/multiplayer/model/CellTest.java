package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.multiplayer.model.Cell
 *
 * 역할 요약:
 * - 잠긴 블록의 한 셀 좌표를 표현하는 불변 값 객체.
 *
 * 테스트 전략:
 * - x(), y() 접근자 동작.
 * - equals/hashCode가 좌표 동일성으로만 비교되는지 검증.
 */
class CellTest {

    @Test
    void equalityBasedOnCoordinates() {
        Cell a = new Cell(1, 2);
        Cell b = new Cell(1, 2);
        Cell c = new Cell(2, 1);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }
}
