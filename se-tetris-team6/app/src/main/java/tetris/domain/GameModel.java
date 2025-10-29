package tetris.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import tetris.data.score.InMemoryScoreRepository;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.setting.SettingService;
import tetris.domain.handler.GameHandler;
import tetris.domain.handler.GameOverHandler;
import tetris.domain.handler.GamePlayHandler;
import tetris.domain.handler.MenuHandler;
import tetris.domain.handler.NameInputHandler;
import tetris.domain.handler.PausedHandler;
import tetris.domain.handler.ScoreboardHandler;
import tetris.domain.handler.SettingsHandler;
import tetris.domain.model.Block;
 
import tetris.domain.model.GameState;
import tetris.domain.model.InputState;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;

/**
 * 게임 핵심 도메인 모델.
 * - 현재 보드/블록/스코어 등 상태를 추적합니다.
 * - {@link GameHandler} 구현들을 상태 머신으로 등록해 UI 흐름을 제어합니다.
 * - {@link GameClock} 이벤트를 받아 중력 및 잠금 지연을 처리합니다.
 */
public final class GameModel {

    /**
     * UI 계층과의 최소 연결 지점.
     * 뷰에서 필요한 오버레이 제어만 위임받습니다.
     */
    public interface UiBridge {
        void showPauseOverlay();
        void hidePauseOverlay();
        void refreshBoard();
    }

    private static final UiBridge NO_OP_UI_BRIDGE = new UiBridge() {
        @Override public void showPauseOverlay() { /* no-op */ }
        @Override public void hidePauseOverlay() { /* no-op */ }
        @Override public void refreshBoard() { /* no-op */ }
    };

    private final Board board = new Board();
    private final InputState inputState = new InputState();
    private final tetris.domain.engine.GameplayEngine gameplayEngine;
    private final ScoreRepository scoreRepository;
    private final ScoreRuleEngine scoreEngine;
    private final SettingService settingService;
    private BlockGenerator blockGenerator;
    private final Map<GameState, GameHandler> handlers = new EnumMap<>(GameState.class);

    private UiBridge uiBridge = NO_OP_UI_BRIDGE;
    private GameState currentState;
    private GameHandler currentHandler;
    

    public GameModel() {
        this(new RandomBlockGenerator(), new InMemoryScoreRepository());
    }

    public GameModel(BlockGenerator generator, ScoreRepository scoreRepository) {
        this.scoreRepository = Objects.requireNonNull(scoreRepository, "scoreRepository");
        this.scoreEngine = new ScoreRuleEngine(scoreRepository);
        // lightweight setting service used by handlers that expect model-level setting operations
        this.settingService = new SettingService(new PreferencesSettingRepository(), scoreRepository);
        setBlockGenerator(generator);
        registerHandlers();
        changeState(GameState.MENU);
        gameplayEngine = new tetris.domain.engine.GameplayEngine(board, inputState, blockGenerator, scoreEngine, uiBridge);
    }

    private void registerHandlers() {
        registerHandler(new MenuHandler());
        registerHandler(new GamePlayHandler());
        registerHandler(new PausedHandler());
        registerHandler(new GameOverHandler());
        registerHandler(new SettingsHandler());
        registerHandler(new ScoreboardHandler());
        registerHandler(new NameInputHandler());
    }

    private void registerHandler(GameHandler handler) {
        handlers.put(handler.getState(), handler);
    }

