/*
 * 테스트 대상: tetris.domain.item.ItemBehavior (인터페이스)
 *
 * 역할 요약:
 * - 아이템 효과 발동 시 onLock이 호출되는 전략 인터페이스.
 *
 * 테스트 전략:
 * - 가장 단순한 구현체를 만들어 임시 ItemContext/ItemBlockModel로 onLock 호출이
 *   예외 없이 통과하는지 확인합니다.
 */
package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.model.Block;
import tetris.domain.item.model.ItemBlockModel;

class ItemBehaviorSmokeTest {

    @Test
    void onLock_noOpImplementation_safe() {
        ItemBehavior behavior = new ItemBehavior() {
            @Override public String id() { return "noop"; }
            @Override public ItemType type() { return ItemType.INSTANT; }
            @Override public void onLock(ItemContext ctx, ItemBlockModel block) { }
        };

        ItemContext ctx = new ItemContext() {
            @Override public tetris.domain.Board getBoard() { return null; }
            @Override public void requestClearCells(int x, int y, int width, int height) {}
            @Override public void requestAddBlocks(int x, int y, int[][] cells) {}
            @Override public void applyScoreDelta(int points) {}
            @Override public void addGlobalBuff(String buffId, long durationTicks, java.util.Map<String, Object> meta) {}
            @Override public long currentTick() { return 0; }
            @Override public void spawnParticles(int x, int y, String type) {}
            @Override public void playSfx(String id) {}
        };
        ItemBlockModel block = new ItemBlockModel(Block.spawn(BlockKind.I, 0, 0), java.util.Collections.emptyList());

        assertDoesNotThrow(() -> behavior.onLock(ctx, block));
    }
}
