package tetris.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import javax.swing.Timer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.model.GameClock
 *
 * 역할 요약:
 * - Swing Timer 기반으로 중력 틱과 잠금 지연을 관리하는 게임 루프 시계.
 * - 레벨/소프트드랍/외부 속도 변경에 따라 중력 딜레이를 재계산하며,
 *   잠금 지연 타이머를 arm/cancel 하여 블록 고정 유예를 제어한다.
 *
 * 테스트 전략:
 * - 계산 로직 중심: 레벨/소프트드랍/속도 보정에 따른 중력 딜레이 값 확인.
 * - 잠금 지연: 0ms 요청 시 즉시 콜백 호출, 기본 지연 arm 후 cancel 동작 확인.
 * - Timer 비동기성은 피하고, 리플렉션으로 내부 Timer 상태를 읽어 동기 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - 레벨 10 설정 시 중력 딜레이가 테이블 값(45ms)으로 설정된다.
 * - 소프트드랍 활성화 시 최소 딜레이(16ms)로 캡된다.
 * - 속도 보정 0.5 배 적용 시 딜레이가 반으로(45→23ms) 조정된다.
 * - armLockDelay(0) 호출 시 즉시 onLockDelayTimeout 콜백이 발생한다.
 * - 기본 잠금 지연 arm 후 cancel 시 타이머가 중지 상태가 된다.
 */
class GameClockTest {

    private static class RecordingListener implements GameClock.Listener {
        int gravityTicks;
        int lockTimeouts;

        @Override
        public void onGravityTick() {
            gravityTicks++;
        }

        @Override
        public void onLockDelayTimeout() {
            lockTimeouts++;
        }
    }

    private RecordingListener listener;
    private GameClock clock;

    @BeforeEach
    void setUp() {
        listener = new RecordingListener();
        clock = new GameClock(listener);
    }

    @Test
    void setLevel_updatesGravityDelayFromTable() throws Exception {
        // given
        clock.setLevel(10); // table value 45ms

        // when
        Timer gravity = getGravityTimer(clock);

        // then
        assertEquals(45, gravity.getDelay());
        assertEquals(45, gravity.getInitialDelay());
    }

    @Test
    void setSoftDrop_capsDelayToMinimum() throws Exception {
        // given
        clock.setLevel(10); // base 45ms

        // when
        clock.setSoftDrop(true);
        Timer gravity = getGravityTimer(clock);

        // then (45 / 6 = 7.5, but min 16ms)
        assertEquals(16, gravity.getDelay());
    }

    @Test
    void setSpeedModifier_scalesDelay() throws Exception {
        // given
        clock.setLevel(10); // 45ms
        clock.setSoftDrop(false);

        // when
        clock.setSpeedModifier(0.5); // half speed → 22.5 → round 23
        Timer gravity = getGravityTimer(clock);

        // then
        assertEquals(23, gravity.getDelay());
    }

    @Test
    void armLockDelay_zero_ms_invokesListenerImmediately() {
        // when
        clock.armLockDelay(0);

        // then
        assertEquals(1, listener.lockTimeouts);
    }

    @Test
    void armDefaultLockDelay_thenCancel_stopsTimer() throws Exception {
        // given
        clock.armDefaultLockDelay();

        // when
        clock.cancelLockDelay();
        Timer lockTimer = getLockTimer(clock);

        // then
        assertFalse(lockTimer.isRunning());
    }

    private Timer getGravityTimer(GameClock target) throws Exception {
        Field f = GameClock.class.getDeclaredField("gravityTimer");
        f.setAccessible(true);
        return (Timer) f.get(target);
    }

    private Timer getLockTimer(GameClock target) throws Exception {
        Field f = GameClock.class.getDeclaredField("lockDelayTimer");
        f.setAccessible(true);
        return (Timer) f.get(target);
    }
}
