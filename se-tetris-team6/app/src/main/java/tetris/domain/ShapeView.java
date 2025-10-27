package tetris.domain;

/**
 * 보드가 인식하는 최소 도형 인터페이스.
 * 블록/아이템 등 어떤 도형이든 이 인터페이스만 구현하면 Board에 배치 가능.
 */
public interface ShapeView {
    int width();
    int height();
    boolean filled(int x, int y); // (x,y) 칸이 점유면 true
}
