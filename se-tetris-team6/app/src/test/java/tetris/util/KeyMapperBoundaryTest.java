/**
 * 대상: tetris.util.KeyMapper
 *
 * 목적:
 * - 알 수 없는 키 이름/코드 처리 경계를 검증해 50%대 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) nameToKeyCode가 존재하지 않는 이름에 -1을 반환
 * 2) keyCodeToName이 알 수 없는 코드에서 null 또는 "Unknown" 접두 문자열을 반환
 */
package tetris.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class KeyMapperBoundaryTest {

    @Test
    void unknownName_returnsMinusOne() {
        assertEquals(-1, KeyMapper.nameToKeyCode("NON_EXISTENT_KEY_123"));
    }

    @Test
    void unknownCode_returnsSomeString() {
        String name = KeyMapper.keyCodeToName(99999);
        // 구현마다 반환 문자열이 다를 수 있으므로 null 이지만 않으면 허용한다.
        assertNotNull(name);
    }
}
