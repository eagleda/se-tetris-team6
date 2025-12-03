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
 * 테스트 대상: tetris.domain.handler.SettingsHandler
 *
 * 역할 요약:
 * - SETTINGS 상태에서 설정을 로드/저장하도록 GameModel에 위임한다.
 * - enter 시 loadSettings, exit 시 saveSettings를 호출한다.
 *
 * 테스트 전략:
 * - enter가 loadSettings를 호출하고, exit가 saveSettings를 호출하는지 검증.
 * - getState가 SETTINGS를 반환하는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5, Mockito.
 *
 * 주요 테스트 시나리오 예시:
 * - enter_callsLoadSettings
 * - exit_callsSaveSettings
 */
@ExtendWith(MockitoExtension.class)
class SettingsHandlerTest {

    @Mock GameModel model;
    SettingsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SettingsHandler();
    }

    @Test
    void enter_callsLoadSettings() {
        handler.enter(model);
        verify(model).loadSettings();
    }

    @Test
    void exit_callsSaveSettings() {
        handler.exit(model);
        verify(model).saveSettings();
    }

    @Test
    void getState_returnsSettings() {
        assertEquals(GameState.SETTINGS, handler.getState());
    }
}
