/**
 * 대상: tetris.multiplayer.session.MultiplayerSessionFactory
 *
 * 목적:
 * - 로컬/네트워크 세션 생성 메서드가 null이 아닌 세션을 반환하고,
 *   초기화 과정에서 예외 없이 플레이어 상태가 준비되는지 확인하여 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) create(mode) 호출 시 LocalMultiplayerSession이 null 아님을 확인
 * 2) createNetworkedSession(mode, localIsPlayerOne, callback) 호출 시 NetworkMultiplayerSession이 null 아님을 확인
 * 3) 시드 지정 createNetworkedSession도 null 아님을 확인
 */
package tetris.multiplayer.session;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;

class MultiplayerSessionFactoryTest {

    @Test
    void createLocalSession_returnsNonNull() {
        LocalMultiplayerSession session = MultiplayerSessionFactory.create(GameMode.STANDARD);
        assertNotNull(session);
    }

    @Test
    void createNetworkedSession_returnsNonNull() {
        NetworkMultiplayerSession session = MultiplayerSessionFactory.createNetworkedSession(GameMode.STANDARD, true, () -> {});
        assertNotNull(session);
    }

    @Test
    void createNetworkedSession_withSeed_returnsNonNull() {
        NetworkMultiplayerSession session = MultiplayerSessionFactory.createNetworkedSession(GameMode.ITEM, false, () -> {}, 1234L);
        assertNotNull(session);
    }
}
