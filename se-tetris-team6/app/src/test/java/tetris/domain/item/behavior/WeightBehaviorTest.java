package tetris.domain.item.behavior;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemContext;
import tetris.domain.item.model.ItemBlockModel;

/*
 * 테스트 대상: tetris.domain.item.behavior.WeightBehavior
 *
 * 역할 요약:
 * - onLock 시 하강 공격을 요청하는 아이템으로, 위쪽 특정 영역에 블록을 추가하도록 요청한다.
 * - 동일 아이템이 두 번 이상 트리거되지 않도록 한다.
 *
 * 테스트 전략:
 * - onLock이 requestAddBlocks를 호출하는지 간단히 검증(빈 배열로 호출만 확인).
 * - spawnParticles/ playSfx 호출 여부 확인.
 * - 두 번째 onLock 호출은 no-op인지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - onLock_firstTime_requestsAddBlocksAndEffects
 * - onLock_secondTime_noOp
 */
@ExtendWith(MockitoExtension.class)
class WeightBehaviorTest {

    @Mock ItemContext ctx;
    ItemBlockModel block;

    @BeforeEach
    void setUp() {
        block = new ItemBlockModel(dummyBlock(1, 1), java.util.List.of());
        block.setPosition(0, 0);
    }

    @Test
    void onLock_firstTime_requestsAddBlocksAndEffects() {
        WeightBehavior behavior = new WeightBehavior();
        behavior.onLock(ctx, block);

        verify(ctx).requestAddBlocks(anyInt(), anyInt(), any(int[][].class));
        verify(ctx).spawnParticles(0, 0, "weight");
        verify(ctx).playSfx("weight_drop");
    }

    @Test
    void onLock_secondTime_noOp() {
        WeightBehavior behavior = new WeightBehavior();
        behavior.onLock(ctx, block);
        reset(ctx);
        behavior.onLock(ctx, block);

        verifyNoInteractions(ctx);
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
}
