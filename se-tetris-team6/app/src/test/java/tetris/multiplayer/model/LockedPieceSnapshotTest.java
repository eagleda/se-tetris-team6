package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.multiplayer.model.LockedPieceSnapshot
 *
 * 역할 요약:
 * - 최근 잠긴 블록의 셀 좌표 리스트를 불변 형태로 보관한다.
 *
 * 테스트 전략:
 * - 빈 리스트 제공 시 isEmpty가 true인 스냅샷을 생성한다.
 * - 리스트가 복사/불변으로 유지되어 외부 변경이 내부에 영향 주지 않는지 검증한다.
 */
class LockedPieceSnapshotTest {

    @Test
    void emptyList_resultsInEmptySnapshot() {
        LockedPieceSnapshot snap = LockedPieceSnapshot.of(List.of());
        assertTrue(snap.isEmpty());
        assertTrue(snap.cells().isEmpty());
    }

    @Test
    void cellsAreCopiedAndUnmodifiable() {
        List<Cell> cells = new ArrayList<>();
        cells.add(new Cell(1, 2));
        LockedPieceSnapshot snap = LockedPieceSnapshot.of(cells);

        assertFalse(snap.isEmpty());
        assertEquals(1, snap.cells().size());
        assertEquals(new Cell(1, 2), snap.cells().get(0));

        // 원본 변경해도 스냅샷은 불변
        cells.add(new Cell(3, 4));
        assertEquals(1, snap.cells().size());
        assertThrows(UnsupportedOperationException.class, () -> snap.cells().add(new Cell(5, 6)));
    }
}
