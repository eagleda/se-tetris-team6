package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 하이스코어 표를 준비/표시합니다.
 */
public final class ScoreboardHandler extends AbstractGameHandler {

    public ScoreboardHandler() {
        super(GameState.SCOREBOARD);
    }

    @Override
    public void enter(GameModel model) {
        model.loadScoreboard();
    }
}
