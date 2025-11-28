package tetris.multiplayer.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 최대 10줄 제한을 갖는 공격 줄 대기 버퍼.
 * - 새로운 공격이 들어오면 뒤에 붙이고, 초과분은 가장 최신 줄부터 제거한다.
 * - 이미 10줄이 가득 차 있으면 공격 전체를 무시한다.
 * - flushAll() 호출 시 FIFO 순서로 꺼내어 보드에 주입한다.
 */
final class PendingAttackBuffer {

    static final int MAX_LINES = 10;

    private final Deque<AttackLine> queue = new ArrayDeque<>();

    int size() {
        return queue.size();
    }

    void enqueue(List<AttackLine> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        if (queue.size() >= MAX_LINES) {
            return; // already full → ignore entire attack
        }
        for (AttackLine row : rows) {
            if (row != null) {
                queue.addLast(row);
            }
        }
        while (queue.size() > MAX_LINES) {
            queue.removeLast();
        }
    }

    List<AttackLine> flushAll() {
        List<AttackLine> result = new ArrayList<>(queue.size());
        while (!queue.isEmpty()) {
            result.add(queue.removeFirst());
        }
        return result;
    }

    /**
     * 현재 대기 중인 AttackLine 리스트를 복사하여 반환한다. (버퍼를 비우지 않음)
     */
    List<AttackLine> peekAll() {
        return new ArrayList<>(queue);
    }
}
