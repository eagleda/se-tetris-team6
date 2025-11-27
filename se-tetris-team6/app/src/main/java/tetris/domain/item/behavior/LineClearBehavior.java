
package tetris.domain.item.behavior;

import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContext;
import tetris.domain.item.ItemType;
import tetris.domain.item.model.ItemBlockModel;

/**
 * 라인 삭제: 고정된 위치의 줄을 즉시 정리한다.
 */
public final class LineClearBehavior implements ItemBehavior {

    private final int assumedBoardWidth;
    private boolean triggered;

    public LineClearBehavior() {
        this(10);
    }

    public LineClearBehavior(int boardWidth) {
        this.assumedBoardWidth = Math.max(1, boardWidth);
    }

    @Override
    public String id() {
        return "line_clear";
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
        // 아이템 칸의 위치 사용 (회전 적용됨)
        int itemX = block.getX();
        int itemY = block.getY();
        if (block.hasItemCell()) {
            itemX += block.getItemCellX();
            itemY += block.getItemCellY();
        }
        ctx.requestClearCells(0, itemY, assumedBoardWidth, 1);
        ctx.spawnParticles(itemX, itemY, "text:L");
        ctx.playSfx("line_clear_item");
        triggered = true;
    }
}
