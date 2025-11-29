package tetris.domain.item;

import tetris.domain.item.model.ItemBlockModel;

/**
 * 아이템 블록의 수명 주기를 정의하는 계약.
 */
public interface ItemBehavior {

    String id();

    ItemType type();

    default void onSpawn(ItemContext ctx, ItemBlockModel block) {}

    default void onTick(ItemContext ctx, ItemBlockModel block, long tick) {}

    default void onLock(ItemContext ctx, ItemBlockModel block) {}

    default void onLineClear(ItemContext ctx, ItemBlockModel block, int[] clearedRows) {}

    default boolean isExpired() { return false; }
}
