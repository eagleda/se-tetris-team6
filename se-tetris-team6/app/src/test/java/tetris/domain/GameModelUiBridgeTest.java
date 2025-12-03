package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tetris.domain.score.Score;

/*
 * 테스트 대상: tetris.domain.GameModel.UiBridge
 *
 * 역할 요약:
 * - UI 계층과 도메인 모델을 느슨하게 연결하는 최소 인터페이스.
 * - 일시정지/게임오버/이름입력 오버레이를 표시하거나 보드를 새로고침하는 콜백을 정의한다.
 *
 * 테스트 전략:
 * - 익명 구현을 만들어 각 콜백이 호출될 때 플래그가 세팅되는지 확인한다.
 * - 기본 메서드(showMultiplayerResult, showLocalMultiplayerResult)가 별도 구현 없이도 호출 가능함을 검증한다.
 *
 * - 사용 라이브러리: JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - showPauseOverlay/hidePauseOverlay/refreshBoard가 순서대로 호출되면 기록이 남는다.
 * - showGameOverOverlay와 showNameEntryOverlay가 전달한 Score 객체를 그대로 받는다.
 * - 기본 메서드 호출 시 예외가 발생하지 않는다.
 */
class GameModelUiBridgeTest {

    @Test
    void callbacksAreInvokedWithoutException() {
        Score score = Score.zero();
        RecordingBridge bridge = new RecordingBridge();

        bridge.showPauseOverlay();
        bridge.refreshBoard();
        bridge.hidePauseOverlay();
        bridge.showGameOverOverlay(score, true);
        bridge.showNameEntryOverlay(score);
        bridge.showMultiplayerResult(1, 0); // default method
        bridge.showLocalMultiplayerResult(2); // default method

        assertTrue(bridge.pauseShown);
        assertTrue(bridge.boardRefreshed);
        assertTrue(bridge.pauseHidden);
        assertEquals(score, bridge.lastScore);
        assertTrue(bridge.gameOverShown);
        assertTrue(bridge.nameEntryShown);
    }

    private static final class RecordingBridge implements GameModel.UiBridge {
        boolean pauseShown;
        boolean boardRefreshed;
        boolean pauseHidden;
        boolean gameOverShown;
        boolean nameEntryShown;
        Score lastScore;

        @Override
        public void showPauseOverlay() {
            pauseShown = true;
        }

        @Override
        public void hidePauseOverlay() {
            pauseHidden = true;
        }

        @Override
        public void refreshBoard() {
            boardRefreshed = true;
        }

        @Override
        public void showGameOverOverlay(Score score, boolean canEnterName) {
            this.lastScore = score;
            this.gameOverShown = true;
        }

        @Override
        public void showNameEntryOverlay(Score score) {
            this.lastScore = score;
            this.nameEntryShown = true;
        }
    }
}
