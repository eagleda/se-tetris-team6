package tetris.concurrent;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import tetris.network.GameEventListener;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;
// ============================

// =================================================================
// 임시 더미 클래스/인터페이스 (실제 프로젝트 구조에 맞게 교체 필요)
// GameThread에서 사용하는 더미 클래스 중 프로토콜이 아닌 것들만 남김
// =================================================================
public class Direction {
    public static final Direction LEFT = new Direction();
    public static final Direction RIGHT = new Direction();
    public static final Direction DOWN = new Direction();
}
class RotationType {}
class GameState {
    public static final GameState PLAYING = new GameState();
    public static final GameState GAME_OVER = new GameState();
}
class LineClearResult {
    private final int linesCleared;
    private final AttackLine[] attackLines;
    public LineClearResult(int lines, AttackLine[] attacks) {
        this.linesCleared = lines;
        this.attackLines = attacks;
    }
    public int getLinesCleared() { return linesCleared; }
    public AttackLine[] getAttackLines() { return attackLines; }
}
// PlayerInput 더미 클래스 제거됨 (import로 대체)
class GameEvent {
    public enum Type { LINE_CLEARED, GAME_OVER, SCORE_UPDATE }
    private final Type type;
    public GameEvent(Type type) { this.type = type; }
    
    public Type getType() {
        return type;
    }
}
// ThreadSafeGameModel의 가상 인터페이스 (실제 구현 시 교체 필요)
class ThreadSafeGameModel {
    private GameState state = GameState.PLAYING;

    // 쓰기 락을 사용한 블록 이동/회전
    public boolean moveBlock(Direction direction) { /* ... */ return true; }
    public boolean rotateBlock(RotationType type) { /* ... */ return true; }
    public LineClearResult tryPlaceBlock() {
        // 블록을 보드에 확정하고 줄 삭제를 시도
        if (Math.random() < 0.1) {
            // AttackLine은 이제 tetris.network.protocol.AttackLine을 사용
            AttackLine[] attacks = {new AttackLine(1)}; // 더미 공격 라인 생성
            return new LineClearResult(3, attacks);
        }
        return new LineClearResult(0, null);
    }
    // 읽기 락을 사용한 상태 반환
    public GameState getGameState() { return state; }
    // 쓰기 락을 사용한 공격 적용
    public void applyAttack(AttackLine[] attackLines) {
        System.out.println(playerId + "가 공격 라인 " + attackLines.length + "개를 받았습니다.");
    }
    // 게임 오버 조건 설정
    public void setGameOver() { this.state = GameState.GAME_OVER; }
    private String playerId;
    public ThreadSafeGameModel(String playerId) { this.playerId = playerId; }
}
// =================================================================
// GameThread 구현 시작
// =================================================================

/**
 * 게임 로직을 담당하는 전용 스레드
 * ...
 */
public class GameThread implements Runnable {
    // === 게임 상태 관리 ===
    private final ThreadSafeGameModel gameModel;     // 스레드 안전한 게임 모델
    private final AtomicBoolean isRunning = new AtomicBoolean(true); // 스레드 실행 상태
    private final AtomicBoolean isPaused = new AtomicBoolean(false); // 게임 일시정지 상태

    // === 입력 처리 ===
    // UI 입력 및 네트워크에서 받은 입력(상대방 입력)을 처리
    // PlayerInput은 이제 tetris.network.protocol.PlayerInput을 사용
    private final BlockingQueue<PlayerInput> inputQueue = new LinkedBlockingQueue<>();
    // 게임 내부에서 발생한 이벤트(줄 삭제, 게임 오버 등)를 처리
    private final BlockingQueue<GameEvent> gameEventQueue = new LinkedBlockingQueue<>();

    // === 타이밍 관리 ===
    private long lastUpdateTime;               // 마지막 업데이트 시간
    private long gameTickInterval = 16;        // 게임 틱 간격 (약 60 FPS)
    private long lastBlockFallTime;            // 마지막 블록 낙하 시간
    private long blockFallInterval = 1000;     // 블록 자동 낙하 간격 (1초)

    // === 플레이어 정보 ===
    private final String playerId;                   // 플레이어 ID
    private final boolean isLocalPlayer;             // 로컬 플레이어 여부

