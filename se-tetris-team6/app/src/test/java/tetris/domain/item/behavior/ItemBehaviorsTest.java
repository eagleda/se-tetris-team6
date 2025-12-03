package tetris.domain.item.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemContext;
import tetris.domain.item.model.ItemBlockModel;

/*
 * 테스트 대상: tetris.domain.item.behavior 패키지의 즉발/버프 아이템들
 *
 * 역할 요약:
 * - Bomb/LineClear/TimeSlow/DoubleScore 아이템이 onLock 시 올바른 좌표와 메타데이터로
 *   ItemContext에 요청을 전달하는지 검증한다.
 *
 * 테스트 전략:
 * - Mock ItemContext에 대해 onLock 호출 후 요청/효과음/파티클 호출 여부를 검증.
 * - 두 번째 onLock 호출은 no-op인지 확인.
 * - 아이템 칸 좌표가 있는 경우 그것을 반영해 전달되는지 확인.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemBehaviorsTest {

    @Mock ItemContext ctx;
    private ItemBlockModel blockWithItemCell;

    @BeforeEach
    void setUp() {
        BlockLike delegate = new DummyBlock(BlockKind.T, 3, 3);
        blockWithItemCell = new ItemBlockModel(delegate, java.util.List.of(), 1, 1);
        blockWithItemCell.setPosition(2, 3);
    }

    @Test
    void bomb_onLock_clearsRadiusOnce() {
        BombBehavior behavior = new BombBehavior(2);

        behavior.onLock(ctx, blockWithItemCell);
        verify(ctx).requestClearCells(2 - 2, 3 - 2, 5, 5);
        verify(ctx).spawnParticles(2, 3, "bomb");
        verify(ctx).playSfx("bomb");
        verifyNoMoreInteractions(ctx);

        // 두 번째 호출은 no-op
        behavior.onLock(ctx, blockWithItemCell);
        verifyNoMoreInteractions(ctx);
    }

    @Test
    void lineClear_onLock_clearsRowAndEffectsOnce() {
        LineClearBehavior behavior = new LineClearBehavior(10);

        behavior.onLock(ctx, blockWithItemCell);
        verify(ctx).requestClearCells(0, 4, 10, 1); // item cell y = 3+1
        verify(ctx).spawnParticles(3, 4, "text:L"); // x=2+1, y=3+1
        verify(ctx).playSfx("line_clear_item");
        verifyNoMoreInteractions(ctx);

        behavior.onLock(ctx, blockWithItemCell);
        verifyNoMoreInteractions(ctx);
    }

    @Test
    void timeSlow_onLock_appliesBuffAndEffectsOnce() {
        TimeSlowBehavior behavior = new TimeSlowBehavior(5000L);

        behavior.onLock(ctx, blockWithItemCell);
        verify(ctx).addGlobalBuff(
                org.mockito.ArgumentMatchers.eq("slow"),
                org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.argThat(meta ->
                        meta.containsKey("durationMs") && meta.containsKey("levelDelta")));
        verify(ctx).spawnParticles(3, 4, "text:Slow");
        verify(ctx).playSfx("slow_on");
        verifyNoMoreInteractions(ctx);

        behavior.onLock(ctx, blockWithItemCell);
        verifyNoMoreInteractions(ctx);
    }

    @Test
    void doubleScore_onLock_appliesBuffAndEffectsOnce() {
        DoubleScoreBehavior behavior = new DoubleScoreBehavior(100, 2.0);

        behavior.onLock(ctx, blockWithItemCell);
        verify(ctx).addGlobalBuff(
                org.mockito.ArgumentMatchers.eq("double_score"),
                org.mockito.ArgumentMatchers.eq(0L),
                org.mockito.ArgumentMatchers.argThat(meta ->
                        meta.containsKey("factor") && Double.compare((Double) meta.get("factor"), 2.0) == 0));
        verify(ctx).spawnParticles(3, 4, "text:2x");
        verify(ctx).playSfx("double_score_on");
        verifyNoMoreInteractions(ctx);

        behavior.onLock(ctx, blockWithItemCell);
        verifyNoMoreInteractions(ctx);
    }

    /** 최소 BlockLike 구현체 (좌표/모양만 사용). */
    private static final class DummyBlock implements BlockLike {
        private final BlockShape shape;
        private int x;
        private int y;

        DummyBlock(BlockKind kind, int w, int h) {
            boolean[][] mask = new boolean[h][w];
            mask[1][1] = true;
            this.shape = new BlockShape(kind, mask);
        }

        @Override public BlockShape getShape() { return shape; }
        @Override public BlockKind getKind() { return shape.kind(); }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public void setPosition(int x, int y) { this.x = x; this.y = y; }
    }
}
