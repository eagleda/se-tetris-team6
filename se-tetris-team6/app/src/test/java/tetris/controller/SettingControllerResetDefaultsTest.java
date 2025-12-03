/*
 * 테스트 대상: tetris.controller.SettingController$4 (resetDefaults 버튼 리스너)
 *
 * 역할 요약:
 * - 설정 패널의 기본값 복원 버튼을 눌렀을 때 SettingService.resetToDefaults를 호출합니다.
 *
 * 테스트 전략:
 * - SettingService/GameController를 mock하고, 패널을 생성한 뒤 리스너를 직접 실행하여
 *   resetToDefaults가 호출되는지 확인합니다.
 */
package tetris.controller;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.setting.SettingService;
import tetris.view.SettingPanel;

class SettingControllerResetDefaultsTest {

    @Test
    void resetDefaults_invokesService() {
        SettingService service = spy(new SettingService(new tetris.data.setting.PreferencesSettingRepository(),
                                                        new InMemoryScoreRepository()));
        GameController gameController = org.mockito.Mockito.mock(GameController.class);
        SettingPanel panel = new SettingPanel();

        // 실제 생성자는 ScoreRepository, Panel, GameController, TetrisFrame
        SettingController controller = new SettingController(new InMemoryScoreRepository(), panel, gameController, null);

        // 컨트롤러 내부 service를 spy로 교체
        try {
            Field f = SettingController.class.getDeclaredField("service");
            f.setAccessible(true);
            f.set(controller, service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        panel.resetDefaultsButton.getActionListeners()[0].actionPerformed(null);

        verify(service, atLeastOnce()).resetToDefaults();
    }
}
