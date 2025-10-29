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

    public RandomBlockGenerator() {
        this(new Random());
    }

    public RandomBlockGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public BlockKind nextBlock() {
        return kinds[random.nextInt(kinds.length)];
    }
}
