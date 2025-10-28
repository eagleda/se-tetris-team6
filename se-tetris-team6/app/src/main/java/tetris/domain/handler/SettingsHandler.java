package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 설정 화면을 담당. 키매핑/난이도 등을 조정합니다.
 */
public final class SettingsHandler extends AbstractGameHandler {

    public SettingsHandler() {
        super(GameState.SETTINGS);
    }

    @Override
    public void enter(GameModel model) {
        model.loadSettings();
    }

    @Override
    public void exit(GameModel model) {
        model.saveSettings();
    }
}
