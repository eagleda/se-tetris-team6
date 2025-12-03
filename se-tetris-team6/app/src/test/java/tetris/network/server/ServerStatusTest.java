package tetris.network.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.network.server.ServerStatus
 *
 * 역할 요약:
 * - 게임 서버의 현재 상태를 나타내는 enum 타입
 * - IDLE(대기), WAITING(클라이언트 연결 대기), CONNECTED(연결됨), IN_GAME(게임 중) 상태를 표현
 * - 서버의 생명주기와 상태 전환을 관리하는 데 사용됨
 *
 * 테스트 전략:
 * - Enum 타입은 일반적으로 복잡한 로직이 없으므로, 기본 enum 동작 검증
 * - values(), valueOf() 등의 기본 메서드 동작 확인
 * - 각 상태값이 올바르게 정의되어 있는지 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 모든 enum 상수가 정의되어 있는지 확인
 * - valueOf()로 문자열을 통해 enum을 가져올 수 있는지 확인
 * - values()가 모든 상태를 반환하는지 확인
 */

public class ServerStatusTest {

    @Test
    void values_shouldReturnAllStatuses() {
        // when
        ServerStatus[] statuses = ServerStatus.values();

        // then
        assertEquals(4, statuses.length, "Should have 4 server statuses");
        assertTrue(containsStatus(statuses, ServerStatus.IDLE));
        assertTrue(containsStatus(statuses, ServerStatus.WAITING));
        assertTrue(containsStatus(statuses, ServerStatus.CONNECTED));
        assertTrue(containsStatus(statuses, ServerStatus.IN_GAME));
    }

    @Test
    void valueOf_shouldReturnCorrectStatus() {
        // when & then
        assertEquals(ServerStatus.IDLE, ServerStatus.valueOf("IDLE"));
        assertEquals(ServerStatus.WAITING, ServerStatus.valueOf("WAITING"));
        assertEquals(ServerStatus.CONNECTED, ServerStatus.valueOf("CONNECTED"));
        assertEquals(ServerStatus.IN_GAME, ServerStatus.valueOf("IN_GAME"));
    }

    @Test
    void valueOf_withInvalidName_shouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            ServerStatus.valueOf("INVALID_STATUS");
        });
    }

    @Test
    void enumConstantsShouldBeUnique() {
        // given
        ServerStatus[] statuses = ServerStatus.values();

        // when & then
        for (int i = 0; i < statuses.length; i++) {
            for (int j = i + 1; j < statuses.length; j++) {
                assertNotEquals(statuses[i], statuses[j], 
                    "Each status should be unique");
            }
        }
    }

    private boolean containsStatus(ServerStatus[] statuses, ServerStatus target) {
        for (ServerStatus status : statuses) {
            if (status == target) {
                return true;
            }
        }
        return false;
    }
}
