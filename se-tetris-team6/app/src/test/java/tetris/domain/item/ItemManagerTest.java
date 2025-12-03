package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.item.model.ItemBlockModel;
import tetris.domain.block.BlockLike;
import tetris.domain.BlockKind;
import tetris.domain.BlockShape;

class ItemManagerTest {

    private ItemManager manager;

    @BeforeEach
    void setUp() {
        manager = new ItemManager();
    }

    @Test
    void addAndConsumeItems() {
        ItemBlockModel a = new ItemBlockModel(dummyBlock(), java.util.List.of());
        ItemBlockModel b = new ItemBlockModel(dummyBlock(), java.util.List.of());

        manager.add(a);
        manager.add(b);

        assertEquals(2, manager.viewActive().size());
        manager.remove(a);
        assertEquals(1, manager.viewActive().size());
        manager.clear();
        assertTrue(manager.viewActive().isEmpty());
    }

    private BlockLike dummyBlock() {
        return new BlockLike() {
            @Override public BlockShape getShape() { return BlockShape.of(BlockKind.I); }
            @Override public BlockKind getKind() { return BlockKind.I; }
            @Override public int getX() { return 0; }
            @Override public int getY() { return 0; }
            @Override public void setPosition(int x, int y) { }
        };
    }
}
