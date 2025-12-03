/*
 * 테스트 대상: tetris.multiplayer.controller.NetworkMultiPlayerController$1 (attachClient에서 등록되는 GameStateListener)
 *
 * 역할 요약:
 * - 네트워크 클라이언트로부터 스냅샷/입력/게임 종료 메시지를 수신해 컨트롤러의 로직으로 위임합니다.
 * - 스냅샷은 Swing EDT에서 applyRemoteSnapshot을 호출하도록 예약합니다.
 *
 * 테스트 전략:
 * - Mockito로 MultiPlayerGame과 GameModel을 스텁하여 실제 소켓/스레드 없이 리스너 메서드를 직접 호출합니다.
 * - onGameStateSnapshot 호출 시 applySnapshot이 대상 플레이어 모델에 전달되는지 확인합니다.
 * - onGameStateChange(PLAYER_INPUT/ATTACK_LINES/GAME_END) 호출이 예외 없이 통과하는지 확인합니다.
 *
 * 주요 테스트 시나리오 예시:
 * - 스냅샷 수신 → 해당 playerId 모델의 applySnapshot이 1회 호출된다.
 * - 다양한 GameMessage 타입을 전달해도 예외가 발생하지 않는다.
 */

package tetris.multiplayer.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import tetris.domain.GameModel;
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.network.client.GameClient;
import tetris.network.client.GameStateListener;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.GameSnapshot;
import tetris.network.protocol.MessageType;

class NetworkMultiPlayerControllerListenerTest {

    private MultiPlayerGame game;
    private GameModel p1;
    private GameModel p2;
    private GameClient client;
    private NetworkMultiPlayerController controller;

    @BeforeEach
    void setUp() {
        game = Mockito.mock(MultiPlayerGame.class, Mockito.withSettings().lenient());
        p1 = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        p2 = Mockito.mock(GameModel.class, Mockito.withSettings().lenient());
        client = Mockito.mock(GameClient.class, Mockito.withSettings().lenient());

        when(game.modelOf(1)).thenReturn(p1);
        when(game.modelOf(2)).thenReturn(p2);

        controller = new NetworkMultiPlayerController(game, 1);
    }

    @Test
    void listener_appliesSnapshot_and_handlesMessages() throws Exception {
        ArgumentCaptor<GameStateListener> captor = ArgumentCaptor.forClass(GameStateListener.class);

        controller.attachClient(client);
        verify(client).setGameStateListener(captor.capture());

        GameStateListener listener = captor.getValue();

        // 스냅샷 전달
        GameSnapshot snapshot = new GameSnapshot(
                2,
                new int[][] { {1} },
                1, 2,
                123, 10, 0,
                0, 0, 0,
                null,
                "NORMAL",
                null,
                -1, -1,
                null);

        listener.onGameStateSnapshot(snapshot);
        // invokeLater로 예약된 작업을 모두 처리
        SwingUtilities.invokeAndWait(() -> {});

        verify(p2, times(1)).applySnapshot(snapshot);

        // 여러 타입의 메시지를 전달해도 예외가 없는지 확인
        assertDoesNotThrow(() -> listener.onGameStateChange(
                new GameMessage(MessageType.PLAYER_INPUT, "tester", null)));
        assertDoesNotThrow(() -> listener.onGameStateChange(
                new GameMessage(MessageType.ATTACK_LINES, "tester", new tetris.network.protocol.AttackLine[] {})));
        assertDoesNotThrow(() -> listener.onGameStateChange(
                new GameMessage(MessageType.GAME_END, "tester", java.util.Map.of("winnerId", 1))));

        // 플레이어 입력/공격 라인 메시지는 단순 로그만 찍으므로 추가 검증 불필요
        verify(game, times(1)).modelOf(2);
    }
}
