package tetris.domain.setting;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import tetris.domain.GameDifficulty;

/*
 * 테스트 대상: tetris.domain.setting.Setting
 *
 * 역할 요약:
 * - 화면 크기, 키 바인딩, 색각 모드, 난이도 등 사용자 설정 값을 보관하고 변경한다.
 * - defaults()로 초기 상태를 제공하고 setter를 통해 안전하게 값 변경을 허용한다.
 *
 * 테스트 전략:
 * - defaults() 초기값 검증: 화면 크기/난이도/색각 모드/기본 키 바인딩.
 * - setter 동작 검증: Map 복사 여부, null 입력 처리, 값 변경 반영 여부 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - defaults()가 기대값으로 초기화되는지 확인.
 * - setKeyBindings가 방어 복사를 수행해 외부 Map 수정에 영향받지 않는지 확인.
 * - setDifficulty에 null을 주면 NORMAL로 처리하는지 확인.
 */
class SettingTest {

    @Test
    void defaults_areInitialized() {
        Setting s = Setting.defaults();
        assertEquals(Setting.ScreenSize.MEDIUM, s.getScreenSize());
        assertFalse(s.isColorBlindMode());
        assertEquals(GameDifficulty.NORMAL, s.getDifficulty());
        assertEquals(KeyEvent.VK_LEFT, s.getKeyBinding("MOVE_LEFT"));
    }

    @Test
    void setKeyBindings_defensiveCopyAndLookupWorks() {
        Setting s = new Setting();
        Map<String, Integer> bindings = new HashMap<>();
        bindings.put("MOVE_LEFT", KeyEvent.VK_A);
        s.setKeyBindings(bindings);

        bindings.put("MOVE_LEFT", KeyEvent.VK_RIGHT); // 외부 수정
        assertEquals(KeyEvent.VK_A, s.getKeyBinding("MOVE_LEFT"));
    }

    @Test
    void setDifficulty_handlesNullAsNormal() {
        Setting s = new Setting();
        s.setDifficulty(null);
        assertEquals(GameDifficulty.NORMAL, s.getDifficulty());

        s.setDifficulty(GameDifficulty.HARD);
        assertEquals(GameDifficulty.HARD, s.getDifficulty());
    }
}
