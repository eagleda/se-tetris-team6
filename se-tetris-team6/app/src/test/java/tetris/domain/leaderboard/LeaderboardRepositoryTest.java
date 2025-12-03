package tetris.domain.leaderboard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.GameMode;

/*
 * 테스트 대상: tetris.domain.leaderboard.LeaderboardRepository
 *
 * 역할 요약:
 * - 리더보드 엔트리를 로드/저장/초기화하며, 모드별 상위 점수를 관리하는 저장소 추상화.
 * - saveAndHighlight는 새 엔트리 삽입 후 정렬된 목록과 하이라이트 인덱스를 반환한다.
 *
 * 테스트 전략:
 * - 간단한 인메모리 Fake 구현을 통해 계약(loadTop/saveEntry/saveAndHighlight/reset)을 검증한다.
 * - 모드별 분리, 점수 내림차순 정렬, 하이라이트 인덱스 계산, reset 동작을 확인한다.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - saveEntry 후 loadTop이 점수 내림차순으로 상위 n개를 반환하는지 확인.
 * - saveAndHighlight가 삽입된 엔트리의 인덱스를 올바르게 반환하는지 검증.
 * - reset 호출 시 모든 모드의 엔트리가 비워지는지 확인.
 */
class LeaderboardRepositoryTest {

    private FakeLeaderboardRepo repo;

    @BeforeEach
    void setup() {
        repo = new FakeLeaderboardRepo();
    }

    @Test
    void saveEntry_and_loadTop_returnsSortedByPoints() {
        repo.saveEntry(new LeaderboardEntry("A", 10, GameMode.STANDARD));
        repo.saveEntry(new LeaderboardEntry("B", 30, GameMode.STANDARD));
        repo.saveEntry(new LeaderboardEntry("C", 20, GameMode.ITEM));

        List<LeaderboardEntry> topStd = repo.loadTop(2, GameMode.STANDARD);
        assertEquals(2, topStd.size());
        assertEquals("B", topStd.get(0).getName());
        assertEquals("A", topStd.get(1).getName());

        List<LeaderboardEntry> topItem = repo.loadTop(1, GameMode.ITEM);
        assertEquals("C", topItem.get(0).getName());
    }

    @Test
    void saveAndHighlight_returnsInsertedIndex() {
        repo.saveEntry(new LeaderboardEntry("A", 10, GameMode.STANDARD));
        repo.saveEntry(new LeaderboardEntry("B", 30, GameMode.STANDARD));

        LeaderboardResult result = repo.saveAndHighlight(new LeaderboardEntry("C", 20, GameMode.STANDARD));
        assertEquals(1, result.highlightIndex()); // B(30), C(20), A(10)
        assertEquals(3, result.entries().size());
    }

    @Test
    void reset_clearsAllModes() {
        repo.saveEntry(new LeaderboardEntry("X", 5, GameMode.ITEM));
        repo.reset();
        assertTrue(repo.loadTop(5, GameMode.ITEM).isEmpty());
        assertTrue(repo.loadTop(5, GameMode.STANDARD).isEmpty());
    }

    private static class FakeLeaderboardRepo implements LeaderboardRepository {
        private final Map<GameMode, List<LeaderboardEntry>> data = new HashMap<>();

        @Override
        public List<LeaderboardEntry> loadTop(int n, GameMode mode) {
            return data.getOrDefault(mode, List.of()).stream()
                    .sorted(Comparator.comparingInt(LeaderboardEntry::getPoints).reversed())
                    .limit(Math.max(0, n))
                    .toList();
        }

        @Override
        public void saveEntry(LeaderboardEntry entry) {
            data.computeIfAbsent(entry.getMode(), k -> new ArrayList<>()).add(entry);
        }

        @Override
        public LeaderboardResult saveAndHighlight(LeaderboardEntry entry) {
            saveEntry(entry);
            List<LeaderboardEntry> sorted = loadTop(Integer.MAX_VALUE, entry.getMode());
            int idx = sorted.indexOf(entry);
            return new LeaderboardResult(sorted, idx);
        }

        @Override
        public void reset() {
            data.clear();
        }
    }
}
