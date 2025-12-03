/*
 * 테스트 대상: tetris.view.PvPGameRenderer
 *
 * 역할 요약:
 * - 두 GameModel의 보드/점수/다음 블록 정보를 텍스트 화면으로 렌더링하는 헬퍼입니다.
 *
 * 테스트 전략:
 * - Mockito로 GameModel과 Board를 스텁하여 간단한 2x2 그리드를 반환하게 만들고,
 *   render 호출 시 생성되는 문자열에 헤더와 보드 경계가 포함되는지 확인합니다.
 *
 * 주요 테스트 시나리오 예시:
 * - 렌더 결과 문자열에 P1/P2 연결 상태가 표시되고, 보드 경계 문자('-','|')가 포함된다.
 */

package tetris.view;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.model.Block;

class PvPGameRendererTest {

    @Test
    void render_includesHeadersAndBoardBorder() {
        GameModel left = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        GameModel right = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        Board leftBoard = Mockito.mock(Board.class, Mockito.withSettings().lenient());
        Board rightBoard = Mockito.mock(Board.class, Mockito.withSettings().lenient());

        when(left.getBoard()).thenReturn(leftBoard);
        when(right.getBoard()).thenReturn(rightBoard);
        when(leftBoard.gridView()).thenReturn(new int[][] { { 1 }, { 0 } });
        when(rightBoard.gridView()).thenReturn(new int[][] { { 2 }, { 0 } });

        String rendered = PvPGameRenderer.render(left, right, true, false, "STATUS");

        assertTrue(rendered.contains("P1: CONNECTED"));
        assertTrue(rendered.contains("P2: DISCONNECTED"));
        assertTrue(rendered.contains("STATUS"));
        assertTrue(rendered.contains("-")); // board border
        assertTrue(rendered.contains("|")); // board border
    }
}
