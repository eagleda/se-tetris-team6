package tetris.data.score;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tetris.domain.score.Score;

/*
 * 테스트 대상: tetris.data.score.InMemoryScoreRepository
 *
 * 역할 요약:
 * - 메모리에 점수 상태를 보관하며 load/save/reset을 제공하는 ScoreRepository 구현체.
 * - 초기 상태는 Score.zero(), save로 상태를 덮어쓰고 reset으로 초기화한다.
 *
 * 테스트 전략:
 * - 초기 load가 zero 스코어를 반환하는지 확인.
 * - save 후 load가 동일한 값으로 반영되는지 확인.
 * - reset 호출 시 zero로 되돌아가는지 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - load_initialIsZero
 * - save_thenLoad_returnsSavedValue
 * - reset_clearsToZero
 */
class InMemoryScoreRepositoryTest {

    @Test
    void load_initialIsZero() {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        Score score = repo.load();
        assertEquals(0, score.getPoints());
        assertEquals(0, score.getLevel());
        assertEquals(0, score.getClearedLines());
    }

    @Test
    void save_thenLoad_returnsSavedValue() {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        Score s = Score.of(100, 3, 5);
        repo.save(s);

        Score loaded = repo.load();
        assertEquals(100, loaded.getPoints());
        assertEquals(3, loaded.getLevel());
        assertEquals(5, loaded.getClearedLines());
    }

    @Test
    void reset_clearsToZero() {
        InMemoryScoreRepository repo = new InMemoryScoreRepository();
        repo.save(Score.of(50, 2, 1));
        repo.reset();

        Score loaded = repo.load();
        assertEquals(0, loaded.getPoints());
        assertEquals(0, loaded.getLevel());
        assertEquals(0, loaded.getClearedLines());
    }
}
