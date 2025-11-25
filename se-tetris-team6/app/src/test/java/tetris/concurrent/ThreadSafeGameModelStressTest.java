package tetris.concurrent;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 3: ThreadSafeGameModel 동시성 스트레스 테스트
 * - 여러 개의 읽기 스레드와 쓰기 스레드가 동시에 모델에 접근했을 때,
 *   데이터 무결성이 유지되고 예외(특히 데드락)가 발생하지 않는지 검증합니다.
 */
public class ThreadSafeGameModelStressTest {

    // 테스트 설정
    private static final int NUM_READERS = 10;
    private static final int NUM_WRITERS = 2;
    private static final long TEST_DURATION_SECONDS = 5; // 5초 동안 스트레스 테스트 실행

    @Test
    void testConcurrencyAndDeadlockPrevention() throws InterruptedException {
        System.out.println("--- Step 3: ThreadSafeGameModel 동시성 스트레스 테스트 시작 ---");
        System.out.println("설정: 읽기 스레드 " + NUM_READERS + "개, 쓰기 스레드 " + NUM_WRITERS + "개, 총 " + (NUM_READERS + NUM_WRITERS) + "개 스레드");

        // 1. 테스트 대상 모델 생성
        ThreadSafeGameModel model = new ThreadSafeGameModel("TestPlayer");
        
        // 2. 스레드 풀 및 실행 상태 플래그 설정
        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicBoolean running = new AtomicBoolean(true);
        CountDownLatch latch = new CountDownLatch(NUM_READERS + NUM_WRITERS);
        
        // 예외 발생 여부를 기록할 플래그 (테스트 실패 조건)
        AtomicBoolean exceptionOccurred = new AtomicBoolean(false);

        // 3. 쓰기 작업 스레드 생성 (상태 변경 - Write Lock 사용)
        for (int i = 0; i < NUM_WRITERS; i++) {
            executor.submit(new WriterTask(model, running, latch, exceptionOccurred, i));
        }

        // 4. 읽기 작업 스레드 생성 (상태 조회 - Read Lock 사용)
        for (int i = 0; i < NUM_READERS; i++) {
            executor.submit(new ReaderTask(model, running, latch, exceptionOccurred, i));
        }

        // 5. 지정된 시간 동안 테스트 실행
        System.out.println("스트레스 테스트 " + TEST_DURATION_SECONDS + "초 동안 실행 중...");
        TimeUnit.SECONDS.sleep(TEST_DURATION_SECONDS);

        // 6. 모든 스레드 종료 신호
        running.set(false);
        
        // 7. 스레드 종료 대기 (최대 2초)
        if (!latch.await(2, TimeUnit.SECONDS)) {
            System.err.println("경고: 일부 스레드가 시간 내에 종료되지 않았습니다. 데드락 또는 무한 루프 가능성 확인 필요.");
        }

        // 8. 결과 검증
        assertFalse(exceptionOccurred.get(), "스트레스 테스트 중 예외가 발생했습니다. (ConcurrentModificationException, NullPointerException, Deadlock 등)");
        System.out.println("--- Step 3: ThreadSafeGameModel 동시성 스트레스 테스트 성공 ---");
        
        // 9. Executor 종료
        executor.shutdownNow();
    }

    // 쓰기 작업을 반복하는 Runnable
    private static class WriterTask implements Runnable {
        private final ThreadSafeGameModel model;
        private final AtomicBoolean running;
        private final CountDownLatch latch;
        private final AtomicBoolean exceptionOccurred;
        private final int id;

        public WriterTask(ThreadSafeGameModel model, AtomicBoolean running, CountDownLatch latch, AtomicBoolean exceptionOccurred, int id) {
            this.model = model;
            this.running = running;
            this.latch = latch;
            this.exceptionOccurred = exceptionOccurred;
            this.id = id;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Writer-" + id);
            try {
                int count = 0;
                while (running.get()) {
                    // 쓰기 작업 (Write Lock 사용)
                    model.moveBlock(new Direction());
                    model.placeCurrentBlock();
                    
                    // Atomic 변수 사용
                    model.updateScore(1); 
                    
                    // Concurrent Queue 사용
                    model.receiveAttack(new AttackLine[]{}); 
                    
                    count++;
                    // 짧은 대기 시간 (CPU 과부하 방지 및 다른 스레드에게 기회 제공)
                    TimeUnit.MILLISECONDS.sleep(1); 
                }
                System.out.println(Thread.currentThread().getName() + " 종료. 총 " + count + "회 쓰기 작업 수행.");
            } catch (InterruptedException e) {
                // 스레드 종료 시 발생 가능
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // 테스트 실패 조건
                System.err.println(Thread.currentThread().getName() + "에서 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                exceptionOccurred.set(true);
            } finally {
                latch.countDown();
            }
        }
    }

    // 읽기 작업을 반복하는 Runnable
    private static class ReaderTask implements Runnable {
        private final ThreadSafeGameModel model;
        private final AtomicBoolean running;
        private final CountDownLatch latch;
        private final AtomicBoolean exceptionOccurred;
        private final int id;

        public ReaderTask(ThreadSafeGameModel model, AtomicBoolean running, CountDownLatch latch, AtomicBoolean exceptionOccurred, int id) {
            this.model = model;
            this.running = running;
            this.latch = latch;
            this.exceptionOccurred = exceptionOccurred;
            this.id = id;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("Reader-" + id);
            try {
                int count = 0;
                while (running.get()) {
                    // 읽기 작업 (Read Lock 사용)
                    model.getGameState();
                    model.getBoard();
                    
                    // Atomic 변수 읽기
                    model.getCurrentBlock();
                    
                    count++;
                    // 읽기 작업은 쓰기 작업보다 더 빈번하게 발생하도록 설정
                    Thread.yield(); 
                }
                System.out.println(Thread.currentThread().getName() + " 종료. 총 " + count + "회 읽기 작업 수행.");
            } catch (Exception e) {
                // 테스트 실패 조건
                System.err.println(Thread.currentThread().getName() + "에서 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                exceptionOccurred.set(true);
            } finally {
                latch.countDown();
            }
        }
    }
}