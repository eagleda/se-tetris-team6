package tetris.network.protocol;

import java.io.Serializable;

/**
 * 전체 게임 상태 스냅샷 (네트워크 전송용 DTO)
 */
public final class GameSnapshot implements Serializable {
    private static final long serialVersionUID = 4L;

    private final int playerId;       // 스냅샷 대상 플레이어 (1 또는 2)
    private final int[][] board;      // [y][x] 셀 값(0=빈칸, >0=블록 id)
    private final int currentBlockId; // 현재 블록 유형 id
    private final int nextBlockId;    // 다음 블록 유형 id
    private final int score;
    private final int elapsedSeconds;
    private final int pendingGarbage;
    private final int blockX;         // 현재 블록 X 위치
    private final int blockY;         // 현재 블록 Y 위치
    private final int blockRotation;  // 현재 블록 회전 상태 (0-3)
    private final boolean[][] attackLines; // 공격 대기열 [줄 인덱스][x] (true=구멍, false=블록)
    // 아이템 모드 전송용
    private final String activeItemLabel;    // 현재 활성 아이템 id (없으면 null)
    private final int itemCellX;             // 아이템 셀의 블록 내 X (없으면 -1)
    private final int itemCellY;             // 아이템 셀의 블록 내 Y (없으면 -1)
    // 라인 클리어 하이라이트용
    private final int[] clearedLines;        // 방금 제거된 라인들 (없으면 null)

    public GameSnapshot(int playerId,
                        int[][] board,
                        int currentBlockId,
                        int nextBlockId,
                        int score,
                        int elapsedSeconds,
                        int pendingGarbage,
                        int blockX,
                        int blockY,
                        int blockRotation,
                        boolean[][] attackLines,
                        String activeItemLabel,
                        int itemCellX,
                        int itemCellY,
                        int[] clearedLines) {
        this.playerId = playerId;
        this.board = board;
        this.currentBlockId = currentBlockId;
        this.nextBlockId = nextBlockId;
        this.score = score;
        this.elapsedSeconds = elapsedSeconds;
        this.pendingGarbage = pendingGarbage;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockRotation = blockRotation;
        this.attackLines = attackLines;
        this.activeItemLabel = activeItemLabel;
        this.itemCellX = itemCellX;
        this.itemCellY = itemCellY;
        this.clearedLines = clearedLines;
    }

    public int playerId() { return playerId; }
    public int[][] board() { return board; }
    public int currentBlockId() { return currentBlockId; }
    public int nextBlockId() { return nextBlockId; }
    public int score() { return score; }
    public int elapsedSeconds() { return elapsedSeconds; }
    public int pendingGarbage() { return pendingGarbage; }
    public int blockX() { return blockX; }
    public int blockY() { return blockY; }
    public int blockRotation() { return blockRotation; }
    public boolean[][] attackLines() { return attackLines; }
    public String activeItemLabel() { return activeItemLabel; }
    public int itemCellX() { return itemCellX; }
    public int itemCellY() { return itemCellY; }
    public int[] clearedLines() { return clearedLines; }
}
