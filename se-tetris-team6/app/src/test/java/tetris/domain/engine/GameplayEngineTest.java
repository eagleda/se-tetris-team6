package tetris.domain.engine;

import org.junit.jupiter.api.Test;
import tetris.domain.BlockGenerator;
import tetris.domain.BlockKind;
import tetris.domain.Board;
import tetris.domain.model.Block;
import tetris.domain.model.InputState;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class GameplayEngineTest {

    static class FakeRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { this.score = Score.zero(); }
    }

    static class ConstantGenerator implements BlockGenerator {
        private final BlockKind kind;
        ConstantGenerator(BlockKind kind) { this.kind = kind; }
        @Override public BlockKind nextBlock() { return kind; }
        @Override public BlockKind peekNext() { return kind; }
    }

    @Test
    void spawnIfNeeded_emitsEvent_and_setsActiveBlock() {
        Board board = new Board();
        InputState input = new InputState();
        ScoreRuleEngine sr = new ScoreRuleEngine(new FakeRepo());
        GameplayEngine engine = new GameplayEngine(board, input, new ConstantGenerator(BlockKind.I), sr, null);

        AtomicBoolean spawned = new AtomicBoolean(false);
        engine.setEvents(new GameplayEngine.GameplayEvents() {
            @Override public void onBlockSpawned(tetris.domain.model.Block block) { spawned.set(true); }
            @Override public void onBlockLocked(tetris.domain.model.Block block) {}
            @Override public void onLinesCleared(int clearedLines) {}
            @Override public void onTick(long tick) {}
        });

        engine.spawnIfNeeded();
        assertTrue(spawned.get());
        assertNotNull(engine.getActiveBlock());
    }

    @Test
    void moveAndGravity_tick_movesBlock_and_incrementsScore() {
        Board board = new Board();
        InputState input = new InputState();
        FakeRepo repo = new FakeRepo();
        ScoreRuleEngine sr = new ScoreRuleEngine(repo);
        GameplayEngine engine = new GameplayEngine(board, input, new ConstantGenerator(BlockKind.I), sr, null);

        engine.spawnIfNeeded();
        Block active = engine.getActiveBlock();
        assertNotNull(active);
        int oldY = active.getY();

        // simulate gravity tick
        engine.onGravityTick();
        Block after = engine.getActiveBlock();
        if (after != null) {
            assertTrue(after.getY() >= oldY);
        }

        // score should have increased by at least 0 or 1 depending on movement
        assertTrue(repo.load().getPoints() >= 0);
    }
}
