package tetris.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.NetworkMode
 *
 * 역할 요약:
 * - 네트워크 모드를 나타내는 열거형(OFFLINE/CLIENT/SERVER).
 *
 * 테스트 전략:
 * - 정의된 상수가 모두 포함되어 있고 개수가 3개인지 확인.
 */
class NetworkModeTest {

    @Test
    void containsAllModes() {
        EnumSet<NetworkMode> all = EnumSet.allOf(NetworkMode.class);
        assertEquals(3, all.size());
        assertTrue(all.contains(NetworkMode.OFFLINE));
        assertTrue(all.contains(NetworkMode.CLIENT));
        assertTrue(all.contains(NetworkMode.SERVER));
    }
}
