/**
 * 대상: tetris.network.server.GameServer, GameServer$ServerHandler 관련 준비 로직
 *
 * 목적:
 * - setHostReady/setClientReady 조합이 started 플래그를 세팅하는지 확인해 0%대 구간을 보강한다.
 * - 실제 소켓을 열지 않고도 체크 로직만 검증한다.
 *
 * 주요 시나리오:
 * 1) 초기 connectedCount는 0이다.
 * 2) hostReady와 clientReady가 모두 true이면 isStarted가 true로 변경된다.
 */
package tetris.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class GameServerReadyTest {

    @Test
    void hostAndClientReady_setsStarted() {
        GameServer server = new GameServer();
        assertEquals(0, server.getConnectedCount());
        server.setSelectedGameMode("NORMAL");

        // host/client ready
        server.setHostReady(true);
        server.setClientReady(mock(ServerHandler.class), true);

        assertTrue(server.isStarted());
    }

    @Test
    void setSelectedGameMode_storesValue() {
        GameServer server = new GameServer();
        server.setSelectedGameMode("ITEM");
        assertEquals("ITEM", server.getSelectedGameMode());
    }
}
