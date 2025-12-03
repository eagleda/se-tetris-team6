package tetris.concurrent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.concurrent.NetworkStats
 *
 * 역할 요약:
 * - 네트워크 연결의 통계 정보를 관리하는 클래스
 * - Ping(RTT), 지연 시간, 패킷 송수신 통계 등을 추적
 * - 네트워크 상태 모니터링 및 오버레이 표시를 위한 데이터 제공
 *
 * 테스트 전략:
 * - 상태를 가진 객체이므로 생성, 상태 변화, 조회를 중심으로 테스트
 * - Ping 값 업데이트, 평균 계산, 통계 초기화 등의 시나리오 검증
 * - 경계값(음수, 매우 큰 값) 처리 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 초기 생성 시 기본값 확인
 * - Ping 값 업데이트 시 최신 값 반영
 * - 여러 Ping 값의 평균 계산
 * - 통계 초기화 동작 확인
 * - 음수 또는 비정상 값 입력 시 처리
 */

public class NetworkStatsTest {

    private NetworkStats networkStats;

    @BeforeEach
    void setUp() {
        networkStats = new NetworkStats();
    }

    @Test
    void constructor_shouldInitializeWithDefaultValues() {
        // when
        long ping = networkStats.getLastPing();
        long avgPing = networkStats.getAveragePing();

        // then
        assertEquals(0, ping, "Initial ping should be 0");
        assertEquals(0, avgPing, "Initial average ping should be 0");
    }

    @Test
    void updatePing_shouldUpdateLastPingValue() {
        // given
        long newPing = 50L;

        // when
        networkStats.updatePing(newPing);

        // then
        assertEquals(newPing, networkStats.getLastPing(), 
            "Last ping should be updated to new value");
    }

    @Test
    void updatePing_multipleValues_shouldCalculateAverage() {
        // given
        networkStats.updatePing(100L);
        networkStats.updatePing(200L);
        networkStats.updatePing(300L);

        // when
        long avgPing = networkStats.getAveragePing();

        // then
        assertEquals(200L, avgPing, "Average ping should be (100+200+300)/3 = 200");
    }

    @Test
    void updatePing_withZero_shouldAcceptValue() {
        // when
        networkStats.updatePing(0L);

        // then
        assertEquals(0L, networkStats.getLastPing(), 
            "Should accept zero as valid ping value");
    }

    @Test
    void updatePing_withNegative_shouldHandleGracefully() {
        // given
        long negativePing = -50L;

        // when
        networkStats.updatePing(negativePing);

        // then
        // Implementation dependent: either reject or clamp to 0
        long ping = networkStats.getLastPing();
        assertTrue(ping >= 0, "Ping should not be negative");
    }

    @Test
    void reset_shouldClearAllStatistics() {
        // given
        networkStats.updatePing(100L);
        networkStats.updatePing(200L);

        // when
        networkStats.reset();

        // then
        assertEquals(0, networkStats.getLastPing(), 
            "Ping should be reset to 0");
        assertEquals(0, networkStats.getAveragePing(), 
            "Average ping should be reset to 0");
    }

    @Test
    void getPingStatus_shouldReturnStatusBasedOnPing() {
        // given & when & then
        networkStats.updatePing(50L);
        String status1 = networkStats.getPingStatus();
        assertTrue(status1.contains("GOOD") || status1.contains("양호"), 
            "Ping < 100ms should be good status");

        networkStats.updatePing(150L);
        String status2 = networkStats.getPingStatus();
        assertTrue(status2.contains("WARNING") || status2.contains("주의"), 
            "Ping 100-200ms should be warning status");

        networkStats.updatePing(250L);
        String status3 = networkStats.getPingStatus();
        assertTrue(status3.contains("LAG") || status3.contains("지연"), 
            "Ping > 200ms should be lag status");
    }

    @Test
    void updatePing_largeValue_shouldHandle() {
        // given
        long largePing = 10000L; // 10 seconds

        // when
        networkStats.updatePing(largePing);

        // then
        assertEquals(largePing, networkStats.getLastPing(), 
            "Should handle large ping values");
    }

    @Test
    void getPacketsSent_shouldReturnInitialZero() {
        // when
        long packetsSent = networkStats.getPacketsSent();

        // then
        assertEquals(0, packetsSent, "Initial packets sent should be 0");
    }

    @Test
    void incrementPacketsSent_shouldIncreaseCount() {
        // when
        networkStats.incrementPacketsSent();
        networkStats.incrementPacketsSent();
        networkStats.incrementPacketsSent();

        // then
        assertEquals(3, networkStats.getPacketsSent(), 
            "Packets sent should be incremented");
    }
}
