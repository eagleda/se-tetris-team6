package tetris.domain.item.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;

/*
 * 테스트 대상: tetris.domain.item.model.ItemBlockModel
 *
 * 역할 요약:
 * - BlockLike를 감싸 아이템 동작(행동 목록, 아이템 셀 좌표, 회전 보정)을 제공하는 어댑터.
 *
 * 테스트 전략:
 * - itemCell 설정/회전 보정 로직이 기대 좌표로 변환되는지 검증.
 * - onSpawn/onLock/onTick/onLineClear가 등록된 ItemBehavior에 위임되는지 확인.
 */
@ExtendWith(MockitoExtension.class)
class ItemBlockModelTest {

    @Mock ItemBehavior behavior;
    @Mock ItemContext ctx;

    @Test
    void itemCellRotation_updatesCoordinatesCW() {
        BlockLike delegate = new DummyBlock(BlockKind.I, 3, 4);
        ItemBlockModel model = new ItemBlockModel(delegate, List.of(), 0, 2);
        model.setPosition(5, 6);

        model.updateItemCellAfterRotation();

        assertTrue(model.hasItemCell());
        // 회전 후 좌표가 음수가 아니고, 현재 shape 크기 내에 존재하는지 확인
        BlockShape shape = model.getShape();
        assertTrue(model.getItemCellX() >= 0 && model.getItemCellX() < shape.width());
        assertTrue(model.getItemCellY() >= 0 && model.getItemCellY() < shape.height());
    }

    @Test
    void delegatesCallbacksToBehaviors() {
        ItemBlockModel model = new ItemBlockModel(new DummyBlock(BlockKind.O, 2, 2), List.of(behavior));

        model.onSpawn(ctx);
        model.onLock(ctx);
        model.onTick(ctx, 5L);
        model.onLineClear(ctx, new int[] { 1, 2 });

        verify(behavior).onSpawn(ctx, model);
        verify(behavior).onLock(ctx, model);
        verify(behavior).onTick(ctx, model, 5L);
        verify(behavior).onLineClear(ctx, model, new int[] { 1, 2 });
    }

    @Test
    void hasItemCell_returnsFalseWhenUnset() {
        ItemBlockModel model = new ItemBlockModel(new DummyBlock(BlockKind.S, 2, 2), List.of());
        assertFalse(model.hasItemCell());
        model.setItemCell(1, 0);
        assertTrue(model.hasItemCell());
    }

    /** 최소 BlockLike 구현체. */
    private static final class DummyBlock implements BlockLike {
        private final BlockShape shape;
        private int x;
        private int y;

        DummyBlock(BlockKind kind, int w, int h) {
            boolean[][] mask = new boolean[h][w];
            mask[0][0] = true;
            this.shape = new BlockShape(kind, mask);
        }

        @Override public BlockShape getShape() { return shape; }
        @Override public BlockKind getKind() { return shape.kind(); }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public void setPosition(int x, int y) { this.x = x; this.y = y; }
    }
}
