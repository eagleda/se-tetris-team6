package tetris.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import tetris.data.score.InMemoryScoreRepository;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.setting.SettingService;
import tetris.domain.block.BlockLike;
import tetris.domain.handler.GameHandler;
import tetris.domain.handler.GameOverHandler;
import tetris.domain.handler.GamePlayHandler;
import tetris.domain.handler.MenuHandler;
import tetris.domain.handler.NameInputHandler;
import tetris.domain.handler.PausedHandler;
import tetris.domain.handler.ScoreboardHandler;
import tetris.domain.handler.SettingsHandler;
import tetris.domain.item.ItemBehavior;
import tetris.domain.item.ItemContextImpl;
import tetris.domain.item.ItemManager;
import tetris.domain.item.ItemType;
import tetris.domain.item.behavior.BombBehavior;
import tetris.domain.item.behavior.DoubleScoreBehavior;
import tetris.domain.item.behavior.TimeSlowBehavior;
import tetris.domain.item.model.ItemBlockModel;
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
public final class GameModel implements tetris.domain.engine.GameplayEngine.GameplayEvents {

    /**
     * UI 계층과의 최소 연결 지점.
     * 뷰에서 필요한 오버레이 제어만 위임받습니다.
     */
    public interface UiBridge {
        void showPauseOverlay();
        void hidePauseOverlay();
        void refreshBoard();
        /** Show the Game Over overlay. If canEnterName is true the UI should
         * provide a name entry flow to persist to the leaderboard. */
        void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName);
        /** Show a dedicated name-entry overlay (optional). */
        void showNameEntryOverlay(tetris.domain.score.Score score);
    }

    private static final UiBridge NO_OP_UI_BRIDGE = new UiBridge() {
        @Override public void showPauseOverlay() { /* no-op */ }
        @Override public void hidePauseOverlay() { /* no-op */ }
        @Override public void refreshBoard() { /* no-op */ }
        @Override public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) { /* no-op */ }
        @Override public void showNameEntryOverlay(tetris.domain.score.Score score) { /* no-op */ }
    };

    private final Board board = new Board();
    private final InputState inputState = new InputState();
    private final tetris.domain.engine.GameplayEngine gameplayEngine;
    private final ScoreRepository scoreRepository;
    private final ScoreRuleEngine scoreEngine;
    private final tetris.domain.leaderboard.LeaderboardRepository leaderboardRepository;
    private final SettingService settingService;
    private BlockGenerator blockGenerator;
    private final Map<GameState, GameHandler> handlers = new EnumMap<>(GameState.class);
    private GameMode currentMode = GameMode.STANDARD;
    private GameMode lastMode = GameMode.STANDARD;
    private final ItemManager itemManager = new ItemManager();
    private final Random itemRandom = new Random();
    private final List<Supplier<ItemBehavior>> behaviorFactories = List.of(
        () -> new DoubleScoreBehavior(600, 2.0),
        () -> new TimeSlowBehavior(600, 0.5),
        () -> new BombBehavior(1)
    );
    public static final class ActiveItemInfo {
        private final BlockLike block;
        private final String label;
        private final ItemType type;

        public ActiveItemInfo(BlockLike block, String label, ItemType type) {
            this.block = block;
            this.label = label;
            this.type = type;
        }

        public BlockLike block() {
            return block;
        }

        public String label() {
            return label;
        }

        public ItemType type() {
            return type;
        }
    }
    private ItemBlockModel activeItemBlock;
    private boolean nextBlockIsItem;
    private int totalClearedLines;
    private long currentTick;
    private double scoreMultiplier = 1.0;
    private long doubleScoreUntilTick;
    private double slowFactor = 1.0;
    private long slowUntilTick;
    private ItemContextImpl itemContext;

    private UiBridge uiBridge = NO_OP_UI_BRIDGE;
    private GameState currentState;
    private GameHandler currentHandler;
    

    public GameModel() {
        this(new RandomBlockGenerator(), new InMemoryScoreRepository());
    }

    public GameModel(BlockGenerator generator, ScoreRepository scoreRepository) {
    this.scoreRepository = Objects.requireNonNull(scoreRepository, "scoreRepository");
    this.scoreEngine = new ScoreRuleEngine(scoreRepository);
    // persistent leaderboard for game-over name saving
    this.leaderboardRepository = new tetris.data.leaderboard.PreferencesLeaderboardRepository();
        // lightweight setting service used by handlers that expect model-level setting operations
        this.settingService = new SettingService(new PreferencesSettingRepository(), scoreRepository);
        setBlockGenerator(generator);
        registerHandlers();
        changeState(GameState.MENU);
        gameplayEngine = new tetris.domain.engine.GameplayEngine(board, inputState, blockGenerator, scoreEngine, uiBridge);
        gameplayEngine.setEvents(this);
        itemContext = new ItemContextImpl(this);
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

    public tetris.domain.leaderboard.LeaderboardRepository getLeaderboardRepository() {
        return leaderboardRepository;
    }

    public java.util.List<tetris.domain.leaderboard.LeaderboardEntry> loadTopScores(GameMode mode, int limit) {
        try {
            return leaderboardRepository.loadTop(limit, mode);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public void clearBoardRegion(int x, int y, int width, int height) {
        board.clearArea(x, y, width, height);
        uiBridge.refreshBoard();
    }

    public void addBoardCells(int x, int y, int[][] cells) {
        if (cells == null) {
            return;
        }
        for (int row = 0; row < cells.length; row++) {
            int[] line = cells[row];
            if (line == null) {
                continue;
            }
            for (int col = 0; col < line.length; col++) {
                int value = line[col];
                if (value <= 0) {
                    continue;
                }
                board.setCell(x + col, y + row, value);
            }
        }
        uiBridge.refreshBoard();
    }

    public void addGlobalBuff(String buffId, long durationTicks, Map<String, Object> meta) {
        if (currentMode != GameMode.ITEM) {
            return;
        }
        long duration = Math.max(0, durationTicks);
        long expiry = currentTick + duration;
        Map<String, Object> data = meta == null ? Collections.emptyMap() : meta;
        if ("double_score".equals(buffId)) {
            Object factorObj = data.getOrDefault("factor", Double.valueOf(2.0));
            double factor = factorObj instanceof Number ? ((Number) factorObj).doubleValue() : 2.0;
            scoreMultiplier = Math.max(0.0, factor);
            doubleScoreUntilTick = expiry;
            scoreEngine.setMultiplier(scoreMultiplier);
        } else if ("slow".equals(buffId)) {
            Object factorObj = data.getOrDefault("factor", Double.valueOf(0.5));
            slowFactor = Math.max(0.1, factorObj instanceof Number ? ((Number) factorObj).doubleValue() : 0.5);
            slowUntilTick = expiry;
            gameplayEngine.setSpeedModifier(slowFactor);
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public GameMode getLastMode() {
        return lastMode;
    }

    public boolean isItemMode() {
        return currentMode == GameMode.ITEM;
    }

    public boolean isNextBlockItem() {
        return isItemMode() && nextBlockIsItem;
    }

    public ActiveItemInfo getActiveItemInfo() {
        if (!isItemMode() || activeItemBlock == null) {
            return null;
        }
        ItemBehavior primary = activeItemBlock.getBehaviors().isEmpty() ? null : activeItemBlock.getBehaviors().get(0);
        String label = primary != null ? primary.id() : null;
        ItemType type = primary != null ? primary.type() : ItemType.INSTANT;
        return new ActiveItemInfo(activeItemBlock.getDelegate(), label, type);
    }

    public void spawnParticles(int x, int y, String type) {
        // TODO: bridge to UI particle system. For now board만 새로고침.
        uiBridge.refreshBoard();
    }

    public void playSfx(String id) {
        // TODO: audio 시스템 연동
    }

    public Block getActiveBlock() {
        return gameplayEngine.getActiveBlock();
    }

    public void setActiveBlock(Block block) {
        this.gameplayEngine.setActiveBlock(block);
    }

    public void setBlockGenerator(BlockGenerator generator) {
        this.blockGenerator = Objects.requireNonNull(generator, "generator");
        if (gameplayEngine != null) {
            gameplayEngine.setBlockGenerator(generator);
        }
    }

    public BlockGenerator getBlockGenerator() {
        return blockGenerator;
    }

    /**
     * Return a preview of the next BlockKind without consuming the generator.
     * This is used by UI components to render a "next block" preview.
     */
    public tetris.domain.BlockKind getNextBlockKind() {
        if (blockGenerator == null) return null;
        return blockGenerator.peekNext();
    }

    public void startGame(GameMode mode) {
        GameMode selected = mode == null ? GameMode.STANDARD : mode;
        this.currentMode = selected;
        this.lastMode = selected;
        resetGameplayState();
        changeState(GameState.PLAYING);
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

    @Override
    public void onBlockSpawned(Block block) {
        if (block == null) {
            return;
        }
        if (currentMode != GameMode.ITEM) {
            activeItemBlock = null;
            nextBlockIsItem = false;
            return;
        }
        if (nextBlockIsItem) {
            List<ItemBehavior> behaviors = new ArrayList<>();
            behaviors.add(rollBehavior());
            ItemBlockModel itemBlock = new ItemBlockModel(block, behaviors);
            activeItemBlock = itemBlock;
            itemManager.add(itemBlock);
            itemBlock.onSpawn(itemContext);
            nextBlockIsItem = false;
        } else {
            activeItemBlock = null;
        }
    }

    @Override
    public void onBlockLocked(Block block) {
        if (currentMode != GameMode.ITEM) {
            return;
        }
        if (activeItemBlock != null && activeItemBlock.getDelegate() == block) {
            itemManager.onLock(itemContext, activeItemBlock);
            activeItemBlock = null;
        }
    }

    @Override
    public void onLinesCleared(int clearedLines) {
        if (clearedLines <= 0) {
            return;
        }
        totalClearedLines += clearedLines;
        if (currentMode != GameMode.ITEM) {
            return;
        }
        if (totalClearedLines % 10 == 0) {
            nextBlockIsItem = true;
        }
        itemManager.onLineClear(itemContext, null);
    }

    @Override
    public void onTick(long tick) {
        currentTick = tick;
        if (currentMode == GameMode.ITEM) {
            itemManager.tick(itemContext, tick);
            refreshBuffs(tick);
        }
    }

    private ItemBehavior rollBehavior() {
        if (behaviorFactories.isEmpty()) {
            return new DoubleScoreBehavior(600, 2.0);
        }
        int index = itemRandom.nextInt(behaviorFactories.size());
        return behaviorFactories.get(index).get();
    }

    private void refreshBuffs(long tick) {
        if (doubleScoreUntilTick > 0 && tick >= doubleScoreUntilTick) {
            doubleScoreUntilTick = 0;
            scoreMultiplier = 1.0;
            scoreEngine.setMultiplier(scoreMultiplier);
        }
        if (slowUntilTick > 0 && tick >= slowUntilTick) {
            slowUntilTick = 0;
            slowFactor = 1.0;
            gameplayEngine.setSpeedModifier(slowFactor);
        }
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
        scoreEngine.setMultiplier(1.0);
        gameplayEngine.setSpeedModifier(1.0);
        itemManager.clear();
        activeItemBlock = null;
        nextBlockIsItem = false;
        totalClearedLines = 0;
        currentTick = 0;
        scoreMultiplier = 1.0;
        doubleScoreUntilTick = 0;
        slowFactor = 1.0;
        slowUntilTick = 0;
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
        // Currently scoring is applied during gameplay via ScoreRuleEngine.
        // If there are end-of-game bonuses they should be computed here.
        // For now, this is a no-op placeholder to leave the final Score in repository.
    }

    public void showGameOverScreen() {
        // Prepare data for UI and determine whether name entry should be allowed.
        tetris.domain.score.Score finalScore = scoreRepository.load();
        boolean qualifies = false;
        List<tetris.domain.leaderboard.LeaderboardEntry> top = loadTopScores(lastMode, 10);
        if (top.isEmpty() || top.size() < 10) {
            qualifies = true;
        } else {
            int lastPoints = top.get(top.size() - 1).getPoints();
            qualifies = finalScore.getPoints() > lastPoints;
        }
        uiBridge.showGameOverOverlay(finalScore, qualifies);
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
        startGame(lastMode);
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
