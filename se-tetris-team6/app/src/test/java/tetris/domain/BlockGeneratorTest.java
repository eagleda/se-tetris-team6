package tetris.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.BlockGenerator
 *
 * 역할 요약:
 * - 다음 스폰될 블록 종류를 결정하는 전략 인터페이스.
 * - nextBlock(), peekNext(), setDifficulty() 기본 계약을 제공한다.
 *
 * 테스트 전략:
 * - 기본 구현이 있는 default 메서드(peekNext, setDifficulty)의 계약을 검증하기 위해
 *   간단한 더블을 만들어 nextBlock 호출 위임이 되는지 확인한다.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - peekNext가 기본적으로 nextBlock 결과를 반환하는지 검증.
 * - setDifficulty 기본 구현이 예외 없이 통과하는지 확인.
 */
class BlockGeneratorTest {

    private static class ConstantGenerator implements BlockGenerator {
        private final BlockKind kind;
        ConstantGenerator(BlockKind kind) { this.kind = kind; }
        @Override public BlockKind nextBlock() { return kind; }
    }

    @Test
    void peekNext_defaultsToNextBlock() {
        BlockGenerator gen = new ConstantGenerator(BlockKind.O);
        assertEquals(BlockKind.O, gen.peekNext());
    }

    @Test
    void setDifficulty_defaultIsNoOp() {
        BlockGenerator gen = new ConstantGenerator(BlockKind.S);
        assertDoesNotThrow(() -> gen.setDifficulty(GameDifficulty.HARD));
        assertEquals(BlockKind.S, gen.nextBlock());
    }
}
