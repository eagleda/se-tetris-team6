package tetris.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tetris.domain.model.Block;
import tetris.domain.model.GameState;
import tetris.domain.Board; 
import tetris.network.protocol.AttackLine;

// 임시 더미 클래스 (실제 구현 시 교체 필요)
class Direction {}
class RotationType {}
class PlacementResult {}
class LineClearResult {}
class GameStateUpdate {}
class GameStateSnapshot {}
class GameStateDelta {}
class LockInfo {}
class PerformanceStats {}
interface GameStateChangeListener {}


/**
 * 멀티스레드 환경에서 안전한 게임 모델
 * - 게임 상태에 대한 동시 접근 제어
 * - 읽기/쓰기 락을 통한 성능 최적화
 */
public class ThreadSafeGameModel {
    // === 동기화 객체 ===
    private final ReadWriteLock gameStateLock = new ReentrantReadWriteLock();

    // === 게임 상태 (원자적 참조) ===
    // 수정 1: GameState는 Enum이므로 인스턴스화 대신 값(PLAYING)으로 초기화
    private AtomicReference<GameState> gameState = new AtomicReference<GameState>(GameState.PLAYING); 
    
    // 수정 2: Board는 기본 생성자가 있다고 가정하고 초기화 (없다면 null로 변경 필요)
    private AtomicReference<Board> gameBoard = new AtomicReference<Board>(new Board()); 
    
    // 수정 3: Block은 기본 생성자가 없으므로 null로 초기화
    private AtomicReference<Block> currentBlock = new AtomicReference<Block>(null);
    private AtomicReference<Block> nextBlock = new AtomicReference<Block>(null);

    // === 점수 및 통계 (원자적 변수) ===
    private AtomicInteger score = new AtomicInteger(0);
    private AtomicInteger linesCleared = new AtomicInteger(0);
    private AtomicInteger level = new AtomicInteger(1);
    private AtomicLong gameTime = new AtomicLong(0);

    // === 공격 큐 (동기화된 컬렉션) ===
    private final Queue<AttackLine[]> incomingAttacks = new ConcurrentLinkedQueue<>();
    private final Queue<AttackLine[]> outgoingAttacks = new ConcurrentLinkedQueue<>();

    // === 게임 설정 ===
    private volatile boolean isPaused;
    private volatile boolean isGameOver;
    private volatile String playerId;

    // 생성자 - 초기 게임 상태 설정
    public ThreadSafeGameModel(String playerId) {
        this.playerId = playerId;
        // 생성자에서 Board와 Block을 초기화하는 로직이 있다면 여기서 수행해야 합니다.
        // 예를 들어, Board도 기본 생성자가 없다면 여기서 오류가 발생할 수 있습니다.
        // 현재는 Board() 생성자가 있다고 가정하고 진행합니다.
    }

    // === 게임 상태 읽기 (읽기 락 사용) ===

    // 현재 게임 상태 반환 (방어적 복사)
    public GameState getGameState() {
        gameStateLock.readLock().lock();
        try {
            // Enum이므로 복사 대신 직접 반환해도 안전합니다.
            return gameState.get(); 
        } finally {
            gameStateLock.readLock().unlock();
        }
    }

    // 현재 보드 상태 반환 (방어적 복사)
    public Board getBoard() {
        gameStateLock.readLock().lock();
        try {
            return gameBoard.get();
        } finally {
            gameStateLock.readLock().unlock();
        }
    }

    // 현재 블록 정보 반환
    public Block getCurrentBlock() {
        return currentBlock.get(); // AtomicReference는 내부적으로 스레드 안전
    }
    
    // === 게임 상태 쓰기 (쓰기 락 사용) ===

    // 블록 이동/회전
    public boolean moveBlock(Direction direction) {
        gameStateLock.writeLock().lock();
        try {
            // 더미 로직: 쓰기 작업이 발생했음을 알림
            System.out.println("Write: moveBlock executed by " + Thread.currentThread().getName());
            // 실제로는 보드와 블록 상태를 변경하는 로직이 들어갑니다.
            return true;
        } finally {
            gameStateLock.writeLock().unlock();
        }
    }

    // 블록 배치 및 새 블록 생성
    public PlacementResult placeCurrentBlock() {
        gameStateLock.writeLock().lock();
        try {
            System.out.println("Write: placeCurrentBlock executed by " + Thread.currentThread().getName());
            // 실제 로직
            return new PlacementResult();
        } finally {
            gameStateLock.writeLock().unlock();
        }
    }

    // 점수 업데이트
    public void updateScore(int points) {
        // Atomic 변수는 락 없이도 안전하게 업데이트 가능
        score.addAndGet(points);
    }
    
    // === 공격 시스템 (동기화된 큐 사용) ===

    // 공격 받기 - 다른 플레이어로부터
    public void receiveAttack(AttackLine[] attackLines) {
        incomingAttacks.offer(attackLines); // ConcurrentLinkedQueue는 스레드 안전
    }
    
    // 게임 일시정지
    public void pauseGame() {
        isPaused = true; // volatile 변수는 간단한 읽기/쓰기에 안전
    }

}