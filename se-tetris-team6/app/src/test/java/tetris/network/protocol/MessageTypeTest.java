package tetris.network.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/*
 * 테스트 대상: tetris.network.protocol.MessageType
 *
 * 역할 요약:
 * - 네트워크 메시지의 타입을 정의하는 enum
 * - 연결 관리, 게임 제어, 게임 데이터, 네트워크 상태, 에러 처리 카테고리로 분류
 * - GameMessage 클래스에서 사용되어 메시지 종류를 구분
 * - 프로토콜의 타입 안전성과 확장성 보장
 *
 * 테스트 전략:
 * - Enum 타입의 기본 동작 검증
 * - 모든 메시지 타입 카테고리별로 존재 확인
 * - values(), valueOf() 메서드 정상 동작 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 모든 메시지 타입이 정의되어 있는지 확인
 * - valueOf()로 문자열을 통해 enum을 가져올 수 있는지 확인
 * - 카테고리별(연결, 게임 제어, 데이터, 상태, 에러) 타입 존재 검증
 */

public class MessageTypeTest {

    @Test
    void values_shouldReturnAllMessageTypes() {
        // when
        MessageType[] types = MessageType.values();

        // then
        assertTrue(types.length >= 20, "Should have at least 20 message types");
    }

    @Test
    void connectionManagementTypes_shouldExist() {
        // when & then
        assertNotNull(MessageType.CONNECTION_REQUEST);
        assertNotNull(MessageType.CONNECTION_ACCEPTED);
        assertNotNull(MessageType.CONNECTION_REJECTED);
        assertNotNull(MessageType.DISCONNECT);
        assertNotNull(MessageType.OPPONENT_DISCONNECTED);
    }

    @Test
    void gameControlTypes_shouldExist() {
        // when & then
        assertNotNull(MessageType.GAME_MODE_SELECT);
        assertNotNull(MessageType.GAME_START);
        assertNotNull(MessageType.GAME_PAUSE);
        assertNotNull(MessageType.GAME_END);
        assertNotNull(MessageType.GAME_RESTART);
    }

    @Test
    void gameDataTypes_shouldExist() {
        // when & then
        assertNotNull(MessageType.PLAYER_INPUT);
        assertNotNull(MessageType.BOARD_STATE);
        assertNotNull(MessageType.BLOCK_PLACEMENT);
        assertNotNull(MessageType.LINE_CLEAR);
        assertNotNull(MessageType.ATTACK_LINES);
        assertNotNull(MessageType.SCORE_UPDATE);
        assertNotNull(MessageType.PLAYER_READY);
        assertNotNull(MessageType.GAME_STATE);
    }

    @Test
    void networkStatusTypes_shouldExist() {
        // when & then
        assertNotNull(MessageType.PING);
        assertNotNull(MessageType.PONG);
        assertNotNull(MessageType.LAG_WARNING);
        assertNotNull(MessageType.SYNC_REQUEST);
    }

    @Test
    void errorHandlingTypes_shouldExist() {
        // when & then
        assertNotNull(MessageType.ERROR);
        assertNotNull(MessageType.TIMEOUT);
    }

    @Test
    void valueOf_shouldReturnCorrectMessageType() {
        // when & then
        assertEquals(MessageType.GAME_START, MessageType.valueOf("GAME_START"));
        assertEquals(MessageType.PLAYER_INPUT, MessageType.valueOf("PLAYER_INPUT"));
        assertEquals(MessageType.ATTACK_LINES, MessageType.valueOf("ATTACK_LINES"));
        assertEquals(MessageType.PING, MessageType.valueOf("PING"));
        assertEquals(MessageType.DISCONNECT, MessageType.valueOf("DISCONNECT"));
    }

    @Test
    void valueOf_withInvalidName_shouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            MessageType.valueOf("INVALID_MESSAGE_TYPE");
        });
    }

    @Test
    void enumConstantsShouldBeUnique() {
        // given
        MessageType[] types = MessageType.values();

        // when & then
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                assertNotEquals(types[i], types[j], 
                    "Each message type should be unique");
            }
        }
    }

    @Test
    void enumName_shouldMatchConstantName() {
        // when & then
        assertEquals("GAME_START", MessageType.GAME_START.name());
        assertEquals("PLAYER_INPUT", MessageType.PLAYER_INPUT.name());
        assertEquals("PING", MessageType.PING.name());
        assertEquals("ERROR", MessageType.ERROR.name());
    }

    @Test
    void criticalGameMessages_shouldExist() {
        // when & then
        MessageType[] types = MessageType.values();
        
        assertTrue(containsType(types, MessageType.GAME_START), 
            "GAME_START is critical");
        assertTrue(containsType(types, MessageType.GAME_END), 
            "GAME_END is critical");
        assertTrue(containsType(types, MessageType.PLAYER_INPUT), 
            "PLAYER_INPUT is critical");
        assertTrue(containsType(types, MessageType.ATTACK_LINES), 
            "ATTACK_LINES is critical");
    }

    private boolean containsType(MessageType[] types, MessageType target) {
        for (MessageType type : types) {
            if (type == target) {
                return true;
            }
        }
        return false;
    }
}
