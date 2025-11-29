package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 게임 오버 이후 이름 입력 플로우를 관리합니다.
 */
public final class NameInputHandler extends AbstractGameHandler {

    public NameInputHandler() {
        super(GameState.NAME_INPUT);
    }

    @Override
    public void enter(GameModel model) {
        model.prepareNameEntry();
    }

    @Override
    public void update(GameModel model) {
        model.processNameEntry();
    }
}
