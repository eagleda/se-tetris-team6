package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 메인 메뉴 상태를 담당하는 핸들러.
 */
public final class MenuHandler extends AbstractGameHandler {

    public MenuHandler() {
        super(GameState.MENU);
    }

    @Override
    public void enter(GameModel model) {
        // TODO: 메뉴 화면 초기화 및 버튼 바인딩
    }

    @Override
    public void update(GameModel model) {
        // 메뉴는 일반적으로 틱 처리 없음, 필요 시 애니메이션 추가
    }
}
