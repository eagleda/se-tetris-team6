package tetris.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 게임 로직을 담당하는 전용 스레드
 * - 게임 상태 업데이트 (블록 이동, 회전, 줄 삭제 등)
 * - 게임 타이머 관리 (블록 자동 낙하)
 * - 플레이어 입력 처리 (키보드 이벤트)
 * - 네트워크 스레드와 동기화
 * - 멀티플레이어 게임에서 각 플레이어별로 하나씩 생성
 */
public class GameThread implements Runnable {
    // === 게임 상태 관리 ===
    private ThreadSafeGameModel gameModel;     // 스레드 안전한 게임 모델
    private AtomicBoolean isRunning;           // 스레드 실행 상태
    private AtomicBoolean isPaused;            // 게임 일시정지 상태

    // === 입력 처리 ===
    private BlockingQueue<PlayerInput> inputQueue;     // 플레이어 입력 큐
    private BlockingQueue<GameEvent> gameEventQueue;   // 게임 이벤트 큐

    // === 타이밍 관리 ===
    private long lastUpdateTime;               // 마지막 업데이트 시간
    private long gameTickInterval;             // 게임 틱 간격 (난이도별 조정)
    private long lastBlockFallTime;            // 마지막 블록 낙하 시간

    // === 플레이어 정보 ===
    private String playerId;                   // 플레이어 ID
    private boolean isLocalPlayer;             // 로컬 플레이어 여부

    // === 네트워크 통신 ===
    private GameEventListener networkListener; // 네트워크로 이벤트 전송

    // === 주요 메서드들 ===

    // 생성자 - 게임 모델과 플레이어 정보 받음
    public GameThread(ThreadSafeGameModel gameModel, String playerId, boolean isLocal);

    // 스레드 메인 실행 루프
    @Override
    public void run();

    // 게임 상태 업데이트 - 매 틱마다 호출
    private void updateGame();

    // 블록 자동 낙하 처리
    private void handleBlockFall();

    // 플레이어 입력 처리 - 입력 큐에서 가져와서 처리
    private void processPlayerInput();

    // 게임 이벤트 처리 - 줄 삭제, 공격 등
    private void processGameEvents();

    // 줄 삭제 처리 및 공격 생성
    private void handleLineClear();

    // 공격 받기 처리 - 다른 플레이어로부터 온 공격
    public void receiveAttack(AttackLine[] attackLines);

    // 게임 종료 조건 확인
    private boolean checkGameOver();

    // === 외부 인터페이스 ===

    // 플레이어 입력 추가 - UI에서 호출
    public void addPlayerInput(PlayerInput input);

    // 게임 일시정지/재개
    public void pauseGame();
    public void resumeGame();

    // 게임 종료
    public void stopGame();

    // 현재 게임 상태 반환 (읽기 전용)
    public GameState getCurrentGameState();

    // 네트워크 이벤트 리스너 등록
    public void setNetworkListener(GameEventListener listener);

    // 게임 속도 조정 (난이도 변경 시)
    public void setGameSpeed(int level);
}
