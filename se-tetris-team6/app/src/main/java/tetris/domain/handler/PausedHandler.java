package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 일시정지 상태 핸들러. UI 오버레이 표시와 타이머 정지를 담당합니다.
 */
public final class PausedHandler extends AbstractGameHandler {

    public PausedHandler() {
        super(GameState.PAUSED);
    }

    @Override
    public void enter(GameModel model) {
        model.pauseClock();
        model.showPauseOverlay();
    }

    @Override
    public void exit(GameModel model) {
        model.hidePauseOverlay();
        model.resumeClock();
    }
}
