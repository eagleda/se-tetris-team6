package tetris.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.ShapeView
 *
 * 역할 요약:
 * - 높이/너비/점유 여부를 노출하는 2D 셰이프 뷰 인터페이스.
 * - Board 등에서 모양 정보를 읽을 때 사용된다.
 *
 * 테스트 전략:
 * - 간단한 스텁 구현을 만들어 height/width/filled 계약을 확인한다.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - filled 좌표가 내부 마스크와 일치하는지 확인.
 * - height/width가 설정값을 그대로 반환하는지 확인.
 */
class ShapeViewTest {

    @Test
    void shapeView_reportsMaskCorrectly() {
        boolean[][] mask = {
                {true, false},
                {false, true}
        };
        ShapeView view = new MaskShapeView(mask);
        assertEquals(2, view.height());
        assertEquals(2, view.width());
        assertTrue(view.filled(0, 0));
        assertTrue(view.filled(1, 1));
        assertFalse(view.filled(1, 0));
    }

    private static class MaskShapeView implements ShapeView {
        private final boolean[][] mask;
        MaskShapeView(boolean[][] mask) { this.mask = mask; }
        @Override public int height() { return mask.length; }
        @Override public int width() { return mask[0].length; }
        @Override public boolean filled(int x, int y) { return mask[y][x]; }
    }
}
