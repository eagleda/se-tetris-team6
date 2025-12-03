package tetris.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.server.ServerStatus
 *
 * 역할 요약:
 * - 서버 상태를 나타내는 DTO 클래스.
 *
 * 테스트 전략:
 * - 생성자 파라미터가 getter로 그대로 노출되는지 검증.
 */
class ServerStatusTest {

    @Test
    void gettersExposeState() {
        ServerStatus status = new ServerStatus(2, "ITEM", true, false);
        assertEquals(2, status.getConnectedClientCount());
        assertEquals("ITEM", status.getSelectedGameMode());
        assertEquals(true, status.isGameInProgress());
        assertFalse(status.isRunning());
    }
}
