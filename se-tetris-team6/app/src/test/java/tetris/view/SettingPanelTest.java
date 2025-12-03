package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import tetris.domain.GameDifficulty;
import tetris.domain.setting.Setting;

/*
 * 테스트 대상: tetris.view.SettingPanel
 *
 * 역할 요약:
 * - 화면 크기/난이도/색각 모드와 싱글·멀티플레이 키 바인딩을 설정하는 Swing 패널.
 *
 * 테스트 전략:
 * - 생성 직후 주요 컴포넌트가 null이 아니고 기본 값이 세팅되어 있는지 확인.
 * - 화면 크기/난이도 콤보 박스가 정의된 enum 값을 그대로 포함하는지 검증.
 * - 패널 초기 가시성(false)과 키 입력 필드의 포커스 비활성 상태를 확인.
 */
class SettingPanelTest {

    @Test
    void initializesCoreComponentsWithDefaults() throws Exception {
        SettingPanel panel = new SettingPanel();

        assertFalse(panel.isVisible());
        assertNotNull(panel.sizeCombo);
        assertNotNull(panel.difficultyCombo);
        assertNotNull(panel.colorBlindCheckbox);
        assertNotNull(panel.resetDefaultsButton);
        assertNotNull(panel.saveButton);

        // Screen size combo contains all enum values
        assertEquals(Setting.ScreenSize.values().length, panel.sizeCombo.getItemCount());
        assertEquals(Setting.ScreenSize.values()[0], panel.sizeCombo.getItemAt(0));

        // Difficulty combo contains all enum values
        assertEquals(GameDifficulty.values().length, panel.difficultyCombo.getItemCount());
        assertEquals(GameDifficulty.values()[0], panel.difficultyCombo.getItemAt(0));

        // Key fields are not focusable by default (capture only)
        assertFalse(panel.keyMoveLeftField.isFocusable());
        assertFalse(panel.keyMoveRightField.isFocusable());
        assertFalse(panel.keyRotateField.isFocusable());
        assertFalse(panel.keySoftDropField.isFocusable());
        assertFalse(panel.keyHardDropField.isFocusable());
    }
}
