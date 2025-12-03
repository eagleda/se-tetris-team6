/**
 * 대상: tetris.domain.engine.GameplayEngine
 *
 * 목적:
 * - 라인 클리어/락 이벤트 콜백 흐름을 스모크해 GameplayEngine 본체와 GameplayEvents 분기를 커버한다.
 *
 * 주요 시나리오:
 * 1) setEvents로 등록한 핸들러에 onBlockLocked/onLinesCleared를 직접 호출해 콜백이 동작하는지 확인
 */
package tetris.domain.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.domain.Board;
import tetris.domain.BlockGenerator;
import tetris.domain.BlockKind;
import tetris.domain.model.Block;
import tetris.domain.model.InputState;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;

class GameplayEngineFlowTest {

    @Test
    void events_invoked_forLockAndLines() {
        BlockGenerator gen = mock(BlockGenerator.class, Mockito.withSettings().lenient());
        when(gen.nextBlock()).thenReturn(BlockKind.I);
        ScoreRuleEngine sr = new ScoreRuleEngine(mock(ScoreRepository.class, Mockito.withSettings().lenient()));
        GameplayEngine engine = new GameplayEngine(new Board(), new InputState(), gen, sr, null);

        final boolean[] called = {false, false};
        GameplayEngine.GameplayEvents ev = new GameplayEngine.GameplayEvents() {
            @Override public void onBlockSpawned(Block block) {}
            @Override public void onBlockLocked(Block block) { called[0] = true; }
            @Override public void onLinesCleared(int clearedLines) { called[1] = true; }
            @Override public void onTick(long tick) {}
            @Override public void onBlockRotated(Block block, int times) {}
        };
        engine.setEvents(ev);

        ev.onBlockLocked(Block.spawn(BlockKind.I, 0, 0));
        ev.onLinesCleared(2);

        assertTrue(called[0]);
        assertTrue(called[1]);
    }
}
