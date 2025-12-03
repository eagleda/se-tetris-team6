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
 * 테스트 대상: tetris.domain.item.behavior.TimeSlowBehavior
 *
 * 역할 요약:
 * - onLock 시 슬로우 버프를 글로벌 버프로 추가하고, 파티클/SFX를 트리거한다.
 * - 이미 트리거되면 중복 실행하지 않는다.
 *
 * 테스트 전략:
 * - onLock이 addGlobalBuff에 durationMs/levelDelta 메타를 전달하는지 검증.
 * - spawnParticles/ playSfx 호출 여부 확인.
 * - 두 번째 onLock 호출이 no-op인지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - onLock_firstTime_appliesSlowBuffAndEffects
 * - onLock_secondTime_noOp
 */
@ExtendWith(MockitoExtension.class)
class TimeSlowBehaviorTest {

    @Mock ItemContext ctx;
    ItemBlockModel block;

    @BeforeEach
    void setUp() {
        block = new ItemBlockModel(dummyBlock(1, 1), java.util.List.of());
        block.setPosition(1, 2);
    }

    @Test
    void onLock_firstTime_appliesSlowBuffAndEffects() {
        TimeSlowBehavior behavior = new TimeSlowBehavior(5000L);
        ArgumentCaptor<Map<String, Object>> metaCaptor = ArgumentCaptor.forClass(Map.class);

        behavior.onLock(ctx, block);

        verify(ctx).addGlobalBuff(eq("slow"), eq(0L), metaCaptor.capture());
        assertEquals(5000L, metaCaptor.getValue().get("durationMs"));
        assertEquals(Integer.valueOf(-1), metaCaptor.getValue().get("levelDelta"));
        verify(ctx).spawnParticles(1, 2, "text:Slow");
        verify(ctx).playSfx("slow_on");
    }

    @Test
    void onLock_secondTime_noOp() {
        TimeSlowBehavior behavior = new TimeSlowBehavior(5000L);
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
