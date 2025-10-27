package tetris.domain;

import java.util.Arrays;

/**
 * 순수 도메인 보드(격자/충돌/배치/줄삭제만 담당).
 * - 크기: 20 x 10
 * - 좌표계: (0,0)=좌상단, x→오른쪽, y→아래
 * - 값: 0=빈칸, >0=blockId (색상/표현은 UI에서)
 */
public final class Board {
    public static final int H = 20;
    public static final int W = 10;

    private final int[][] grid; // grid[y][x]

    public Board() {
        this.grid = new int[H][W];
    }

    /** 외부 배열로부터 깊은 복사 생성자(테스트/리플레이용) */
    public Board(int[][] gridCopy) {
        this.grid = deepCopy(gridCopy);
    }

    /** 보드 스냅샷(깊은 복사) */
    public int[][] gridView() {
        return deepCopy(grid);
    }

    /** 경계+충돌 판정 */
    public boolean canPlace(ShapeView shape, int originX, int originY) {
        for (int y = 0; y < shape.height(); y++) {
            for (int x = 0; x < shape.width(); x++) {
                if (!shape.filled(x, y)) continue;
                int gx = originX + x, gy = originY + y;
                if (gx < 0 || gx >= W || gy < 0 || gy >= H) return false;
                if (grid[gy][gx] != 0) return false;
            }
        }
        return true;
    }

    /** 잠금(고정). 경계/충돌은 이미 확인했다고 가정 */
    public void place(ShapeView shape, int originX, int originY, int blockId) {
        for (int y = 0; y < shape.height(); y++) {
            for (int x = 0; x < shape.width(); x++) {
                if (!shape.filled(x, y)) continue;
                int gx = originX + x, gy = originY + y;
                if (gx < 0 || gx >= W || gy < 0 || gy >= H) continue; // 방어
                grid[gy][gx] = blockId;
            }
        }
    }

    /**
     * 가득 찬 줄을 모두 삭제하고 위 라인들을 아래로 당김.
     * @return 삭제된 줄 수
     */
    public int clearLines() {
        int cleared = 0;
        for (int y = H - 1; y >= 0; y--) {
            if (isFullRow(y)) {
                clearRow(y);
                cleared++;
                y++; // 위가 내려왔으니 같은 y를 재검사
            }
        }
        return cleared;
    }

    /** 스폰 가능 여부(초기 배치 가능?) */
    public boolean canSpawn(ShapeView shape, int spawnX, int spawnY) {
        return canPlace(shape, spawnX, spawnY);
    }

    // 내부 유틸
    private boolean isFullRow(int y) {
        for (int x = 0; x < W; x++) if (grid[y][x] == 0) return false;
        return true;
    }

    private void clearRow(int y) {
        for (int r = y; r > 0; r--) {
            System.arraycopy(grid[r - 1], 0, grid[r], 0, W);
        }
        Arrays.fill(grid[0], 0);
    }

    private static int[][] deepCopy(int[][] src) {
        int[][] cp = new int[src.length][];
        for (int i = 0; i < src.length; i++) cp[i] = Arrays.copyOf(src[i], src[i].length);
        return cp;
    }
}
