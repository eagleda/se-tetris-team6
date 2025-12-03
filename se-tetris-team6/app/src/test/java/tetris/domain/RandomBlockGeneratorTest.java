package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

class RandomBlockGeneratorTest {

    @Test // Normal 난이도에서는 모든 블록이 균등 분포(±3%)로 나오는지 확인
    void normalDifficultyProducesUniformDistribution() {
        BlockKind[] allowed = {
            BlockKind.I, BlockKind.J, BlockKind.L,
            BlockKind.O, BlockKind.S, BlockKind.T, BlockKind.Z
        };
        Distribution dist = sampleDistribution(GameDifficulty.NORMAL, 100_000, 42L);
        double expected = dist.samples() / (double) allowed.length;
        double tolerance = 0.03; // 3% 허용 오차

        dist.log();

        for (BlockKind kind : allowed) {
            double observed = dist.counts().getOrDefault(kind, 0);
            double deviation = Math.abs(observed - expected) / expected;
            assertTrue(deviation < tolerance,
                    () -> "Normal difficulty distribution skewed for " + kind);
        }
    }

    @Test // Easy 난이도에서 I블록이 Normal 대비 약 20% 더 자주 등장하는지 검증
    void easyDifficultyBoostsIBlocksByRoughlyTwentyPercent() {
        double normalRate = sampleIRate(GameDifficulty.NORMAL);
        double easyRate = sampleIRate(GameDifficulty.EASY);
        assertTrue(easyRate > normalRate * 1.15,
                () -> String.format("Easy rate %.4f should be > normal rate %.4f * 1.15", easyRate, normalRate));
    }

    @Test // Hard 난이도에서 I블록이 Normal 대비 약 20% 덜 등장하는지 검증
    void hardDifficultyReducesIBlocksByRoughlyTwentyPercent() {
        double normalRate = sampleIRate(GameDifficulty.NORMAL);
        double hardRate = sampleIRate(GameDifficulty.HARD);
        assertTrue(hardRate < normalRate * 0.85,
                () -> String.format("Hard rate %.4f should be < normal rate %.4f * 0.85", hardRate, normalRate));
    }

    @Test // peekNext()가 nextBlock()의 결과와 동일한지 버퍼 정합성 확인
    void peekNextMatchesNextBlock() {
        RandomBlockGenerator generator = new RandomBlockGenerator(new Random(7L));
        BlockKind peeked = generator.peekNext();
        BlockKind firstNext = generator.nextBlock();
        assertEquals(peeked, firstNext);
    }

    private double sampleIRate(GameDifficulty difficulty) {
        Distribution dist = sampleDistribution(difficulty, 150_000, 99L);
        dist.log();
        return dist.ratio(BlockKind.I);
    }

    private Distribution sampleDistribution(GameDifficulty difficulty, int samples, long seed) {
        RandomBlockGenerator generator = new RandomBlockGenerator(new Random(seed));
        generator.setDifficulty(difficulty);
        Map<BlockKind, Integer> counts = new EnumMap<>(BlockKind.class);
        for (int i = 0; i < samples; i++) {
            BlockKind kind = generator.nextBlock();
            counts.merge(kind, 1, Integer::sum);
        }
        return new Distribution(counts, samples, difficulty.name());
    }

    private record Distribution(Map<BlockKind, Integer> counts, int samples, String label) {
        double ratio(BlockKind kind) {
            return counts.getOrDefault(kind, 0) / (double) samples;
        }

        void log() {
            System.out.printf("=== %s distribution (%d samples) ===%n", label, samples);
            counts.forEach((kind, count) ->
                    System.out.printf("Kind %s -> %d occurrences (%.4f)%n",
                            kind.name(), count, ratio(kind)));
        }
    }
}