    // === 네트워크 통신 ===
    private GameEventListener networkListener; // 네트워크로 이벤트 전송

    // === 주요 메서드들 ===

    // 생성자 - 게임 모델과 플레이어 정보 받음
    public GameThread(ThreadSafeGameModel client1Model, String playerId, boolean isLocal) {
        this.gameModel = client1Model;
        this.playerId = playerId;
        this.isLocalPlayer = isLocal;
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastBlockFallTime = System.currentTimeMillis();
    }

    // 스레드 메인 실행 루프
    @Override
    public void run() {
        System.out.println("GameThread [" + playerId + "] 시작됨.");
        while (isRunning.get()) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastUpdateTime;
            lastUpdateTime = currentTime;

            if (isPaused.get() || gameModel.getGameState() == GameState.GAME_OVER) {
                // 일시정지 또는 게임 오버 상태에서는 대기
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            // 1. 플레이어 입력 처리
            processPlayerInput();

            // 2. 게임 상태 업데이트 (자동 낙하 및 블록 배치)
            updateGame();

            // 3. 게임 이벤트 처리 (줄 삭제, 공격 전송 등)
            processGameEvents();

            // 4. 틱 간격 유지
            long sleepTime = gameTickInterval - (System.currentTimeMillis() - currentTime);
            if (sleepTime > 0) {
                try {
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    isRunning.set(false);
                }
            }
        }
        System.out.println("GameThread [" + playerId + "] 종료됨.");
    }

    // 게임 상태 업데이트 - 매 틱마다 호출
    private void updateGame() {
        // 게임 오버 조건 확인
        if (checkGameOver()) {
            gameModel.setGameOver();
            gameEventQueue.offer(new GameEvent(GameEvent.Type.GAME_OVER));
            return;
        }

        // 블록 자동 낙하 처리
        handleBlockFall();
    }

