package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.MessageType
 *
 * 역할 요약:
 * - 네트워크로 주고받는 모든 메시지 타입을 정의하는 열거형.
 *
 * 테스트 전략:
 * - 주요 메시지 타입들이 Enum에 포함되어 있는지 확인한다.
 */
class MessageTypeTest {

    @Test
    void containsCoreMessageTypes() {
        EnumSet<MessageType> all = EnumSet.allOf(MessageType.class);
        assertTrue(all.contains(MessageType.CONNECTION_REQUEST));
        assertTrue(all.contains(MessageType.CONNECTION_ACCEPTED));
        assertTrue(all.contains(MessageType.GAME_START));
        assertTrue(all.contains(MessageType.PLAYER_INPUT));
        assertTrue(all.contains(MessageType.ATTACK_LINES));
        assertTrue(all.contains(MessageType.GAME_STATE));
        assertTrue(all.contains(MessageType.PING));
        assertTrue(all.contains(MessageType.PONG));
        assertTrue(all.contains(MessageType.ERROR));
    }
}
