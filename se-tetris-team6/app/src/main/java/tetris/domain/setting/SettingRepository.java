package tetris.domain.setting;

/**
 * Repository abstraction for loading and saving application settings.
 */
public interface SettingRepository {

    /**
     * Load persisted settings, or defaults if none are persisted.
     */
    Setting load();

    /**
     * Persist settings.
     */
    void save(Setting settings);

    /**
     * Reset persisted settings to defaults (both in memory and in storage).
     */
    void resetToDefaults();
}
