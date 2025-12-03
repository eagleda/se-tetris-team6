package tetris.domain.score;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.score.ScoreRepository
 *
 * 역할 요약:
 * - 점수 상태를 로드/저장/리셋하는 저장소 추상화.
 *
 * 테스트 전략:
 * - 간단한 인메모리 구현(FakeScoreRepo)로 load/save/reset 계약을 검증한다.
 * - save 후 load가 저장된 값과 일치하고, reset이 zero로 초기화하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - save_thenLoad_matchesSavedScore
 * - reset_setsZeroScore
 */
class ScoreRepositoryTest {

    @Test
    void save_thenLoad_matchesSavedScore() {
        FakeScoreRepo repo = new FakeScoreRepo();
        Score s = Score.of(123, 4, 7);
        repo.save(s);

        Score loaded = repo.load();
        assertEquals(123, loaded.getPoints());
        assertEquals(4, loaded.getLevel());
        assertEquals(7, loaded.getClearedLines());
    }

    @Test
    void reset_setsZeroScore() {
        FakeScoreRepo repo = new FakeScoreRepo();
        repo.save(Score.of(10, 1, 1));
        repo.reset();
        Score loaded = repo.load();
        assertEquals(0, loaded.getPoints());
        assertEquals(0, loaded.getLevel());
        assertEquals(0, loaded.getClearedLines());
    }

    private static class FakeScoreRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { this.score = Score.zero(); }
    }
}
