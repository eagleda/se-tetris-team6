package tetris.domain.item.behavior;

import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;
import tetris.domain.item.ItemType;
import tetris.domain.item.model.ItemBlockModel;

/**
 * 무게추: 고정 지점 아래 전체 영역을 정리하는 즉발 아이템.
 */
public final class WeightBehavior implements ItemBehavior {

    private final int width;
    private final int extraHeight;
    private boolean triggered;

    public WeightBehavior() {
        this(4, 1000);
    }

    public WeightBehavior(int width, int extraHeight) {
        this.width = Math.max(1, width);
        this.extraHeight = Math.max(1, extraHeight);
    }

    @Override
    public String id() {
        return "weight";
    }

    @Override
    public ItemType type() {
        return ItemType.INSTANT;
    }

    @Override
    public void onLock(ItemContext ctx, ItemBlockModel block) {
        if (triggered) {
            return;
        }
        int xLeft = block.getX();
        int yBelow = block.getY() + 1;
        ctx.requestClearCells(xLeft, yBelow, width, extraHeight);
        ctx.spawnParticles(block.getX(), block.getY(), "weight");
        ctx.playSfx("weight_drop");
        triggered = true;
    }
}
