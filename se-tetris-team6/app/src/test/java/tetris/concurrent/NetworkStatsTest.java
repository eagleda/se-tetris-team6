package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.concurrent.NetworkStats
 *
 * 역할 요약:
 * - 네트워크 전송/수신/지연시간을 담는 불변 DTO.
 *
 * 테스트 전략:
 * - 생성자에 전달한 값이 getter로 그대로 노출되는지 확인.
 */
class NetworkStatsTest {

    @Test
    void gettersExposeConstructorValues() {
        NetworkStats stats = new NetworkStats(10, 20, 150);

        assertEquals(10, stats.getMessagesSent());
        assertEquals(20, stats.getMessagesReceived());
        assertEquals(150, stats.getCurrentLatency());
    }
}
