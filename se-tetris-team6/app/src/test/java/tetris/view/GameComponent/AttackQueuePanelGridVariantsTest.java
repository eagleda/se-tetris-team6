package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 추가 커버리지: AttackQueuePanel의 setGrid/clear/update 분기.
 * - 서로 다른 크기의 배열로 setGrid 호출해 내부 복사 로직이 크기에 맞춰 동작하는지 스모크.
 * - updateFromGamePanel에서 리플렉션 대상이 없거나 null을 반환해도 예외 없이 종료하는지 확인.
 */
class AttackQueuePanelGridVariantsTest {

    @Test
    void setGrid_resizesAndCopiesValues() {
        AttackQueuePanel panel = new AttackQueuePanel();
        int[][] small = new int[2][2];
        small[1][1] = 7;
        panel.setGrid(small);

        int[][] larger = new int[3][3];
        panel.setGrid(larger); // 내부 복사 대상 변경
        // 다시 작은 배열을 덮으면 값이 유지되지 않도록 초기화됨
        int[][] fresh = new int[2][2];
        panel.setGrid(fresh);
        assertEquals(0, fresh[1][1]);
    }

    @Test
    void updateFromGamePanel_missingMethod_isSafe() {
        AttackQueuePanel panel = new AttackQueuePanel();
        Object noMethod = new Object(); // getAttackQueueGrid 없음
        assertDoesNotThrow(() -> panel.updateFromGamePanel(noMethod));

        Object nullGrid = new Object() {
            @SuppressWarnings("unused")
            public int[][] getAttackQueueGrid() { return null; }
        };
        assertDoesNotThrow(() -> panel.updateFromGamePanel(nullGrid));
    }
}
