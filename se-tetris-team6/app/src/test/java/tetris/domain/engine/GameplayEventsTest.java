/*
 * 테스트 대상: tetris.domain.engine.GameplayEngine.GameplayEvents
 *
 * 역할 요약:
 * - 게임 진행 중 발생하는 이벤트 콜백을 UI/네트워크 등 상위 계층에 전달하기 위한 인터페이스입니다.
 * - 블록 회전/이동, 점수 갱신, 게임 오버와 같은 이벤트가 발생했을 때 호출됩니다.
 *
 * 테스트 전략:
 * - 최소 구현체를 익명 클래스로 만들어 모든 메서드가 호출되어도 예외 없이 동작하는지 확인합니다.
 * - 추상 메서드가 많아 Mockito 없이도 호출 가능하도록 단순 no-op 구현을 사용합니다.
 *
 * 주요 테스트 시나리오 예시:
 * - 모든 콜백 메서드 호출 시 예외가 발생하지 않는다.
 */

package tetris.domain.engine;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;
import tetris.domain.model.Block;

class GameplayEventsTest {

    @Test
    void callbacks_doNotThrowOnNoOpImplementation() {
        GameplayEngine.GameplayEvents events = new GameplayEngine.GameplayEvents() {
            @Override public void onBlockSpawned(Block block) {}
            @Override public void onBlockLocked(Block block) {}
            @Override public void onLinesCleared(int clearedLines) {}
            @Override public void onTick(long tick) {}
            @Override public void onBlockRotated(Block block, int times) {}
            @Override public void onGameOver() {}
        };

        Block dummy = new Block(BlockShape.of(BlockKind.I), 0, 0);
        assertDoesNotThrow(() -> {
            events.onBlockSpawned(dummy);
            events.onBlockRotated(dummy, 90);
            events.onBlockLocked(dummy);
            events.onLinesCleared(2);
            events.onTick(42L);
            events.onGameOver();
        });
    }
}
