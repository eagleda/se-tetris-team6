package tetris.domain.item.behavior;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemContext;
import tetris.domain.item.model.ItemBlockModel;

/*
 * 테스트 대상: tetris.domain.item.behavior.DoubleScoreBehavior
 *
 * 역할 요약:
 * - onLock 시 점수 배수 버프를 글로벌 버프로 추가하고, 파티클/SFX를 트리거한다.
 * - 이미 트리거된 경우 중복 실행하지 않는다.
 *
 * 테스트 전략:
 * - onLock이 addGlobalBuff를 적절한 메타(factor)와 함께 호출하는지 검증.
 * - spawnParticles/ playSfx 호출 여부 확인.
 * - 두 번째 onLock 호출이 no-op인지 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - onLock_firstTime_appliesBuffAndEffects
 * - onLock_secondTime_noOp
 */
@ExtendWith(MockitoExtension.class)
class DoubleScoreBehaviorTest {

    @Mock ItemContext ctx;
    ItemBlockModel block;

    @BeforeEach
    void setUp() {
        block = new ItemBlockModel(dummyBlock(1, 1), java.util.List.of());
        block.setPosition(4, 5);
    }

    @Test
    void onLock_firstTime_appliesBuffAndEffects() {
        DoubleScoreBehavior behavior = new DoubleScoreBehavior(10L, 2.5);
        ArgumentCaptor<Map<String, Object>> metaCaptor = ArgumentCaptor.forClass(Map.class);

        behavior.onLock(ctx, block);

        verify(ctx).addGlobalBuff(eq("double_score"), eq(0L), metaCaptor.capture());
        assertEquals(2.5, metaCaptor.getValue().get("factor"));
        verify(ctx).spawnParticles(4, 5, "text:2x");
        verify(ctx).playSfx("double_score_on");
    }

    @Test
    void onLock_secondTime_noOp() {
        DoubleScoreBehavior behavior = new DoubleScoreBehavior(10L, 2.5);
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
