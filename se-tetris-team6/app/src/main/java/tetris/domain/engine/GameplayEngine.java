package tetris.domain.engine;

import tetris.domain.Board;
import tetris.domain.BlockGenerator;
import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.model.Block;
import tetris.domain.model.GameClock;
import tetris.domain.model.InputState;
import tetris.domain.score.ScoreRuleEngine;
import tetris.domain.GameModel.UiBridge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.Timer;

/**
 * Encapsulates gameplay responsibilities previously inside GameModel:
 * - active block lifecycle (spawn/lock)
 * - gravity ticks and game clock
 * - movement/rotation/hard drop/hold
 */
public class GameplayEngine implements GameClock.Listener {

    public interface GameplayEvents {
        void onBlockSpawned(Block block);
        void onBlockLocked(Block block);
        void onLinesCleared(int clearedLines);
        void onTick(long tick);
        void onBlockRotated(Block block, int times); // times: 시계방향 회전 횟수
        default void onGameOver() {} // 스폰 불가 등 즉시 게임오버 알림
    }

    private final Board board;
    private final InputState inputState;
    private final ScoreRuleEngine scoreEngine;
    private UiBridge uiBridge;
    private GameClock clock;
    private BlockGenerator blockGenerator;
    private Block activeBlock;
    private boolean clockStarted;
    private GameplayEvents events;
    private long tickCounter;
    private List<Integer> lastClearedRows = Collections.emptyList();
    private Timer lineClearPauseTimer;
    private List<Integer> pendingClearRows = Collections.emptyList();
    private boolean awaitingLineClearCommit;

