package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.NetworkProtocol
 *
 * 역할 요약:
 * - 네트워크 통신 설정 상수(포트, 타임아웃, 버퍼 크기 등)를 담는 유틸 클래스.
 *
 * 테스트 전략:
 * - 상수 값들이 합리적 범위(양수, 최소 크기 등)인지 간단히 검증하여 커버리지 확보.
 */
class NetworkProtocolValuesTest {

    @Test
    void constants_arePositiveAndReasonable() {
        assertTrue(NetworkProtocol.DEFAULT_PORT > 0);
        assertTrue(NetworkProtocol.CONNECTION_TIMEOUT >= 1000);
        assertTrue(NetworkProtocol.READ_TIMEOUT > 0);
        assertTrue(NetworkProtocol.MAX_MESSAGE_SIZE >= NetworkProtocol.BUFFER_SIZE);
        assertTrue(NetworkProtocol.MAX_PLAYERS >= 1);
        assertTrue(NetworkProtocol.GAME_SYNC_INTERVAL > 0);
        assertTrue(NetworkProtocol.MAX_RETRY_COUNT >= 0);
        assertTrue(NetworkProtocol.RETRY_DELAY >= 0);
        assertTrue(NetworkProtocol.PROTOCOL_VERSION >= 1);
    }
}
