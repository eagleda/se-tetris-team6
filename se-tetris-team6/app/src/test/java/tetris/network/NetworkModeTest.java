package tetris.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.network.NetworkMode
 *
 * 역할 요약:
 * - 네트워크 연결 모드를 나타내는 enum 타입
 * - OFFLINE(오프라인), SERVER(호스트), CLIENT(클라이언트) 세 가지 모드 정의
 * - NetworkManager에서 현재 네트워크 모드 상태 관리에 사용
 *
 * 테스트 전략:
 * - Enum 타입의 기본 동작 검증
 * - values(), valueOf() 메서드 정상 동작 확인
 * - 모든 enum 상수가 정의되어 있는지 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 세 가지 모드(OFFLINE, SERVER, CLIENT)가 정의되어 있는지 확인
 * - valueOf()로 문자열을 통해 enum을 가져올 수 있는지 확인
 * - values()가 모든 모드를 반환하는지 확인
 */

public class NetworkModeTest {

    @Test
    void values_shouldReturnAllModes() {
        // when
        NetworkMode[] modes = NetworkMode.values();

        // then
        assertEquals(3, modes.length, "Should have 3 network modes");
        assertTrue(containsMode(modes, NetworkMode.OFFLINE));
        assertTrue(containsMode(modes, NetworkMode.SERVER));
        assertTrue(containsMode(modes, NetworkMode.CLIENT));
    }

    @Test
    void valueOf_shouldReturnCorrectMode() {
        // when & then
        assertEquals(NetworkMode.OFFLINE, NetworkMode.valueOf("OFFLINE"));
        assertEquals(NetworkMode.SERVER, NetworkMode.valueOf("SERVER"));
        assertEquals(NetworkMode.CLIENT, NetworkMode.valueOf("CLIENT"));
    }

    @Test
    void valueOf_withInvalidName_shouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            NetworkMode.valueOf("INVALID_MODE");
        });
    }

    @Test
    void enumConstantsShouldBeUnique() {
        // given
        NetworkMode[] modes = NetworkMode.values();

        // when & then
        for (int i = 0; i < modes.length; i++) {
            for (int j = i + 1; j < modes.length; j++) {
                assertNotEquals(modes[i], modes[j], 
                    "Each mode should be unique");
            }
        }
    }

    @Test
    void offlineMode_shouldBeFirstEnum() {
        // when
        NetworkMode firstMode = NetworkMode.values()[0];

        // then
        assertEquals(NetworkMode.OFFLINE, firstMode, 
            "OFFLINE should be the first enum constant");
    }

    @Test
    void enumName_shouldMatchConstantName() {
        // when & then
        assertEquals("OFFLINE", NetworkMode.OFFLINE.name());
        assertEquals("SERVER", NetworkMode.SERVER.name());
        assertEquals("CLIENT", NetworkMode.CLIENT.name());
    }

    private boolean containsMode(NetworkMode[] modes, NetworkMode target) {
        for (NetworkMode mode : modes) {
            if (mode == target) {
                return true;
            }
        }
        return false;
    }
}
