package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 실제 테트리스 플레이 루프를 담당.
 * 중력, 사용자 입력 해석, 라인 삭제 등을 orchestrate 하게 됩니다.
 */
public final class GamePlayHandler extends AbstractGameHandler {

    public GamePlayHandler() {
        super(GameState.PLAYING);
    }

    @Override
    public void enter(GameModel model) {
        model.spawnIfNeeded();
        model.resumeClock();
    }

    @Override
    public void update(GameModel model) {
        model.stepGameplay();
    }

    @Override
    public void exit(GameModel model) {
        model.pauseClock();
    }
}
