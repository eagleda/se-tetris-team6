package tetris.multiplayer.model;

import java.util.Arrays;

/**
 * 대기 중인 공격 줄 한 줄을 표현하는 값 객체.
 * - holes[x] == true  → 해당 열은 구멍(빈칸)으로 남겨 둔다.
 * - holes[x] == false → 회색 공격 블록을 채워 넣는다.
 */
public final class AttackLine {

    private final boolean[] holes;

    public AttackLine(boolean[] holes) {
        if (holes == null || holes.length == 0) {
            throw new IllegalArgumentException("holes must not be null or empty");
        }
        this.holes = holes.clone();
    }

    public int width() {
        return holes.length;
    }

    public boolean isHole(int x) {
        return holes[x];
    }

    public boolean[] copyHoles() {
        return holes.clone();
    }

    @Override
    public String toString() {
        return "AttackLine" + Arrays.toString(holes);
    }
}
