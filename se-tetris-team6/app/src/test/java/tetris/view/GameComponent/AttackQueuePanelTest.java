package tetris.view.GameComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.GameComponent.AttackQueuePanel
 *
 * 역할 요약:
 * - 공격 대기 줄을 10x10 그리드로 표시하는 패널.
 *
 * 테스트 전략:
 * - setGrid로 전달한 값이 내부 그리드에 반영되는지 리플렉션으로 검증.
 * - updateFromGamePanel이 리플렉션으로 int[][]를 받아들이는지 확인.
 */
class AttackQueuePanelTest {

    @Test
    void setGrid_copiesValues() throws Exception {
        AttackQueuePanel panel = new AttackQueuePanel();
        int[][] grid = new int[10][10];
        grid[0][0] = 1;
        panel.setGrid(grid);

        int[][] internal = getGrid(panel);
        assertEquals(1, internal[0][0]);
    }

    @Test
    void updateFromGamePanel_usesReflection() throws Exception {
        AttackQueuePanel panel = new AttackQueuePanel();
        Object stub = new Object() {
            @SuppressWarnings("unused")
            public int[][] getAttackQueueGrid() {
                int[][] g = new int[10][10];
                g[1][1] = 2;
                return g;
            }
        };

        panel.updateFromGamePanel(stub);
        int[][] internal = getGrid(panel);
        assertEquals(2, internal[1][1]);
    }

    @SuppressWarnings("unchecked")
    private int[][] getGrid(AttackQueuePanel panel) throws Exception {
        Field f = AttackQueuePanel.class.getDeclaredField("grid");
        f.setAccessible(true);
        return (int[][]) f.get(panel);
    }
}