    // 블록 자동 낙하 처리
    private void handleBlockFall() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlockFallTime >= blockFallInterval) {
            lastBlockFallTime = currentTime;

            // 블록을 한 칸 아래로 이동 시도
            boolean moved = gameModel.moveBlock(Direction.DOWN);

            if (!moved) {
                // 더 이상 내려갈 수 없으면 블록 배치 확정 및 줄 삭제 처리
                LineClearResult result = gameModel.tryPlaceBlock();
                if (result.getLinesCleared() > 0) {
                    gameEventQueue.offer(new GameEvent(GameEvent.Type.LINE_CLEARED));
                    // 줄 삭제 결과에 따라 공격 라인 전송
                    handleLineClear(result);
                }
                // TODO: 다음 블록 생성 로직 호출 필요
            }
        }
    }

    // 플레이어 입력 처리 - 입력 큐에서 가져와서 처리
    private void processPlayerInput() {
        // 큐에 있는 모든 입력을 처리 (최대 처리 시간 제한을 두는 것이 좋음)
        PlayerInput input;
        while ((input = inputQueue.poll()) != null) {
            // 1. 로컬 게임 로직 처리
            // PlayerInput.InputType은 PlayerInput 레코드의 필드이므로 input.inputType()을 사용
            switch (input.inputType()) { 
                case MOVE_LEFT:
                    gameModel.moveBlock(Direction.LEFT);
                    break;
                case MOVE_RIGHT:
                    gameModel.moveBlock(Direction.RIGHT);
                    break;
                case ROTATE:
                    gameModel.rotateBlock(new RotationType());
                    break;
                case HARD_DROP:
                    // TODO: 하드 드롭 로직 구현 (블록을 즉시 바닥으로 이동)
                    LineClearResult result = gameModel.tryPlaceBlock();
                    if (result.getLinesCleared() > 0) {
                        gameEventQueue.offer(new GameEvent(GameEvent.Type.LINE_CLEARED));
                        handleLineClear(result);
                    }
                    // TODO: 다음 블록 생성 로직 호출 필요
                    break;
                case PAUSE:
                    if (isPaused.get()) {
                        resumeGame();
                    } else {
                        pauseGame();
                    }
                    break;
                default:
                    // 알 수 없는 입력
                    break;
            }
            
            // 2. (추가) 로컬 플레이어의 입력이라면 네트워크로 전송
            // 상대방의 입력은 NetworkManager에서 처리되어 이미 inputQueue에 들어왔으므로,
            // 여기서 다시 전송하면 무한 루프가 발생할 수 있음.
            // 따라서, 이 로직은 UI에서 직접 addPlayerInput을 호출할 때만 실행되어야 하거나,
            // PlayerInput 객체에 isLocal 필드를 추가하여 구분해야 함.
            // 현재는 P2P 동기화 방식(입력을 상대방에게 전송하여 상대방 보드에 적용)을 따른다고 가정하고,
            // 로컬 플레이어의 입력만 전송합니다.
            if (isLocalPlayer && networkListener != null) {
                // GameEventListener에 sendPlayerInput이 추가되었으므로 호출 가능
                networkListener.sendPlayerInput(input);
            }
        }
    }

    // 게임 이벤트 처리 - 줄 삭제, 공격 등
    private void processGameEvents() {
        GameEvent event;
        while ((event = gameEventQueue.poll()) != null) {
            switch (event.getType()) {
                case LINE_CLEARED:
                    // UI에 줄 삭제 애니메이션 요청 등
                    System.out.println(playerId + ": 줄 삭제 발생!");
                    break;
                case GAME_OVER:
                    System.out.println(playerId + ": 게임 오버!");
                    // UI에 게임 오버 메시지 표시 요청
                    break;
                case SCORE_UPDATE:
                    // UI에 점수 업데이트 요청
                    break;
            }
        }
    }

    // 줄 삭제 처리 및 공격 생성 (블록 배치 확정 시 호출됨)
    private void handleLineClear(LineClearResult result) {
        if (result.getLinesCleared() > 0 && networkListener != null) {
            AttackLine[] attacks = result.getAttackLines();
            if (attacks != null && attacks.length > 0) {
                // 공격 라인을 네트워크 리스너(NetworkManager)를 통해 상대방에게 전송
                networkListener.sendAttackLines(attacks);
                System.out.println(playerId + ": 상대방에게 공격 라인 " + attacks.length + "개 전송 요청.");
            }
        }
    }

    // 공격 받기 처리 - 다른 플레이어로부터 온 공격 (NetworkManager에서 호출)
    public void receiveAttack(AttackLine[] attackLines) {
        if (attackLines != null && attackLines.length > 0) {
            // ThreadSafeGameModel에 공격 적용 요청
            gameModel.applyAttack(attackLines);
        }
    }

    // 게임 종료 조건 확인
    private boolean checkGameOver() {
        // TODO: ThreadSafeGameModel에서 게임 오버 조건(블록이 쌓여서 천장에 닿았는지) 확인
        // 임시로 5% 확률로 게임 오버 시뮬레이션
        if (Math.random() < 0.0001) {
            return true;
        }
        return false;
    }

    // === 외부 인터페이스 ===

    // 플레이어 입력 추가 - UI에서 호출
    public void addPlayerInput(PlayerInput input) {
        inputQueue.offer(input);
    }

    // 게임 일시정지/재개
    public void pauseGame() {
        isPaused.set(true);
        System.out.println(playerId + " 게임 일시정지.");
    }

    public void resumeGame() {
        isPaused.set(false);
        // 일시정지 해제 시 타이머 초기화 (블록이 바로 떨어지는 것을 방지)
        lastBlockFallTime = System.currentTimeMillis();
        System.out.println(playerId + " 게임 재개.");
    }

    // 게임 종료
    public void stopGame() {
        isRunning.set(false);
    }

    // 현재 게임 상태 반환 (읽기 전용)
    public GameState getCurrentGameState() {
        return gameModel.getGameState();
    }

    // 네트워크 이벤트 리스너 등록
    public void setNetworkListener(GameEventListener listener) {
        this.networkListener = listener;
    }

    // 게임 속도 조정 (난이도 변경 시)
    public void setGameSpeed(int level) {
        // 레벨에 따라 블록 낙하 간격 조정 (예: 1000ms - (level * 50ms))
        this.blockFallInterval = Math.max(100, 1000 - (level * 50));
        System.out.println(playerId + " 게임 속도 변경: Level " + level + ", Fall Interval: " + blockFallInterval + "ms");
    }
}