package tetris.concurrent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import tetris.domain.model.GameState;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;

/**
 * 게임 로직을 담당하는 전용 스레드. 아직 멀티플레이어 전체 로직이 완성되지
 * 않았기 때문에, 아래 구현은 "안전하게 컴파일되는 최소 기능"을 제공한다.
 * 추후 실제 규칙을 채워 넣을 수 있도록 hook 방식으로 작성했다.
 */
public class GameThread implements Runnable {
    private static final long DEFAULT_TICK_MS = 16L;

    // === 게임 상태 관리 ===
    private final ThreadSafeGameModel gameModel;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    // === 입력 / 이벤트 처리 ===
    private final BlockingQueue<PlayerInput> inputQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<GameEvent> gameEventQueue = new LinkedBlockingQueue<>();

    // === 타이밍 관리 ===
    private volatile long gameTickInterval = 500L;
    private long lastBlockFallTime;

    // === 플레이어 정보 ===
    private final String playerId;
    private final boolean isLocalPlayer;

    // === 네트워크 통신 ===
    private volatile GameEventListener networkListener;

    public GameThread(ThreadSafeGameModel gameModel, String playerId, boolean isLocal) {
        this.gameModel = Objects.requireNonNull(gameModel, "gameModel");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.isLocalPlayer = isLocal;
    }

    @Override
    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            return; // 이미 실행 중
        }

        lastBlockFallTime = System.currentTimeMillis();
        try {
            while (isRunning.get()) {
                if (isPaused.get()) {
                    sleep(Duration.ofMillis(10));
                    continue;
                }

                updateGame();
                sleep(Duration.ofMillis(DEFAULT_TICK_MS));
            }
        } finally {
            isRunning.set(false);
        }
    }

    private void sleep(Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateGame() {
        processPlayerInput();
        processGameEvents();
        handleBlockFall();

        if (checkGameOver()) {
            stopGame();
            GameEventListener listener = networkListener;
            if (listener != null) {
                listener.onGameEvent(new GameEvent(GameEvent.Type.GAME_OVER, playerId));
            }
        }
    }

    private void handleBlockFall() {
        long now = System.currentTimeMillis();
        if (now - lastBlockFallTime < gameTickInterval) {
            return;
        }
        lastBlockFallTime = now;

        // ThreadSafeGameModel은 아직 짜여 있지 않으므로 호출만 해둔다.
        gameModel.placeCurrentBlock();
    }

    private void processPlayerInput() {
        PlayerInput input;
        while ((input = inputQueue.poll()) != null) {
            // 아직 입력 처리 규칙이 없으므로, 이벤트만 만들어 네트워크 쪽으로 전달한다.
            GameEventListener listener = networkListener;
            if (listener != null) {
                listener.onGameEvent(new GameEvent(GameEvent.Type.GENERIC, input));
            }
        }
    }

    private void processGameEvents() {
        GameEvent event;
        while ((event = gameEventQueue.poll()) != null) {
            GameEventListener listener = networkListener;
            if (listener != null) {
                listener.onGameEvent(event);
            }
        }
    }

    private void handleLineClear() {
        gameEventQueue.offer(new GameEvent(GameEvent.Type.LINE_CLEAR, playerId));
    }

    public void receiveAttack(AttackLine[] attackLines) {
        gameModel.receiveAttack(attackLines);
        gameEventQueue.offer(new GameEvent(GameEvent.Type.ATTACK_RECEIVED, attackLines));
    }

    private boolean checkGameOver() {
        return gameModel.getGameState() == GameState.GAME_OVER;
    }

    public void addPlayerInput(PlayerInput input) {
        if (input != null) {
            inputQueue.offer(input);
        }
    }

    public void pauseGame() {
        isPaused.set(true);
    }

    public void resumeGame() {
        isPaused.set(false);
    }

    public void stopGame() {
        isRunning.set(false);
    }

    public GameState getCurrentGameState() {
        return gameModel.getGameState();
    }

    public void setNetworkListener(GameEventListener listener) {
        this.networkListener = listener;
    }

    public void setGameSpeed(int level) {
        // level이 높아질수록 간격을 더 짧게 줄인다 (최소 50ms 보장)
        this.gameTickInterval = Math.max(50L, 500L - (long) level * 25L);
    }
}
