package tetris.domain.item.behavior;

import java.util.HashMap;
import java.util.Map;

import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;
import tetris.domain.item.ItemType;
import tetris.domain.item.model.ItemBlockModel;

/**
 * 일정 시간 동안 점수 배수를 적용하는 아이템.
 */
public final class DoubleScoreBehavior implements ItemBehavior {

    private final long durationTicks;
    private final double factor;
    private boolean triggered;

    public DoubleScoreBehavior(long durationTicks, double factor) {
        this.durationTicks = durationTicks;
        this.factor = factor;
    }

    @Override
    public String id() {
        return "double_score";
    }

    @Override
    public ItemType type() {
        return ItemType.TIMED;
    }

    @Override
    public void onLock(ItemContext ctx, ItemBlockModel block) {
        if (triggered) {
            return;
        }
        // 아이템 칸의 위치 사용 (회전 적용됨)
        int itemX = block.getX();
        int itemY = block.getY();
        if (block.hasItemCell()) {
            itemX += block.getItemCellX();
            itemY += block.getItemCellY();
        }
        Map<String, Object> meta = new HashMap<>();
        meta.put("factor", factor);
        ctx.addGlobalBuff(id(), durationTicks, meta);
        ctx.spawnParticles(itemX, itemY, "text:2x");
        ctx.playSfx("double_score_on");
        triggered = true;
    }
}
