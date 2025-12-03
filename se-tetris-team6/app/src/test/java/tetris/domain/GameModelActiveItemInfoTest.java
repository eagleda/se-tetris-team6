package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.GameModel.ActiveItemInfo
 *
 * 역할 요약:
 * - 아이템 블록 정보(label/type/아이템 셀 위치)를 보관하는 불변 객체.
 *
 * 테스트 전략:
 * - 생성자 인자를 그대로 노출하는지, itemCell 좌표에 따라 hasItemCell이 올바르게 동작하는지 확인.
 */
class GameModelActiveItemInfoTest {

    @Test
    void getters_andHasItemCell_workAsExpected() {
        GameModel.ActiveItemInfo info = new GameModel.ActiveItemInfo(
                new tetris.domain.block.BlockLike() {
                    @Override public tetris.domain.BlockShape getShape() { return tetris.domain.BlockShape.of(tetris.domain.BlockKind.I); }
                    @Override public tetris.domain.BlockKind getKind() { return tetris.domain.BlockKind.I; }
                    @Override public int getX() { return 0; }
                    @Override public int getY() { return 0; }
                    @Override public void setPosition(int x, int y) {}
                },
                "double_score",
                tetris.domain.item.ItemType.TIMED,
                1, 2);

        assertEquals("double_score", info.label());
        assertEquals(tetris.domain.item.ItemType.TIMED, info.type());
        assertTrue(info.hasItemCell());
        assertEquals(1, info.itemCellX());
        assertEquals(2, info.itemCellY());
        assertEquals(tetris.domain.BlockKind.I, info.block().getKind());
    }

    @Test
    void hasItemCell_falseWhenUnset() {
        GameModel.ActiveItemInfo info = new GameModel.ActiveItemInfo(
                null, null, tetris.domain.item.ItemType.INSTANT, -1, -1);
        assertFalse(info.hasItemCell());
    }
}
