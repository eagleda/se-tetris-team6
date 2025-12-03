package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.NetworkProtocol
 *
 * 역할 요약:
 * - 네트워크 포트, 타임아웃, 버퍼 등 통신 상수를 정의하는 유틸리티 클래스.
 *
 * 테스트 전략:
 * - 주요 상수 값이 기대한 값과 일치하는지 확인한다.
 */
class NetworkProtocolTest {

    @Test
    void constantsMatchSpecification() {
        assertEquals(12345, NetworkProtocol.DEFAULT_PORT);
        assertEquals(1, NetworkProtocol.PROTOCOL_VERSION);
        assertEquals("UTF-8", NetworkProtocol.CHARSET);
        assertEquals(10000, NetworkProtocol.CONNECTION_TIMEOUT);
        assertEquals(5000, NetworkProtocol.READ_TIMEOUT);
        assertEquals(4096, NetworkProtocol.BUFFER_SIZE);
        assertEquals(2, NetworkProtocol.MAX_PLAYERS);
    }
}
