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
        recomputeWeights();
    }

    @Override
    public BlockKind nextBlock() {
        if (dirty) {
            recomputeWeights();
        }
        double pick = random.nextDouble() * totalWeight;
        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (pick < cumulativeWeights[i]) {
                return kinds[i];
            }
        }
        return kinds[kinds.length - 1];
    }

    @Override
    public void setDifficulty(GameDifficulty difficulty) {
        GameDifficulty next = difficulty == null ? GameDifficulty.NORMAL : difficulty;
        if (this.difficulty != next) {
            this.difficulty = next;
            this.dirty = true;
        }
    }

    private void recomputeWeights() {
        double baseWeight = 1.0;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = baseWeight;
        }

        int indexI = BlockKind.I.ordinal();
        if (difficulty == GameDifficulty.EASY) {
            weights[indexI] = baseWeight * 1.2;
        } else if (difficulty == GameDifficulty.HARD) {
            weights[indexI] = baseWeight * 0.8;
        } else {
            weights[indexI] = baseWeight;
        }

        totalWeight = 0.0;
        for (int i = 0; i < weights.length; i++) {
            totalWeight += weights[i];
            cumulativeWeights[i] = totalWeight;
        }
        dirty = false;
    }
}
