package tetris.multiplayer.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;

/*
 * 테스트 대상: tetris.multiplayer.session.MultiplayerSessionFactory
 *
 * 역할 요약:
 * - 로컬/네트워크 멀티플레이 세션을 생성하여 필요한 모델/컨트롤러/핸들러를 한 번에 준비한다.
 *
 * 테스트 전략:
 * - 로컬 세션 생성 시 두 플레이어가 모두 로컬이며 모드가 전달되는지 확인.
 * - 네트워크 세션 생성 시 localIsPlayerOne 플래그에 따라 로컬 플레이어가 올바르게 설정되는지 확인.
 * - 필수 컴포넌트(controller/handler/game)가 null이 아닌지 검증.
 */
class MultiplayerSessionFactoryTest {

    @Test
    void createLocalSession_initializesPlayersAndMode() {
        LocalMultiplayerSession session = MultiplayerSessionFactory.create(GameMode.ITEM);

        assertNotNull(session.game());
        assertTrue(session.isPlayerOneLocal());
        assertTrue(session.isPlayerTwoLocal());
        assertEquals(GameMode.ITEM, session.playerOneModel().getCurrentMode());
        assertEquals(GameMode.ITEM, session.playerTwoModel().getCurrentMode());
    }

    @Test
    void createNetworkedSession_setsLocalPlayerFlag() {
        NetworkMultiplayerSession session = MultiplayerSessionFactory.createNetworkedSession(GameMode.STANDARD, true, () -> {});
        assertNotNull(session.game());
        assertTrue(session.isPlayerOneLocal());
        assertTrue(session.handler() != null);
        assertEquals(GameMode.STANDARD, session.playerOneModel().getCurrentMode());

        NetworkMultiplayerSession sessionP2Local = MultiplayerSessionFactory.createNetworkedSession(GameMode.ITEM, false, () -> {});
        assertNotNull(sessionP2Local.networkController());
        assertTrue(sessionP2Local.isPlayerTwoLocal());
        assertEquals(GameMode.ITEM, sessionP2Local.playerTwoModel().getCurrentMode());
    }
}
