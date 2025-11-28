package tetris.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import tetris.domain.BlockGenerator;
import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.RandomBlockGenerator;
import tetris.domain.block.BlockLike;
import tetris.domain.setting.SettingService;
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
import tetris.domain.item.behavior.LineClearBehavior;
import tetris.domain.item.behavior.WeightBehavior;
import tetris.domain.item.model.ItemBlockModel;
import tetris.domain.model.Block;
import tetris.domain.model.GameClock;
import tetris.domain.model.GameState;
import tetris.domain.model.InputState;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;
import tetris.multiplayer.model.Cell;
import tetris.multiplayer.model.LockedPieceSnapshot;
import tetris.multiplayer.session.LocalMultiplayerSession;

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

        /**
         * Show the Game Over overlay. If canEnterName is true the UI should
         * provide a name entry flow to persist to the leaderboard.
         */
        void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName);

        /** Show a dedicated name-entry overlay (optional). */
        void showNameEntryOverlay(tetris.domain.score.Score score);

        /** 멀티플레이 승자 표시용 오버레이를 보여준다. */
        default void showMultiplayerResult(int winnerId) {
            /* no-op */
        }
    }
    /**
     * 현재 슬로우 버프의 남은 시간을 ms 단위로 반환합니다. 버프가 없으면 0을 반환합니다.
     */
    public long getSlowBuffRemainingTimeMs() {
        if (slowBuffExpiresAtMs <= 0) return 0L;
        long remaining = slowBuffExpiresAtMs - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    /**
     * 현재 더블 스코어 버프의 남은 시간을 ms 단위로 반환합니다. 버프가 없으면 0을 반환합니다.
     */
    public long getDoubleScoreBuffRemainingTimeMs() {
        if (doubleScoreUntilTick <= 0) return 0L;
        // Tick을 ms로 환산 (tick은 내부적으로 1프레임 단위, 1프레임=약 16ms로 가정)
        long nowTick = currentTick;
        long remainingTick = doubleScoreUntilTick - nowTick;
        long remainingMs = remainingTick * 16L;
        return Math.max(0L, remainingMs);
    }

    private static final UiBridge NO_OP_UI_BRIDGE = new UiBridge() {
        @Override
        public void showPauseOverlay() {
            /* no-op */ }

        @Override
        public void hidePauseOverlay() {
            /* no-op */ }

        @Override
        public void refreshBoard() {
            /* no-op */ }

        @Override
        public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) {
            /* no-op */ }

        @Override
        public void showNameEntryOverlay(tetris.domain.score.Score score) {
            /* no-op */ }
    };

    private final Board board = new Board();
    private final InputState inputState = new InputState();
    final tetris.domain.engine.GameplayEngine gameplayEngine;
    private final ScoreRepository scoreRepository;
    private final ScoreRuleEngine scoreEngine;
    private final tetris.domain.leaderboard.LeaderboardRepository leaderboardRepository;
    private final SettingService settingService;
    private BlockGenerator blockGenerator;
    private final Map<GameState, GameHandler> handlers = new EnumMap<>(GameState.class);
    private GameHandler defaultPlayHandler;
    private LocalMultiplayerSession activeLocalSession;
    private GameMode currentMode = GameMode.STANDARD;
    private GameMode lastMode = GameMode.STANDARD;
    private final ItemManager itemManager = new ItemManager();
    private final Random itemRandom = new Random();
    private final List<Supplier<ItemBehavior>> behaviorFactories = List.of(
            () -> new DoubleScoreBehavior(600, 2.0),
            TimeSlowBehavior::new,
            () -> new BombBehavior(),
            () -> new LineClearBehavior(),
            WeightBehavior::new);
    private final List<MultiplayerHook> multiplayerHooks = new CopyOnWriteArrayList<>();

    public static final class ActiveItemInfo {
        private final BlockLike block;
        private final String label;
        private final ItemType type;
        private final int itemCellX;
        private final int itemCellY;

        public ActiveItemInfo(BlockLike block, String label, ItemType type, int itemCellX, int itemCellY) {
            this.block = block;
            this.label = label;
            this.type = type;
            this.itemCellX = itemCellX;
            this.itemCellY = itemCellY;
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
        
        public int itemCellX() {
            return itemCellX;
        }
        
        public int itemCellY() {
            return itemCellY;
        }
        
        public boolean hasItemCell() {
            return itemCellX >= 0 && itemCellY >= 0;
        }
    }

    /**
     * 멀티 대전 쓰레기 줄 규칙을 GameModel의 수명주기 이벤트와 연결하기 위한 훅.
     * onPieceLocked는 줄 삭제 결과를, beforeNextSpawn은 쓰레기 주입 시점을 알린다.
     */
    public interface MultiplayerHook {
        void onPieceLocked(LockedPieceSnapshot snapshot, int[] clearedRows, int boardWidth);

        void beforeNextSpawn();
    }

    private static final int DEFAULT_ITEM_SPAWN_INTERVAL = 2;
    private static final int BLOCKS_PER_SPEED_STEP = 12;
    private static final int LINES_PER_SPEED_STEP = 4;
    private static final int MAX_SPEED_LEVEL = 20;
    private static final int LINE_CLEAR_HIGHLIGHT_DELAY_MS = 250;
    private static final long SLOW_ITEM_DURATION_MS = 15_000L;
    private static final long INACTIVITY_STAGE1_MS = 2000;
    private static final long INACTIVITY_STAGE2_MS = 5000;
    private static final int INACTIVITY_PENALTY_POINTS = 10;

    private ItemBlockModel activeItemBlock;
    private boolean nextBlockIsItem;
    private int totalClearedLines;
    private int totalSpawnedBlocks;
    private long currentTick;
    private double scoreMultiplier = 1.0;
    private long doubleScoreUntilTick;
    private int slowLevelOffset;
    private long slowBuffExpiresAtMs;
    private ItemContextImpl itemContext;
    private Supplier<ItemBehavior> behaviorOverride = () -> new WeightBehavior();
    private int itemSpawnIntervalLines = DEFAULT_ITEM_SPAWN_INTERVAL;
    private int currentGravityLevel;
    private boolean colorBlindMode;
    private long lastInputMillis = System.currentTimeMillis();
    private int inactivityPenaltyStage;
    private long pauseStartedAt = -1;
    private long gameplayStartedAtMillis = -1;
    private long accumulatedPauseMillis;
    private LockedPieceSnapshot lastLockedPieceSnapshot;

    private UiBridge uiBridge = NO_OP_UI_BRIDGE;
    private GameState currentState;
    private GameHandler currentHandler;

    public GameModel(BlockGenerator generator,
            ScoreRepository scoreRepository,
            tetris.domain.leaderboard.LeaderboardRepository leaderboardRepository,
            SettingService settingService) {
        this.scoreRepository = Objects.requireNonNull(scoreRepository, "scoreRepository");
        this.scoreEngine = new ScoreRuleEngine(scoreRepository);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository, "leaderboardRepository");
        this.settingService = Objects.requireNonNull(settingService, "settingService");
        this.colorBlindMode = this.settingService.getSettings().isColorBlindMode();
        setBlockGenerator(generator == null ? new RandomBlockGenerator() : generator);
        registerHandlers();
        changeState(GameState.MENU);
        gameplayEngine = new tetris.domain.engine.GameplayEngine(board, inputState, blockGenerator, scoreEngine,
                uiBridge);
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
        if (handler.getState() == GameState.PLAYING && defaultPlayHandler == null) {
            // 싱글 플레이 루프 복귀 시 다시 사용할 기본 핸들러를 저장해 둔다.
            defaultPlayHandler = handler;
        }
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

    /**
     * 멀티 대전 규칙 엔진이 라인 삭제/스폰 이벤트를 구독하도록 등록한다.
     * Local/P2P 컨트롤러가 훅을 넣어 쓰레기 줄 버퍼를 계산할 때 사용한다.
     */
    public void addMultiplayerHook(MultiplayerHook hook) {
        if (hook == null) {
            return;
        }
        multiplayerHooks.add(hook);
    }

    public void removeMultiplayerHook(MultiplayerHook hook) {
        if (hook == null) {
            return;
        }
        multiplayerHooks.remove(hook);
    }

    /**
     * 활성 블록을 현재 보드에 시뮬레이션으로 놓았을 때 가득 찬(삭제 대상이 될) 줄 인덱스 목록을 반환합니다.
     * 실제 보드는 변경하지 않습니다. 반환 순서는 아래(큰 y) -> 위(작은 y) 순서입니다.
     */
    public java.util.List<Integer> getPendingFullLines() {
        // active 블록이 없으면 빈 리스트 반환
        if (gameplayEngine == null || gameplayEngine.getActiveBlock() == null) {
            return java.util.Collections.emptyList();
        }
        // 보드 스냅샷(깊은 복사) 가져와 시뮬레이션
        int[][] temp = board.gridView();
        tetris.domain.model.Block active = gameplayEngine.getActiveBlock();
        BlockShape shape = active.getShape();
        int blockId = shape.kind().ordinal() + 1;
        for (int sy = 0; sy < shape.height(); sy++) {
            for (int sx = 0; sx < shape.width(); sx++) {
                if (!shape.filled(sx, sy))
                    continue;
                int gx = active.getX() + sx;
                int gy = active.getY() + sy;
                if (gx < 0 || gx >= Board.W || gy < 0 || gy >= Board.H)
                    continue;
                temp[gy][gx] = blockId;
            }
        }
        // 시뮬레이션된 보드에서 가득 찬 행 수집 (아래->위)
        java.util.List<Integer> fullRows = new ArrayList<>();
        for (int y = Board.H - 1; y >= 0; y--) {
            boolean full = true;
            for (int x = 0; x < Board.W; x++) {
                if (temp[y][x] == 0) {
                    full = false;
                    break;
                }
            }
            if (full)
                fullRows.add(y);
        }
        return fullRows;
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

    public int getSpeedLevel() {
        return effectiveGravityLevel();
    }

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public void setColorBlindMode(boolean enabled) {
        if (this.colorBlindMode == enabled) {
            return;
        }
        this.colorBlindMode = enabled;
        settingService.getSettings().setColorBlindMode(enabled);
        uiBridge.refreshBoard();
    }

    public List<Integer> getLastClearedLines() {
        if (gameplayEngine == null) {
            return Collections.emptyList();
        }
        return gameplayEngine.getLastClearedRows();
    }

    public void clearLastClearedLines() {
        if (gameplayEngine != null) {
            gameplayEngine.clearLastClearedRows();
        }
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
        if (width <= 0 || height <= 0) {
            return;
        }
        // Line-clear 아이템: 보드 전체 폭을 덮는 영역이면 실제 줄 삭제처럼 처리해 위 블록을 한 칸씩 내린다.
        boolean clearsFullRows = x <= 0 && x + width >= Board.W;
        if (clearsFullRows) {
            java.util.List<Integer> rows = new ArrayList<>();
            int start = Math.max(0, y);
            int end = Math.min(Board.H, y + height);
            for (int row = start; row < end; row++) {
                rows.add(row);
            }
            board.clearRows(rows);
        } else {
            board.clearArea(x, y, width, height);
        }
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
        Map<String, Object> data = meta == null ? Collections.emptyMap() : meta;
        if ("double_score".equals(buffId)) {
            long duration = Math.max(0, durationTicks);
            long expiry = currentTick + duration;
            Object factorObj = data.getOrDefault("factor", Double.valueOf(2.0));
            double factor = factorObj instanceof Number ? ((Number) factorObj).doubleValue() : 2.0;
            scoreMultiplier = Math.max(0.0, factor);
            doubleScoreUntilTick = expiry;
            scoreEngine.setMultiplier(scoreMultiplier);
        } else if ("slow".equals(buffId)) {
            Object durationObj = data.get("durationMs");
            long durationMs = durationObj instanceof Number
                    ? Math.max(0L, ((Number) durationObj).longValue())
                    : SLOW_ITEM_DURATION_MS;
            Object levelDeltaObj = data.getOrDefault("levelDelta", Integer.valueOf(-1));
            int levelDelta = levelDeltaObj instanceof Number ? ((Number) levelDeltaObj).intValue() : -1;
            slowLevelOffset = Math.min(0, levelDelta);
            slowBuffExpiresAtMs = System.currentTimeMillis() + durationMs;
            applyGravityLevel();
        }
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public long getElapsedMillis() {
        if (gameplayStartedAtMillis <= 0) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        long paused = accumulatedPauseMillis;
        if (pauseStartedAt > 0) {
            paused += Math.max(0L, now - pauseStartedAt);
        }
        long elapsed = now - gameplayStartedAtMillis - paused;
        return Math.max(0L, elapsed);
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
        int itemCellX = activeItemBlock.getItemCellX();
        int itemCellY = activeItemBlock.getItemCellY();
        return new ActiveItemInfo(activeItemBlock.getDelegate(), label, type, itemCellX, itemCellY);
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

    public void setItemSpawnIntervalLines(int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval must be positive");
        }
        this.itemSpawnIntervalLines = interval;
    }

    public void setItemBehaviorOverride(String behaviorId) {
        if (behaviorId == null || behaviorId.isBlank()) {
            this.behaviorOverride = null;
            return;
        }
        this.behaviorOverride = resolveBehaviorOverride(behaviorId);
    }

    public void resetItemTestingConfig() {
        this.behaviorOverride = TimeSlowBehavior::new;
        this.itemSpawnIntervalLines = DEFAULT_ITEM_SPAWN_INTERVAL;
    }

    /**
     * Return a preview of the next BlockKind without consuming the generator.
     * This is used by UI components to render a "next block" preview.
     */
    public tetris.domain.BlockKind getNextBlockKind() {
        if (blockGenerator == null)
            return null;
        return blockGenerator.peekNext();
    }

    public void startGame(GameMode mode) {
        GameMode selected = mode == null ? GameMode.STANDARD : mode;
        this.currentMode = selected;
        this.lastMode = selected;
        if (activeLocalSession != null) {
            // 로컬 멀티가 활성화되어 있다면 각 플레이어 모델을 동일한 모드로 재가동한다.
            activeLocalSession.restartPlayers(selected);
            handlers.put(GameState.PLAYING, activeLocalSession.handler());
        } else if (defaultPlayHandler != null) {
            handlers.put(GameState.PLAYING, defaultPlayHandler);
        }
        resetGameplayState();
        changeState(GameState.PLAYING);
    }

    public void spawnIfNeeded() {
        if (gameplayEngine.getActiveBlock() == null) {
            notifyBeforeNextSpawnHooks();
        }
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
        totalSpawnedBlocks++;
        updateGravityProgress();
        if (currentMode != GameMode.ITEM) {
            activeItemBlock = null;
            nextBlockIsItem = false;
            return;
        }
        if (nextBlockIsItem) {
            ItemBehavior behavior = rollBehavior();
            String behaviorId = behavior.id();
            
            // Weight나 Bomb 아이템인 경우 블록 형태 강제
            if ("weight".equals(behaviorId) && block.getKind() != BlockKind.W) {
                block.setShape(BlockShape.of(BlockKind.W));
            } else if ("bomb".equals(behaviorId) && block.getKind() != BlockKind.B) {
                block.setShape(BlockShape.of(BlockKind.B));
            }
            
            List<ItemBehavior> behaviors = new ArrayList<>();
            behaviors.add(behavior);
            
            // Weight나 Bomb가 아닌 경우 랜덤 아이템 칸 선택
            int itemCellX = -1;
            int itemCellY = -1;
            if (!"weight".equals(behaviorId) && !"bomb".equals(behaviorId)) {
                BlockShape shape = block.getShape();
                List<int[]> filledCells = new ArrayList<>();
                for (int y = 0; y < shape.height(); y++) {
                    for (int x = 0; x < shape.width(); x++) {
                        if (shape.filled(x, y)) {
                            filledCells.add(new int[]{x, y});
                        }
                    }
                }
                if (!filledCells.isEmpty()) {
                    int[] selected = filledCells.get(itemRandom.nextInt(filledCells.size()));
                    itemCellX = selected[0];
                    itemCellY = selected[1];
                }
            }
            
            ItemBlockModel itemBlock = new ItemBlockModel(block, behaviors, itemCellX, itemCellY);
            activeItemBlock = itemBlock;
            itemManager.add(itemBlock);
            itemBlock.onSpawn(itemContext);
            nextBlockIsItem = false;
        } else {
            activeItemBlock = null;
        }

        if (secondaryListener != null) {
            secondaryListener.onBlockSpawned(block);
        }
    }

    @Override
    public void onBlockLocked(Block block) {
        cacheLastLockedPiece(block);
        if (currentMode == GameMode.ITEM && activeItemBlock != null && activeItemBlock.getDelegate() == block) {
            itemManager.onLock(itemContext, activeItemBlock);
            activeItemBlock = null;
        }

        // 대전/아이템 모드에서 블록 고정 시 공격 대기열 적용
        if (currentMode == GameMode.STANDARD || currentMode == GameMode.ITEM /* 또는 대전 모드 플래그 */) {
            commitPendingGarbageLines();
        }

        // 다음 스폰 전에 대기 중인 공격(incoming)을 보드에 주입한다.
        notifyBeforeNextSpawnHooks();
      }

    @Override
    public void onBlockRotated(Block block, int times) {
        if (currentMode != GameMode.ITEM) {
            return;
        }
        if (activeItemBlock != null && activeItemBlock.getDelegate() == block) {
            for (int i = 0; i < times; i++) {
                activeItemBlock.updateItemCellAfterRotation();
            }
        }
    }

    @Override
    public void onLinesCleared(int clearedLines) {
        if (clearedLines <= 0) {
            return;
        }
        totalClearedLines += clearedLines;
        notifyMultiplayerLineClear();
        updateGravityProgress();
        gameplayEngine.pauseForLineClear(LINE_CLEAR_HIGHLIGHT_DELAY_MS);
        if (currentMode != GameMode.ITEM) {
            return;
        }
        if (itemSpawnIntervalLines > 0 && totalClearedLines % itemSpawnIntervalLines == 0) {
            nextBlockIsItem = true;
        }
        itemManager.onLineClear(itemContext, null);

        if (secondaryListener != null) {
            secondaryListener.onLinesCleared(clearedLines);
        }
    }

    @Override
    public void onTick(long tick) {
        currentTick = tick;
        if (currentMode == GameMode.ITEM) {
            itemManager.tick(itemContext, tick);
            refreshBuffs(tick);
        }
        checkInactivityPenalty();
    }

    @Override
    public void onGameOver() {
        if (currentState != GameState.GAME_OVER) {
            changeState(GameState.GAME_OVER);
            showGameOverScreen();
        }
    }

    private Supplier<ItemBehavior> resolveBehaviorOverride(String behaviorId) {
        String key = behaviorId.toLowerCase(Locale.ROOT);
        return switch (key) {
            case "line_clear", "lineclear", "line-clear" -> LineClearBehavior::new;
            case "double_score", "doublescore", "double-score", "double" -> () -> new DoubleScoreBehavior(600, 2.0);
            case "time_slow", "timeslow", "slow" -> TimeSlowBehavior::new;
            case "bomb" -> () -> new BombBehavior();
            case "weight", "weight_drop", "weight-drop" -> WeightBehavior::new;
            default -> throw new IllegalArgumentException("Unknown item behavior: " + behaviorId);
        };
    }

    private ItemBehavior rollBehavior() {
        if (behaviorOverride != null) {
            return behaviorOverride.get();
        }
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
        if (slowBuffExpiresAtMs > 0 && System.currentTimeMillis() >= slowBuffExpiresAtMs) {
            slowBuffExpiresAtMs = 0;
            slowLevelOffset = 0;
            applyGravityLevel();
        }
    }

    private void checkInactivityPenalty() {
        if (currentState != GameState.PLAYING) {
            return; // 일시정지/메뉴 등에서는 패널티를 계산하지 않는다.
        }
        long elapsed = System.currentTimeMillis() - lastInputMillis;
        if (inactivityPenaltyStage < 1 && elapsed >= INACTIVITY_STAGE1_MS) {
            applyInactivityPenaltyStage(1);
        }
        if (inactivityPenaltyStage < 2 && elapsed >= INACTIVITY_STAGE2_MS) {
            applyInactivityPenaltyStage(2);
        }
    }

    private void updateGravityProgress() {
        int levelFromBlocks = totalSpawnedBlocks / BLOCKS_PER_SPEED_STEP;
        int levelFromLines = totalClearedLines / LINES_PER_SPEED_STEP;
        int desiredLevel = Math.min(MAX_SPEED_LEVEL, Math.max(levelFromBlocks, levelFromLines));
        if (desiredLevel > currentGravityLevel) {
            int bonusLevels = desiredLevel - currentGravityLevel;
            currentGravityLevel = desiredLevel;
            if (gameplayEngine != null) {
                applyGravityLevel();
            }
            awardSpeedBonus(bonusLevels);
            uiBridge.refreshBoard();
        }
    }
    
    private void awardSpeedBonus(int bonusLevels) {
        if (bonusLevels <= 0) {
            return;
        }
        int previousLevel = currentGravityLevel - bonusLevels;
        long bonus = 0;
        for (int level = previousLevel + 1; level <= currentGravityLevel; level++) {
            bonus += level * 1000L;
        }
        Score current = scoreRepository.load();
        int bonusInt = (int) Math.min(Integer.MAX_VALUE, bonus);
        Score updated = current.withAdditionalPoints(bonusInt);
        scoreRepository.save(updated);
        System.out.printf("스피드 증가 : lv.%d -> lv.%d, 보너스 점수 : %d%n",
                previousLevel, currentGravityLevel, bonusInt);
    }

    private void applyInactivityPenaltyStage(int stage) {
        inactivityPenaltyStage = stage;
        scoreEngine.applyPenalty(INACTIVITY_PENALTY_POINTS);
        System.out.printf("무입력 패널티(Stage %d) 적용: -%d점%n",
                stage, INACTIVITY_PENALTY_POINTS);
    }

    private void resetInputAxes() {
        inputState.setLeft(false);
        inputState.setRight(false);
        inputState.setSoftDrop(false);
        inputState.clearOneShotInputs();
    }

    private void recordPlayerInput() {
        inactivityPenaltyStage = 0;
        lastInputMillis = System.currentTimeMillis();
    }

    /**
     * 메뉴로 돌아갈 때 남아있는 타이머/패널티/속도 상태를 정리한다.
     */
    private void resetRuntimeForMenu() {
        resetInputAxes();
        inactivityPenaltyStage = 0;
        lastInputMillis = System.currentTimeMillis();
        pauseStartedAt = -1;
        currentTick = 0;
        scoreMultiplier = 1.0;
        slowLevelOffset = 0;
        slowBuffExpiresAtMs = 0;
        doubleScoreUntilTick = 0;
        gameplayEngine.setSpeedModifier(1.0);
        applyGravityLevel();
        gameplayEngine.setActiveBlock(null);
        stopClockCompletely();
        gameplayStartedAtMillis = -1;
        accumulatedPauseMillis = 0;
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
        totalSpawnedBlocks = 0;
        currentGravityLevel = 0;
        inactivityPenaltyStage = 0;
        lastInputMillis = System.currentTimeMillis();
        gameplayStartedAtMillis = lastInputMillis;
        accumulatedPauseMillis = 0;
        currentTick = 0;
        scoreMultiplier = 1.0;
        doubleScoreUntilTick = 0;
        slowLevelOffset = 0;
        slowBuffExpiresAtMs = 0;
        gameplayEngine.setActiveBlock(null);
        applyGravityLevel();
        gameplayEngine.stopClockCompletely();
        pauseStartedAt = -1;
    }

    private void applyGravityLevel() {
        if (gameplayEngine == null) {
            return;
        }
        int effectiveLevel = effectiveGravityLevel();
        gameplayEngine.setSpeedModifier(1.0); // neutralize any lingering modifiers
        gameplayEngine.setGravityLevel(effectiveLevel);
    }

    private int effectiveGravityLevel() {
        int level = currentGravityLevel + slowLevelOffset;
        return Math.max(0, level);
    }

    private void stopClockCompletely() {
        gameplayEngine.stopClockCompletely();
    }

    /**
     * 로컬 멀티 세션을 상태 머신에 연결한다. (재호출 시 마지막 세션을 덮어쓴다)
     */
    public void enableLocalMultiplayer(LocalMultiplayerSession session) {
        if (session == null) {
            return;
        }
        this.activeLocalSession = session;
        handlers.put(GameState.PLAYING, session.handler());
    }

    /**
     * 메뉴 복귀 / 싱글 모드 전환 등으로 더 이상 세션이 필요 없을 때 정리한다.
     */
    public void clearLocalMultiplayerSession() {
        if (activeLocalSession == null) {
            return;
        }
        activeLocalSession.shutdown();
        if (defaultPlayHandler != null) {
            handlers.put(GameState.PLAYING, defaultPlayHandler);
        }
        activeLocalSession = null;
    }

    public boolean isLocalMultiplayerActive() {
        return activeLocalSession != null;
    }

    public Optional<LocalMultiplayerSession> getActiveLocalMultiplayerSession() {
        return Optional.ofNullable(activeLocalSession);
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
        // 이름 입력은 항상 허용해, 순위 밖이어도 기록을 남길 수 있게 한다.
        uiBridge.showGameOverOverlay(finalScore, true);
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
        // Placeholder for name entry preparation; handled by NameInputHandler in UI
        // flow
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

    /**
     * 로컬 멀티 전용 승리 결과 오버레이를 UI에 요청한다.
     */
    public void showMultiplayerResult(int winnerId) {
        uiBridge.showMultiplayerResult(winnerId);
    }

    // === 외부 제어 진입점 ===

    public void pauseGame() {
        if (currentState == GameState.PLAYING) {
            pauseStartedAt = System.currentTimeMillis();
            changeState(GameState.PAUSED);
        }
    }

    public void resumeGame() {
        if (currentState == GameState.PAUSED) {
            if (pauseStartedAt > 0) {
                long pausedDuration = System.currentTimeMillis() - pauseStartedAt;
                lastInputMillis += pausedDuration;
                accumulatedPauseMillis += Math.max(0L, pausedDuration);
                pauseStartedAt = -1;
            }
            changeState(GameState.PLAYING);
        }
    }

    public void quitToMenu() {
        if (currentState != GameState.MENU) {
            resetRuntimeForMenu();
            changeState(GameState.MENU);
            clearLocalMultiplayerSession();
        }
    }

    public void restartGame() {
        startGame(lastMode);
    }

    public void moveBlockLeft() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        recordPlayerInput();
        gameplayEngine.moveBlockLeft();
    }

    public void moveBlockRight() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        recordPlayerInput();
        gameplayEngine.moveBlockRight();
    }

    public void moveBlockDown() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        recordPlayerInput();
        gameplayEngine.moveBlockDown();
    }

    public void rotateBlockClockwise() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        // weight 아이템일 때 회전 무시
        if (isItemMode() && activeItemBlock != null && !activeItemBlock.getBehaviors().isEmpty()) {
            ItemBehavior behavior = activeItemBlock.getBehaviors().get(0);
            if ("weight".equals(behavior.id())) {
                return;
            }
        }
        recordPlayerInput();
        gameplayEngine.rotateBlockClockwise();
    }

    public void rotateBlockCounterClockwise() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        // weight 아이템일 때 회전 무시
        if (isItemMode() && activeItemBlock != null && !activeItemBlock.getBehaviors().isEmpty()) {
            ItemBehavior behavior = activeItemBlock.getBehaviors().get(0);
            if ("weight".equals(behavior.id())) {
                return;
            }
        }
        recordPlayerInput();
        gameplayEngine.rotateBlockCounterClockwise();
    }

    public void hardDropBlock() {
        if (!isPlayingState() || !ensureActiveBlockPresent()) {
            return;
        }
        recordPlayerInput();
        gameplayEngine.hardDropBlock();
    }

    public void holdCurrentBlock() {
        if (!isPlayingState()) {
            return;
        }
        recordPlayerInput();
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
    // - 마지막으로 잠긴 블록의 모양/위치를 스냅샷(LockedPieceSnapshot)으로 저장해 둔다.
    // - GameplayEngine이 줄을 지웠을 때, 어떤 블록이 몇 줄을 지웠는지 멀티플레이어 훅에 알려준다.
    // - 다음 블록이 스폰되기 직전에 멀티플레이어 훅을 호출하여, 대기 중인 공격 라인 등을
    //   실제 보드에 적용할 수 있게 한다.
    // => 싱글 플레이 핵심 로직은 그대로 두고, 멀티 전용 규칙(공격/쓰레기 줄 처리)은
    //    MultiplayerHook 구현체에서 처리하도록 하기 위한 연결 지점

    private void notifyBeforeNextSpawnHooks() {
        if (multiplayerHooks.isEmpty()) {
            return;
        }
        for (MultiplayerHook hook : multiplayerHooks) {
            hook.beforeNextSpawn();
        }
    }

    private void cacheLastLockedPiece(Block block) {
        if (block == null) {
            lastLockedPieceSnapshot = null;
            return;
        }
        BlockShape shape = block.getShape();
        List<Cell> cells = new ArrayList<>();
        for (int y = 0; y < shape.height(); y++) {
            for (int x = 0; x < shape.width(); x++) {
                if (!shape.filled(x, y)) {
                    continue;
                }
                cells.add(new Cell(block.getX() + x, block.getY() + y));
            }
        }
        lastLockedPieceSnapshot = LockedPieceSnapshot.of(cells);
    }

    private void notifyMultiplayerLineClear() {
        if (multiplayerHooks.isEmpty() || lastLockedPieceSnapshot == null) {
            return;
        }
        List<Integer> rows = gameplayEngine == null ? Collections.emptyList() : gameplayEngine.getLastClearedRows();
        if (rows == null || rows.isEmpty()) {
            return;
        }
        int[] cleared = new int[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            cleared[i] = rows.get(i);
        }
        LockedPieceSnapshot snapshot = lastLockedPieceSnapshot;
        for (MultiplayerHook hook : multiplayerHooks) {
            hook.onPieceLocked(snapshot, cleared, Board.W);
        }
        lastLockedPieceSnapshot = null;

    }

    public void commitPendingGarbageLines() {
        if (this.pendingGarbageLines > 0) {
            // 1. 보드에 쓰레기 줄 추가
            // board.addGarbageLines() 메서드가 Board 클래스에 구현되어야 함
            //board.addGarbageLines(this.pendingGarbageLines); 

            System.out.printf("[LOG] GameModel: 대기열 %d줄 보드에 적용됨.%n", this.pendingGarbageLines);
            
            // 2. 대기열 비우기
            this.pendingGarbageLines = 0; 
            
            // 3. UI 갱신 및 게임 오버 체크
            uiBridge.refreshBoard();
            
            // TODO: 공격 적용 후 블록이 겹쳐서 게임 오버 조건이 충족되는지 확인하는 로직 추가
        }
    }

    private tetris.domain.engine.GameplayEngine.GameplayEvents secondaryListener;

    public void setSecondaryListener(tetris.domain.engine.GameplayEngine.GameplayEvents listener) {
        this.secondaryListener = listener;
    }

     // 공격 대기열 필드 추가 (최대 10줄)
    private int pendingGarbageLines = 0; 
    /**
     * 네트워크를 통해 수신된 공격 라인을 처리합니다.
     * @param attackLines 수신된 공격 라인 배열
     */
    public void applyAttackLines(tetris.network.protocol.AttackLine[] attackLines) {
        if (attackLines == null || attackLines.length == 0) {
            return;
        }

        int incomingStrength = 0;
        for (tetris.network.protocol.AttackLine line : attackLines) {
            // AttackLine이 getStrength()를 구현했다고 가정
            incomingStrength += line.getStrength(); 
        }

        if (incomingStrength > 0) {
            // 1. 현재 대기열이 10줄이 이미 차 있다면, 새로운 공격은 무시 (요구사항 5-2)
            if (this.pendingGarbageLines >= 10) {
                System.out.println("[LOG] GameModel: 대기열이 가득 차 새로운 공격 (" + incomingStrength + "줄) 무시됨.");
                return;
            }

            // 2. 새로운 공격을 대기열에 누적
            this.pendingGarbageLines += incomingStrength;

            // 3. 10줄 초과 시, 10줄로 제한 (제일 아래쪽 부분을 잘라냄) (요구사항 5-3)
            if (this.pendingGarbageLines > 10) {
                this.pendingGarbageLines = 10;
                System.out.println("[LOG] GameModel: 공격 대기열이 10줄로 제한됨.");
            }
            
            System.out.printf("[LOG] GameModel: 공격 대기열에 %d줄 추가됨. 현재 대기열: %d줄%n", 
                incomingStrength, this.pendingGarbageLines);
            
            // UI 갱신 (대기열 표시 영역 업데이트)
            uiBridge.refreshBoard(); 
        }
    }
}
