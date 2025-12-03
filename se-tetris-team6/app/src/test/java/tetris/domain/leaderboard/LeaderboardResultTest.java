package tetris.domain.leaderboard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;

/*
 * 테스트 대상: tetris.domain.leaderboard.LeaderboardResult
 *
 * 역할 요약:
 * - 리더보드 업데이트 결과를 entries 목록과 highlightIndex를 함께 보관하는 레코드.
 *
 * 테스트 전략:
 * - 레코드 필드가 생성자 인자를 그대로 보존하는지 확인.
 * - 불변 리스트 참조를 통해 내용이 유지되는지 간단히 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - entries와 highlightIndex가 기대대로 저장되는지 확인.
 */
class LeaderboardResultTest {

    @Test
    void fieldsAreExposedAsProvided() {
        List<LeaderboardEntry> list = List.of(new LeaderboardEntry("A", 10, GameMode.STANDARD));
        LeaderboardResult result = new LeaderboardResult(list, 0);

        assertEquals(list, result.entries());
        assertEquals(0, result.highlightIndex());
    }
}
