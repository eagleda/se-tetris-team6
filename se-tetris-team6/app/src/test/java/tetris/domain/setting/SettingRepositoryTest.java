package tetris.domain.setting;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tetris.domain.GameDifficulty;

/*
 * 테스트 대상: tetris.domain.setting.SettingRepository
 *
 * 역할 요약:
 * - 설정을 로드/저장/초기화하는 저장소 추상화.
 * - 구현체가 최소 계약(load/save/resetToDefaults)을 지키도록 안내한다.
 *
 * 테스트 전략:
 * - 단순 인메모리 구현(FakeRepository)을 통해 load/save/resetToDefaults 계약을 검증한다.
 * - save 후 load가 동일 값을 반환하고, resetToDefaults가 defaults()로 복원되는지 확인.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - save_thenLoad_returnsSavedSetting
 * - resetToDefaults_restoresDefaultSetting
 */
class SettingRepositoryTest {

    @Test
    void save_thenLoad_returnsSavedSetting() {
        FakeRepository repo = new FakeRepository();
        Setting s = Setting.defaults();
        s.setDifficulty(GameDifficulty.HARD);
        repo.save(s);

        Setting loaded = repo.load();
        assertEquals(GameDifficulty.HARD, loaded.getDifficulty());
    }

    @Test
    void resetToDefaults_restoresDefaultSetting() {
        FakeRepository repo = new FakeRepository();
        Setting s = Setting.defaults();
        s.setColorBlindMode(true);
        repo.save(s);

        repo.resetToDefaults();
        Setting loaded = repo.load();
        assertFalse(loaded.isColorBlindMode());
        assertEquals(Setting.ScreenSize.MEDIUM, loaded.getScreenSize());
    }

    private static class FakeRepository implements SettingRepository {
        private Setting current = Setting.defaults();
        @Override public Setting load() { return current; }
        @Override public void save(Setting settings) { this.current = settings; }
        @Override public void resetToDefaults() { this.current = Setting.defaults(); }
    }
}
