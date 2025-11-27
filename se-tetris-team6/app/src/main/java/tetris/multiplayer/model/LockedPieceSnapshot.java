package tetris.multiplayer.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 최근에 잠긴 블록을 {@link Cell} 좌표 리스트로 캡처한 스냅샷.
 * - 블록 종류/회전값 대신 셀 좌표만 보관해 규칙 엔진을 도메인-중립으로 유지.
 * - 공격 규칙이 구멍 패턴을 계산할 때 참조한다.
 */
public final class LockedPieceSnapshot {

    private final List<Cell> cells;

    private LockedPieceSnapshot(List<Cell> cells) {
        this.cells = List.copyOf(Objects.requireNonNull(cells, "cells"));
    }

    public static LockedPieceSnapshot of(List<Cell> cells) {
        return new LockedPieceSnapshot(cells);
    }

    public List<Cell> cells() {
        return Collections.unmodifiableList(cells);
    }

    public boolean isEmpty() {
        return cells.isEmpty();
    }
}
