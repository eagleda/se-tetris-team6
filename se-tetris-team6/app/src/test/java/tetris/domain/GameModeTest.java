package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.GameMode
 *
 * 역할 요약:
 * - 게임 모드(STANDARD/ITEM/TIME_LIMIT/LOCAL_MULTIPLAYER/NETWORK_MULTIPLAYER)를 구분하는 enum.
 *
 * 테스트 전략:
 * - values()와 valueOf()가 모든 상수를 포함하는지 검증.
 * - toString 기본 동작 확인.
 */
class GameModeTest {

    @Test
    void values_containsAllModes() {
        GameMode[] modes = GameMode.values();
        assertEquals(3, modes.length);
        assertNotNull(GameMode.valueOf("STANDARD"));
        assertNotNull(GameMode.valueOf("ITEM"));
        assertNotNull(GameMode.valueOf("TIME_LIMIT"));
    }

    @Test
    void toString_returnsName() {
        assertEquals("STANDARD", GameMode.STANDARD.toString());
    }
}
