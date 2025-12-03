package tetris.multiplayer.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;
import tetris.domain.model.GameState;

/*
 * 테스트 대상: tetris.multiplayer.session.LocalMultiplayerSession
 *
 * 역할 요약:
 * - 로컬 2P 멀티플레이에 필요한 플레이어 모델/컨트롤러/핸들러를 묶는 값 객체.
 * - 플레이어 재시작/종료 시 두 모델을 동일하게 제어한다.
 *
 * 테스트 전략:
 * - 팩토리로 생성 시 필수 컴포넌트가 모두 초기화되는지 확인.
 * - restartPlayers(GameMode.ITEM) 호출 후 두 플레이어의 모드/상태가 PLAYING인지 검증.
 * - shutdown 호출 시 두 플레이어가 MENU 상태로 돌아가는지 확인.
 */
class LocalMultiplayerSessionTest {

    @Test
    void createAndRestartPlayers_setsModesAndStates() {
        LocalMultiplayerSession session = MultiplayerSessionFactory.create(GameMode.STANDARD);

        assertNotNull(session.playerOneModel());
        assertNotNull(session.playerTwoModel());
        assertNotNull(session.controller());
        assertNotNull(session.handler());
        assertTrue(session.isPlayerOneLocal());
        assertTrue(session.isPlayerTwoLocal());

        session.restartPlayers(GameMode.ITEM);
        assertEquals(GameMode.ITEM, session.playerOneModel().getCurrentMode());
        assertEquals(GameMode.ITEM, session.playerTwoModel().getCurrentMode());
        assertEquals(GameState.PLAYING, session.playerOneModel().getCurrentState());
        assertEquals(GameState.PLAYING, session.playerTwoModel().getCurrentState());

        session.shutdown();
        assertEquals(GameState.MENU, session.playerOneModel().getCurrentState());
        assertEquals(GameState.MENU, session.playerTwoModel().getCurrentState());
    }
}
