package tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;
import tetris.domain.item.ItemManager;
import tetris.domain.item.ItemType;
import tetris.domain.item.model.ItemBlockModel;
import tetris.domain.model.Block;

class ItemManagerTest {

    private ItemManager manager;
    private TestContext context;
    private Block block;

    @BeforeEach
    void setUp() {
        manager = new ItemManager();
        context = new TestContext();
        block = Block.spawn(BlockKind.I, 0, 0);
    }

    @Test
    void tickDelegatesToBehaviors() {
        TestBehavior behavior = new TestBehavior();
        ItemBlockModel item = new ItemBlockModel(block, List.of(behavior));

        manager.add(item);
        manager.tick(context, 42L);

        assertEquals(1, behavior.tickCalls);
        assertEquals(42L, behavior.lastTick);
    }

    @Test
    void lockRemovesItemAndInvokesBehavior() {
        TestBehavior behavior = new TestBehavior();
        ItemBlockModel item = new ItemBlockModel(block, List.of(behavior));

        manager.add(item);
        manager.onLock(context, item);

        assertTrue(behavior.lockCalled);
        assertTrue(manager.viewActive().isEmpty());
    }

    @Test
    void lineClearBroadcastsToActiveItems() {
        TestBehavior behavior = new TestBehavior();
        ItemBlockModel item = new ItemBlockModel(block, List.of(behavior));
        manager.add(item);

        manager.onLineClear(context, new int[]{1, 2});
        assertArrayEquals(new int[]{1, 2}, behavior.lastClearedRows);
    }

    private static final class TestBehavior implements ItemBehavior {
        int tickCalls;
        long lastTick;
        boolean lockCalled;
        int[] lastClearedRows;

        @Override
        public String id() {
            return "test";
        }

        @Override
        public ItemType type() {
            return ItemType.INSTANT;
        }

        @Override
        public void onTick(ItemContext ctx, ItemBlockModel block, long tick) {
            tickCalls++;
            lastTick = tick;
        }

        @Override
        public void onLock(ItemContext ctx, ItemBlockModel block) {
            lockCalled = true;
        }

        @Override
        public void onLineClear(ItemContext ctx, ItemBlockModel block, int[] clearedRows) {
            lastClearedRows = clearedRows;
        }
    }

    private static final class TestContext implements ItemContext {
        Map<String, Object> lastBuffMeta;
        String lastBuffId;
        long lastDuration;

        @Override public tetris.domain.Board getBoard() { return new tetris.domain.Board(); }
        @Override public void requestClearCells(int x, int y, int w, int h) {}
        @Override public void requestAddBlocks(int x, int y, int[][] cells) {}
        @Override public void applyScoreDelta(int points) {}
        @Override public void addGlobalBuff(String buffId, long durationTicks, Map<String, Object> meta) {
            this.lastBuffId = buffId;
            this.lastDuration = durationTicks;
            this.lastBuffMeta = meta;
        }
        @Override public long currentTick() { return 0; }
        @Override public void spawnParticles(int x, int y, String type) {}
        @Override public void playSfx(String id) {}
    }
}
