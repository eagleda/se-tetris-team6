package tetris.domain.block;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;

/*
 * 테스트 대상: tetris.domain.block.BlockLike
 *
 * 역할 요약:
 * - 블록 형태를 추상화한 인터페이스로, 위치/모양(kind, shape)을 노출한다.
 * - 구현체가 제대로 계약을 따르는지 확인하는 기본 동작 테스트가 필요하다.
 *
 * 테스트 전략:
 * - 테스트 더블을 만들어 get/setPosition, getShape, getKind가 기대대로 동작하는지 확인한다.
 * - 인터페이스 계약 위반이 없는지 최소한의 시나리오 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - setPosition 후 getX/getY가 업데이트되는지 확인.
 * - getShape/getKind가 생성 시 전달한 값을 그대로 반환하는지 확인.
 */
class BlockLikeTest {

    @Test
    void blockLike_stubMaintainsState() {
        boolean[][] mask = {{true}};
        BlockShape shape = new BlockShape(BlockKind.I, mask);
        BlockLikeStub stub = new BlockLikeStub(shape, BlockKind.I);

        stub.setPosition(3, 4);
        assertEquals(3, stub.getX());
        assertEquals(4, stub.getY());
        assertSame(shape, stub.getShape());
        assertEquals(BlockKind.I, stub.getKind());
    }

    private static class BlockLikeStub implements BlockLike {
        private final BlockShape shape;
        private final BlockKind kind;
        private int x;
        private int y;
        BlockLikeStub(BlockShape shape, BlockKind kind) {
            this.shape = shape;
            this.kind = kind;
        }
        @Override public BlockShape getShape() { return shape; }
        @Override public BlockKind getKind() { return kind; }
        @Override public int getX() { return x; }
        @Override public int getY() { return y; }
        @Override public void setPosition(int x, int y) { this.x = x; this.y = y; }
    }
}
