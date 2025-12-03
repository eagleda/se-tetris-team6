/**
 * 대상: tetris.network.protocol.NetworkProtocol
 *
 * 목적:
 * - 상수 값과 유효 포트 범위를 간단히 스모크하여 0% 커버리지 클래스를 보강한다.
 */
package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NetworkProtocolSmokeTest {

    @Test
    void constants_areWithinExpectedRange() {
        assertTrue(NetworkProtocol.DEFAULT_PORT > 0);
    }
}
