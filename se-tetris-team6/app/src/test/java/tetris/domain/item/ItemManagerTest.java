/**
 * 대상: tetris.domain.item.ItemManager
 *
 * 목적:
 * - add/remove/viewActive/tick/onLock/onLineClear/clear 흐름이 예외 없이 수행되고
 *   활성 아이템 목록이 예상대로 변하는지 검증하여 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) add로 등록 후 viewActive로 읽을 수 있는지 확인
 * 2) tick/onLineClear가 등록된 아이템에 위임되는지 확인
 * 3) onLock 호출 시 아이템이 제거되는지 확인
 * 4) clear로 목록이 비워지는지 확인
 */
package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;
import tetris.domain.item.ItemManager;
import tetris.domain.item.ItemType;
import tetris.domain.item.model.ItemBlockModel;

class ItemManagerTest {

    private static final class DummyBlockLike implements tetris.domain.block.BlockLike {
        @Override public BlockShape getShape() { return BlockShape.of(BlockKind.I); }
        @Override public BlockKind getKind() { return BlockKind.I; }
        @Override public int getX() { return 0; }
        @Override public int getY() { return 0; }
        @Override public void setPosition(int x, int y) { }
    }

    @Test
    void add_tick_lineClear_lock_and_clear_flow() {
        ItemManager manager = new ItemManager();
        CountingBehavior behavior = new CountingBehavior();
        ItemBlockModel item = new ItemBlockModel(new DummyBlockLike(), java.util.List.of(behavior));

        manager.add(item);
        assertEquals(1, manager.viewActive().size());

        manager.tick(null, 1L);
        manager.onLineClear(null, new int[] {0});
        assertEquals(1, behavior.tickCount);
        assertEquals(1, behavior.lineClearCount);

        manager.onLock(null, item);
        assertEquals(1, behavior.lockCount);
        assertTrue(manager.viewActive().isEmpty(), "onLock should remove item");

        manager.add(new ItemBlockModel(new DummyBlockLike(), java.util.List.of(new CountingBehavior())));
        manager.clear();
        assertTrue(manager.viewActive().isEmpty(), "clear should remove all items");
    }

    private static final class CountingBehavior implements ItemBehavior {
        int tickCount = 0;
        int lockCount = 0;
        int lineClearCount = 0;

        @Override
        public String id() { return "count"; }

        @Override
        public ItemType type() { return ItemType.ACTIVE; }

        @Override
        public void onTick(ItemContext ctx, ItemBlockModel block, long tick) {
            tickCount++;
        }

        @Override
        public void onLock(ItemContext ctx, ItemBlockModel block) {
            lockCount++;
        }

        @Override
        public void onLineClear(ItemContext ctx, ItemBlockModel block, int[] clearedRows) {
            lineClearCount++;
        }
    }
}