    public void changeState(GameState next) {
        Objects.requireNonNull(next, "next");
        System.out.printf("[LOG] GameModel.changeState(%s -> %s)%n",
            currentState, next);
        if (currentHandler != null) {
            currentHandler.exit(this);
        }
        currentState = next;
        currentHandler = Optional.ofNullable(handlers.get(next))
            .orElseThrow(() -> new IllegalStateException("No handler for state: " + next));
        currentHandler.enter(this);
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void update() {
        if (currentHandler != null) {
            currentHandler.update(this);
        }
    }

    public Board getBoard() {
        return board;
    }

    public InputState getInputState() {
        return inputState;
    }

    public Score getScore() {
        return scoreRepository.load();
    }

    public ScoreRepository getScoreRepository() {
        return scoreRepository;
    }

    public ScoreRuleEngine getScoreEngine() {
        return scoreEngine;
    }

    public Block getActiveBlock() {
        return gameplayEngine.getActiveBlock();
    }

    public void setActiveBlock(Block block) {
        this.gameplayEngine.setActiveBlock(block);
    }

    public void setBlockGenerator(BlockGenerator generator) {
        this.blockGenerator = Objects.requireNonNull(generator, "generator");
    }

    public BlockGenerator getBlockGenerator() {
        return blockGenerator;
    }

    public void spawnIfNeeded() {
        gameplayEngine.spawnIfNeeded();
        if (gameplayEngine.getActiveBlock() == null) {
            changeState(GameState.GAME_OVER);
        }
    }

    public void resumeClock() {
        gameplayEngine.resumeClock();
    }

    public void pauseClock() {
        gameplayEngine.pauseClock();
    }

    private boolean isPlayingState() {
        return currentState == GameState.PLAYING;
    }

    private boolean ensureActiveBlockPresent() {
        if (gameplayEngine.getActiveBlock() == null) {
            spawnIfNeeded();
        }
        return gameplayEngine.getActiveBlock() != null;
    }

    

    private void resetInputAxes() {
        inputState.setLeft(false);
        inputState.setRight(false);
        inputState.setSoftDrop(false);
        inputState.clearOneShotInputs();
    }

    private void resetGameplayState() {
        resetInputAxes();
        board.clear();
        scoreEngine.resetScore();
        gameplayEngine.setActiveBlock(null);
        gameplayEngine.stopClockCompletely();
    }

    private void stopClockCompletely() {
        gameplayEngine.stopClockCompletely();
    }

    

    public void stepGameplay() {
        gameplayEngine.stepGameplay();
    }

    public void bindUiBridge(UiBridge bridge) {
        this.uiBridge = Objects.requireNonNull(bridge, "bridge");
        this.gameplayEngine.setUiBridge(this.uiBridge);
    }

    public void clearUiBridge() {
        uiBridge = NO_OP_UI_BRIDGE;
    }

    public void showPauseOverlay() {
        uiBridge.showPauseOverlay();
    }

    public void hidePauseOverlay() {
        uiBridge.hidePauseOverlay();
    }

    public void computeFinalScore() {
        // TODO: 남은 보너스 계산 등 최종 점수 확정
    }

    public void showGameOverScreen() {
        // TODO: GameOver 화면에 필요한 데이터 전달
    }

    public void loadSettings() {
        // Load settings via SettingService so handlers can rely on model-level API
        settingService.getSettings();
    }

    public void saveSettings() {
        settingService.save();
    }

    public void loadScoreboard() {
        // Ensure scoreboard data is loaded; delegate to repository
        scoreRepository.load();
    }

    public void prepareNameEntry() {
        // Placeholder for name entry preparation; handled by NameInputHandler in UI flow
    }

    public void processNameEntry() {
        // Placeholder for processing name entry (persisting, validation etc.)
    }

    public void commitLineClear(int linesCleared, int combo) {
        // Delegate to score engine for line clear scoring
        if (scoreEngine != null && linesCleared > 0) {
            scoreEngine.onLinesCleared(linesCleared);
        }
    }

    

    // === 외부 제어 진입점 ===

    public void pauseGame() {
        if (currentState == GameState.PLAYING) {
            changeState(GameState.PAUSED);
        }
    }

    public void resumeGame() {
        if (currentState == GameState.PAUSED) {
            changeState(GameState.PLAYING);
        }
    }

    public void quitToMenu() {
        if (currentState != GameState.MENU) {
            stopClockCompletely();
            changeState(GameState.MENU);
        }
    }

    public void restartGame() {
        resetGameplayState();
        changeState(GameState.PLAYING);
    }

    public void moveBlockLeft() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        gameplayEngine.moveBlockLeft();
    }

    public void moveBlockRight() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        gameplayEngine.moveBlockRight();
    }

    public void moveBlockDown() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        gameplayEngine.moveBlockDown();
    }

    public void rotateBlockClockwise() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        gameplayEngine.rotateBlockClockwise();
    }

    public void rotateBlockCounterClockwise() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        gameplayEngine.rotateBlockCounterClockwise();
    }

    public void hardDropBlock() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        gameplayEngine.hardDropBlock();
    }

    public void holdCurrentBlock() {
        if (!isPlayingState()) {
            return;
        }
        gameplayEngine.holdCurrentBlock();
    }

    // === 상태별 보조 동작 ===

    // Menu navigation handled by UI handlers; methods removed as they were unused.

    public void proceedFromGameOver() {
        if (currentState == GameState.GAME_OVER) {
            changeState(GameState.NAME_INPUT);
        }
        uiBridge.refreshBoard();
    }

    public void navigateSettingsUp() {
        if (currentState == GameState.SETTINGS) {
            // TODO: 설정 항목 위로 이동
        }
        uiBridge.refreshBoard();
    }

    public void navigateSettingsDown() {
        if (currentState == GameState.SETTINGS) {
            // TODO: 설정 항목 아래로 이동
        }
        uiBridge.refreshBoard();
    }

    public void selectCurrentSetting() {
        if (currentState == GameState.SETTINGS) {
            // TODO: 설정 선택 또는 값 변경
        }
        uiBridge.refreshBoard();
    }

    public void exitSettings() {
        if (currentState == GameState.SETTINGS) {
            changeState(GameState.MENU);
        }
        uiBridge.refreshBoard();
    }

    public void resetAllSettings() {
        if (currentState == GameState.SETTINGS) {
            // Reset to defaults via settingService
            settingService.resetToDefaults();
        }
        uiBridge.refreshBoard();
    }

    public void exitScoreboard() {
        if (currentState == GameState.SCOREBOARD) {
            changeState(GameState.MENU);
        }
        uiBridge.refreshBoard();
    }

    public void scrollScoreboardUp() {
        if (currentState == GameState.SCOREBOARD) {
            // TODO: 스코어보드 스크롤 업
        }
        uiBridge.refreshBoard();
    }

    public void scrollScoreboardDown() {
        if (currentState == GameState.SCOREBOARD) {
            // TODO: 스코어보드 스크롤 다운
        }
        uiBridge.refreshBoard();
    }

    public void confirmNameInput() {
        if (currentState == GameState.NAME_INPUT) {
            processNameEntry();
        }
        uiBridge.refreshBoard();
    }

    public void deleteCharacterFromName() {
        if (currentState == GameState.NAME_INPUT) {
            // TODO: 이름 글자 삭제
        }
        uiBridge.refreshBoard();
    }

    public void cancelNameInput() {
        if (currentState == GameState.NAME_INPUT) {
            changeState(GameState.MENU);
        }
        uiBridge.refreshBoard();
    }

    public void addCharacterToName(char character) {
        if (currentState == GameState.NAME_INPUT) {
            // TODO: 이름 글자 추가
        }
        uiBridge.refreshBoard();
    }
}
