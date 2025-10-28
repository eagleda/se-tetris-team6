package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 게임 오버 이후 점수 집계와 화면 전환을 담당합니다.
 */
public final class GameOverHandler extends AbstractGameHandler {

    public GameOverHandler() {
        super(GameState.GAME_OVER);
    }

    @Override
    public void enter(GameModel model) {
        model.pauseClock();
        model.computeFinalScore();
        model.showGameOverScreen();
    }
}
