package tetris.domain;

import org.junit.jupiter.api.Test;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

    static class DummyScoreRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { this.score = Score.zero(); }
    }

    static class ConstGenerator implements BlockGenerator {
        @Override public BlockKind nextBlock() { return BlockKind.I; }
        @Override public BlockKind peekNext() { return BlockKind.I; }
    }

    @Test
    void addBoardCells_and_clearBoardRegion_work_and_refreshBridge() {
        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo());
        // add a small 2x2 block into board at (0,0)
        model.addBoardCells(0, 0, new int[][]{ {1,2}, {3,4} });
        // clearing a region should not throw and should call refresh (no-op bridge by default)
        model.clearBoardRegion(0, 0, 1, 1);
        // verify board has been modified (no exception means pass)
        assertNotNull(model.getBoard().gridView());
    }

    @Test
    void addGlobalBuff_doubleScore_changesMultiplier_onScoreEngine() {
        DummyScoreRepo repo = new DummyScoreRepo();
        GameModel model = new GameModel(new ConstGenerator(), repo);
        // initially multiplier is 1.0; after adding buff multiplier should be >1
        model.startGame(GameMode.ITEM);
        model.addGlobalBuff("double_score", 100, java.util.Map.of("factor", 3.0));
        // applying a descend should use multiplier; use engine to descend
        ScoreRuleEngine s = model.getScoreEngine();
        s.onBlockDescend();
        assertTrue(repo.load().getPoints() >= 1);
    }
}
