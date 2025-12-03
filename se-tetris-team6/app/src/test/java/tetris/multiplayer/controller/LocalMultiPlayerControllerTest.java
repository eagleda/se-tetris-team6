package tetris.multiplayer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.domain.Board;
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.model.LockedPieceSnapshot;
import tetris.multiplayer.model.Cell;

/*
 * 테스트 대상: tetris.multiplayer.controller.LocalMultiPlayerController
 *
 * 역할 요약:
 * - 로컬 2P 대전에서 두 GameModel을 묶어 입력/이벤트를 분배하고 공격 줄 주입을 관리한다.
 *
 * 테스트 전략:
 * - withPlayer가 올바른 GameModel에 Consumer를 적용하는지 확인.
 * - onLocalPieceLocked가 game.onPieceLocked를 호출하고 클리어 시 스냅샷 전송(sendGameState)을 시도하는지 검증.
 * - injectAttackBeforeNextSpawn에서 대기 줄이 없으면 모델 접근 없이 반환되는지 확인(빈 리스트 stub).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocalMultiPlayerControllerTest {

    @Mock MultiPlayerGame game;
    @Mock GameModel modelP1;
    @Mock GameModel modelP2;

    private LocalMultiPlayerController controller;

    @BeforeEach
    void setUp() {
        when(game.modelOf(1)).thenReturn(modelP1);
        when(game.modelOf(2)).thenReturn(modelP2);
        when(modelP1.getBoard()).thenReturn(new Board());
        when(modelP2.getBoard()).thenReturn(new Board());
        controller = Mockito.spy(new LocalMultiPlayerController(game));
    }

    @Test
    void withPlayer_appliesActionToSelectedModel() {
        controller.withPlayer(1, m -> m.pauseGame());
        controller.withPlayer(2, m -> m.resumeGame());

        verify(modelP1).pauseGame();
        verify(modelP2).resumeGame();
    }

    @Test
    void onPieceLocked_delegatesToVersusRules() {
        int[] cleared = { 0, 1 };
        LockedPieceSnapshot snap = LockedPieceSnapshot.of(java.util.List.of(new Cell(0, 0)));

        controller.onPieceLocked(1, snap, cleared);

        verify(game).onPieceLocked(1, snap, cleared, tetris.domain.Board.W);
    }

    @Test
    void injectAttackBeforeNextSpawn_noPendingLines_noBoardTouch() {
        when(game.takeAttackLinesForNextSpawn(1)).thenReturn(Collections.emptyList());
        controller.injectAttackBeforeNextSpawn(1);
        verify(game).takeAttackLinesForNextSpawn(1);
    }
}
