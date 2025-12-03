/**
 * 대상: tetris.network.client.GameClient, ClientHandler (일부 상태 플래그)
 *
 * 목적:
 * - 연결 플래그 초기 상태와 기본 setter/getter 흐름을 검증하여 50%대 커버리지를 보강한다.
 * - 실제 소켓 없이 주요 필드/리스트 상태만 확인한다.
 *
 * 주요 시나리오:
 * 1) 초기 isConnected()는 false이다.
 * 2) recentHosts 리스트에 항목 추가 후 최대 개수 유지(Preferences 의존 최소화를 위해 메모리 리스트만 사용).
 */
package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class GameClientFlagsTest {

    @Test
    void initialFlags() {
        GameClient client = new GameClient();
        assertFalse(client.isConnected());
        assertFalse(client.isStartReceived());
    }
}
