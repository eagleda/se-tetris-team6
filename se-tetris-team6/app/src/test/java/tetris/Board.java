package seoultech.se.tetris.domain;

import seoultech.se.tetris.blocks.Block;
import java.util.Arrays;

public class Board {
    public static final int HEIGHT = 20;
    public static final int WIDTH  = 10;

    // 0 = empty, 1~7 = 테트로미노 ID(색/종류). 임시로 1로도 가능
    private final int[][] grid = new int[HEIGHT][WIDTH];

    public int getWidth()  { return WIDTH; }
    public int getHeight() { return HEIGHT; }

    // 렌더/세이브용 깊은 복사
    public int[][] copyGrid() {
        int[][] copy = new int[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) System.arraycopy(grid[y], 0, copy[y], 0, WIDTH);
        return copy;
    }

    public void clearAll() {
        for (int y = 0; y < HEIGHT; y++) Arrays.fill(grid[y], 0);
    }

    // 스폰 버퍼(y<0) 허용. 좌/우/바닥은 엄격 체크, 내부(y>=0)만 충돌 검사
    public boolean canPlace(Block b, int x, int y) {
        for (int j = 0; j < b.height(); j++) {
            for (int i = 0; i < b.width(); i++) {
                if (b.getShape(i, j) == 0) continue;
                int bx = x + i, by = y + j;
                if (bx < 0 || bx >= WIDTH || by >= HEIGHT) return false;
                if (by >= 0 && grid[by][bx] != 0) return false;
            }
        }
        return true;
    }

    // 스폰 시 게임오버 판정(내부(y>=0) 겹치면 실패)
    public boolean isGameOverOnSpawn(Block b, int x, int y) {
        for (int j = 0; j < b.height(); j++) {
            for (int i = 0; i < b.width(); i++) {
                if (b.getShape(i, j) == 0) continue;
                int bx = x + i, by = y + j;
                if (bx < 0 || bx >= WIDTH || by >= HEIGHT) return true;
                if (by >= 0 && grid[by][bx] != 0) return true;
            }
        }
        return false;
    }

    // 블록 고정(임시로 blockId=1 넣어도 됨)
    public void lock(Block b, int x, int y, int blockId) {
        for (int j = 0; j < b.height(); j++) {
            for (int i = 0; i < b.width(); i++) {
                if (b.getShape(i, j) == 1) {
                    int bx = x + i, by = y + j;
                    if (0 <= bx && bx < WIDTH && 0 <= by && by < HEIGHT) {
                        grid[by][bx] = blockId;
                    }
                }
            }
        }
    }

    // 가득 찬 줄 삭제 + 낙하, 삭제 줄 수 반환
    public int clearLines() {
        int write = HEIGHT - 1, cleared = 0;
        for (int read = HEIGHT - 1; read >= 0; read--) {
            if (isFullRow(read)) { cleared++; continue; }
            if (write != read) System.arraycopy(grid[read], 0, grid[write], 0, WIDTH);
            write--;
        }
        for (int y = write; y >= 0; y--) Arrays.fill(grid[y], 0);
        return cleared;
    }

    private boolean isFullRow(int y) {
        for (int x = 0; x < WIDTH; x++) if (grid[y][x] == 0) return false;
        return true;
    }

    // 하드드롭/고스트 계산 보조
    public int findDropY(Block b, int x, int startY) {
        int y = startY;
        while (canPlace(b, x, y + 1)) y++;
        return y;
    }
}
