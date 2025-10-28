package tetris.domain.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.Timer;

/**
 * Swing {@link Timer} 기반으로 동작하는 게임 루프 시계.
 * <p>
 * - 중력 틱을 발생시켜 {@link Listener#onGravityTick()} 으로 미노 낙하를 통보합니다.<br>
 * - 잠금 지연 타이머를 제공해 {@link #armLockDelay(int)} / {@link #cancelLockDelay()} 로 고정 유예를 제어합니다.<br>
 * - 레벨과 소프트 드롭 여부에 따라 중력 속도를 재계산합니다.
 * </p>
 */
public final class GameClock {

    /**
     * GameClock 이벤트 콜백.
     */
    public interface Listener {
        void onGravityTick();
        void onLockDelayTimeout();
    }

    private static final int MIN_GRAVITY_DELAY = 16; // 60fps 상한
    private static final int DEFAULT_LOCK_DELAY_MS = 500;
    private static final int SOFT_DROP_FACTOR = 6;

    /** NES/Guideline 기반 레벨별 기본 중력(ms) */
    private static final int[] LEVEL_GRAVITY_TABLE = { // Level 0~20+ gravity delay (ms)
        1000, // Level 0 (start)
        793,  // Level 1
        618,  // Level 2
        473,  // Level 3
        355,  // Level 4
        262,  // Level 5
        190,  // Level 6
        135,  // Level 7
        94,   // Level 8
        66,   // Level 9
        45,   // Level 10
        35,   // Level 11
        28,   // Level 12
        22,   // Level 13
        17,   // Level 14
        13,   // Level 15
        10,   // Level 16
        8,    // Level 17
        6,    // Level 18
        5,    // Level 19
        4     // Level 20+ (cap)
    };

    private final Listener listener;
    private final Timer gravityTimer;
    private Timer lockDelayTimer;

    private int level;
    private boolean softDropActive;
    private boolean running;

    public GameClock(Listener listener) {
        this(listener, LEVEL_GRAVITY_TABLE[0]);
    }

    public GameClock(Listener listener, int initialGravityDelayMs) {
        this.listener = Objects.requireNonNull(listener, "listener");
        gravityTimer = new Timer(
            Math.max(MIN_GRAVITY_DELAY, initialGravityDelayMs),
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.onGravityTick();
                }
            }
        );
        gravityTimer.setRepeats(true);
        gravityTimer.setCoalesce(true);
    }

    public void start() {
        if (running) {
            return;
        }
        System.out.println("[LOG] GameClock.start()");
        gravityTimer.start();
        running = true;
    }

    public void stop() {
        gravityTimer.stop();
        cancelLockDelay();
        running = false;
    }

    public void pause() {
        if (!running) {
            return;
        }
        gravityTimer.stop();
        if (lockDelayTimer != null) {
            lockDelayTimer.stop();
        }
        running = false;
    }

    public void resume() {
        if (running) {
            return;
        }
        System.out.println("[LOG] GameClock.resume()");
        gravityTimer.start();
        if (lockDelayTimer != null && !lockDelayTimer.isRunning()) {
            lockDelayTimer.start();
        }
        running = true;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
        updateGravityDelay();
    }

    public int getLevel() {
        return level;
    }

    public void setSoftDrop(boolean active) {
        if (softDropActive == active) {
            return;
        }
        softDropActive = active;
        updateGravityDelay();
    }

    public boolean isSoftDropActive() {
        return softDropActive;
    }

    public void armLockDelay(int delayMs) {
        if (delayMs <= 0) {
            listener.onLockDelayTimeout();
            return;
        }

        ensureLockTimer();
        lockDelayTimer.setInitialDelay(delayMs);
        lockDelayTimer.setDelay(delayMs);
        lockDelayTimer.restart();
    }

    public void armDefaultLockDelay() {
        armLockDelay(DEFAULT_LOCK_DELAY_MS);
    }

    public void cancelLockDelay() {
        if (lockDelayTimer != null) {
            lockDelayTimer.stop();
        }
    }

    private void ensureLockTimer() {
        if (lockDelayTimer != null) {
            return;
        }
        lockDelayTimer = new Timer(DEFAULT_LOCK_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lockDelayTimer.stop();
                listener.onLockDelayTimeout();
            }
        });
        lockDelayTimer.setRepeats(false);
        lockDelayTimer.setCoalesce(true);
    }

    private void updateGravityDelay() {
        int base = computeDelayForLevel(level);
        int delay = softDropActive
            ? Math.max(MIN_GRAVITY_DELAY, base / SOFT_DROP_FACTOR)
            : base;
        gravityTimer.setDelay(delay);
        gravityTimer.setInitialDelay(delay);
    }

    private int computeDelayForLevel(int level) {
        if (level < LEVEL_GRAVITY_TABLE.length) {
            return LEVEL_GRAVITY_TABLE[level];
        }
        int last = LEVEL_GRAVITY_TABLE[LEVEL_GRAVITY_TABLE.length - 1];
        double multiplier = Math.pow(0.8, level - (LEVEL_GRAVITY_TABLE.length - 1));
        return Math.max(MIN_GRAVITY_DELAY, (int) Math.round(last * multiplier));
    }
}
