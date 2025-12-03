package tetris.domain.leaderboard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tetris.domain.GameMode;

/*
 * 테스트 대상: tetris.domain.leaderboard.LeaderboardEntry
 *
 * 역할 요약:
 * - 리더보드 엔트리를 나타내며 이름, 점수, 게임 모드를 보관한다.
 * - withMode로 모드를 변경한 새 엔트리를 생성하며 equals/hashCode를 구현한다.
 *
 * 테스트 전략:
 * - 생성 시 null 모드가 STANDARD로 치환되는지 확인.
 * - equals/hashCode가 동일 값에 대해 동치인지, 다른 값에 대해 다르게 동작하는지 검증.
 * - withMode가 이름/점수는 유지하고 모드만 바꾼 새 인스턴스를 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - null mode -> STANDARD
 * - equals/hashCode 계약 검증
 * - withMode_createsNewEntryWithMode
 */
class LeaderboardEntryTest {

    @Test
    void nullMode_defaultsToStandard() {
        LeaderboardEntry entry = new LeaderboardEntry("Alice", 1000, null);
        assertEquals(GameMode.STANDARD, entry.getMode());
    }

    @Test
    void equalsAndHashCode_matchForSameValues() {
        LeaderboardEntry a = new LeaderboardEntry("Bob", 500, GameMode.ITEM);
        LeaderboardEntry b = new LeaderboardEntry("Bob", 500, GameMode.ITEM);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void withMode_createsNewEntryWithMode() {
        LeaderboardEntry original = new LeaderboardEntry("Cara", 700, GameMode.STANDARD);
        LeaderboardEntry changed = original.withMode(GameMode.ITEM);

        assertEquals("Cara", changed.getName());
        assertEquals(700, changed.getPoints());
        assertEquals(GameMode.ITEM, changed.getMode());
        assertNotEquals(original, changed);
    }
}
