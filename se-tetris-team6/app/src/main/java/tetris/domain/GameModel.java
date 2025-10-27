package tetris.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import tetris.domain.BlockKind;
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
import tetris.domain.model.ScoreData;
import tetris.domain.model.GameClock;

/**
 * 게임 핵심 도메인 모델.
 * - 현재 보드/블록/스코어 등 상태를 추적합니다.
 * - {@link GameHandler} 구현들을 상태 머신으로 등록해 UI 흐름을 제어합니다.
 * - {@link GameClock} 이벤트를 받아 중력 및 잠금 지연을 처리합니다.
 */
public final class GameModel implements GameClock.Listener {

    /**
     * UI 계층과의 최소 연결 지점.
     * 뷰에서 필요한 오버레이 제어만 위임받습니다.
     */
    public interface UiBridge {
        void showPauseOverlay();
        void hidePauseOverlay();
    }

    private static final UiBridge NO_OP_UI_BRIDGE = new UiBridge() {
        @Override public void showPauseOverlay() { /* no-op */ }
        @Override public void hidePauseOverlay() { /* no-op */ }
    };

    private final Board board = new Board();
    private final ScoreData scoreData = new ScoreData();
    private final InputState inputState = new InputState();
    private final GameClock clock = new GameClock(this);
    private final Map<GameState, GameHandler> handlers = new EnumMap<>(GameState.class);

    private UiBridge uiBridge = NO_OP_UI_BRIDGE;
    private GameState currentState;
    private GameHandler currentHandler;
    private Block activeBlock;
    private boolean clockStarted;

    public GameModel() {
        registerHandlers();
        changeState(GameState.MENU);
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

    public ScoreData getScoreData() {
        return scoreData;
    }

    public InputState getInputState() {
        return inputState;
    }

    public Block getActiveBlock() {
        return activeBlock;
    }

    public void setActiveBlock(Block block) {
        this.activeBlock = block;
    }

    public void spawnIfNeeded() {
        if (activeBlock == null) {
            activeBlock = Block.spawn(BlockKind.T, Board.W / 2 - 1, 0);
        }
    }

    public void resumeClock() {
        if (!clockStarted) {
            clock.start();
            clockStarted = true;
        } else {
            clock.resume();
        }
    }

    public void pauseClock() {
        if (clockStarted) {
            clock.pause();
        }
    }

    public void stepGameplay() {
        if (activeBlock == null) {
            return;
        }

        // 현재 위치가 더 이상 유효하지 않다면 즉시 충돌로 간주하고 처리 중단
        if (!board.canPlace(activeBlock.getShape(), activeBlock.getX(), activeBlock.getY())) {
            changeState(GameState.GAME_OVER);
            return;
        }

        clock.setSoftDrop(inputState.isSoftDrop());

        int dx = 0;
        if (inputState.isLeft() && !inputState.isRight()) {
            dx = -1;
        } else if (inputState.isRight() && !inputState.isLeft()) {
            dx = 1;
        }

        if (dx != 0) {
            int targetX = activeBlock.getX() + dx;
            if (board.canPlace(activeBlock.getShape(), targetX, activeBlock.getY())) {
                activeBlock.moveBy(dx, 0);
            }
        }

        boolean rotateCW = inputState.popRotateCW();
        boolean rotateCCW = inputState.popRotateCCW();

        if (rotateCW) {
            BlockShape rotated = activeBlock.getShape().rotatedCW();
            if (board.canPlace(rotated, activeBlock.getX(), activeBlock.getY())) {
                activeBlock.setShape(rotated);
            }
        }

        if (rotateCCW) {
            BlockShape rotated = activeBlock.getShape()
                .rotatedCW()
                .rotatedCW()
                .rotatedCW();
            if (board.canPlace(rotated, activeBlock.getX(), activeBlock.getY())) {
                activeBlock.setShape(rotated);
            }
        }

        // 아직 구현되지 않은 입력은 소비만 하여 중복 처리를 막는다.
        inputState.popHardDrop();
        inputState.popHold();
    }

    public void bindUiBridge(UiBridge bridge) {
        this.uiBridge = Objects.requireNonNull(bridge, "bridge");
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
        // TODO: 설정 데이터 로드
    }

    public void saveSettings() {
        // TODO: 설정 데이터 저장
    }

    public void loadScoreboard() {
        // TODO: 점수판 데이터 로드
    }

    public void prepareNameEntry() {
        // TODO: 이름 입력 준비
    }

    public void processNameEntry() {
        // TODO: 이름 입력 처리
    }

    public void commitLineClear(int linesCleared, int combo) {
        // TODO: 점수 계산 규칙 반영
    }

    @Override
    public void onGravityTick() {
        // TODO: 중력 이동/충돌 처리
    }

    @Override
    public void onLockDelayTimeout() {
        // TODO: 블록 잠금 및 새 블록 스폰
    }
}
