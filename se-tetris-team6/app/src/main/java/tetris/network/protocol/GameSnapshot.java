package tetris.network.protocol;

import java.io.Serializable;

/**
 * 전체 게임 상태 스냅샷 (네트워크 전송용 DTO)
 */
public final class GameSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int[][] board;      // [y][x] 셀 값(0=빈칸, >0=블록 id)
    private final int currentBlockId; // 현재 블록 유형 id
    private final int nextBlockId;    // 다음 블록 유형 id
    private final int score;
    private final int elapsedSeconds;
    private final int pendingGarbage;
    private final int blockX;         // 현재 블록 X 위치
    private final int blockY;         // 현재 블록 Y 위치
    private final int blockRotation;  // 현재 블록 회전 상태 (0-3)

    public GameSnapshot(int[][] board,
                        int currentBlockId,
                        int nextBlockId,
                        int score,
                        int elapsedSeconds,
                        int pendingGarbage,
                        int blockX,
                        int blockY,
                        int blockRotation) {
        this.board = board;
        this.currentBlockId = currentBlockId;
        this.nextBlockId = nextBlockId;
        this.score = score;
        this.elapsedSeconds = elapsedSeconds;
        this.pendingGarbage = pendingGarbage;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockRotation = blockRotation;
    }

    public int[][] board() { return board; }
    public int currentBlockId() { return currentBlockId; }
    public int nextBlockId() { return nextBlockId; }
    public int score() { return score; }
    public int elapsedSeconds() { return elapsedSeconds; }
    public int pendingGarbage() { return pendingGarbage; }
    public int blockX() { return blockX; }
    public int blockY() { return blockY; }
    public int blockRotation() { return blockRotation; }
}
