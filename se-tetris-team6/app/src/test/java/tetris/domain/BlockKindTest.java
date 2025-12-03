package tetris.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.BlockKind
 *
 * 역할 요약:
 * - 테트로미노 및 확장 블록 종류를 열거형으로 정의한다.
 * - Enum이므로 값의 존재와 순서를 통해 도메인 내 분기/매핑에 활용된다.
 *
 * 테스트 전략:
 * - enum 값이 null이 아니고 예상 개수를 유지하는지 확인.
 * - name()/ordinal()을 통해 직렬화나 스위치 분기 시 기대 값이 보존되는지 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - 모든 값이 null이 아님을 확인.
 * - 특정 값(I,J,L,O,S,T,Z,W,B)이 포함되어 있는지 확인.
 */
class BlockKindTest {

    @Test
    void values_arePresentAndNonNull() {
        BlockKind[] kinds = BlockKind.values();
        assertTrue(kinds.length >= 7);
        for (BlockKind k : kinds) {
            assertNotNull(k);
        }
        assertArrayEquals(
                new BlockKind[]{BlockKind.I, BlockKind.J, BlockKind.L, BlockKind.O, BlockKind.S, BlockKind.T, BlockKind.Z, BlockKind.W, BlockKind.B},
                BlockKind.values()
        );
    }

    @Test
    void valueOf_returnsSameInstance() {
        assertSame(BlockKind.I, BlockKind.valueOf("I"));
        assertSame(BlockKind.T, BlockKind.valueOf("T"));
    }
}
