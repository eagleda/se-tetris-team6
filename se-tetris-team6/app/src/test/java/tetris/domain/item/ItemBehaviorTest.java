package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.item.ItemBehavior
 *
 * 역할 요약:
 * - 아이템 블록의 수명주기 이벤트(onSpawn/onTick/onLock/onLineClear)를 정의하는 인터페이스.
 * - id(), type(), isExpired() 계약을 구현체가 채운다.
 *
 * 테스트 전략:
 * - 간단한 구현체(FakeBehavior)를 만들어 기본 메서드 호출 시 호출 횟수를 기록하고 계약을 검증한다.
 * - id/type/isExpired가 기대값을 반환하는지 확인한다.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - lifecycleCallbacks_areInvoked
 * - id_and_type_and_isExpired_returnExpectedValues
 */
class ItemBehaviorTest {

    @Test
    void lifecycleCallbacks_areInvoked() {
        FakeBehavior behavior = new FakeBehavior();
        ItemContext ctx = new ItemBehaviorTest.NoopContext();
        var block = new tetris.domain.item.model.ItemBlockModel(
                dummyBlock(), java.util.List.of()
        );

        behavior.onSpawn(ctx, block);
        behavior.onTick(ctx, block, 5);
        behavior.onLineClear(ctx, block, new int[]{0});
        behavior.onLock(ctx, block);

        assertEquals(1, behavior.spawnCount);
        assertEquals(1, behavior.tickCount);
        assertEquals(1, behavior.lineClearCount);
        assertEquals(1, behavior.lockCount);
    }

    @Test
    void id_and_type_and_isExpired_returnExpectedValues() {
        FakeBehavior behavior = new FakeBehavior();
        assertEquals("fake", behavior.id());
        assertEquals(ItemType.ACTIVE, behavior.type());
        assertFalse(behavior.isExpired());
    }

    private static class FakeBehavior implements ItemBehavior {
        int spawnCount, tickCount, lineClearCount, lockCount;
        @Override public String id() { return "fake"; }
        @Override public ItemType type() { return ItemType.ACTIVE; }
        @Override public void onSpawn(ItemContext ctx, tetris.domain.item.model.ItemBlockModel block) { spawnCount++; }
        @Override public void onTick(ItemContext ctx, tetris.domain.item.model.ItemBlockModel block, long tick) { tickCount++; }
        @Override public void onLineClear(ItemContext ctx, tetris.domain.item.model.ItemBlockModel block, int[] clearedRows) { lineClearCount++; }
        @Override public void onLock(ItemContext ctx, tetris.domain.item.model.ItemBlockModel block) { lockCount++; }
    }

    private static class NoopContext implements ItemContext {
        @Override public tetris.domain.Board getBoard() { return new tetris.domain.Board(); }
        @Override public void requestClearCells(int x, int y, int width, int height) {}
        @Override public void requestAddBlocks(int x, int y, int[][] cells) {}
        @Override public void applyScoreDelta(int points) {}
        @Override public void addGlobalBuff(String buffId, long durationTicks, java.util.Map<String, Object> meta) {}
        @Override public long currentTick() { return 0; }
        @Override public void spawnParticles(int x, int y, String type) {}
        @Override public void playSfx(String id) {}
    }

    private static tetris.domain.block.BlockLike dummyBlock() {
        boolean[][] mask = {{true}};
        return new tetris.domain.block.BlockLike() {
            @Override public tetris.domain.BlockShape getShape() { return new tetris.domain.BlockShape(tetris.domain.BlockKind.I, mask); }
            @Override public tetris.domain.BlockKind getKind() { return tetris.domain.BlockKind.I; }
            @Override public int getX() { return 0; }
            @Override public int getY() { return 0; }
            @Override public void setPosition(int x, int y) {}
        };
    }
}
