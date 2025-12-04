package tetris;

import java.awt.GraphicsEnvironment;
import org.junit.jupiter.api.Assumptions;

/**
 * Utility to skip GUI-dependent tests when running in a headless environment (e.g., CI).
 */
public final class HeadlessTestSupport {
    private HeadlessTestSupport() {}

    public static void skipInHeadless() {
        Assumptions.assumeFalse(
                GraphicsEnvironment.isHeadless(),
                "Headless 환경(CI)에서는 TetrisFrame GUI 테스트를 건너뜀"
        );
    }
}
