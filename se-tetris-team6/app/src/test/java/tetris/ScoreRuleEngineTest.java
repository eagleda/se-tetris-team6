package tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;

class ScoreRuleEngineTest {

    private TrackingScoreRepository repository;
    private ScoreRuleEngine engine;

    @BeforeEach
    void setUp() {
        repository = new TrackingScoreRepository();
        engine = new ScoreRuleEngine(repository);
    }

    @Test
    void appliesLineClearPointsWithMultiplier() {
        engine.setMultiplier(2.0);
        engine.onLinesCleared(2); // base 300 * 2

        Score saved = repository.lastSaved();
        assertEquals(600, saved.getPoints());
        assertEquals(2, saved.getClearedLines());
    }

    @Test
    void onBlockDescendAccumulatesBasePoints() {
        engine.onBlockDescend();
        engine.onBlockDescend();

        Score saved = repository.lastSaved();
        assertEquals(2, saved.getPoints());
    }

    @Test
    void resetScoreClearsRepository() {
        engine.onLinesCleared(1);
        assertTrue(repository.history.size() > 0);

        engine.resetScore();
        assertEquals(0, repository.lastSaved().getPoints());
    }

    private static final class TrackingScoreRepository implements ScoreRepository {
        private Score score = Score.zero();
        private final List<Score> history = new ArrayList<>();

        @Override
        public Score load() {
            return score;
        }

        @Override
        public void save(Score score) {
            this.score = score;
            history.add(score);
        }

        @Override
        public void reset() {
            score = Score.zero();
            history.add(score);
        }

        Score lastSaved() {
            return score;
        }
    }
}

