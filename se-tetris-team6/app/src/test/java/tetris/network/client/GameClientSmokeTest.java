/**
 * 대상: tetris.network.client.GameClient
 *
 * 목적:
 * - 기본 플래그와 setter/getter를 스모크해 미싱 라인을 보강한다.
 */
package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameClientSmokeTest {

    @Test
    void initialFlags_andSetConnected() {
        GameClient client = new GameClient();
        assertFalse(client.isConnected());
        // 연결 시뮬레이션은 실제 소켓 의존성이 있어 여기서는 초기값만 검증한다.
    }
}
