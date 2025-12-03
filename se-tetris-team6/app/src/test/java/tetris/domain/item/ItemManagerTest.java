package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.domain.item.model.ItemBlockModel;

class ItemManagerTest {

    private ItemManager manager;

    @BeforeEach
    void setUp() {
        manager = new ItemManager();
    }

    @Test
    void addAndConsumeItems() {
        Map<String, Integer> items = new HashMap<>();
        items.put("bomb", 1);
        items.put("slow", 2);

        items.forEach((id, count) -> {
            for (int i = 0; i < count; i++) {
                manager.addItem(id, new ItemBlockModel(0, 0, List.of()));
            }
        });

        assertEquals(3, manager.getInventorySize());
        assertNotNull(manager.consumeNextItem());
        assertEquals(2, manager.getInventorySize());
    }
}
