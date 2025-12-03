/**
 * 대상: tetris.concurrent.GameThread.LineClearResult
 *
 * 목적:
 * - 라인 클리어 결과 객체의 필드 저장/게터 동작을 검증해 내부 클래스 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) linesCleared/attackLines/points 값이 그대로 노출되는지 확인
 */
package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import tetris.network.protocol.AttackLine;

class GameThreadLineClearResultTest {

    @Test
    void getters_returnConstructorValues() {
        AttackLine[] lines = new AttackLine[] { new AttackLine(1), new AttackLine(2) };
        GameThread.LineClearResult result = new GameThread.LineClearResult(3, lines, 50);
        assertEquals(3, result.getLinesCleared());
        assertEquals(lines, result.getAttackLines());
        assertEquals(50, result.getPoints());
    }
}
