package tetris.domain.item.behavior;

import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;
import tetris.domain.item.ItemType;
import tetris.domain.item.model.ItemBlockModel;

/**
 * 고정 지점 주변의 셀을 폭파하는 즉발 아이템.
 */
public final class BombBehavior implements ItemBehavior {

    private final int radius;
    private boolean triggered;    

    public BombBehavior(int radius) {
        this.radius = Math.max(0, radius);
    }

    @Override
    public String id() {
        return "bomb";
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
        int cx = block.getX();
        int cy = block.getY();
        int diameter = radius * 2 + 1;
        ctx.requestClearCells(cx - radius, cy - radius, diameter, diameter);
        ctx.spawnParticles(cx, cy, "bomb");
        ctx.playSfx("bomb");
        triggered = true;
    }
}
