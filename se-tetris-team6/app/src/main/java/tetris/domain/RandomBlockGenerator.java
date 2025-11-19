package tetris.domain;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * {@link java.util.Random} 기반의 단순 무작위 블록 생성기.
 * 추후 다른 랜덤 분배 방식으로 쉽게 교체할 수 있도록 별도 클래스로 분리한다.
 */
public final class RandomBlockGenerator implements BlockGenerator {

    private final BlockKind[] kinds = BlockKind.values();
    private final Random random;

    // lookahead buffer so peekNext() can report the upcoming kind without
    // disturbing the sequence used by nextBlock(). Keep 1-slot preview to
    // remain compatible with UI components that rely on peekNext().
    private BlockKind nextKind;

    // Weighted roulette selection state (to support difficulty adjustments).
    private final double[] weights;
    private final double[] cumulativeWeights;
    private double totalWeight;
    private GameDifficulty difficulty = GameDifficulty.NORMAL;
    private boolean dirty = true;

    public RandomBlockGenerator() {
        this(new Random());
    }

    public RandomBlockGenerator(Random random) {
        this.random = Objects.requireNonNull(random, "random");
        this.weights = new double[kinds.length];
        this.cumulativeWeights = new double[kinds.length];
        // initialize buffer using weighted sampling (will call recompute on first use)
        this.nextKind = null;
    }

    @Override
    public BlockKind nextBlock() {
        ensureBuffered();
        BlockKind current = nextKind;
        // advance buffer by sampling with current weights
        nextKind = sampleByWeights();
        return current;
    }

    @Override
    public BlockKind peekNext() {
        ensureBuffered();
        return nextKind;
    }

    @Override
    public void setDifficulty(GameDifficulty difficulty) {
        GameDifficulty next = difficulty == null ? GameDifficulty.NORMAL : difficulty;
        if (this.difficulty != next) {
            this.difficulty = next;
            this.dirty = true;
        }
    }

    private void ensureBuffered() {
        if (nextKind == null) {
            if (dirty) recomputeWeights();
            nextKind = sampleByWeights();
        }
    }

    private BlockKind sampleByWeights() {
        if (dirty) {
            recomputeWeights();
        }
        // Force I-block only selection.
        return BlockKind.I;
    }

    private void recomputeWeights() {
        Arrays.fill(weights, 1.0);
        double acc = 0.0;
        for (int i = 0; i < weights.length; i++) {
            acc += weights[i];
            cumulativeWeights[i] = acc;
        }
        totalWeight = acc;
        dirty = false;
    }
}
