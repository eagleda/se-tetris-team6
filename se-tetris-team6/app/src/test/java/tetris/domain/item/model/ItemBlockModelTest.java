package tetris.domain.item.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;

/*
 * 테스트 대상: tetris.domain.item.model.ItemBlockModel
 *
 * 역할 요약:
 * - 블록 모델을 감싸 아이템 관련 행동(ItemBehavior)과 아이템 셀 좌표를 관리한다.
 * - 회전 시 아이템 셀 좌표를 업데이트하고, onSpawn/onLock/onTick/onLineClear를 behaviors에 위임한다.
 *
 * 테스트 전략:
 * - 아이템 셀 좌표 회전(updateItemCellAfterRotation) 검증.
 * - behaviors 위임 여부를 스텁 구현으로 호출 횟수 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - hasItemCell=true일 때 updateItemCellAfterRotation이 좌표를 시계방향 회전에 맞게 변환하는지 확인.
 * - 각 이벤트(onSpawn/onLock/onTick/onLineClear)가 behavior에 전달되는지 확인.
 */
class ItemBlockModelTest {

    @Test
    void updateItemCellAfterRotation_rotatesCoordinatesCW() {
        ItemBlockModel model = new ItemBlockModel(dummyBlock(2, 3), List.of(), 0, 1);
        assertTrue(model.hasItemCell());

        model.updateItemCellAfterRotation();

        // 현재 구현은 prevShape 높이(2)를 사용하여 (h-1-y, x) -> (0,0)으로 이동
        assertEquals(0, model.getItemCellX());
        assertEquals(0, model.getItemCellY());
    }

    @Test
    void behaviors_receiveDelegatedEvents() {
        TrackingBehavior behavior = new TrackingBehavior();
        ItemBlockModel model = new ItemBlockModel(dummyBlock(1, 1), List.of(behavior));
        ItemContext ctx = new NoopItemContext(); // no-op stub

        model.onSpawn(ctx);
        model.onTick(ctx, 5);
        model.onLineClear(ctx, new int[]{0, 1});
        model.onLock(ctx);

        assertEquals(1, behavior.spawnCount);
        assertEquals(1, behavior.lockCount);
        assertEquals(1, behavior.tickCount);
        assertEquals(1, behavior.lineClearCount);
        assertArrayEquals(new int[]{0, 1}, behavior.lastClearedRows);
    }

    private BlockLike dummyBlock(int w, int h) {
        boolean[][] mask = new boolean[h][w];
        for (int y = 0; y < h; y++) mask[y][0] = true;
        return new BlockLike() {
            private int x, y;
            @Override public BlockShape getShape() { return new BlockShape(BlockKind.I, mask); }
            @Override public BlockKind getKind() { return BlockKind.I; }
            @Override public int getX() { return x; }
            @Override public int getY() { return y; }
            @Override public void setPosition(int x, int y) { this.x = x; this.y = y; }
        };
    }

    private static class TrackingBehavior implements ItemBehavior {
        int spawnCount, lockCount, tickCount, lineClearCount;
        int[] lastClearedRows;

        @Override public String id() { return "tracking"; }
        @Override public tetris.domain.item.ItemType type() { return tetris.domain.item.ItemType.ACTIVE; }
        @Override public void onSpawn(ItemContext context, ItemBlockModel model) { spawnCount++; }
        @Override public void onLock(ItemContext context, ItemBlockModel model) { lockCount++; }
        @Override public void onTick(ItemContext context, ItemBlockModel model, long tick) { tickCount++; }
        @Override public void onLineClear(ItemContext context, ItemBlockModel model, int[] clearedRows) {
            lineClearCount++;
            lastClearedRows = clearedRows;
        }
    }

    private static class NoopItemContext implements ItemContext {
        @Override public tetris.domain.Board getBoard() { return new tetris.domain.Board(); }
        @Override public void requestClearCells(int x, int y, int width, int height) {}
        @Override public void requestAddBlocks(int x, int y, int[][] cells) {}
        @Override public void applyScoreDelta(int points) {}
        @Override public void addGlobalBuff(String buffId, long durationTicks, java.util.Map<String, Object> meta) {}
        @Override public long currentTick() { return 0; }
        @Override public void spawnParticles(int x, int y, String type) {}
        @Override public void playSfx(String id) {}
    }
}
