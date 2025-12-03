package tetris.domain.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;

/*
 * 테스트 대상: tetris.domain.block.BlockLike
 *
 * 역할 요약:
 * - 블록 형태/종류/좌표를 최소한으로 노출하는 도메인 인터페이스.
 *
 * 테스트 전략:
 * - 간단한 익명 구현체를 만들어 getter/setter가 정상 동작하는지 검증한다.
 */
class BlockLikeTest {

    @Test
    void gettersAndSetPosition_workAsContract() {
        boolean[][] mask = new boolean[][] { { true, true }, { false, true } };
        BlockShape shape = new BlockShape(BlockKind.L, mask);

        BlockLike block = new BlockLike() {
            private int x;
            private int y;

            @Override public BlockShape getShape() { return shape; }
            @Override public BlockKind getKind() { return BlockKind.L; }
            @Override public int getX() { return x; }
            @Override public int getY() { return y; }
            @Override public void setPosition(int x, int y) { this.x = x; this.y = y; }
        };

        block.setPosition(3, 5);

        assertEquals(shape, block.getShape());
        assertEquals(BlockKind.L, block.getKind());
        assertEquals(3, block.getX());
        assertEquals(5, block.getY());
    }
}
