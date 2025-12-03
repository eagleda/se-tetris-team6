package tetris.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.model.GameState
 *
 * 역할 요약:
 * - 상위 게임 상태를 나타내는 열거형으로, 상태 머신이 선택하는 핸들러의 키 역할을 한다.
 *
 * 테스트 전략:
 * - 정의된 모든 상태 값이 누락 없이 포함되어 있는지 확인한다.
 * - EnumSet 사용 시 중복 없이 관리되는지 검증한다.
 *
 * - 사용 라이브러리: JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - values()에 MENU/PLAYING/PAUSED/GAME_OVER/SETTINGS/SCOREBOARD/NAME_INPUT가 모두 포함된다.
 * - EnumSet.allOf(GameState.class)의 크기가 7이며 중복이 없다.
 */
class GameStateTest {

    @Test
    void values_containsAllStates() {
        Set<GameState> all = EnumSet.allOf(GameState.class);
        assertEquals(7, all.size());
        assertTrue(all.contains(GameState.MENU));
        assertTrue(all.contains(GameState.PLAYING));
        assertTrue(all.contains(GameState.PAUSED));
        assertTrue(all.contains(GameState.GAME_OVER));
        assertTrue(all.contains(GameState.SETTINGS));
        assertTrue(all.contains(GameState.SCOREBOARD));
        assertTrue(all.contains(GameState.NAME_INPUT));
    }
}
