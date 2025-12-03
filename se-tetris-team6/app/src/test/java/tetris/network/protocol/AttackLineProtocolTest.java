package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.AttackLine
 *
 * 역할 요약:
 * - 네트워크로 전송할 공격 줄의 강도를 표현하는 단순 DTO.
 *
 * 테스트 전략:
 * - 생성자에 전달한 strength가 그대로 노출되는지 확인한다.
 * - 직렬화 가능성을 간접적으로 확인하기 위해 instanceof Serializable 검증을 추가한다.
 */
class AttackLineProtocolTest {

    @Test
    void strengthIsExposedAsGiven() {
        AttackLine line = new AttackLine(3);
        assertEquals(3, line.getStrength());
    }

    @Test
    void implementsSerializable() {
        AttackLine line = new AttackLine(1);
        assertTrue(line instanceof java.io.Serializable);
    }
}
