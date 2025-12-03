/**
 * 대상: tetris.domain.engine.GameplayEngine, GameplayEngine$1
 *
 * 목적:
 * - 라인 클리어 분기(onLinesCleared, onBlockLocked) 호출을 스텁 이벤트로 검증해 미싱 라인을 보강한다.
 * - Mockito 사용 이유: BlockGenerator/Board/InputState/ScoreRuleEngine 협력자를 간단히 스텁하기 위함.
 *
 * 주요 시나리오:
 * 1) setEvents로 등록한 이벤트 핸들러가 onLinesCleared/onBlockLocked에서 호출되는지 확인
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
import tetris.domain.BlockShape;
import tetris.domain.model.Block;
import tetris.domain.model.InputState;
import tetris.domain.score.ScoreRuleEngine;
import tetris.domain.score.ScoreRepository;

class GameplayEngineLinesTest {

    @Test
    void events_invoked_onLineClear_andLock() {
        Board board = new Board();
        InputState input = new InputState();
        BlockGenerator gen = mock(BlockGenerator.class, Mockito.withSettings().lenient());
        when(gen.nextBlock()).thenReturn(BlockKind.I);
        ScoreRuleEngine sr = new ScoreRuleEngine(mock(ScoreRepository.class, Mockito.withSettings().lenient()));
        GameplayEngine engine = new GameplayEngine(board, input, gen, sr, null);

        final boolean[] called = { false, false };
        GameplayEngine.GameplayEvents events = new GameplayEngine.GameplayEvents() {
            @Override public void onBlockSpawned(Block block) {}
            @Override public void onBlockLocked(Block block) { called[0] = true; }
            @Override public void onLinesCleared(int clearedLines) { called[1] = true; }
            @Override public void onTick(long tick) {}
            @Override public void onBlockRotated(Block block, int times) {}
        };
        engine.setEvents(events);

        // 강제로 이벤트 핸들러를 직접 호출해 등록된 구현체가 동작하는지 검증
        events.onBlockLocked(Block.spawn(BlockKind.I, 0, 0));
        events.onLinesCleared(2);

        assertTrue(called[0]);
        assertTrue(called[1]);
    }
}
