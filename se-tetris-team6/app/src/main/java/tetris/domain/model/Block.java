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
    private int rotation; // 회전 상태 (0-3)

    public Block(BlockShape shape, int spawnX, int spawnY) {
        this.shape = shape;
        this.x = spawnX;
        this.y = spawnY;
        this.rotation = 0;
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

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation % 4;
    }

    public void rotateCW() {
        shape = shape.rotatedCW();
        rotation = (rotation + 1) % 4;
    }

    /**
     * 시계 반대 방향 회전 (CCW). 기존 rotateCW 3회와 동일하지만 rotation 필드를 즉시 반영.
     */
    public void rotateCCW() {
        shape = shape.rotatedCW().rotatedCW().rotatedCW();
        rotation = (rotation + 3) % 4;
    }

    public Block copy() {
        Block copied = new Block(shape, x, y);
        copied.rotation = this.rotation;
        return copied;
    }
}
