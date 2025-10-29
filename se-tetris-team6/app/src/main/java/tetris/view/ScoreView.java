package tetris.view;

import tetris.domain.score.Score;

/**
 * 점수를 화면에 표현하는 View 계약.
 */
public interface ScoreView {
    void renderScore(Score score);
}
