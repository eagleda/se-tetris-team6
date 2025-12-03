package tetris.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.BlockShape
 *
 * 역할 요약:
 * - 테트로미노 도형의 점유 마스크와 종류(kind)를 표현한다.
 * - height/width/filled로 모양을 노출하고, 시계방향 회전된 새 BlockShape를 생성한다.
 *
 * 테스트 전략:
 * - 순수 도형 로직 검증: 초기 마스크가 그대로 반영되는지 확인.
 * - 회전 로직 검증: rotatedCW()가 크기와 좌표를 올바르게 변환하는지 경계값 포함 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - 2x3 마스크를 회전했을 때 3x2로 변환되고 좌표 매핑이 맞는지 검증.
 * - kind가 회전 후에도 보존되는지 확인.
 */
class BlockShapeTest {

    @Test
    void rotation_preservesKindAndRotatesMask() {
        boolean[][] mask = {
                {true, false, true},
                {false, true, false}
        };
        BlockShape shape = new BlockShape(BlockKind.T, mask);

        assertEquals(2, shape.height());
        assertEquals(3, shape.width());
        assertTrue(shape.filled(0, 0));
        assertTrue(shape.filled(1, 1));

        BlockShape rotated = shape.rotatedCW();

        // 회전 후 크기 확인 (3x2)
        assertEquals(3, rotated.height());
        assertEquals(2, rotated.width());
        // 좌표 변환 확인: rot rows = { {0,1}, {1,0}, {0,1} }
        assertTrue(rotated.filled(1, 0));
        assertTrue(rotated.filled(0, 1));
        assertTrue(rotated.filled(1, 2));
        // kind 유지
        assertEquals(BlockKind.T, rotated.kind());
    }
}
