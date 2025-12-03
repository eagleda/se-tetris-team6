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
 * 테스트 대상: tetris.domain.item.behavior.BombBehavior
 *
 * 역할 요약:
 * - onLock 시 폭발 효과를 트리거하여 지정 반경 내 셀을 clear하고 파티클/SFX를 실행한다.
 * - 한 번 트리거되면 재호출 시 중복 실행되지 않는다.
 *
 * 테스트 전략:
 * - onLock 호출 시 requestClearCells/ spawnParticles/ playSfx가 예상 좌표로 호출되는지 검증.
 * - radius가 반영된 가로세로(2*radius+1)로 clear 요청되는지 확인.
 * - 두 번째 onLock 호출은 no-op인지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito (ItemContext 목킹).
 *
 * 주요 테스트 시나리오 예시:
 * - onLock_firstTime_triggersExplosion
 * - onLock_secondTime_noOp
 */
@ExtendWith(MockitoExtension.class)
class BombBehaviorTest {

    @Mock ItemContext ctx;
    ItemBlockModel block;

    @BeforeEach
    void setUp() {
        block = new ItemBlockModel(dummyBlock(5, 5), java.util.List.of(), 0, 0);
        block.setPosition(2, 3);
    }

    @Test
    void onLock_firstTime_triggersExplosion() {
        BombBehavior behavior = new BombBehavior(1); // diameter = 3
        behavior.onLock(ctx, block);

        verify(ctx).requestClearCells(1, 2, 3, 3);
        verify(ctx).spawnParticles(2, 3, "bomb");
        verify(ctx).playSfx("bomb");
    }

    @Test
    void onLock_secondTime_noOp() {
        BombBehavior behavior = new BombBehavior(1);
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
