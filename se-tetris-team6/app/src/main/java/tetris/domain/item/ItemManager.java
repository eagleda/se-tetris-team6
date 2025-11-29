package tetris.domain.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tetris.domain.item.model.ItemBlockModel;

/**
 * 활성 아이템 블록을 추적하고 틱/락 이벤트를 중계하는 관리자.
 */
public final class ItemManager {

    private final List<ItemBlockModel> active = new ArrayList<>();

    public void add(ItemBlockModel item) {
        if (item != null) {
            active.add(item);
        }
    }

    public void remove(ItemBlockModel item) {
        active.remove(item);
    }

    public List<ItemBlockModel> viewActive() {
        return Collections.unmodifiableList(active);
    }

    public void tick(ItemContext context, long tick) {
        for (ItemBlockModel model : new ArrayList<>(active)) {
            model.onTick(context, tick);
        }
    }

    public void onLock(ItemContext context, ItemBlockModel locked) {
        locked.onLock(context);
        remove(locked);
    }

    public void onLineClear(ItemContext context, int[] clearedRows) {
        for (ItemBlockModel model : active) {
            model.onLineClear(context, clearedRows);
        }
    }

    public void clear() {
        active.clear();
    }
}
