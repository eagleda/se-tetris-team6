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

    public ItemBlockModel(BlockLike delegate, List<ItemBehavior> behaviors) {
        this.delegate = delegate;
        this.behaviors = new ArrayList<>(behaviors);
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
