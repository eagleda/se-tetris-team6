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
 * 테스트 대상: tetris.domain.item.behavior.LineClearBehavior
 *
 * 역할 요약:
 * - onLock 시 해당 줄을 삭제 요청하고 파티클/SFX를 트리거하는 즉발 아이템.
 * - 이미 트리거되면 중복 실행하지 않는다.
 *
 * 테스트 전략:
 * - onLock이 requestClearCells(보드 너비만큼 한 줄)과 spawnParticles/playSfx를 호출하는지 검증.
 * - 두 번째 onLock 호출은 no-op인지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - onLock_firstTime_clearsLineAndPlaysEffects
 * - onLock_secondTime_noOp
 */
@ExtendWith(MockitoExtension.class)
class LineClearBehaviorTest {

    @Mock ItemContext ctx;
    ItemBlockModel block;

    @BeforeEach
    void setUp() {
        block = new ItemBlockModel(dummyBlock(2, 2), java.util.List.of());
        block.setPosition(3, 4);
    }

    @Test
    void onLock_firstTime_clearsLineAndPlaysEffects() {
        LineClearBehavior behavior = new LineClearBehavior(10);
        behavior.onLock(ctx, block);

        verify(ctx).requestClearCells(0, 4, 10, 1);
        verify(ctx).spawnParticles(3, 4, "text:L");
        verify(ctx).playSfx("line_clear_item");
    }

    @Test
    void onLock_secondTime_noOp() {
        LineClearBehavior behavior = new LineClearBehavior(10);
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
