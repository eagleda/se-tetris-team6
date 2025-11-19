// File: src/test/java/tetris/network/protocol/GameMessageTest.java
package tetris.network.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.*; // 직렬화/역직렬화를 위해 필요한 클래스들
import static org.junit.jupiter.api.Assertions.*; // 테스트 결과를 단언(검증)하는 도구

class GameMessageTest {

    @Test // 이 메소드가 테스트라는 것을 JUnit에게 알림
    @DisplayName("GameMessage 객체가 성공적으로 직렬화되고 역직렬화되어야 한다")
    void testSerializationAndDeserialization() {

        // 1. [준비] 테스트에 사용할 원본 메시지 객체를 만듭니다.
        // "Player1이 왼쪽으로 움직였다"는 내용의 메시지를 생성합니다.
        PlayerInput inputPayload = new PlayerInput(InputType.MOVE_LEFT);
        GameMessage originalMessage = new GameMessage(MessageType.PLAYER_INPUT, "Player1_ID", inputPayload);
        System.out.println("Test start: Original message = " + originalMessage);

        byte[] serializedData = null;
        
        // 2. [실행-1] 직렬화: 객체를 byte 배열로 변환합니다. (객체 -> byte[])
        // 네트워크로 보낼 수 있는 형태로 '압축 포장'하는 과정입니다.
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            
            oos.writeObject(originalMessage); // 마법같은 한 줄! 이 메소드가 객체를 byte로 바꿔줍니다.
            serializedData = bos.toByteArray(); // 변환된 byte 데이터를 꺼냅니다.
            System.out.println("Serialization successful! Data size: " + serializedData.length + " bytes");

        } catch (IOException e) {
            fail("GameMessage 직렬화 중 예외 발생", e); // 실패하면 테스트 중단
        }

        // 3. [검증-1] 직렬화된 데이터가 실제로 생성되었는지 확인합니다.
        assertNotNull(serializedData); // 데이터가 null이 아니어야 함
        assertTrue(serializedData.length > 0); // 데이터 크기가 0보다 커야 함

        GameMessage deserializedMessage = null;

        // 4. [실행-2] 역직렬화: byte 배열을 다시 객체로 복원합니다. (byte[] -> 객체)
        // 상대방이 받은 '압축 포장'을 뜯어 원래 내용물을 확인하는 과정입니다.
        try (ByteArrayInputStream bis = new ByteArrayInputStream(serializedData);
            ObjectInputStream ois = new ObjectInputStream(bis)) {
            
            // 마법같은 한 줄! byte 데이터를 읽어 원래 GameMessage 객체로 복원합니다.
            deserializedMessage = (GameMessage) ois.readObject(); 
            System.out.println("Deserialization successful! Restored message = " + deserializedMessage);

        } catch (IOException | ClassNotFoundException e) {
            fail("GameMessage 역직렬화 중 예외 발생", e); // 실패하면 테스트 중단
        }

        // 5. [검증-2] 최종 검증: 원본 객체와 복원된 객체가 100% 동일한지 확인합니다.
        assertNotNull(deserializedMessage); // 복원된 객체가 null이 아니어야 함
        
        // 하나하나 비교해봅니다.
        assertEquals(originalMessage.getType(), deserializedMessage.getType(), "Message type must be the same.");
        assertEquals(originalMessage.getSenderId(), deserializedMessage.getSenderId(), "Sender ID must be the same.");
        
        // 내용물(payload)도 같은지 비교합니다.
        assertEquals(originalMessage.getPayload(), deserializedMessage.getPayload(), "Payload content must be the same.");

        // 마지막으로, 객체 전체가 완전히 동일한지 비교합니다.
        assertEquals(originalMessage, deserializedMessage, "The restored object must be completely identical to the original.");
        
        System.out.println("Test passed: Original and restored objects match.");
    }
}