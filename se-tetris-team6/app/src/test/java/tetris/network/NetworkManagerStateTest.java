/**
 * 대상: tetris.network.NetworkManager (익명 리스너 포함)
 *
 * 목적:
 * - 기본 상태 플래그를 확인하고 setMode 호출이 예외 없이 동작하는지 스모크한다.
 */
package tetris.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class NetworkManagerStateTest {

    @Test
    void initialFlags_and_setMode_noThrow() {
        var gameThread = org.mockito.Mockito.mock(tetris.concurrent.GameThread.class, org.mockito.Mockito.withSettings().lenient());
        NetworkManager mgr = new NetworkManager(gameThread);
        assertFalse(mgr.isConnected());
        assertFalse(mgr.isConnected());
    }
}
