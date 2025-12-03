package tetris.domain.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/*
 * 테스트 대상: tetris.domain.handler.NameInputHandler
 *
 * 역할 요약:
 * - NAME_INPUT 상태에서 이름 입력 플로우를 관리하며, enter 시 준비, update 시 처리 메서드를 호출한다.
 *
 * 테스트 전략:
 * - enter가 prepareNameEntry를 호출하고, update가 processNameEntry를 호출하는지 검증.
 * - getState가 NAME_INPUT을 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - enter_callsPrepareNameEntry
 * - update_callsProcessNameEntry
 */
@ExtendWith(MockitoExtension.class)
class NameInputHandlerTest {

    @Mock GameModel model;
    NameInputHandler handler;

    @BeforeEach
    void setUp() {
        handler = new NameInputHandler();
    }

    @Test
    void enter_callsPrepareNameEntry() {
        handler.enter(model);
        verify(model).prepareNameEntry();
    }

    @Test
    void update_callsProcessNameEntry() {
        handler.update(model);
        verify(model).processNameEntry();
    }

    @Test
    void getState_returnsNameInput() {
        assertEquals(GameState.NAME_INPUT, handler.getState());
    }
}
