/*
 * 테스트 대상: tetris.network.client.GameStateListener
 *
 * 역할 요약:
 * - 서버로부터 스냅샷/상태 메시지를 수신했을 때 호출되는 콜백 인터페이스입니다.
 *
 * 테스트 전략:
 * - 익명 구현체를 생성해 필수 메서드(onOpponentBoardUpdate, onGameStateChange, onGameStateSnapshot)를 호출하고
 *   예외 없이 통과하는지 확인합니다.
 */

package tetris.network.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.GameSnapshot;
import tetris.network.protocol.MessageType;

class GameStateListenerTest {

    @Test
    void callbacks_doNotThrow() {
        GameStateListener listener = new GameStateListener() {
            @Override public void onOpponentBoardUpdate(GameMessage message) {}
            @Override public void onGameStateChange(GameMessage message) {}
            @Override public void onGameStateSnapshot(GameSnapshot snapshot) {}
        };

        assertDoesNotThrow(() -> listener.onOpponentBoardUpdate(
                new GameMessage(MessageType.GAME_STATE, "server", "payload")));
        assertDoesNotThrow(() -> listener.onGameStateChange(
                new GameMessage(MessageType.PLAYER_INPUT, "server", "payload")));
        assertDoesNotThrow(() -> listener.onGameStateSnapshot(
                new GameSnapshot(1, new int[1][1], 0, 0, 0, 0, 0, 0, 0, 0, null, "NORMAL", null, -1, -1, null)));
        assertDoesNotThrow(() -> listener.onConnectionTimeout("timeout"));
    }
}
