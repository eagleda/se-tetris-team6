package tetris.domain.handler;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 화면/상태 전환을 담당하는 핸들러 기본 계약.
 * 상태 진입/갱신/이탈 단계에서 필요한 로직을 캡슐화합니다.
 */
public interface GameHandler {

    /**
     * 이 핸들러가 대표하는 게임 상태.
     */
    GameState getState();

    /**
     * 상태 진입 시 실행. UI 갱신, 모델 초기화 등을 수행합니다.
     */
    void enter(GameModel model);

    /**
     * 매 틱 호출되는 업데이트 로직.
     */
    void update(GameModel model);

    /**
     * 상태 종료 시 정리 작업.
     */
    void exit(GameModel model);
}
