package tetris.domain;

import tetris.domain.BlockKind;

/**
 * 블록 스폰 시 사용할 다음 미노 종류를 결정하는 전략 인터페이스.
 * 구현을 교체해 랜덤/가방 시스템 등 다양한 분배 로직을 적용할 수 있다.
 */
public interface BlockGenerator {

    /**
     * 다음에 스폰할 블록 종류를 반환한다.
     */
    BlockKind nextBlock();
}
