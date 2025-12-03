package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.multiplayer.model.VersusRules
 *
 * 역할 요약:
 * - 잠긴 블록/삭제 줄 정보를 받아 공격 줄을 생성하고 상대 버퍼에 적재한다.
 * - 버퍼 조회/소비를 통해 UI나 컨트롤러가 공격을 주입할 수 있게 한다.
 *
 * 테스트 전략:
 * - clearedYs가 최소치보다 작거나 스냅샷이 비면 공격이 생성되지 않는지 확인.
 * - 스냅샷의 기여 셀을 구멍으로 표시한 공격 줄이 상대 버퍼에 쌓이는지 검증.
 * - consume 호출 후 버퍼가 비워지는지 확인.
 */
class VersusRulesTest {

    @Test
    void ignoresWhenNotEnoughLinesOrEmptySnapshot() {
        VersusRules rules = new VersusRules(2);
        LockedPieceSnapshot emptySnap = LockedPieceSnapshot.of(List.of());

        rules.onPieceLocked(1, emptySnap, new int[] { 0, 1 }, 10);
        assertEquals(0, rules.getPendingLineCount(2));

        rules.onPieceLocked(1, sampleSnap(), new int[] { 0 }, 10); // only 1 line < minLines
        assertEquals(0, rules.getPendingLineCount(2));
    }

    @Test
    void buildsAttackLinesForOpponentWhenEnoughLinesCleared() {
        VersusRules rules = new VersusRules(2);
        LockedPieceSnapshot snap = sampleSnap(); // cells at (1,0) and (2,1)

        rules.onPieceLocked(1, snap, new int[] { 0, 1 }, 5);

        assertEquals(2, rules.getPendingLineCount(2));
        List<AttackLine> pending = rules.getPendingAttackLines(2);
        assertEquals(2, pending.size());
        // y=0 line has hole at x=1, y=1 line has hole at x=2
        assertTrue(pending.get(0).isHole(1));
        assertTrue(pending.get(1).isHole(2));

        List<AttackLine> consumed = rules.consumeAttackLinesForNextSpawn(2);
        assertEquals(2, consumed.size());
        assertEquals(0, rules.getPendingLineCount(2));
    }

    private LockedPieceSnapshot sampleSnap() {
        return LockedPieceSnapshot.of(List.of(new Cell(1, 0), new Cell(2, 1)));
    }
}
