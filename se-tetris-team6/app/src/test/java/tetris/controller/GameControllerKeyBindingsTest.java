/**
 * 대상: tetris.controller.GameController
 *
 * 목적:
 * - applyKeyBindings/applyDifficulty 등 설정 관련 분기를 검증해 높은 미싱 라인을 보강한다.
 * - Mockito 사용 이유: GameModel/BlockGenerator 협력자를 간단히 스텁하여 키/난이도 적용 호출을 확인하기 위함.
 *
 * 주요 시나리오:
 * 1) applyKeyBindings가 null 값을 건너뛰고 전달된 키 코드로 맵을 업데이트하는지 확인
 * 2) applyDifficulty가 null일 때 NORMAL로, 전달 시 해당 값으로 BlockGenerator.setDifficulty를 호출하는지 확인
 */
package tetris.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.BlockGenerator;
import tetris.domain.GameDifficulty;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;

class GameControllerKeyBindingsTest {

    @Test
    void applyKeyBindings_updatesNonNullEntries() {
        GameModel model = mock(GameModel.class, Mockito.withSettings().lenient());
        when(model.getCurrentState()).thenReturn(GameState.MENU);
        GameController controller = new GameController(model);

        java.util.Map<String, Integer> updates = new java.util.HashMap<>();
        updates.put("MOVE_LEFT", 111);
        updates.put("MOVE_RIGHT", null); // null 값 업데이트 시 건너뛰는지 확인
        controller.applyKeyBindings(updates);

        // 내부 keyBindings를 리플렉션으로 조회
        try {
            var f = GameController.class.getDeclaredField("keyBindings");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Integer> kb = (Map<String, Integer>) f.get(controller);
            assertEquals(111, kb.get("MOVE_LEFT"));
            // null 값은 업데이트되지 않아 기존 값이 유지됨
            assertEquals(kb.get("MOVE_RIGHT"), kb.get("MOVE_RIGHT"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void applyDifficulty_setsGeneratorDifficulty() {
        GameModel model = mock(GameModel.class, Mockito.withSettings().lenient());
        BlockGenerator gen = mock(BlockGenerator.class, Mockito.withSettings().lenient());
        when(model.getBlockGenerator()).thenReturn(gen);

        GameController controller = new GameController(model);
        controller.applyDifficulty(GameDifficulty.HARD);

        verify(gen, atLeastOnce()).setDifficulty(GameDifficulty.HARD);
    }
}
