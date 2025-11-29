package tetris.controller;

import java.util.Objects;

import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;
import tetris.view.ScoreView;

/**
 * 점수 View와 도메인 점수 모델을 연결하는 컨트롤러.
 */
public final class ScoreController {

    private final ScoreRepository repository;
    private final ScoreRuleEngine ruleEngine;
    private final ScoreView view;

    public ScoreController(ScoreRepository repository, ScoreRuleEngine ruleEngine, ScoreView view) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.ruleEngine = Objects.requireNonNull(ruleEngine, "ruleEngine");
        this.view = Objects.requireNonNull(view, "view");

        this.ruleEngine.addListener(this::renderScore);
        renderScore(repository.load());
    }

    public void resetScore() {
        ruleEngine.resetScore();
    }

    private void renderScore(Score score) {
        view.renderScore(score);
    }
}
