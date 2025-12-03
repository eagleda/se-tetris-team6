package tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import tetris.domain.GameDifficulty;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingService;
import tetris.domain.score.ScoreRepository;
import tetris.util.KeyMapper;
import tetris.view.SettingPanel;
import tetris.view.TetrisFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.controller.SettingController
 *
 * 역할 요약:
 * - 설정 패널의 버튼/콤보 이벤트를 받아 SettingService에 저장/로드/초기화를 위임하고,
 *   GameController/TetrisFrame/GameModel에 즉시 반영한다.
 *
 * 테스트 전략:
 * - 사용 라이브러리:
 *   - JUnit 5 (junit-jupiter)
 *   - Mockito로 GameController, TetrisFrame, ScoreRepository, SettingService 등을 mock 하고,
 *     컨트롤러가 이들에게 기대한 메서드를 호출하는지를 검증한다.
 *
 * - 설계 가정:
 *   - 컨트롤러는 생성자에서 실제 SettingService를 만든다. 테스트에서는 reflection으로 mock 서비스로 교체한다.
 *   - UI는 실제 SettingPanel 인스턴스를 사용하고, 액션 리스너를 직접 호출한다.
 *
 * - 테스트 방식:
 *   - given: mock 협력자 + panel 준비, 컨트롤러 생성 후 service를 mock으로 치환
 *   - when : 버튼/콤보 액션을 발생
 *   - then : SettingService, GameController, TetrisFrame 호출 검증
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SettingControllerTest {

    @Mock ScoreRepository scoreRepository;
    @Mock GameController gameController;
    @Mock TetrisFrame frame;
    @Mock SettingService mockService;

    SettingPanel panel;
    SettingController controller;

    @BeforeEach
    void setUp() throws Exception {
        panel = new SettingPanel();
        panel.keyMoveLeftField.setText("LEFT");
        panel.keyMoveRightField.setText("RIGHT");
        panel.keyRotateField.setText("UP");
        panel.keySoftDropField.setText("DOWN");
        panel.keyHardDropField.setText("SPACE");
        panel.p1KeyMoveLeftField.setText("A");
        panel.p1KeyMoveRightField.setText("D");
        panel.p1KeyRotateField.setText("W");
        panel.p1KeySoftDropField.setText("S");
        panel.p1KeyHardDropField.setText("SPACE");
        panel.p2KeyMoveLeftField.setText("LEFT");
        panel.p2KeyMoveRightField.setText("RIGHT");
        panel.p2KeyRotateField.setText("UP");
        panel.p2KeySoftDropField.setText("DOWN");
        panel.p2KeyHardDropField.setText("ENTER");
        panel.colorBlindCheckbox.setSelected(true);
        panel.sizeCombo.setSelectedItem(Setting.ScreenSize.MEDIUM);
        panel.difficultyCombo.setSelectedItem(GameDifficulty.HARD);

        controller = new SettingController(scoreRepository, panel, gameController, frame);

        Field f = SettingController.class.getDeclaredField("service");
        f.setAccessible(true);
        f.set(controller, mockService);
        // 컨트롤러 생성 시 발생한 초기 호출은 검증 대상이 아니므로 리셋 후 기본 스텁을 다시 설정
        reset(mockService, gameController);
        when(mockService.getSettings()).thenReturn(Setting.defaults());
    }

    @Test
    void saveButton_persistsAndApplies() {
        Setting current = Setting.defaults();
        current.setDifficulty(GameDifficulty.HARD);
        current.setColorBlindMode(true);
        Map<String, Integer> kb = new java.util.HashMap<>();
        kb.put("MOVE_LEFT", java.awt.event.KeyEvent.VK_J);
        current.setKeyBindings(kb);
        when(mockService.getSettings()).thenReturn(current);
        // UI 상태를 기대값과 맞춘다
        panel.colorBlindCheckbox.setSelected(true);
        panel.difficultyCombo.setSelectedItem(GameDifficulty.HARD);

        getListener(panel.saveButton).actionPerformed(new ActionEvent(this, 0, "save"));

        verify(mockService, atLeastOnce()).setKeyBindings(any(Map.class));
        verify(mockService, atLeastOnce()).setColorBlindMode(anyBoolean());
        verify(mockService, atLeastOnce()).setScreenSize(any(Setting.ScreenSize.class));
        verify(mockService, atLeastOnce()).setDifficulty(any(GameDifficulty.class));
        verify(mockService, atLeastOnce()).save();
        verify(gameController, atLeastOnce()).applyKeyBindings(anyMap());
        verify(gameController, atLeastOnce()).applyDifficulty(GameDifficulty.HARD);
        verify(gameController, atLeastOnce()).applyColorBlindMode(true);
    }

    @Test
    void resetDefaultsButton_resetsService() {
        getListener(panel.resetDefaultsButton).actionPerformed(new ActionEvent(this, 0, "resetDefaults"));

        verify(mockService).resetToDefaults();
    }

    @Test
    void sizeCombo_updatesScreenSizeImmediately() {
        panel.sizeCombo.setSelectedItem(Setting.ScreenSize.LARGE);

        verify(mockService, atLeastOnce()).setScreenSize(Setting.ScreenSize.LARGE);
        verify(frame).applyScreenSize(Setting.ScreenSize.LARGE);
    }

    @Test
    void difficultyCombo_appliesToGameController() {
        panel.difficultyCombo.setSelectedItem(GameDifficulty.EASY);

        verify(mockService, atLeastOnce()).setDifficulty(GameDifficulty.EASY);
        verify(gameController, atLeastOnce()).applyDifficulty(GameDifficulty.EASY);
    }

    @Test
    void resetScoresButton_confirmsAndResets() throws Exception {
        when(frame.getGameModel()).thenReturn(mock(tetris.domain.GameModel.class));
        when(frame.getGameModel().getLeaderboardRepository()).thenReturn(mock(tetris.domain.leaderboard.LeaderboardRepository.class));
        try (MockedStatic<JOptionPane> mocked = mockStatic(JOptionPane.class)) {
            mocked.when(() -> JOptionPane.showConfirmDialog(any(), anyString(), anyString(), anyInt(), anyInt()))
                    .thenReturn(JOptionPane.YES_OPTION);

            getListener(panel.resetScoresButton).actionPerformed(new ActionEvent(this, 0, "resetScores"));

            verify(mockService).resetScoreboard();
            verify(frame.getGameModel().getLeaderboardRepository()).reset();
        }
    }

    @Test
    void persistFromPanel_collectsBindingsAndSaves() throws Exception {
        // given: 패널의 모든 키 입력을 유효한 이름으로 채운다
        panel.keyMoveLeftField.setText("LEFT");
        panel.keyMoveRightField.setText("RIGHT");
        panel.keyRotateField.setText("UP");
        panel.keySoftDropField.setText("DOWN");
        panel.keyHardDropField.setText("SPACE");
        panel.p1KeyMoveLeftField.setText("A");
        panel.p1KeyMoveRightField.setText("D");
        panel.p1KeyRotateField.setText("W");
        panel.p1KeySoftDropField.setText("S");
        panel.p1KeyHardDropField.setText("SHIFT");
        panel.p2KeyMoveLeftField.setText("J");
        panel.p2KeyMoveRightField.setText("L");
        panel.p2KeyRotateField.setText("I");
        panel.p2KeySoftDropField.setText("K");
        panel.p2KeyHardDropField.setText("ENTER");
        panel.colorBlindCheckbox.setSelected(true);
        panel.sizeCombo.setSelectedItem(Setting.ScreenSize.LARGE);
        panel.difficultyCombo.setSelectedItem(GameDifficulty.EASY);

        Setting afterSave = Setting.defaults();
        afterSave.setColorBlindMode(true);
        afterSave.setDifficulty(GameDifficulty.EASY);
        when(mockService.getSettings()).thenReturn(afterSave);

        // when
        invokePrivate(controller, "persistFromPanel");

        // then: key binding map이 모든 엔트리를 포함
        ArgumentCaptor<Map<String, Integer>> captor = ArgumentCaptor.forClass(Map.class);
        verify(mockService).setKeyBindings(captor.capture());
        Map<String, Integer> kb = captor.getValue();
        assertEquals(KeyMapper.nameToKeyCode("LEFT"), kb.get("MOVE_LEFT"));
        assertEquals(KeyMapper.nameToKeyCode("ENTER"), kb.get("P2_HARD_DROP"));
        assertEquals(15, kb.size()); // 5 기본 + 5 P1 + 5 P2

        verify(mockService).setColorBlindMode(true);
        verify(mockService, atLeastOnce()).setScreenSize(Setting.ScreenSize.LARGE);
        verify(mockService, atLeastOnce()).setDifficulty(GameDifficulty.EASY);
        verify(mockService).save();
        verify(gameController, atLeastOnce()).applyKeyBindings(anyMap());
        verify(gameController, atLeastOnce()).applyDifficulty(GameDifficulty.EASY);
        verify(gameController).applyColorBlindMode(true);
    }

    @Test
    void loadToPanel_populatesTextsAndCallsApplyDifficulty() throws Exception {
        Setting s = Setting.defaults();
        Map<String, Integer> kb = new HashMap<>();
        kb.put("MOVE_LEFT", java.awt.event.KeyEvent.VK_Z);
        kb.put("P1_ROTATE_CW", java.awt.event.KeyEvent.VK_Q);
        kb.put("P2_HARD_DROP", java.awt.event.KeyEvent.VK_P);
        s.setKeyBindings(kb);
        s.setColorBlindMode(false);
        s.setScreenSize(Setting.ScreenSize.SMALL);
        s.setDifficulty(GameDifficulty.NORMAL);
        when(mockService.getSettings()).thenReturn(s);

        invokePrivate(controller, "loadToPanel");

        assertEquals("Z", panel.keyMoveLeftField.getText());
        assertEquals("Q", panel.p1KeyRotateField.getText());
        assertEquals("P", panel.p2KeyHardDropField.getText());
        assertFalse(panel.colorBlindCheckbox.isSelected());
        assertEquals(Setting.ScreenSize.SMALL, panel.sizeCombo.getSelectedItem());
        assertEquals(GameDifficulty.NORMAL, panel.difficultyCombo.getSelectedItem());
        verify(gameController, atLeastOnce()).applyDifficulty(GameDifficulty.NORMAL);
    }

    private java.awt.event.ActionListener getListener(AbstractButton b) {
        return b.getActionListeners()[0];
    }

    private java.awt.event.ActionListener getListener(javax.swing.JComboBox<?> combo) {
        return combo.getActionListeners()[0];
    }

    private void invokePrivate(Object target, String method) throws Exception {
        Method m = SettingController.class.getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(target);
    }
}
