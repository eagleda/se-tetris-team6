package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 중복되는 보일러플레이트를 줄이기 위한 추상 베이스 핸들러.
 */
public abstract class AbstractGameHandler implements GameHandler {

    private final GameState state;

    protected AbstractGameHandler(GameState state) {
        this.state = state;
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public void enter(GameModel model) {
        // 기본 구현은 없음
    }

    @Override
    public void update(GameModel model) {
        // 기본 구현은 없음
    }

    @Override
    public void exit(GameModel model) {
        // 기본 구현은 없음
    }
}
