package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tetris.domain.GameModel;
import tetris.infrastructure.GameModelFactory;

/*
 * 테스트 대상: tetris.multiplayer.model.PlayerState
 *
 * 역할 요약:
 * - 멀티플레이어 한 명의 모델/식별자/로컬 여부/준비 상태를 보관하는 값 객체.
 *
 * 테스트 전략:
 * - id가 1 또는 2가 아닐 때 예외가 발생하는지 확인.
 * - ready 플래그 getter/setter, isLocal 플래그를 검증.
 */
class PlayerStateTest {

    @Test
    void constructor_rejectsInvalidId() {
        GameModel model = GameModelFactory.createDefault();
        assertThrows(IllegalArgumentException.class, () -> new PlayerState(0, model, true));
        assertThrows(IllegalArgumentException.class, () -> new PlayerState(3, model, false));
    }

    @Test
    void readyFlagAndLocalFlag_areStored() {
        GameModel model = GameModelFactory.createDefault();
        PlayerState state = new PlayerState(1, model, true);

        assertEquals(1, state.getId());
        assertEquals(model, state.getModel());
        assertTrue(state.isLocal());
        assertFalse(state.isReady());

        state.setReady(true);
        assertTrue(state.isReady());
    }
}
