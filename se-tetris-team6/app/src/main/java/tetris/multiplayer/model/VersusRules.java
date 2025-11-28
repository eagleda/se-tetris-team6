package tetris.multiplayer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 멀티 대전 규칙을 계산하는 순수 도메인 컴포넌트.
 * - 공격 발생 조건, 공격 줄 생성, 버퍼 관리까지 책임진다.
 * - Local/P2P 어디서든 동일한 규칙을 재사용할 수 있도록 외부 의존성을 배제했다.
 */
public final class VersusRules {

    private final PendingAttackBuffer p1Buffer = new PendingAttackBuffer();
    private final PendingAttackBuffer p2Buffer = new PendingAttackBuffer();
    private final int minLinesForAttack;
    private final Random random = new Random();

    public VersusRules() {
        this(2);
    }

    public VersusRules(int minLinesForAttack) {
        this.minLinesForAttack = Math.max(2, minLinesForAttack);
    }

    /**
     * 블록이 잠긴 뒤 라인 삭제가 완료되면 호출된다.
     * @param playerId   공격을 발생시킨 플레이어 (1 또는 2)
     * @param snapshot   잠긴 블록의 셀 좌표 정보
     * @param clearedYs  삭제된 라인들의 y 인덱스 배열
     * @param boardWidth 보드 너비 (구멍 패턴 계산에 사용)
     */
    public void onPieceLocked(int playerId,
                              LockedPieceSnapshot snapshot,
                              int[] clearedYs,
                              int boardWidth) {
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }
        if (clearedYs == null || clearedYs.length < minLinesForAttack) {
            return;
        }
        List<AttackLine> attack = buildAttackLines(boardWidth, clearedYs, snapshot, random);
        opponentBuffer(playerId).enqueue(attack);
    }

    public List<AttackLine> consumeAttackLinesForNextSpawn(int playerId) {
        return buffer(playerId).flushAll();
    }

    /**
     * UI에서 대기 중인 공격 줄 수치를 표시할 때 사용한다.
     */
    public int getPendingLineCount(int playerId) {
        return buffer(playerId).size();
    }

    /**
     * UI에서 대기 중인 공격 줄의 실제 패턴(구멍 위치)을 표시할 때 사용한다.
     */
    public List<AttackLine> getPendingAttackLines(int playerId) {
        return buffer(playerId).peekAll();
    }

    private PendingAttackBuffer buffer(int playerId) {
        return playerId == 1 ? p1Buffer : p2Buffer;
    }

    private PendingAttackBuffer opponentBuffer(int playerId) {
        return playerId == 1 ? p2Buffer : p1Buffer;
    }

    private static List<AttackLine> buildAttackLines(int boardWidth,
                                                     int[] clearedYs,
                                                     LockedPieceSnapshot snapshot,
                                                     Random rng) {
        List<AttackLine> rows = new ArrayList<>(clearedYs.length);
        for (int y : clearedYs) {
            boolean[] holes = new boolean[boardWidth];
            // 삭제를 완성한 블록 셀 중 하나를 구멍으로 선택 (없으면 폭 전체 중 랜덤)
            List<Cell> contributors = new ArrayList<>();
            for (Cell cell : snapshot.cells()) {
                if (cell.y() == y && cell.x() >= 0 && cell.x() < boardWidth) {
                    contributors.add(cell);
                }
            }
            int holeX;
            if (!contributors.isEmpty()) {
                Cell chosen = contributors.get(rng.nextInt(contributors.size()));
                holeX = chosen.x();
            } else {
                holeX = boardWidth <= 0 ? 0 : rng.nextInt(boardWidth);
            }
            holes[holeX] = true; // 단 하나의 구멍만 남긴다.
            rows.add(new AttackLine(holes));
        }
        return rows;
    }
}
