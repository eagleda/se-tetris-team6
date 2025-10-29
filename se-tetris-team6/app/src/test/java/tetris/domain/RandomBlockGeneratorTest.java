package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

class RandomBlockGeneratorTest {

    private static final int SAMPLES = 50_000;
    private static final double TOLERANCE = 0.05; // absolute tolerance (±5%)

    @Test
    void generatesBlocksWithExpectedDistributionForNormalDifficulty() {
        assertProbabilityWithinTolerance(GameDifficulty.NORMAL);
    }

    @Test
    void generatesBlocksWithExpectedDistributionForEasyDifficulty() {
        assertProbabilityWithinTolerance(GameDifficulty.EASY);
    }

    @Test
    void generatesBlocksWithExpectedDistributionForHardDifficulty() {
        assertProbabilityWithinTolerance(GameDifficulty.HARD);
    }

    private void assertProbabilityWithinTolerance(GameDifficulty difficulty) {
        RandomBlockGenerator generator = new RandomBlockGenerator(new Random(42L));
        generator.setDifficulty(difficulty);

        int[] counts = new int[BlockKind.values().length];
        for (int i = 0; i < SAMPLES; i++) {
            BlockKind kind = generator.nextBlock();
            counts[kind.ordinal()]++;
        }

        double expectedIProbability = expectedProbabilityForI(difficulty);
        double expectedOtherProbability = expectedProbabilityForOthers(expectedIProbability);

        double actualIProbability = counts[BlockKind.I.ordinal()] / (double) SAMPLES;
        assertEquals(expectedIProbability, actualIProbability, TOLERANCE,
            "I block probability deviates more than allowed tolerance for " + difficulty);

        for (BlockKind kind : BlockKind.values()) {
            if (kind == BlockKind.I) {
                continue;
            }
            double actual = counts[kind.ordinal()] / (double) SAMPLES;
            assertEquals(expectedOtherProbability, actual, TOLERANCE,
                kind + " block probability deviates more than allowed tolerance for " + difficulty);
        }
    }

    private double expectedProbabilityForI(GameDifficulty difficulty) {
        double base = 1.0;
        double weightI = base;
        if (difficulty == GameDifficulty.EASY) {
            weightI = base * 1.2;
        } else if (difficulty == GameDifficulty.HARD) {
            weightI = base * 0.8;
        }
        double total = weightI + base * (BlockKind.values().length - 1);
        return weightI / total;
    }

    private double expectedProbabilityForOthers(double expectedIProbability) {
        double remainingProbability = 1.0 - expectedIProbability;
        int otherKinds = BlockKind.values().length - 1;
        return remainingProbability / otherKinds;
    }
}
