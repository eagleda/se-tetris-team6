package tetris.infrastructure;

import tetris.data.leaderboard.PreferencesLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.BlockGenerator;
import tetris.domain.GameModel;
import tetris.domain.RandomBlockGenerator;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;

/**
 * 임시 컴포지션 루트 역할을 수행하며 GameModel과 그 의존성을 조립합니다.
 * 인프라/프레젠테이션 계층에서만 사용해야 하며, 도메인은 이 팩토리를 참조하지 않습니다.
 */
public final class GameModelFactory {

    private GameModelFactory() {
    }

    public static GameModel createDefault() {
        ScoreRepository scoreRepository = new InMemoryScoreRepository();
        LeaderboardRepository leaderboardRepository = new PreferencesLeaderboardRepository();
        SettingRepository settingRepository = new PreferencesSettingRepository();
        SettingService settingService = new SettingService(settingRepository, scoreRepository);
        BlockGenerator generator = new RandomBlockGenerator();
        return new GameModel(generator, scoreRepository, leaderboardRepository, settingService);
    }
}
