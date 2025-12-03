package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.GameDifficulty
 *
 * 역할 요약:
 * - 게임 전역에서 공유하는 난이도 열거형.
 * - UI/도메인에서 동일한 상수를 사용해 일관된 난이도 설정을 전달한다.
 *
 * 테스트 전략:
 * - 선언된 세 가지 난이도(EASY/NORMAL/HARD)가 순서대로 유지되는지 검증한다.
 * - values().length를 통해 누락/중복이 없는지 확인한다.
 *
 * - 사용 라이브러리: JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - values() 배열이 [EASY, NORMAL, HARD] 순서를 가진다.
 * - valueOf로 각 이름을 찾으면 동일 인스턴스를 반환한다.
 */
class GameDifficultyTest {

    @Test
    void values_areStableAndOrdered() {
        assertArrayEquals(
                new GameDifficulty[] { GameDifficulty.EASY, GameDifficulty.NORMAL, GameDifficulty.HARD },
                GameDifficulty.values());
        assertEquals(3, GameDifficulty.values().length);
    }
}
