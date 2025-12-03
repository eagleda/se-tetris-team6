/**
 * 대상: tetris.network.server.GameServer
 *
 * 목적:
 * - 기본 ready 플래그, 모드 설정(get/setSelectedGameMode) 스모크로 서버 측 미싱 라인을 보강한다.
 */
package tetris.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameServerSmokeTest {

    @Test
    void readyFlag_and_modeSetter() {
        GameServer server = new GameServer();
        assertFalse(server.isStarted());
        server.setSelectedGameMode("NORMAL");
        server.setHostReady(true);
        // hostReady 플래그는 내부에서만 사용되므로 단순 실행 스모크
        assertEquals("NORMAL", server.getSelectedGameMode());
    }
}
