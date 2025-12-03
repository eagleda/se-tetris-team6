package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.multiplayer.model.AttackLine
 *
 * 역할 요약:
 * - 멀티플레이 공격 줄 한 줄의 구멍 패턴을 보관하는 값 객체.
 * - holes 배열을 복사 보관하여 불변성을 유지한다.
 *
 * 테스트 전략:
 * - null/빈 배열 생성 시 예외 발생 여부 확인.
 * - width/ isHole/ copyHoles가 원본 패턴을 그대로 유지하면서 복사본을 반환하는지 검증.
 */
class AttackLineTest {

    @Test
    void constructor_rejectsNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new AttackLine(null));
        assertThrows(IllegalArgumentException.class, () -> new AttackLine(new boolean[0]));
    }

    @Test
    void widthAndHoles_arePreservedAndCopied() {
        boolean[] pattern = { true, false, true };
        AttackLine line = new AttackLine(pattern);

        assertEquals(3, line.width());
        assertTrue(line.isHole(0));
        assertFalse(line.isHole(1));
        assertTrue(line.isHole(2));

        boolean[] copy = line.copyHoles();
        assertArrayEquals(pattern, copy);
        copy[0] = false; // 수정해도 원본 불변
        assertTrue(line.isHole(0));
    }
}
