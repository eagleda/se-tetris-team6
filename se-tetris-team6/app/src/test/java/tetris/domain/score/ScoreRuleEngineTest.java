package tetris.domain.score;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ScoreRuleEngineTest {

    static class FakeRepo implements ScoreRepository {
        private Score score = Score.zero();
        boolean resetCalled = false;

        @Override
        public Score load() {
            return score;
        }

        @Override
        public void save(Score score) {
            this.score = score;
        }

        @Override
        public void reset() {
            this.score = Score.zero();
            resetCalled = true;
        }
    }

    @Test
    void onBlockDescend_incrementsPoints_and_notifiesListener() {
        FakeRepo repo = new FakeRepo();
        ScoreRuleEngine engine = new ScoreRuleEngine(repo);
        AtomicReference<Score> seen = new AtomicReference<>();
        engine.addListener(seen::set);

        engine.onBlockDescend();

        assertNotNull(seen.get());
        assertEquals(1, repo.load().getPoints());
        assertEquals(repo.load().getPoints(), seen.get().getPoints());
    }

    @Test
    void onLinesCleared_appliesCorrectBasePoints_forFourLines() {
        FakeRepo repo = new FakeRepo();
        ScoreRuleEngine engine = new ScoreRuleEngine(repo);

        engine.onLinesCleared(4);

        // base for 4 lines = 800
        assertEquals(800, repo.load().getPoints());
        assertEquals(4, repo.load().getClearedLines());
    }

    @Test
    void resetScore_resetsRepository_and_notifies() {
        FakeRepo repo = new FakeRepo();
        ScoreRuleEngine engine = new ScoreRuleEngine(repo);
        engine.onBlockDescend();
        assertTrue(repo.load().getPoints() > 0);

        AtomicReference<Score> last = new AtomicReference<>();
        engine.addListener(last::set);
        engine.resetScore();

        assertTrue(repo.resetCalled);
        assertEquals(0, repo.load().getPoints());
        assertNotNull(last.get());
        assertEquals(0, last.get().getPoints());
    }
}
