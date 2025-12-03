package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import tetris.domain.RandomBlockGenerator;
import tetris.domain.BlockKind;
import tetris.domain.GameModel;
import tetris.data.score.InMemoryScoreRepository;
import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.domain.setting.SettingService;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.Setting;
import tetris.network.protocol.PlayerInput;
import tetris.network.protocol.InputType;

/**
 * GameThread의 낙관적 입력 적용에 대한 기본 통합 테스트입니다.
 *
 * 이 테스트는 `GameThread.applyImmediateInput(...)`를 통해 ROTATE 입력을 적용하면
 * 활성 블록의 회전 상태가 진행되는지 확인합니다.
 */
public class GameThreadTest {

    @Test
    public void applyImmediateRotateAdvancesBlockRotation() {
        // 준비(Arrange): T 블록을 생성할 결정론적(deterministic) 생성기
        RandomBlockGenerator generator = new RandomBlockGenerator();
        generator.forceNextBlock(BlockKind.T);

        InMemoryScoreRepository scoreRepo = new InMemoryScoreRepository();
        InMemoryLeaderboardRepository lbRepo = new InMemoryLeaderboardRepository();

        // SettingService를 위한 최소한의 SettingRepository 구현
        SettingRepository repo = new SettingRepository() {
            @Override public Setting load() { return Setting.defaults(); }
            @Override public void save(Setting settings) { /* no-op */ }
            @Override public void resetToDefaults() { /* no-op */ }
        };
        SettingService settingService = new SettingService(repo, scoreRepo);

        GameModel model = new GameModel(generator, scoreRepo, lbRepo, settingService);

        // 모델이 PLAYING 상태가 되고 활성 블록을 생성하도록 게임을 시작합니다.
        model.startGame(null);
        model.spawnIfNeeded();

        assertNotNull(model.getActiveBlock(), "Active block should be present after spawn");

        int beforeRot = model.getActiveBlock().getRotation();
        // 출력: 회전 전 값 (Output: Rotation value before input)
        System.out.println("Rotation before input: " + beforeRot);

        GameThread thread = new GameThread(model, "player1", true);

        // 실행(Act): 즉각적인 회전 입력(낙관적 예측)을 적용합니다.
        thread.applyImmediateInput(new PlayerInput(InputType.ROTATE));

        int afterRot = model.getActiveBlock().getRotation();
        // 출력: 회전 후 값 (Output: Rotation value after input)
        System.out.println("Rotation after input: " + afterRot);

        // 검증(Assert): 회전이 진행되었습니다 (4로 나눈 나머지). after와 before가 달라야 유효합니다.
        assertNotEquals(beforeRot, afterRot, "Block rotation should change after applying ROTATE input");
    }
}