package tetris.network.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.network.protocol.GameSnapshot
 *
 * 역할 요약:
 * - 플레이어 보드, 점수, 블록 위치, 공격 대기열 등 전체 게임 상태를 전달하는 직렬화 DTO.
 *
 * 테스트 전략:
 * - 생성자에 전달한 모든 값이 getter로 그대로 노출되는지 확인한다.
 * - 배열 필드는 같은 참조를 유지하는지(assertSame)와 내용이 동일한지(assertArrayEquals) 검증한다.
 */
class GameSnapshotTest {

    @Test
    void gettersReturnGivenValues() {
        int[][] board = new int[][] { {1, 0}, {0, 2} };
        boolean[][] attacks = new boolean[][] { {true, false}, {false, true} };
        int[] cleared = new int[] { 0, 1 };

        GameSnapshot snap = new GameSnapshot(
                1,          // playerId
                board,
                3,          // currentBlockId
                4,          // nextBlockId
                5000,       // score
                123,        // elapsedSeconds
                2,          // pendingGarbage
                5,          // blockX
                6,          // blockY
                1,          // blockRotation
                attacks,
                "ITEM",     // gameMode
                "weight",   // activeItemLabel
                0,          // itemCellX
                1,          // itemCellY
                cleared     // clearedLines
        );

        assertEquals(1, snap.playerId());
        assertSame(board, snap.board());
        assertEquals(3, snap.currentBlockId());
        assertEquals(4, snap.nextBlockId());
        assertEquals(5000, snap.score());
        assertEquals(123, snap.elapsedSeconds());
        assertEquals(2, snap.pendingGarbage());
        assertEquals(5, snap.blockX());
        assertEquals(6, snap.blockY());
        assertEquals(1, snap.blockRotation());
        assertSame(attacks, snap.attackLines());
        assertEquals("ITEM", snap.gameMode());
        assertEquals("weight", snap.activeItemLabel());
        assertEquals(0, snap.itemCellX());
        assertEquals(1, snap.itemCellY());
        assertSame(cleared, snap.clearedLines());

        assertArrayEquals(new int[] {0,1}, snap.clearedLines());
    }
}
