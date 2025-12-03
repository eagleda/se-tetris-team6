/**
 * 대상: tetris.concurrent.NetworkThread, NetworkThread$NetworkReader (상태/통계)
 *
 * 목적:
 * - 생성 직후 상태 플래그와 NetworkStats 반환값을 확인하고, shutdown 호출이 예외 없이 동작하는지 검증하여
 *   저커버리지(30%대) 구간을 보강한다.
 *
 * 주요 시나리오:
 * 1) isConnected 초기값 false 확인, currentLatency 초기값 0 확인
 * 2) getNetworkStats가 null이 아니고 필드 접근이 가능함을 확인
 * 3) shutdown 호출이 예외 없이 수행
 *
 * Mockito 불사용: 콜백은 간단한 더미 구현을 사용.
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import tetris.network.INetworkThreadCallback;
import tetris.network.protocol.GameMessage;

class NetworkThreadStatsTest {

    private static final class DummyCallback implements INetworkThreadCallback {
        @Override public void handleReceivedMessage(GameMessage message) {}
        @Override public void handleConnectionEstablished() {}
        @Override public void handleConnectionLost() {}
        @Override public void handleLatencyWarning(long latency) {}
        @Override public void handleNetworkError(Exception error) {}
    }

    @Test
    void initialState_andShutdown() {
        NetworkThread thread = new NetworkThread(new DummyCallback(), "localhost", 1);
        assertFalse(thread.isConnected());
        assertEquals(0, thread.getCurrentLatency());
        assertNotNull(thread.getNetworkStats());

        thread.shutdown(); // should not throw
    }
}
