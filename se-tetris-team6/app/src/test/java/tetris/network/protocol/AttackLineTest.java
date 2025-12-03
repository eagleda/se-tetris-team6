package tetris.network.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

/*
 * 테스트 대상: tetris.network.protocol.AttackLine
 *
 * 역할 요약:
 * - 상대방에게 전송할 공격 라인 정보를 담는 데이터 클래스
 * - strength 필드로 공격의 강도(줄 수)를 표현
 * - Serializable 구현으로 네트워크 전송 가능
 * - 불변(immutable) 객체로 설계
 *
 * 테스트 전략:
 * - 생성자 및 getter 동작 검증
 * - 직렬화/역직렬화 테스트 (네트워크 전송 시뮬레이션)
 * - 다양한 strength 값에 대한 동작 확인
 * - 불변성 검증
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 양수 strength로 객체 생성
 * - 0 및 음수 strength 처리 (경계값)
 * - 직렬화 후 역직렬화 시 데이터 보존
 * - 여러 객체 간 독립성 확인
 */

public class AttackLineTest {

    @Test
    void constructor_withPositiveStrength_shouldCreateObject() {
        // given
        int strength = 3;

        // when
        AttackLine attackLine = new AttackLine(strength);

        // then
        assertEquals(strength, attackLine.getStrength(), 
            "Strength should match constructor argument");
    }

    @Test
    void constructor_withZeroStrength_shouldCreateObject() {
        // given
        int strength = 0;

        // when
        AttackLine attackLine = new AttackLine(strength);

        // then
        assertEquals(0, attackLine.getStrength(), 
            "Should allow zero strength");
    }

    @Test
    void constructor_withNegativeStrength_shouldCreateObject() {
        // given
        int strength = -5;

        // when
        AttackLine attackLine = new AttackLine(strength);

        // then
        assertEquals(strength, attackLine.getStrength(), 
            "Should allow negative strength (validation is caller's responsibility)");
    }

    @Test
    void getStrength_shouldReturnCorrectValue() {
        // given
        AttackLine attackLine = new AttackLine(7);

        // when
        int strength = attackLine.getStrength();

        // then
        assertEquals(7, strength, "Should return the strength value");
    }

    @Test
    void serialization_shouldPreserveData() throws IOException, ClassNotFoundException {
        // given
        AttackLine original = new AttackLine(4);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);

        // when
        objectOut.writeObject(original);
        objectOut.flush();
        
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        AttackLine deserialized = (AttackLine) objectIn.readObject();

        // then
        assertEquals(original.getStrength(), deserialized.getStrength(), 
            "Strength should be preserved after serialization");
    }

    @Test
    void multipleInstances_shouldBeIndependent() {
        // given
        AttackLine attack1 = new AttackLine(2);
        AttackLine attack2 = new AttackLine(5);

        // when & then
        assertEquals(2, attack1.getStrength());
        assertEquals(5, attack2.getStrength());
        assertNotEquals(attack1.getStrength(), attack2.getStrength(), 
            "Different instances should have independent values");
    }

    @Test
    void serialization_withLargeStrength_shouldWork() throws IOException, ClassNotFoundException {
        // given
        AttackLine original = new AttackLine(Integer.MAX_VALUE);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);

        // when
        objectOut.writeObject(original);
        objectOut.flush();
        
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        AttackLine deserialized = (AttackLine) objectIn.readObject();

        // then
        assertEquals(Integer.MAX_VALUE, deserialized.getStrength(), 
            "Should handle large strength values");
    }

    @Test
    void immutability_strengthShouldNotChange() {
        // given
        int originalStrength = 3;
        AttackLine attackLine = new AttackLine(originalStrength);
        int initialStrength = attackLine.getStrength();

        // when
        // Try to use the object (getter doesn't modify state)
        attackLine.getStrength();
        attackLine.getStrength();

        // then
        assertEquals(initialStrength, attackLine.getStrength(), 
            "Strength should remain constant (immutable)");
    }
}
