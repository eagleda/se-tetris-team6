package tetris.data.leaderboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardResult;

/*
 * 테스트 대상: tetris.data.leaderboard.InMemoryLeaderboardRepository
 *
 * 역할 요약:
 * - 모드별로 점수를 정렬/보관하며 지정된 용량(capacity)까지만 유지하는 메모리 리더보드.
 *
 * 테스트 전략:
 * - 모드별 저장/조회 시 정렬이 내림차순으로 유지되는지 확인.
 * - 용량을 초과하면 해당 모드에서만 오래된(점수 낮은) 항목이 제거되는지 검증.
 * - saveAndHighlight가 하이라이트 인덱스를 기대대로 돌려주는지 확인.
 */
class InMemoryLeaderboardRepositoryTest {

    private InMemoryLeaderboardRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryLeaderboardRepository(2);
    }

    @Test
    void saveAndHighlight_sortsByPoints_descending() {
        LeaderboardResult r1 = repo.saveAndHighlight(new LeaderboardEntry("alice", 100, GameMode.STANDARD));
        LeaderboardResult r2 = repo.saveAndHighlight(new LeaderboardEntry("bob", 200, GameMode.STANDARD));

        assertEquals(0, r1.highlightIndex()); // 첫 삽입 시 자기 자신이 0번
        assertEquals(0, r2.highlightIndex()); // 더 높은 점수는 정렬 후 0번으로 이동
        List<LeaderboardEntry> list = r2.entries();
        assertEquals(2, list.size());
        assertEquals("bob", list.get(0).getName());
        assertEquals("alice", list.get(1).getName());
    }

    @Test
    void saveAndHighlight_trimsPerMode_capacityRespected() {
        repo.saveAndHighlight(new LeaderboardEntry("a", 100, GameMode.STANDARD));
        repo.saveAndHighlight(new LeaderboardEntry("b", 90, GameMode.STANDARD));
        LeaderboardResult r3 = repo.saveAndHighlight(new LeaderboardEntry("c", 80, GameMode.STANDARD));

        // capacity 2 => 가장 낮은 점수 c는 제거되어 highlight -1
        assertEquals(-1, r3.highlightIndex());
        assertIterableEquals(
                List.of("a", "b"),
                repo.loadTop(10, GameMode.STANDARD).stream().map(LeaderboardEntry::getName).toList());
    }

    @Test
    void loadTop_filtersByMode_andRespectsLimit() {
        repo.saveAndHighlight(new LeaderboardEntry("std1", 50, GameMode.STANDARD));
        repo.saveAndHighlight(new LeaderboardEntry("std2", 70, GameMode.STANDARD));
        repo.saveAndHighlight(new LeaderboardEntry("item1", 999, GameMode.ITEM));

        List<LeaderboardEntry> standard = repo.loadTop(1, GameMode.STANDARD);
        assertEquals(1, standard.size());
        assertEquals("std2", standard.get(0).getName()); // 높은 점수 우선

        List<LeaderboardEntry> item = repo.loadTop(5, GameMode.ITEM);
        assertEquals(1, item.size());
        assertEquals("item1", item.get(0).getName());
    }

    @Test
    void reset_clearsAllEntries() {
        repo.saveAndHighlight(new LeaderboardEntry("p", 10, GameMode.STANDARD));
        assertTrue(repo.loadTop(10, GameMode.STANDARD).size() > 0);

        repo.reset();
        assertTrue(repo.loadTop(10, GameMode.STANDARD).isEmpty());
        assertTrue(repo.loadTop(10, GameMode.ITEM).isEmpty());
    }
}
