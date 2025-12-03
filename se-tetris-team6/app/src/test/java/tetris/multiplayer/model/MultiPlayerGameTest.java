package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.GameModel;
import tetris.domain.GameMode;
import tetris.domain.score.Score;
import tetris.infrastructure.GameModelFactory;

/*
 * 테스트 대상: tetris.multiplayer.model.MultiPlayerGame
 *
 * 역할 요약:
 * - 두 PlayerState와 VersusRules를 묶어 대전 한 판의 상태/공격 흐름/승패 판정을 관리한다.
 *
 * 테스트 전략:
 * - markLoser/endWithDraw로 승패/무승부 상태가 설정되는지 확인.
 * - onPieceLocked → takeAttackLines로 VersusRules 위임이 동작하는지 검증.
 * - compareScores가 플레이어 점수에 따라 양수/음수/0을 반환하는지 확인.
 */
class MultiPlayerGameTest {

    private PlayerState p1;
    private PlayerState p2;
    private VersusRules rules;
    private MultiPlayerGame game;

    @BeforeEach
    void setUp() {
        GameModel m1 = GameModelFactory.createDefault();
        GameModel m2 = GameModelFactory.createDefault();
        p1 = new PlayerState(1, m1, true);
        p2 = new PlayerState(2, m2, false);
        rules = new VersusRules();
        game = new MultiPlayerGame(p1, p2, rules);
    }

    @Test
    void markLoserAndDraw_setsWinnerState() {
        assertFalse(game.isGameOver());

        game.markLoser(1);
        assertTrue(game.isGameOver());
        assertEquals(2, game.getWinnerId());

        game.endWithDraw();
        assertTrue(game.isDraw());
        assertEquals(0, game.getLoserId());
        assertEquals(-1, game.getWinnerId());
    }

    @Test
    void compareScores_reflectsRepositoryValues() {
        p1.getModel().getScoreRepository().save(Score.of(100, 0, 0));
        p2.getModel().getScoreRepository().save(Score.of(50, 0, 0));
        assertTrue(game.compareScores() > 0);

        p2.getModel().getScoreRepository().save(Score.of(200, 0, 0));
        assertTrue(game.compareScores() < 0);

        p1.getModel().getScoreRepository().save(Score.of(200, 0, 0));
        assertEquals(0, game.compareScores());
    }

    @Test
    void onPieceLockedForwardsToVersusRules() {
        LockedPieceSnapshot snap = LockedPieceSnapshot.of(List.of(new Cell(0, 0), new Cell(1, 1)));
        game.onPieceLocked(1, snap, new int[] { 0, 1 }, 4);

        List<AttackLine> pending = game.getPendingAttackLines(2);
        assertEquals(2, pending.size());

        List<AttackLine> taken = game.takeAttackLinesForNextSpawn(2);
        assertEquals(2, taken.size());
        assertTrue(game.getPendingAttackLines(2).isEmpty());
    }

    @Test
    void accessors_returnInjectedComponents() {
        assertNotNull(game.player(1));
        assertNotNull(game.opponent(1));
        assertEquals(rules, game.rules());
        assertEquals(p1.getModel(), game.modelOf(1));
    }
}
