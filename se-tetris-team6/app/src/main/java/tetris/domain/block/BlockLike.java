package tetris.domain.block;

import tetris.domain.BlockKind;
import tetris.domain.BlockShape;

/**
 * 최소한의 블록 정보만 노출하는 도메인 계층 인터페이스.
 * 기존 {@code tetris.domain.model.Block}과 아이템 블록 래퍼가 모두 구현한다.
 */
public interface BlockLike {

    BlockShape getShape();

    BlockKind getKind();

    int getX();

    int getY();

    void setPosition(int x, int y);
}
