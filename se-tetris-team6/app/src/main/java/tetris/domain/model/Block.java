package tetris.domain.model;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;

/**
 * 현재 조작 중인 미노를 표현하는 도메인 객체.
 */
public final class Block implements BlockLike {
    private BlockShape shape;
    private int x;
    private int y;

    public Block(BlockShape shape, int spawnX, int spawnY) {
        this.shape = shape;
        this.x = spawnX;
        this.y = spawnY;
    }

    public static Block spawn(BlockKind kind, int spawnX, int spawnY) {
        return new Block(BlockShape.of(kind), spawnX, spawnY);
    }

    @Override
    public BlockShape getShape() {
        return shape;
    }

    public void setShape(BlockShape shape) {
        this.shape = shape;
    }

    @Override
    public BlockKind getKind() {
        return shape.kind();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void moveBy(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void rotateCW() {
        shape = shape.rotatedCW();
    }

    public Block copy() {
        return new Block(shape, x, y);
    }
}
