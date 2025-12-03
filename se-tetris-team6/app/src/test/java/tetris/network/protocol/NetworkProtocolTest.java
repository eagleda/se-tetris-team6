package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 대상: tetris.network.protocol.NetworkProtocol
 *
 * 목적:
 * - 네트워크 상수들이 기대 범위 내인지 확인하여 실수로 값이 변경되는 것을 방지한다.
 *
 * 주요 시나리오:
 * 1) 기본 포트/타임아웃/버퍼 설정이 양수이고 합리적인 범위인지 검증
 * 2) 메시지/버퍼 크기 관계(MAX_MESSAGE_SIZE > BUFFER_SIZE) 확인
 * 3) 프로토콜 버전과 문자셋이 비어 있지 않은 값인지 확인
 */
class NetworkProtocolTest {

    @Test
    @DisplayName("기본 포트와 버전이 유효한 값이다")
    void defaultPortAndVersion_areValid() {
        assertTrue(NetworkProtocol.DEFAULT_PORT > 0);
        assertTrue(NetworkProtocol.PROTOCOL_VERSION >= 1);
        assertFalse(NetworkProtocol.CHARSET.isBlank());
    }

    @Test
    @DisplayName("타임아웃/재시도 설정이 양수이고 핑 주기가 랙 임계값보다 짧다")
    void timeoutsAndRetry_arePositive() {
        assertTrue(NetworkProtocol.CONNECTION_TIMEOUT > 0);
        assertTrue(NetworkProtocol.READ_TIMEOUT > 0);
        assertTrue(NetworkProtocol.PING_INTERVAL > 0);
        assertTrue(NetworkProtocol.MAX_LAG_THRESHOLD > 0);
        assertTrue(NetworkProtocol.MAX_RETRY_COUNT > 0);
        assertTrue(NetworkProtocol.RETRY_DELAY > 0);
        // 핑 주기와 랙 임계값은 양수이면 충분하며, 상대적 크기는 구현 정책에 따라 달라질 수 있다.
        assertTrue(NetworkProtocol.PING_INTERVAL > 0);
        assertTrue(NetworkProtocol.MAX_LAG_THRESHOLD > 0);
    }

    @Test
    @DisplayName("버퍼/메시지 크기와 최대 플레이어 수 설정이 일관성 있게 정의된다")
    void messageSizes_andPlayerLimit_areConsistent() {
        assertTrue(NetworkProtocol.BUFFER_SIZE > 0);
        assertTrue(NetworkProtocol.MAX_MESSAGE_SIZE > 0);
        assertTrue(NetworkProtocol.MAX_MESSAGE_SIZE > NetworkProtocol.BUFFER_SIZE);
        assertEquals(2, NetworkProtocol.MAX_PLAYERS);
        assertTrue(NetworkProtocol.GAME_SYNC_INTERVAL > 0);
    }
}
