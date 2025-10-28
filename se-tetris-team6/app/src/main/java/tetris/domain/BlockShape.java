package tetris.domain;

import java.util.Arrays;

/**
 * 7종 테트로미노 도형(도메인 순수형).
 * Board에 그대로 넘길 수 있도록 ShapeView 구현.
 */
public final class BlockShape implements ShapeView {
    private final BlockKind kind;
    private final boolean[][] mask; // [h][w] = true면 점유

    public BlockShape(BlockKind kind, boolean[][] mask) {
        this.kind = kind;
        int h = mask.length;
        int w = mask[0].length;
        this.mask = new boolean[h][w];
        for (int y = 0; y < h; y++) this.mask[y] = Arrays.copyOf(mask[y], w);
    }

    public BlockKind kind() { return kind; }

    @Override public int height() { return mask.length; }
    @Override public int width()  { return mask[0].length; }
    @Override public boolean filled(int x, int y) { return mask[y][x]; }

    /** 시계방향 90도 회전 */
    public BlockShape rotatedCW() {
        int h = height(), w = width();
        boolean[][] rot = new boolean[w][h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
            rot[x][h - 1 - y] = mask[y][x];
        return new BlockShape(kind, rot);
    }

    /** 7종 기본 팩토리 */
    public static BlockShape of(BlockKind k) {
        switch (k) {
            case I: return new BlockShape(k, new boolean[][]{
                    { true, true, true, true }
            });
            case O: return new BlockShape(k, new boolean[][]{
                    { true, true },
                    { true, true }
            });
            case T: return new BlockShape(k, new boolean[][]{
                    { true, true, true },
                    { false, true, false }
            });
            case S: return new BlockShape(k, new boolean[][]{
                    { false, true, true },
                    { true,  true, false }
            });
            case Z: return new BlockShape(k, new boolean[][]{
                    { true,  true, false },
                    { false, true, true }
            });
            case J: return new BlockShape(k, new boolean[][]{
                    { true, false, false },
                    { true, true,  true  }
            });
            case L: return new BlockShape(k, new boolean[][]{
                    { false, false, true },
                    { true,  true,  true }
            });
            default: throw new IllegalArgumentException("Unknown kind: " + k);
        }
    }
}