package tetris.domain;

import org.junit.jupiter.api.Test;
import tetris.domain.score.Score;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    void immutability_and_withMethods() {
        Score s = Score.of(10, 1, 2);
        Score s2 = s.withAdditionalPoints(5);
        assertEquals(15, s2.getPoints());
        Score s3 = s2.withClearedLinesAdded(3);
        assertEquals(5, s3.getClearedLines());
        Score s4 = s3.withLevel(3);
        assertEquals(3, s4.getLevel());
    }

    @Test
    void withAdditionalPoints_negative_noChange() {
        Score s = Score.zero();
        Score s2 = s.withAdditionalPoints(-5);
        assertSame(s, s2);
    }
}
