package tetris.network.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

/*
 * 테스트 대상: tetris.network.protocol.PlayerInput
 *
 * 역할 요약:
 * - 플레이어의 키 입력을 네트워크 전송용 데이터 객체로 표현
 * - InputType enum을 포함하는 record 타입 (Java 14+)
 * - Serializable 구현으로 네트워크 전송 가능
 * - 불변(immutable) 객체로 설계
 *
 * 테스트 전략:
 * - record 타입의 기본 동작 검증 (생성자, accessor)
 * - 직렬화/역직렬화 테스트
 * - equals(), hashCode(), toString() 자동 생성 메서드 검증
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 다양한 InputType으로 객체 생성
 * - inputType() accessor 동작 확인
 * - 직렬화 후 역직렬화 시 데이터 보존
 * - equals() 및 hashCode() 동작 확인
 */

public class PlayerInputTest {

    @Test
    void constructor_withInputType_shouldCreateObject() {
        // given
        InputType inputType = InputType.MOVE_LEFT;

        // when
        PlayerInput playerInput = new PlayerInput(inputType);

        // then
        assertEquals(inputType, playerInput.inputType(), 
            "InputType should match constructor argument");
    }

    @Test
    void inputType_shouldReturnCorrectValue() {
        // given
        PlayerInput playerInput = new PlayerInput(InputType.ROTATE);

        // when
        InputType inputType = playerInput.inputType();

        // then
        assertEquals(InputType.ROTATE, inputType);
    }

    @Test
    void serialization_shouldPreserveData() throws IOException, ClassNotFoundException {
        // given
        PlayerInput original = new PlayerInput(InputType.HARD_DROP);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);

        // when
        objectOut.writeObject(original);
        objectOut.flush();
        
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        PlayerInput deserialized = (PlayerInput) objectIn.readObject();

        // then
        assertEquals(original.inputType(), deserialized.inputType(), 
            "InputType should be preserved after serialization");
    }

    @Test
    void equals_withSameInputType_shouldReturnTrue() {
        // given
        PlayerInput input1 = new PlayerInput(InputType.MOVE_RIGHT);
        PlayerInput input2 = new PlayerInput(InputType.MOVE_RIGHT);

        // when & then
        assertEquals(input1, input2, "Records with same data should be equal");
    }

    @Test
    void equals_withDifferentInputType_shouldReturnFalse() {
        // given
        PlayerInput input1 = new PlayerInput(InputType.MOVE_LEFT);
        PlayerInput input2 = new PlayerInput(InputType.MOVE_RIGHT);

        // when & then
        assertNotEquals(input1, input2, 
            "Records with different data should not be equal");
    }

    @Test
    void hashCode_withSameInputType_shouldBeEqual() {
        // given
        PlayerInput input1 = new PlayerInput(InputType.SOFT_DROP);
        PlayerInput input2 = new PlayerInput(InputType.SOFT_DROP);

        // when & then
        assertEquals(input1.hashCode(), input2.hashCode(), 
            "Records with same data should have same hash code");
    }

    @Test
    void toString_shouldContainInputType() {
        // given
        PlayerInput playerInput = new PlayerInput(InputType.HOLD);

        // when
        String str = playerInput.toString();

        // then
        assertTrue(str.contains("HOLD"), 
            "toString should contain the input type");
    }

    @Test
    void allInputTypes_shouldBeSupported() {
        // when & then
        assertDoesNotThrow(() -> new PlayerInput(InputType.MOVE_LEFT));
        assertDoesNotThrow(() -> new PlayerInput(InputType.MOVE_RIGHT));
        assertDoesNotThrow(() -> new PlayerInput(InputType.SOFT_DROP));
        assertDoesNotThrow(() -> new PlayerInput(InputType.ROTATE));
        assertDoesNotThrow(() -> new PlayerInput(InputType.ROTATE_CCW));
        assertDoesNotThrow(() -> new PlayerInput(InputType.HARD_DROP));
        assertDoesNotThrow(() -> new PlayerInput(InputType.HOLD));
        assertDoesNotThrow(() -> new PlayerInput(InputType.PAUSE));
    }

    @Test
    void multipleInstances_shouldBeIndependent() {
        // given
        PlayerInput input1 = new PlayerInput(InputType.MOVE_LEFT);
        PlayerInput input2 = new PlayerInput(InputType.ROTATE);
        PlayerInput input3 = new PlayerInput(InputType.HARD_DROP);

        // when & then
        assertEquals(InputType.MOVE_LEFT, input1.inputType());
        assertEquals(InputType.ROTATE, input2.inputType());
        assertEquals(InputType.HARD_DROP, input3.inputType());
    }
}