    public GameplayEngine(Board board, InputState inputState, BlockGenerator generator, ScoreRuleEngine scoreEngine, UiBridge uiBridge) {
        this.board = Objects.requireNonNull(board);
        this.inputState = Objects.requireNonNull(inputState);
        this.blockGenerator = Objects.requireNonNull(generator);
        this.scoreEngine = Objects.requireNonNull(scoreEngine);
        this.uiBridge = uiBridge == null ? new UiBridge() {
            @Override public void showPauseOverlay() {}
            @Override public void hidePauseOverlay() {}
            @Override public void refreshBoard() {}
            @Override public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) {}
            @Override public void showNameEntryOverlay(tetris.domain.score.Score score) {}
        } : uiBridge;
        this.clock = new GameClock(this);
    }

    public void setEvents(GameplayEvents events) {
        this.events = events;
    }

    public void setUiBridge(UiBridge bridge) {
        this.uiBridge = Objects.requireNonNull(bridge, "bridge");
    }

    public void setBlockGenerator(BlockGenerator generator) {
        this.blockGenerator = Objects.requireNonNull(generator);
    }

    public void setSpeedModifier(double modifier) {
        clock.setSpeedModifier(modifier);
    }

    public void setGravityLevel(int level) {
        clock.setLevel(level);
    }

    public int getGravityLevel() {
        return clock.getLevel();
    }

    public List<Integer> getLastClearedRows() {
        return lastClearedRows;
    }

    public void clearLastClearedRows() {
        lastClearedRows = Collections.emptyList();
    }

    public void pauseForLineClear(int durationMs) {
        if (!clockStarted) {
            return;
        }
        clock.pause();
        if (lineClearPauseTimer != null && lineClearPauseTimer.isRunning()) {
            lineClearPauseTimer.stop();
        }
        int delay = Math.max(50, durationMs);
        lineClearPauseTimer = new Timer(delay, e -> {
            ((Timer) e.getSource()).stop();
            commitPendingLineClear();
        });
        lineClearPauseTimer.setRepeats(false);
        lineClearPauseTimer.start();
    }

    private void commitPendingLineClear() {
        if (awaitingLineClearCommit) {
            board.clearRows(pendingClearRows);
            pendingClearRows = Collections.emptyList();
            awaitingLineClearCommit = false;
            boolean spawned = spawnNewBlock();
            if (!spawned && events != null) {
                System.out.println("[LOG][Game] commitPendingLineClear(): spawn failed after clear → game over");
                events.onGameOver();
            }
            uiBridge.refreshBoard();
        }
        clock.resume();
    }

    public Block getActiveBlock() { return activeBlock; }
    public void setActiveBlock(Block b) { this.activeBlock = b; }

    public void spawnIfNeeded() {
        if (activeBlock == null && !spawnNewBlock()) {
            System.out.println("[LOG][Game] spawnIfNeeded(): spawn failed → game over");
            if (events != null) {
                events.onGameOver();
            }
        }
        uiBridge.refreshBoard();
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

    public void stopClockCompletely() {
        if (clockStarted) {
            clock.stop();
            clockStarted = false;
        }
        if (lineClearPauseTimer != null && lineClearPauseTimer.isRunning()) {
            lineClearPauseTimer.stop();
        }
    }

    private boolean spawnNewBlock() {
        BlockGenerator generator = Objects.requireNonNull(blockGenerator, "blockGenerator");
        BlockKind nextKind = Objects.requireNonNull(generator.nextBlock(), "nextBlock");
        Block next = Block.spawn(nextKind, Board.W / 2 - 1, 0);
        if (!board.canSpawn(next.getShape(), next.getX(), next.getY())) {
            System.out.printf("[LOG][Game] Spawn failed for %s at (%d,%d) — cannot place at spawn%n",
                    nextKind, next.getX(), next.getY());
            if (events != null) {
                events.onGameOver();
            }
            return false;
        }
        activeBlock = next;
        if (events != null) {
            events.onBlockSpawned(activeBlock);
        }
        return true;
    }

    private void lockActiveBlock() {
        if (activeBlock == null) return;
        Block current = activeBlock;
        BlockShape shape = current.getShape();
        int blockId = shape.kind().ordinal() + 1;
        board.place(shape, current.getX(), current.getY(), blockId);
        List<Integer> rowsToClear = board.fullRowsSnapshot();
        activeBlock = null;
        if (rowsToClear.isEmpty()) {
            lastClearedRows = Collections.emptyList();
            if (events != null) {
                events.onBlockLocked(current);
            }
            boolean spawned = spawnNewBlock();
            if (!spawned && events != null) {
                events.onGameOver();
            }
            return;
        }
        lastClearedRows = rowsToClear;
        pendingClearRows = new ArrayList<>(rowsToClear);
        awaitingLineClearCommit = true;
        scoreEngine.onLinesCleared(rowsToClear.size());
        if (events != null) {
            events.onBlockLocked(current);
            events.onLinesCleared(rowsToClear.size());
        }
    }

    public void stepGameplay() {
        if (activeBlock == null) return;
        if (!board.canPlace(activeBlock.getShape(), activeBlock.getX(), activeBlock.getY())) {
            System.out.printf("[LOG][Game] Active block overlap detected at (%d,%d), triggering game over%n",
                    activeBlock.getX(), activeBlock.getY());
            if (events != null) {
                events.onGameOver(); // 스폰 영역 침범 등 배치 불가 상태 → 즉시 게임오버
            }
            return;
        }

        clock.setSoftDrop(inputState.isSoftDrop());

        int dx = 0;
        if (inputState.isLeft() && !inputState.isRight()) dx = -1;
        else if (inputState.isRight() && !inputState.isLeft()) dx = 1;

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
                if (events != null) { // 이벤트 호출
                    events.onBlockRotated(activeBlock, 1);
                }
            }
        }

        if (rotateCCW) {
            BlockShape rotated = activeBlock.getShape().rotatedCW().rotatedCW().rotatedCW();
            if (board.canPlace(rotated, activeBlock.getX(), activeBlock.getY())) {
                activeBlock.setShape(rotated);
                if (events != null) { //이벤트 호출
                    events.onBlockRotated(activeBlock, 3);
                }
            }
        }

        inputState.popHardDrop();
        inputState.popHold();
    }

    @Override
    public void onGravityTick() {
        tickCounter++;
        if (events != null) {
            events.onTick(tickCounter);
        }
        if (activeBlock == null) return;
        uiBridge.refreshBoard();

        if (board.canPlace(activeBlock.getShape(), activeBlock.getX(), activeBlock.getY() + 1)) {
            activeBlock.moveBy(0, 1);
            scoreEngine.onBlockDescend();
        } else {
            lockActiveBlock();
        }
        uiBridge.refreshBoard();
    }

    @Override
    public void onLockDelayTimeout() {
        // can be implemented later if lock-delay feature is added
    }

    // Movement helpers exposed for controller/delegation
    public void moveBlockLeft() {
        if (activeBlock == null) return;
        if (board.canPlace(activeBlock.getShape(), activeBlock.getX() - 1, activeBlock.getY())) {
            activeBlock.moveBy(-1, 0);
            uiBridge.refreshBoard();
        }
    }

    public void moveBlockRight() {
        if (activeBlock == null) return;
        if (board.canPlace(activeBlock.getShape(), activeBlock.getX() + 1, activeBlock.getY())) {
            activeBlock.moveBy(1, 0);
            uiBridge.refreshBoard();
        }
    }

    public void moveBlockDown() {
        if (activeBlock == null) return;
        if (board.canPlace(activeBlock.getShape(), activeBlock.getX(), activeBlock.getY() + 1)) {
            activeBlock.moveBy(0, 1);
            scoreEngine.onBlockDescend();
            uiBridge.refreshBoard();
        }
    }

    public void rotateBlockClockwise() {
        if (activeBlock == null) return;
        try {
            BlockShape beforeShape = activeBlock.getShape();
            int beforeRot = activeBlock.getRotation();
            BlockShape rotated = beforeShape.rotatedCW();
            boolean can = board.canPlace(rotated, activeBlock.getX(), activeBlock.getY());
            System.out.printf("[LOG][Engine] rotateBlockClockwise() attempt: kind=%s pos=(%d,%d) rotBefore=%d canPlace=%b%n",
                    beforeShape.kind(), activeBlock.getX(), activeBlock.getY(), beforeRot, can);
            if (can) {
                activeBlock.setShape(rotated);
                if (events != null) { // 이벤트 호출
                    events.onBlockRotated(activeBlock, 1);
                }
                System.out.printf("[LOG][Engine] rotateBlockClockwise() success: newRot=%d%n", activeBlock.getRotation());
            } else {
                System.out.println("[LOG][Engine] rotateBlockClockwise() blocked: rotation not applied due to collision or out-of-bounds");
            }
        } catch (Exception ex) {
            System.out.println("[LOG][Engine] rotateBlockClockwise() exception: " + ex);
        } finally {
            uiBridge.refreshBoard();
        }
    }

    public void rotateBlockCounterClockwise() {
        if (activeBlock == null) return;
        BlockShape rotated = activeBlock.getShape().rotatedCW().rotatedCW().rotatedCW();
        if (board.canPlace(rotated, activeBlock.getX(), activeBlock.getY())) {
            activeBlock.setShape(rotated);
            events.onBlockRotated(activeBlock, 3); // 반시계방향 = 시계방향 3회
        }
        uiBridge.refreshBoard();
    }

    public void hardDropBlock() {
        if (activeBlock == null) return;
        while (board.canPlace(activeBlock.getShape(), activeBlock.getX(), activeBlock.getY() + 1)) {
            activeBlock.moveBy(0, 1);
            scoreEngine.onBlockDescend();
        }
        lockActiveBlock();
        uiBridge.refreshBoard();
    }

    public void holdCurrentBlock() {
        inputState.pressHold();
        uiBridge.refreshBoard();
    }
}
