package tetris.network.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.network.protocol.NetworkProtocol
 *
 * 역할 요약:
 * - 네트워크 통신 규약과 상수들을 정의하는 유틸리티 클래스
 * - 포트 번호, 타임아웃, 재시도, 메시지 크기 제한 등의 상수 제공
 * - 프로토콜 버전 관리 및 네트워크 설정의 중앙 집중화
 *
 * 테스트 전략:
 * - 모든 상수가 올바른 값으로 정의되어 있는지 확인
 * - 상수 간의 관계가 논리적으로 타당한지 검증
 * - 경계값이 합리적인 범위 내에 있는지 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 기본 설정 값(포트, 버전, 문자셋) 확인
 * - 타임아웃 값이 양수이고 합리적인지 검증
 * - 재시도 설정이 적절한지 확인
 * - 메시지 크기 제한이 양수인지 확인
 */

public class NetworkProtocolTest {

    @Test
    void defaultPort_shouldBePositive() {
        // when
        int port = NetworkProtocol.DEFAULT_PORT;

        // then
        assertTrue(port > 0, "Default port should be positive");
        assertTrue(port <= 65535, "Port should be within valid range");
        assertEquals(12345, port, "Default port should be 12345");
    }

    @Test
    void protocolVersion_shouldBePositive() {
        // when
        int version = NetworkProtocol.PROTOCOL_VERSION;

        // then
        assertTrue(version > 0, "Protocol version should be positive");
        assertEquals(1, version, "Protocol version should be 1");
    }

    @Test
    void charset_shouldBeUTF8() {
        // when
        String charset = NetworkProtocol.CHARSET;

        // then
        assertEquals("UTF-8", charset, "Charset should be UTF-8");
    }

    @Test
    void connectionTimeout_shouldBeReasonable() {
        // when
        int timeout = NetworkProtocol.CONNECTION_TIMEOUT;

        // then
        assertTrue(timeout > 0, "Connection timeout should be positive");
        assertEquals(10000, timeout, "Connection timeout should be 10 seconds");
    }

    @Test
    void readTimeout_shouldBeReasonable() {
        // when
        int timeout = NetworkProtocol.READ_TIMEOUT;

        // then
        assertTrue(timeout > 0, "Read timeout should be positive");
        assertEquals(5000, timeout, "Read timeout should be 5 seconds");
    }

    @Test
    void pingInterval_shouldBeReasonable() {
        // when
        int interval = NetworkProtocol.PING_INTERVAL;

        // then
        assertTrue(interval > 0, "Ping interval should be positive");
        assertEquals(1000, interval, "Ping interval should be 1 second");
    }

    @Test
    void maxLagThreshold_shouldBeReasonable() {
        // when
        int threshold = NetworkProtocol.MAX_LAG_THRESHOLD;

        // then
        assertTrue(threshold > 0, "Max lag threshold should be positive");
        assertEquals(200, threshold, "Max lag threshold should be 200ms");
    }

    @Test
    void maxRetryCount_shouldBeReasonable() {
        // when
        int maxRetry = NetworkProtocol.MAX_RETRY_COUNT;

        // then
        assertTrue(maxRetry > 0, "Max retry count should be positive");
        assertEquals(3, maxRetry, "Max retry count should be 3");
    }

    @Test
    void retryDelay_shouldBeReasonable() {
        // when
        int delay = NetworkProtocol.RETRY_DELAY;

        // then
        assertTrue(delay > 0, "Retry delay should be positive");
        assertEquals(1000, delay, "Retry delay should be 1 second");
    }

    @Test
    void maxMessageSize_shouldBeReasonable() {
        // when
        int maxSize = NetworkProtocol.MAX_MESSAGE_SIZE;

        // then
        assertTrue(maxSize > 0, "Max message size should be positive");
        assertEquals(1024 * 10, maxSize, "Max message size should be 10KB");
    }

    @Test
    void bufferSize_shouldBeReasonable() {
        // when
        int bufferSize = NetworkProtocol.BUFFER_SIZE;

        // then
        assertTrue(bufferSize > 0, "Buffer size should be positive");
        assertEquals(4096, bufferSize, "Buffer size should be 4KB");
    }

    @Test
    void maxPlayers_shouldBeTwo() {
        // when
        int maxPlayers = NetworkProtocol.MAX_PLAYERS;

        // then
        assertEquals(2, maxPlayers, "Max players should be 2 for P2P");
    }

    @Test
    void gameSyncInterval_shouldBeReasonable() {
        // when
        int interval = NetworkProtocol.GAME_SYNC_INTERVAL;

        // then
        assertTrue(interval > 0, "Game sync interval should be positive");
        assertEquals(50, interval, "Game sync interval should be 50ms");
    }

    @Test
    void timeoutRelationship_shouldBeLogical() {
        // when
        int connectionTimeout = NetworkProtocol.CONNECTION_TIMEOUT;
        int readTimeout = NetworkProtocol.READ_TIMEOUT;

        // then
        assertTrue(connectionTimeout >= readTimeout, 
            "Connection timeout should be >= read timeout");
    }

    @Test
    void bufferSizeRelationship_shouldBeLogical() {
        // when
        int bufferSize = NetworkProtocol.BUFFER_SIZE;
        int maxMessageSize = NetworkProtocol.MAX_MESSAGE_SIZE;

        // then
        assertTrue(maxMessageSize >= bufferSize, 
            "Max message size should be >= buffer size");
    }

    @Test
    void pingIntervalRelationship_shouldBeLogical() {
        // when
        int pingInterval = NetworkProtocol.PING_INTERVAL;
        int maxLagThreshold = NetworkProtocol.MAX_LAG_THRESHOLD;

        // then
        assertTrue(maxLagThreshold < pingInterval * 5, 
            "Lag threshold should be reasonable compared to ping interval");
    }
}
