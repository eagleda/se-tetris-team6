package tetris;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import tetris.HeadlessTestSupport;

/*
 * 테스트 대상: tetris.App
 *
 * 역할 요약:
 * - 엔트리 포인트로 GameModel 생성 및 TetrisFrame 초기화.
 *
 * 테스트 전략:
 * - main 실행이 예외 없이 반환하는지 확인(실제 UI는 headless 환경에서 띄우지 않을 수 있음).
 */
class AppSmokeTest {

    @Test
    void mainRunsWithoutException() {
        HeadlessTestSupport.skipInHeadless();
        assertDoesNotThrow(() -> App.main(new String[]{}));
    }
}
