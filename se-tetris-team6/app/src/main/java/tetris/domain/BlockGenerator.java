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

    /**
     * 다음에 스폰할 블록을 미리 조회(peek)합니다. 구현은 상태를 변경하지 않고
     * 다음 블록 종류를 반환해야 합니다(미리보기 용도).
     */
    default BlockKind peekNext() {
        // 기본 구현: nextBlock()와 동일하게 동작할 수 있으나
        // 구현체에서 상태를 유지하도록 오버라이드하는 것을 권장합니다.
        return nextBlock();
    }
}
