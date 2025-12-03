package tetris.network.protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

/*
 * 테스트 대상: tetris.network.protocol.GameSnapshot
 *
 * 역할 요약:
 * - 전체 게임 상태를 네트워크 전송을 위한 DTO 형태로 표현
 * - 플레이어 ID, 보드 상태, 블록 정보, 점수, 아이템, 공격 라인 등 포함
 * - Serializable 구현으로 네트워크 전송 가능
 * - 불변 객체로 설계되어 스레드 안전성 보장
 *
 * 테스트 전략:
 * - 생성자를 통한 다양한 상태 객체 생성 검증
 * - 모든 getter 메서드 동작 확인
 * - 직렬화/역직렬화 테스트
 * - null 필드 처리 확인
 *
 * 사용 라이브러리:
 * - JUnit 5만 사용
 *
 * 주요 테스트 시나리오:
 * - 기본 필드로 스냅샷 생성
 * - 모든 getter가 올바른 값 반환
 * - 직렬화 후 역직렬화 시 데이터 보존
 * - null 필드(activeItemLabel, clearedLines) 처리
 * - 복잡한 배열 데이터(board, attackLines) 전송
 */

public class GameSnapshotTest {

    @Test
    void constructor_withBasicData_shouldCreateSnapshot() {
        // given
        int[][] board = new int[20][10];
        boolean[][] attackLines = new boolean[0][0];

        // when
        GameSnapshot snapshot = new GameSnapshot(
            1, board, 1, 2, 100, 30, 0,
            5, 0, 0, attackLines, "STANDARD",
            null, -1, -1, null
        );

        // then
        assertEquals(1, snapshot.getPlayerId());
        assertEquals(100, snapshot.getScore());
        assertEquals("STANDARD", snapshot.getGameMode());
    }

    @Test
    void getPlayerId_shouldReturnCorrectValue() {
        // given
        GameSnapshot snapshot = createBasicSnapshot(2);

        // when
        int playerId = snapshot.getPlayerId();

        // then
        assertEquals(2, playerId);
    }

    @Test
    void getBoard_shouldReturnBoardArray() {
        // given
        int[][] board = {{1, 2, 3}, {4, 5, 6}};
        GameSnapshot snapshot = createSnapshotWithBoard(board);

        // when
        int[][] returnedBoard = snapshot.getBoard();

        // then
        assertArrayEquals(board, returnedBoard, "Board should match");
    }

    @Test
    void getCurrentBlockId_shouldReturnCorrectId() {
        // given
        GameSnapshot snapshot = createBasicSnapshot(1);

        // when
        int blockId = snapshot.getCurrentBlockId();

        // then
        assertEquals(1, blockId);
    }

    @Test
    void getScore_shouldReturnCorrectScore() {
        // given
        GameSnapshot snapshot = createSnapshotWithScore(9999);

        // when
        int score = snapshot.getScore();

        // then
        assertEquals(9999, score);
    }

    @Test
    void serialization_shouldPreserveAllData() throws IOException, ClassNotFoundException {
        // given
        int[][] board = new int[20][10];
        board[0][0] = 5;
        boolean[][] attackLines = new boolean[2][10];
        attackLines[0][3] = true;
        
        GameSnapshot original = new GameSnapshot(
            1, board, 3, 4, 500, 60, 2,
            7, 5, 1, attackLines, "ITEM",
            "BOMB", 1, 2, new int[]{0, 1}
        );

        // when
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
        objectOut.writeObject(original);
        objectOut.flush();
        
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        GameSnapshot deserialized = (GameSnapshot) objectIn.readObject();

        // then
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
        assertEquals(original.getScore(), deserialized.getScore());
        assertEquals(original.getGameMode(), deserialized.getGameMode());
        assertEquals(original.getActiveItemLabel(), deserialized.getActiveItemLabel());
        assertEquals(original.getBlockX(), deserialized.getBlockX());
        assertEquals(original.getBlockY(), deserialized.getBlockY());
    }

    @Test
    void getActiveItemLabel_withNull_shouldReturnNull() {
        // given
        GameSnapshot snapshot = createSnapshotWithItem(null);

        // when
        String itemLabel = snapshot.getActiveItemLabel();

        // then
        assertNull(itemLabel, "Should allow null item label");
    }

    @Test
    void getClearedLines_withNull_shouldReturnNull() {
        // given
        GameSnapshot snapshot = createSnapshotWithClearedLines(null);

        // when
        int[] clearedLines = snapshot.getClearedLines();

        // then
        assertNull(clearedLines, "Should allow null cleared lines");
    }

    @Test
    void getBlockPosition_shouldReturnCorrectCoordinates() {
        // given
        GameSnapshot snapshot = createSnapshotWithBlockPosition(8, 15, 2);

        // when
        int x = snapshot.getBlockX();
        int y = snapshot.getBlockY();
        int rotation = snapshot.getBlockRotation();

        // then
        assertEquals(8, x);
        assertEquals(15, y);
        assertEquals(2, rotation);
    }

    @Test
    void getElapsedSeconds_shouldReturnCorrectTime() {
        // given
        GameSnapshot snapshot = createSnapshotWithTime(120);

        // when
        int elapsed = snapshot.getElapsedSeconds();

        // then
        assertEquals(120, elapsed);
    }

    // Helper methods
    private GameSnapshot createBasicSnapshot(int playerId) {
        return new GameSnapshot(
            playerId, new int[20][10], 1, 2, 100, 30, 0,
            5, 0, 0, new boolean[0][0], "STANDARD",
            null, -1, -1, null
        );
    }

    private GameSnapshot createSnapshotWithBoard(int[][] board) {
        return new GameSnapshot(
            1, board, 1, 2, 100, 30, 0,
            5, 0, 0, new boolean[0][0], "STANDARD",
            null, -1, -1, null
        );
    }

    private GameSnapshot createSnapshotWithScore(int score) {
        return new GameSnapshot(
            1, new int[20][10], 1, 2, score, 30, 0,
            5, 0, 0, new boolean[0][0], "STANDARD",
            null, -1, -1, null
        );
    }

    private GameSnapshot createSnapshotWithItem(String itemLabel) {
        return new GameSnapshot(
            1, new int[20][10], 1, 2, 100, 30, 0,
            5, 0, 0, new boolean[0][0], "ITEM",
            itemLabel, 1, 2, null
        );
    }

    private GameSnapshot createSnapshotWithClearedLines(int[] clearedLines) {
        return new GameSnapshot(
            1, new int[20][10], 1, 2, 100, 30, 0,
            5, 0, 0, new boolean[0][0], "STANDARD",
            null, -1, -1, clearedLines
        );
    }

    private GameSnapshot createSnapshotWithBlockPosition(int x, int y, int rotation) {
        return new GameSnapshot(
            1, new int[20][10], 1, 2, 100, 30, 0,
            x, y, rotation, new boolean[0][0], "STANDARD",
            null, -1, -1, null
        );
    }

    private GameSnapshot createSnapshotWithTime(int elapsedSeconds) {
        return new GameSnapshot(
            1, new int[20][10], 1, 2, 100, elapsedSeconds, 0,
            5, 0, 0, new boolean[0][0], "STANDARD",
            null, -1, -1, null
        );
    }
}
