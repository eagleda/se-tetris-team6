package tetris.multiplayer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.multiplayer.model.PendingAttackBuffer
 *
 * 역할 요약:
 * - 최대 10줄까지 공격 줄을 누적하고, 초과분은 최신 줄부터 버린다.
 * - flushAll로 FIFO 순서로 비우거나, peekAll로 복사본을 조회한다.
 *
 * 테스트 전략:
 * - null/빈 리스트 enqueue 시 무시되는지 확인.
 * - 12줄 enqueue 시 앞의 10줄만 남고 최신 2줄이 제거되는지 확인.
 * - 버퍼가 이미 꽉 찼을 때 추가 enqueue를 무시하는지 확인.
 * - flushAll 이후 버퍼가 비워지는지 확인.
 */
class PendingAttackBufferTest {

    @Test
    void enqueue_skipsNullOrEmpty() {
        PendingAttackBuffer buffer = new PendingAttackBuffer();
        buffer.enqueue(null);
        buffer.enqueue(List.of());
        assertEquals(0, buffer.size());
    }

    @Test
    void enqueue_trimsToMaxAndKeepsOldestFirst() {
        PendingAttackBuffer buffer = new PendingAttackBuffer();
        // 12개의 서로 다른 줄 생성
        List<AttackLine> twelve = java.util.stream.IntStream.range(0, 12)
                .mapToObj(i -> new AttackLine(new boolean[] { i % 2 == 0 }))
                .toList();

        buffer.enqueue(twelve);

        assertEquals(10, buffer.size());
        List<AttackLine> peek = buffer.peekAll();
        // 최신 2개가 제거되고 앞의 10개가 남는다
        for (int i = 0; i < 10; i++) {
            assertEquals(twelve.get(i), peek.get(i));
        }
    }

    @Test
    void enqueue_ignoredWhenAlreadyFull() {
        PendingAttackBuffer buffer = new PendingAttackBuffer();
        List<AttackLine> ten = java.util.stream.IntStream.range(0, 10)
                .mapToObj(i -> new AttackLine(new boolean[] { true }))
                .toList();
        buffer.enqueue(ten);
        buffer.enqueue(List.of(new AttackLine(new boolean[] { false }))); // should be ignored

        assertEquals(10, buffer.size());
    }

    @Test
    void flushAll_returnsFifoAndClears() {
        PendingAttackBuffer buffer = new PendingAttackBuffer();
        List<AttackLine> rows = List.of(
                new AttackLine(new boolean[] { true }),
                new AttackLine(new boolean[] { false }));
        buffer.enqueue(rows);

        List<AttackLine> flushed = buffer.flushAll();

        assertEquals(rows, flushed);
        assertEquals(0, buffer.size());
        assertTrue(buffer.peekAll().isEmpty());
    }
}
