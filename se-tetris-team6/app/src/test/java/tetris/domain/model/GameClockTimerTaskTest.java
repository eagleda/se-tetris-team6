/**
 * 대상: tetris.domain.model.GameClock$TimerTask (armLockDelay/armDefaultLockDelay)
 *
 * 목적:
 * - lockDelay 타이머를 arm/cancel 하는 흐름을 검증해 GameClock$2 내부 클래스 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) armLockDelay(0) 호출 시 리스너가 즉시 실행되는지 확인
 * 2) armDefaultLockDelay 후 cancelLockDelay로 타이머가 정리되는지 확인
 */
package tetris.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameClockTimerTaskTest {

    @Test
    void armLockDelay_zero_executesImmediately() {
        final boolean[] called = { false };
        GameClock clock = new GameClock(new GameClock.Listener() {
            @Override public void onGravityTick() {}
            @Override public void onLockDelayTimeout() { called[0] = true; }
        });
        clock.armLockDelay(0);
        assertTrue(called[0]);
    }

    @Test
    void armDefaultLockDelay_thenCancel_stopsExecution() throws Exception {
        final int[] counter = { 0 };
        GameClock clock = new GameClock(new GameClock.Listener() {
            @Override public void onGravityTick() {}
            @Override public void onLockDelayTimeout() { counter[0]++; }
        });
        clock.armDefaultLockDelay();
        clock.cancelLockDelay();
        Thread.sleep(120);
        assertEquals(0, counter[0], "cancelLockDelay should prevent timeout");
    }
}
