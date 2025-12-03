package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import tetris.multiplayer.model.Cell;
import tetris.multiplayer.model.LockedPieceSnapshot;

/*
 * 테스트 대상: tetris.domain.GameModel.MultiplayerHook
 *
 * 역할 요약:
 * - 멀티플레이 규칙 엔진이 GameModel의 수명주기 이벤트에 대응하도록 연결하는 훅 인터페이스.
 * - 블록 잠금 시 스냅샷/삭제 줄 정보, 다음 스폰 전 시점 알림을 제공한다.
 *
 * 테스트 전략:
 * - 간단한 구현체를 만들어 onPieceLocked, beforeNextSpawn 호출 시 기록이 남는지 검증한다.
 * - 전달된 스냅샷/삭제 줄 배열/보드 너비 값이 그대로 수신되는지 확인한다.
 *
 * - 사용 라이브러리: JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - onPieceLocked를 호출하면 호출 횟수와 마지막 파라미터가 저장된다.
 * - beforeNextSpawn 호출 플래그가 true가 된다.
 */
class GameModelMultiplayerHookTest {

    @Test
    void hookReceivesSnapshotAndClearedRows() {
        RecordingHook hook = new RecordingHook();
        LockedPieceSnapshot snapshot = LockedPieceSnapshot.of(List.of(new Cell(1, 2), new Cell(3, 4)));
        int[] cleared = new int[] { 5, 6 };

        hook.onPieceLocked(snapshot, cleared, 10);
        hook.beforeNextSpawn();

        assertEquals(1, hook.lockedCount);
        assertEquals(snapshot, hook.lastSnapshot);
        assertEquals(2, hook.lastClearedRows.length);
        assertEquals(10, hook.lastBoardWidth);
        assertTrue(hook.beforeSpawnCalled);
    }

    private static final class RecordingHook implements GameModel.MultiplayerHook {
        int lockedCount;
        LockedPieceSnapshot lastSnapshot;
        int[] lastClearedRows;
        int lastBoardWidth;
        boolean beforeSpawnCalled;

        @Override
        public void onPieceLocked(LockedPieceSnapshot snapshot, int[] clearedRows, int boardWidth) {
            lockedCount++;
            lastSnapshot = snapshot;
            lastClearedRows = clearedRows;
            lastBoardWidth = boardWidth;
        }

        @Override
        public void beforeNextSpawn() {
            beforeSpawnCalled = true;
        }
    }
}
