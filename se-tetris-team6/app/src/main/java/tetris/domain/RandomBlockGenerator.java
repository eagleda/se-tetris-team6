package tetris.domain;

import java.util.Objects;
import java.util.Random;

/**
 * {@link java.util.Random} 기반의 단순 무작위 블록 생성기.
 * 추후 다른 랜덤 분배 방식으로 쉽게 교체할 수 있도록 별도 클래스로 분리한다.
 */
public final class RandomBlockGenerator implements BlockGenerator {

    private final BlockKind[] kinds = BlockKind.values();
    private final Random random;
    // maintain a lookahead so peekNext() can report the upcoming kind without
    // disturbing the sequence used by nextBlock(). This implements a 1-slot
    // preview buffer (sufficient for simple UI preview).
    private BlockKind nextKind;

    public RandomBlockGenerator() {
        this(new Random());
    }

    public RandomBlockGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
        this.nextKind = kinds[this.random.nextInt(kinds.length)];
    }

    @Override
    public BlockKind nextBlock() {
        BlockKind current = nextKind != null ? nextKind : kinds[random.nextInt(kinds.length)];
        // advance buffer
        nextKind = kinds[random.nextInt(kinds.length)];
        return current;
    }

    @Override
    public BlockKind peekNext() {
        if (nextKind == null) {
            nextKind = kinds[random.nextInt(kinds.length)];
        }
        return nextKind;
    }
}
