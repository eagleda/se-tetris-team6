package tetris.domain.item.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.block.BlockLike;
import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;

/**
 * 기존 {@link tetris.domain.model.Block}을 감싸 아이템 행동을 위임하는 어댑터.
 */
public final class ItemBlockModel implements BlockLike {

    private final BlockLike delegate;
    private final List<ItemBehavior> behaviors;
    private int itemCellX = -1; // 아이템 칸의 블록 내 상대 x 좌표
    private int itemCellY = -1; // 아이템 칸의 블록 내 상대 y 좌표

    public ItemBlockModel(BlockLike delegate, List<ItemBehavior> behaviors) {
        this.delegate = delegate;
        this.behaviors = new ArrayList<>(behaviors);
    }
    
    public ItemBlockModel(BlockLike delegate, List<ItemBehavior> behaviors, int itemCellX, int itemCellY) {
        this.delegate = delegate;
        this.behaviors = new ArrayList<>(behaviors);
        this.itemCellX = itemCellX;
        this.itemCellY = itemCellY;
    }
    
    public boolean hasItemCell() {
        return itemCellX >= 0 && itemCellY >= 0;
    }
    
    public int getItemCellX() {
        return itemCellX;
    }
    
    public int getItemCellY() {
        return itemCellY;
    }
    
    public void setItemCell(int x, int y) {
        this.itemCellX = x;
        this.itemCellY = y;
    }
    
    public void updateItemCellAfterRotation() {
        if (!hasItemCell()) {
            return;
        }
        // 회전 전 shape을 기준으로 변환 (회전 전 높이 사용)
        BlockShape currentShape = getShape();
        // 역회전하여 원래 크기 확인
        BlockShape prevShape = currentShape.rotatedCW().rotatedCW().rotatedCW();
        int h = prevShape.height();
        // 시계방향 90도 회전: (x, y) -> (h-1-y, x)
        int newX = h - 1 - itemCellY;
        int newY = itemCellX;
        itemCellX = newX;
        itemCellY = newY;
    }

    public List<ItemBehavior> getBehaviors() {
        return Collections.unmodifiableList(behaviors);
    }

    public void onSpawn(ItemContext context) {
        for (ItemBehavior behavior : behaviors) {
            behavior.onSpawn(context, this);
        }
    }

    public void onLock(ItemContext context) {
        for (ItemBehavior behavior : behaviors) {
            behavior.onLock(context, this);
        }
    }

    public void onTick(ItemContext context, long tick) {
        for (ItemBehavior behavior : behaviors) {
            behavior.onTick(context, this, tick);
        }
    }

    public void onLineClear(ItemContext context, int[] clearedRows) {
        for (ItemBehavior behavior : behaviors) {
            behavior.onLineClear(context, this, clearedRows);
        }
    }

    @Override
    public BlockShape getShape() {
        return delegate.getShape();
    }

    @Override
    public BlockKind getKind() {
        return delegate.getKind();
    }

    @Override
    public int getX() {
        return delegate.getX();
    }

    @Override
    public int getY() {
        return delegate.getY();
    }

    @Override
    public void setPosition(int x, int y) {
        delegate.setPosition(x, y);
    }

    public BlockLike getDelegate() {
        return delegate;
    }
}
