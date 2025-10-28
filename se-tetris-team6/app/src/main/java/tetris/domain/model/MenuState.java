package tetris.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * 메인 메뉴 선택 상태를 관리하는 단순 모델.
 * - 메뉴 항목과 현재 선택 인덱스를 관리합니다.
 * - 선택된 항목을 활성화하면 {@link GameModel}에 적절한 동작을 위임합니다.
 */
public final class MenuState {

    private final List<String> items = new ArrayList<>();
    private int selectedIndex;

    public MenuState() {
        // 기본 메뉴 항목
        items.add("GAME");
        items.add("SETTINGS");
        items.add("SCOREBOARD");
        items.add("EXIT");
        selectedIndex = 0;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public List<String> getItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<String> newItems) {
        Objects.requireNonNull(newItems, "newItems");
        if (newItems.isEmpty()) {
            throw new IllegalArgumentException("Menu items must not be empty");
        }
        items.clear();
        items.addAll(newItems);
        selectedIndex = Math.min(selectedIndex, items.size() - 1);
    }

    public void reset() {
        selectedIndex = 0;
    }

    public void moveUp() {
        selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
    }

    public void moveDown() {
        selectedIndex = (selectedIndex + 1) % items.size();
    }

    public void activateSelected(GameModel model) {
        Objects.requireNonNull(model, "model");
        String selected = items.get(selectedIndex);
        switch (selected) {
            case "GAME" -> model.restartGame();
            case "SETTINGS" -> model.changeState(GameState.SETTINGS);
            case "SCOREBOARD" -> model.changeState(GameState.SCOREBOARD);
            case "EXIT" -> model.requestGameExit();
            default -> {
                // 확장을 위해 남겨둔 fallback
            }
        }
    }
}
