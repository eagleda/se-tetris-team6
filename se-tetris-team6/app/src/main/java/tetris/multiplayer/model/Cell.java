package tetris.multiplayer.model;

import java.util.Objects;

/**
 * 마지막으로 잠긴 블록의 셀 좌표를 표현하기 위한 불변 값 객체.
 * - 엔진 내부 타입(Block/Shape)에 의존하지 않고 순수 좌표만 보존한다.
 * - 멀티플레이 규칙 엔진이 블록 정보를 재구성할 때 사용한다.
 */
public final class Cell {
    private final int x;
    private final int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cell other)) {
            return false;
        }
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Cell{" + "x=" + x + ", y=" + y + '}';
    }
}
