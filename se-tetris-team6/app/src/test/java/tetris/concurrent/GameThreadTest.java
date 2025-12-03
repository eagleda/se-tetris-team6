package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.domain.BlockKind;
import tetris.domain.GameModel;
import tetris.domain.RandomBlockGenerator;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;
import tetris.network.GameEventListener;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.InputType;
import tetris.network.protocol.PlayerInput;
import tetris.domain.model.Block;

/**
 * GameThread의 통합 및 단위 테스트 모음입니다.
 * (기존 테스트 포함 + 메인 루프, 일시정지, 이벤트 처리 테스트 추가)
 */
public class GameThreadTest {

    private GameModel gameModel;
    private SettingService settingService;
    private RandomBlockGenerator generator;

    // 테스트 전 공통 설정
    @BeforeEach
    public void setUp() {
        generator = new RandomBlockGenerator();
        generator.forceNextBlock(BlockKind.T); // 예측 가능한 블록을 위해 T 블록 강제 설정

        InMemoryScoreRepository scoreRepo = new InMemoryScoreRepository();
        InMemoryLeaderboardRepository lbRepo = new InMemoryLeaderboardRepository();

        // 최소한의 SettingRepository 구현
        SettingRepository repo = new SettingRepository() {
            @Override public Setting load() { return Setting.defaults(); }
            @Override public void save(Setting settings) { /* no-op */ }
            @Override public void resetToDefaults() { /* no-op */ }
        };
        settingService = new SettingService(repo, scoreRepo);
        gameModel = new GameModel(generator, scoreRepo, lbRepo, settingService);
        
        // 게임 시작 및 블록 스폰
        gameModel.startGame(null);
        gameModel.spawnIfNeeded();
    }

    /**
     * 기존 테스트: 즉각적인 회전 적용이 블록 회전을 진행시키는지 검증
     */
    @Test
    public void applyImmediateRotateAdvancesBlockRotation() {
        // 준비(Arrange)
        assertNotNull(gameModel.getActiveBlock(), "Active block should be present after spawn");
        int beforeRot = gameModel.getActiveBlock().getRotation();

        GameThread thread = new GameThread(gameModel, "player1", true);

        // 실행(Act): 즉각적인 회전 입력(낙관적 예측)을 적용합니다.
        thread.applyImmediateInput(new PlayerInput(InputType.ROTATE));

        int afterRot = gameModel.getActiveBlock().getRotation();
        
        System.out.println("Rotation before input: " + beforeRot);
        System.out.println("Rotation after input: " + afterRot);

        // 검증(Assert): 회전이 진행되었습니다.
        assertNotEquals(beforeRot, afterRot, "Block rotation should change after applying ROTATE input");
    }


    /**
     * 추가 테스트 1: 메인 루프가 PlayerInput을 처리하는지 검증
     */
    @Test
    public void mainLoopProcessesQueuedPlayerInput() throws InterruptedException {
        // 준비(Arrange)
        GameThread thread = new GameThread(gameModel, "player3", true);
        Thread gameThread = new Thread(thread);
        gameThread.start();
        
        // 초기 위치 확인
        int initialX = gameModel.getActiveBlock().getX();
        
        // 실행(Act): 입력 큐에 MOVE_LEFT를 추가
        thread.addPlayerInput(new PlayerInput(InputType.MOVE_LEFT));
        
        // 메인 루프가 입력을 처리할 시간을 줍니다. (60 FPS = 16ms 틱)
        TimeUnit.MILLISECONDS.sleep(30); 
        
        // 검증(Assert)
        int finalX = gameModel.getActiveBlock().getX();
        
        // 블록이 왼쪽으로 이동했는지 확인
        assertEquals(initialX - 1, finalX, "Block X coordinate should decrease by 1 after MOVE_LEFT input is processed");
        
        // 정리
        thread.stopGame();
        gameThread.join(500);
    }
    
    /**
     * Mock GameEventListener 구현 (네트워크 호출 검증용)
     */
    private static class MockNetworkListener implements GameEventListener {
        private final AtomicBoolean attackSent = new AtomicBoolean(false);

        @Override
        public void sendPlayerInput(PlayerInput input) { /* no-op */ }

        @Override
        public void sendAttackLines(AttackLine[] attackLines) {
            attackSent.set(true);
        }
        
        @Override
        public void sendBlockRotation(Block block) { /* no-op */ }

        public boolean wasAttackSent() {
            return attackSent.get();
        }
    }

    /**
     * 추가 테스트 2: onLinesCleared 이벤트 발생 시 네트워크 리스너가 호출되는지 검증
     */
    @Test
    public void onLinesClearedTriggersNetworkListener() throws InterruptedException {
        // 준비(Arrange)
        GameThread thread = new GameThread(gameModel, "player4", true);
        MockNetworkListener mockListener = new MockNetworkListener();
        thread.setNetworkListener(mockListener);
        
        // 실제 GameThread의 이벤트 처리 루프를 검증하기 위해 스레드를 실행합니다.
        Thread gameThread = new Thread(thread);
        gameThread.start();
        
        // onLinesCleared를 호출하여 이벤트 큐에 넣고 메인 루프가 처리하도록 합니다.
        thread.onLinesCleared(4);
        
        TimeUnit.MILLISECONDS.sleep(50); // 이벤트 큐가 처리될 시간을 줍니다.

        // 검증(Assert)
        assertTrue(mockListener.wasAttackSent(), "Network listener should be called to send attack lines after 4 lines are cleared");
        
        // 정리
        thread.stopGame();
        gameThread.join(500);
    }
}
