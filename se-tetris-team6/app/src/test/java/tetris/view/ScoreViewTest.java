package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tetris.domain.score.Score;

/*
 * 테스트 대상: tetris.view.ScoreView
 *
 * 역할 요약:
 * - 점수를 렌더링하는 뷰 계약 인터페이스.
 *
 * 테스트 전략:
 * - 간단한 구현체를 만들어 renderScore 호출 시 전달된 Score가 그대로 전달되는지 검증.
 */
class ScoreViewTest {

    @Test
    void renderScore_isInvokedWithProvidedScore() {
        RecordingScoreView view = new RecordingScoreView();
        Score score = Score.of(100, 1, 2);

        view.renderScore(score);

        assertEquals(score, view.lastScore);
    }

    private static class RecordingScoreView implements ScoreView {
        Score lastScore;

        @Override
        public void renderScore(Score score) {
            this.lastScore = score;
        }
    }
}
