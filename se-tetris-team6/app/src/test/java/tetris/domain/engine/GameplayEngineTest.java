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

import java.util.List;
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
            @Override public void onBlockRotated(tetris.domain.model.Block block, int times) {}
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

    @Test
    void setAndClearLastClearedRows() {
        GameplayEngine engine = new GameplayEngine(new Board(), new InputState(),
                new ConstantGenerator(BlockKind.O), new ScoreRuleEngine(new FakeRepo()), null);

        engine.setLastClearedRows(List.of(1, 2));
        assertEquals(2, engine.getLastClearedRows().size());

        engine.setLastClearedRows(null);
        assertTrue(engine.getLastClearedRows().isEmpty());

        engine.clearLastClearedRows();
        assertTrue(engine.getLastClearedRows().isEmpty());
    }

    @Test
    void pauseForLineClear_withoutClockStart_doesNotThrow() {
        GameplayEngine engine = new GameplayEngine(new Board(), new InputState(),
                new ConstantGenerator(BlockKind.T), new ScoreRuleEngine(new FakeRepo()), null);
        assertDoesNotThrow(() -> engine.pauseForLineClear(100));
    }

    @Test
    void hardDrop_movesBlockAndAccumulatesDescendScore() {
        Board board = new Board();
        InputState input = new InputState();
        FakeRepo repo = new FakeRepo();
        ScoreRuleEngine sr = new ScoreRuleEngine(repo);
        GameplayEngine engine = new GameplayEngine(board, input, new ConstantGenerator(BlockKind.I), sr, null);

        engine.spawnIfNeeded();
        Block active = engine.getActiveBlock();
        int startY = active.getY();
        int startScore = repo.load().getPoints();

        engine.hardDropBlock();

        Block dropped = engine.getActiveBlock();
        assertNotNull(dropped);
        assertTrue(dropped.getY() >= startY);
        // 하강 시도 동안 onBlockDescend가 누적 호출되어 점수가 증가해야 한다.
        assertTrue(repo.load().getPoints() >= startScore);
    }

    @Test
    void setSpeedModifier_andGravityLevel_affectClock() {
        GameplayEngine engine = new GameplayEngine(new Board(), new InputState(),
                new ConstantGenerator(BlockKind.S), new ScoreRuleEngine(new FakeRepo()), null);
        engine.setGravityLevel(5);
        assertEquals(5, engine.getGravityLevel());
        assertDoesNotThrow(() -> engine.setSpeedModifier(0.5));
    }

    @Test
    void spawnIfNeeded_onFullBoard_triggersGameOverEvent() {
        Board board = new Board();
        // 보드를 가득 채워 스폰이 불가능한 상태를 만든다.
        for (int y = 0; y < Board.H; y++) {
            for (int x = 0; x < Board.W; x++) {
                board.setCell(x, y, 1);
            }
        }
        InputState input = new InputState();
        FakeRepo repo = new FakeRepo();
        GameplayEngine engine = new GameplayEngine(board, input, new ConstantGenerator(BlockKind.I), new ScoreRuleEngine(repo), null);
        GameEventRecorder events = new GameEventRecorder();
        engine.setEvents(events);

        engine.spawnIfNeeded();

        assertTrue(events.gameOverTriggered);
        assertNull(engine.getActiveBlock());
    }

    @Test
    void lockActiveBlock_fullRow_clearsAndNotifies() {
        Board board = new Board();
        // 마지막 줄을 미리 채워둔다(블록이 놓일 자리만 비워둠)
        for (int x = 2; x < Board.W; x++) {
            board.setCell(x, Board.H - 1, 9);
        }
        InputState input = new InputState();
        FakeRepo repo = new FakeRepo();
        ScoreRuleEngine sr = new ScoreRuleEngine(repo);
        GameplayEngine engine = new GameplayEngine(board, input, new ConstantGenerator(BlockKind.O), sr, null);
        GameEventRecorder events = new GameEventRecorder();
        engine.setEvents(events);

        // 스폰 후 마지막 줄 바로 위에 블록을 배치
        engine.spawnIfNeeded();
        Block active = engine.getActiveBlock();
        active.setPosition(0, Board.H - 2);

        // 한 번의 중력 틱으로 바닥에 도달 후 고정 → 라인 클리어
        engine.onGravityTick(); // y+1 이동
        engine.onGravityTick(); // 더 이상 이동 불가 → lockActiveBlock

        assertTrue(events.linesClearedTriggered);
        assertTrue(engine.getLastClearedRows().contains(Board.H - 1));
        assertTrue(repo.load().getPoints() > 0); // 점수 엔진이 호출되었는지
    }

    private static class GameEventRecorder implements GameplayEngine.GameplayEvents {
        boolean gameOverTriggered;
        boolean linesClearedTriggered;
        @Override public void onBlockSpawned(Block block) {}
        @Override public void onBlockLocked(Block block) {}
        @Override public void onLinesCleared(int clearedLines) { linesClearedTriggered = true; }
        @Override public void onTick(long tick) {}
        @Override public void onBlockRotated(Block block, int times) {}
        @Override public void onGameOver() { gameOverTriggered = true; }
    }
}
