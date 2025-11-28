package tetris.multiplayer.model;

import java.util.List;
import java.util.Objects;

import tetris.domain.GameModel;

/**
 * 두 명의 {@link PlayerState}와 {@link VersusRules}를 묶어 "2인 대전 한 판"을 표현.
 * - 공격 발생, 대기 공격 줄 조회, 승패 판정을 중앙에서 관리한다.
 * - 컨트롤러/핸들러는 이 객체를 통해 상태를 조회하고 규칙을 실행한다.
 */
public final class MultiPlayerGame {

    private final PlayerState p1;
    private final PlayerState p2;
    private final VersusRules versusRules;
    private Integer loserId;

    public MultiPlayerGame(PlayerState p1, PlayerState p2, VersusRules versusRules) {
        this.p1 = Objects.requireNonNull(p1, "p1");
        this.p2 = Objects.requireNonNull(p2, "p2");
        this.versusRules = Objects.requireNonNull(versusRules, "versusRules");
    }

    public PlayerState player(int id) {
        return id == 1 ? p1 : p2;
    }

    public PlayerState opponent(int id) {
        return id == 1 ? p2 : p1;
    }

    public GameModel modelOf(int id) {
        return player(id).getModel();
    }

    public VersusRules rules() {
        return versusRules;
    }

    public boolean isGameOver() {
        return loserId != null;
    }

    public Integer getLoserId() {
        return loserId;
    }

    public Integer getWinnerId() {
        return loserId == null ? null : (loserId == 1 ? 2 : 1);
    }

    public void markLoser(int playerId) {
        if (playerId != 1 && playerId != 2) {
            throw new IllegalArgumentException("player id must be 1 or 2: " + playerId);
        }
        // 멀티 컨트롤러가 공격 줄 주입 이후 스폰 불가를 감지하면 여기로 들어온다.
        loserId = playerId;
    }

    public void onPieceLocked(int playerId,
                              LockedPieceSnapshot snapshot,
                              int[] clearedYs,
                              int boardWidth) {
        // VersusRules에 위임하여 공격 여부와 공격 줄 패턴을 계산한다.
        versusRules.onPieceLocked(playerId, snapshot, clearedYs, boardWidth);
    }

    public List<AttackLine> takeAttackLinesForNextSpawn(int playerId) {
        // 다음 블록 스폰 직전에 호출되어야 하며, 반환된 줄은 즉시 보드에 주입한다.
        return versusRules.consumeAttackLinesForNextSpawn(playerId);
    }

    public int getPendingLines(int playerId) {
        return versusRules.getPendingLineCount(playerId);
    }

    /**
     * 특정 플레이어가 받을 공격 줄의 실제 패턴을 반환한다.
     */
    public java.util.List<AttackLine> getPendingAttackLines(int playerId) {
        return versusRules.getPendingAttackLines(playerId);
    }
}
