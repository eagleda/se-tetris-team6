/**
 * 대상: tetris.network.NetworkManager.handleReceivedMessage
 *
 * 목적:
 * - 수신 메시지 타입별로 GameDataListener 콜백이 호출되는지 검증해 본체/익명 콜백 커버리지를 보강한다.
 * - SwingUtilities.invokeLater로 래핑되어 있어 CountDownLatch로 대기 후 결과 확인.
 *
 * 주요 시나리오:
 * 1) PLAYER_INPUT → onOpponentInput 호출
 * 2) ATTACK_LINES → onIncomingAttack 호출
 * 3) BOARD_STATE → onGameStateUpdate 호출
 * 4) GAME_START → onGameStart 호출
 *
 * Mockito 사용 이유:
 * - GameThread 생성 시 GameModel.getBoard()/getInputState()/setSecondaryListener 호출을 스텁해야 하므로
 *   최소한의 협력자 mocking이 필요하다.
 */
package tetris.network;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tetris.concurrent.GameThread;
import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.model.InputState;
import tetris.domain.model.GameState;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.InputType;
import tetris.network.protocol.MessageType;
import tetris.network.protocol.PlayerInput;

class NetworkManagerHandleMessageTest {

    @Test
    void handleReceivedMessage_invokesGameDataListener() throws Exception {
        GameModel gm = mock(GameModel.class, Mockito.withSettings().lenient());
        when(gm.getBoard()).thenReturn(new Board());
        when(gm.getInputState()).thenReturn(new InputState());
        GameThread dummyThread = new GameThread(gm, "p1", true);

        NetworkManager manager = new NetworkManager(dummyThread);

        CountDownLatch latch = new CountDownLatch(4);
        manager.setGameDataListener(new GameDataListener() {
            @Override public void onOpponentInput(PlayerInput input) { latch.countDown(); }
            @Override public void onIncomingAttack(AttackLine[] lines) { latch.countDown(); }
            @Override public void onGameStateUpdate(GameState state) { latch.countDown(); }
            @Override public void onGameStart() { latch.countDown(); }
            @Override public void onGameEnd(String winner) { }
        });

        manager.handleReceivedMessage(new GameMessage(MessageType.PLAYER_INPUT, "s", new PlayerInput(InputType.MOVE_LEFT)));
        manager.handleReceivedMessage(new GameMessage(MessageType.ATTACK_LINES, "s", new AttackLine[] { new AttackLine(1) }));
        manager.handleReceivedMessage(new GameMessage(MessageType.BOARD_STATE, "s", GameState.PLAYING));
        manager.handleReceivedMessage(new GameMessage(MessageType.GAME_START, "s", null));

        assertTrue(latch.await(1, TimeUnit.SECONDS), "All callbacks should be invoked");
    }
}
