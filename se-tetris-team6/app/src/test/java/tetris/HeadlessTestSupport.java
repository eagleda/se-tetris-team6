package tetris;

import org.junit.jupiter.api.Assumptions;

import java.awt.GraphicsEnvironment;

/**
 * CI 같은 headless 환경에서 GUI 테스트를 자동으로 skip 하기 위한 유틸.
 */
public final class HeadlessTestSupport {

    private HeadlessTestSupport() {
        // util class
    }

    /**
     * 그래픽 환경이 headless이면 해당 테스트를 skip한다.
     * - 로컬 IDE: headless 아님 → 테스트 정상 실행
     * - GitHub Actions(ubuntu-latest): headless → 테스트 자동 skip
     */
    public static void skipInHeadless() {
        Assumptions.assumeFalse(
                GraphicsEnvironment.isHeadless(),
                "Headless environment - skipping GUI test"
        );
    }
}