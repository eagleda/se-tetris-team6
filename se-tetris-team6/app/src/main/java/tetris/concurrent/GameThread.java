package tetris.concurrent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tetris.network.GameEventListener;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;
import tetris.domain.GameModel;
import tetris.domain.Board;
import tetris.domain.engine.GameplayEngine;
import tetris.domain.model.GameState;
import tetris.domain.model.InputState;
import tetris.domain.model.Block;
import tetris.domain.score.Score;

import tetris.domain.model.GameState;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;

/**
 * 도메인 레이어와 완전 연동된 게임 로직 전용 스레드
 * 
 * 주요 특징:
 * - GameModel을 통한 모든 게임 로직 처리 (Facade 패턴)
 * - GameplayEngine.GameplayEvents 구현으로 이벤트 처리
 * - 스레드 안전성 보장
 * - 네트워크 통신 연동
 */
public class GameThread implements Runnable, GameplayEngine.GameplayEvents {

    // === 도메인 객체들 ===
        private final GameModel gameModel;
        private final Board board;
        private final InputState inputState;

    // === 스레드 안전성 ===
        private final ReadWriteLock gameStateLock = new ReentrantReadWriteLock();
        private final AtomicBoolean isRunning = new AtomicBoolean(true);
        private final AtomicBoolean isPaused = new AtomicBoolean(false);

    // === 입력 처리 ===
        private final BlockingQueue<PlayerInput> inputQueue = new LinkedBlockingQueue<>();
        private final BlockingQueue<GameEvent> gameEventQueue = new LinkedBlockingQueue<>();

    // === 타이밍 관리 ===
        private long lastUpdateTime;
        private final long gameTickInterval = 16; // 60 FPS

        // === 플레이어 정보 ===
        private final String playerId;
        private final boolean isLocalPlayer;

        // === 네트워크 통신 ===
    private GameEventListener networkListener;
    
    // === 게임 이벤트 클래스 ===
    public static class GameEvent {
        public enum Type { 
            LINE_CLEARED, GAME_OVER, SCORE_UPDATE, 
            BLOCK_SPAWNED, BLOCK_LOCKED, ATTACK_RECEIVED, BLOCK_ROTATED
        }
        
        private final Type type;
        private final Object data;
        
        public GameEvent(Type type) { this(type, null); }
        public GameEvent(Type type, Object data) {
            this.type = type;
            this.data = data;
        }
        
        public Type getType() { return type; }
        public Object getData() { return data; }
    }
    
    // === 줄 삭제 결과 클래스 ===
    public static class LineClearResult {
        private final int linesCleared;
        private final AttackLine[] attackLines;
        private final int points;
        
        public LineClearResult(int linesCleared, AttackLine[] attackLines, int points) {
            this.linesCleared = linesCleared;
            this.attackLines = attackLines;
            this.points = points;
        }
        
        public int getLinesCleared() { return linesCleared; }
        public AttackLine[] getAttackLines() { return attackLines; }
        public int getPoints() { return points; }
    }
    
    // === 생성자 ===
    public GameThread(GameModel gameModel, String playerId, boolean isLocalPlayer) {
        this.gameModel = gameModel;
        this.playerId = playerId;
        this.isLocalPlayer = isLocalPlayer;
        
        // 도메인 객체들 참조 (읽기 전용)
        this.board = gameModel.getBoard();
        this.inputState = gameModel.getInputState();

        gameModel.setSecondaryListener(this);
        
        this.lastUpdateTime = System.currentTimeMillis();
        
        System.out.println("GameThread [" + playerId + "] 도메인 레이어와 연동 완료");
    }
    
    // === 메인 실행 루프 ===
    @Override
    public void run() {
        System.out.println("GameThread [" + playerId + "] 시작됨");
        
        try {
            while (isRunning.get()) {
                long currentTime = System.currentTimeMillis();
                lastUpdateTime = currentTime;
                
                // 게임 상태 확인
                GameState currentState = getCurrentGameState();
                if (isPaused.get() || currentState == GameState.GAME_OVER) {
                    handlePausedState();
                    continue;
                }
                
                // 1. 플레이어 입력 처리
                processPlayerInput();
                
                // 2. 게임 로직 업데이트 (GameModel이 담당)
                updateGameLogic();

                // 지속 입력 초기화
                resetContinuousInputs();
                
                // 3. 게임 이벤트 처리
                processGameEvents();
                
                // 4. 틱 간격 유지
                maintainTickInterval(currentTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("GameThread [" + playerId + "] 인터럽트됨");
        } finally {
            cleanup();
        }
        
        System.out.println("GameThread [" + playerId + "] 종료됨");
    }
    
    // === GameplayEngine.GameplayEvents 구현 ===
    @Override
    public void onBlockSpawned(Block block) {
        gameEventQueue.offer(new GameEvent(GameEvent.Type.BLOCK_SPAWNED, block));
        System.out.println(playerId + ": 새 블록 생성 - " + block.getKind());
    }
    
    @Override
    public void onBlockLocked(Block block) {
        gameEventQueue.offer(new GameEvent(GameEvent.Type.BLOCK_LOCKED, block));
        System.out.println(playerId + ": 블록 고정 - " + block.getKind());
    }
    
    @Override
    public void onLinesCleared(int clearedLines) {
        if (clearedLines > 0) {
            // 공격 라인 생성
            AttackLine[] attackLines = generateAttackLines(clearedLines);
            Score currentScore = gameModel.getScore();
            
            LineClearResult result = new LineClearResult(
                clearedLines, 
                attackLines, 
                currentScore.getPoints()
            );
            
            gameEventQueue.offer(new GameEvent(GameEvent.Type.LINE_CLEARED, result));
            System.out.println(playerId + ": " + clearedLines + "줄 삭제!");

            if (isLocalPlayer && networkListener != null) {
                networkListener.sendAttackLines(attackLines);
            }
        }
    }
    
    @Override
    public void onTick(long tick) {
        // 틱 이벤트 처리 (필요시)
    }
    @Override
    public void onBlockRotated(Block block, int times) {
        // GameEvent 큐에 회전 이벤트를 추가하여 메인 루프에서 처리하도록 합니다.
        gameEventQueue.offer(new GameEvent(GameEvent.Type.BLOCK_ROTATED, block));
        System.out.println(playerId + ": 블록 회전됨 - " + block.getKind() + ", 시계방향 회전 횟수: " + times);
    }
    
    // === 입력 처리 부분 ===
    private void processPlayerInput() {
        PlayerInput input;
        int processedCount = 0;
        final int maxInputsPerTick = 10;
        
        while ((input = inputQueue.poll()) != null && processedCount < maxInputsPerTick) {
            processedCount++;
            
            gameStateLock.writeLock().lock();
            try {
                
                convertPlayerInputToInputState(input);
                
                // 로컬 플레이어의 입력이면 네트워크로 전송
                if (isLocalPlayer && networkListener != null) {
                    networkListener.sendPlayerInput(input);
                }
                
            } finally {
                gameStateLock.writeLock().unlock();
            }
        }
    }

    // ✅ 새로운 메서드: PlayerInput을 InputState로 변환
    private void convertPlayerInputToInputState(PlayerInput input) {
        switch (input.inputType()) {
            case MOVE_LEFT:
                inputState.setLeft(true);        // 지속 입력
                break;
            case MOVE_RIGHT:
                inputState.setRight(true);       // 지속 입력
                break;
            case SOFT_DROP:
                inputState.setSoftDrop(true);    // 지속 입력
                break;
            case ROTATE:
                inputState.pressRotateCW();      // 1회성 입력
                break;
            case ROTATE_CCW:
                inputState.pressRotateCCW();     // 1회성 입력
                break;
            case HARD_DROP:
                inputState.pressHardDrop();      // 1회성 입력
                break;
            case HOLD:
                inputState.pressHold();          // 1회성 입력
                break;
            case PAUSE:
                togglePause();                   // 특수 처리
                break;
        }
    }

    // ✅ 기존 updateGameLogic()은 그대로 유지
    private void updateGameLogic() {
        gameStateLock.writeLock().lock();
        try {
            // GameModel이 InputState를 읽어서 처리함
            gameModel.stepGameplay();  // 🎯 여기서 InputState 기반으로 처리!
            
            // 블록 생성 필요 시 처리
            if (gameModel.getActiveBlock() == null) {
                gameModel.spawnIfNeeded();
            }
            // 스폰 실패 또는 배치 불가 상태라면 즉시 게임 오버 처리
            if (gameModel.getActiveBlock() == null && gameModel.getCurrentState() != GameState.GAME_OVER) {
                System.out.println("[LOG][GameThread] Active block null after spawnIfNeeded → forcing onGameOver()");
                gameModel.onGameOver();
                gameEventQueue.offer(new GameEvent(GameEvent.Type.GAME_OVER));
            }
        } finally {
            gameStateLock.writeLock().unlock();
        }
    }

    // 추가: 틱 종료 시 지속 입력 초기화 (중요!)
    private void resetContinuousInputs() {
        // 지속 입력들은 매 틱마다 초기화해야 함
        inputState.setLeft(false);
        inputState.setRight(false);
        inputState.setSoftDrop(false);
        // 1회성 입력들은 GameplayEngine.stepGameplay()에서 자동으로 pop됨
    }
    
    // === 게임 이벤트 처리 ===
    private void processGameEvents() {
        GameEvent event;
        while ((event = gameEventQueue.poll()) != null) {
            switch (event.getType()) {
                case LINE_CLEARED:
                    handleLineClearEvent((LineClearResult) event.getData());
                    break;
                case GAME_OVER:
                    handleGameOverEvent();
                    break;
                case BLOCK_SPAWNED:
                    handleBlockSpawnedEvent((Block) event.getData());
                    break;
                case BLOCK_LOCKED:
                    handleBlockLockedEvent((Block) event.getData());
                    break;
                case ATTACK_RECEIVED:
                    handleAttackReceivedEvent((AttackLine[]) event.getData());
                    break;
                case BLOCK_ROTATED: // 👈 이 부분을 추가
                    handleBlockRotatedEvent((Block) event.getData());
                    break;
            }
        }
    }
    
    private void handleLineClearEvent(LineClearResult result) {
        System.out.println(playerId + ": " + result.getLinesCleared() + 
                            "줄 삭제! 점수: " + result.getPoints());
        
        // 공격 라인 전송
        if (result.getAttackLines() != null && result.getAttackLines().length > 0 
            && networkListener != null) {
            networkListener.sendAttackLines(result.getAttackLines());
            System.out.println(playerId + ": 공격 라인 " + 
                                result.getAttackLines().length + "개 전송");
        }
    }

    private void handleBlockRotatedEvent(Block block) {
        // 1. 네트워크 동기화 로직
        // 로컬 플레이어인 경우에만 회전 정보를 네트워크 리스너를 통해 전송합니다.
        if (isLocalPlayer && networkListener != null) {
            // GameEventListener에 추가된 sendBlockRotation 메서드를 호출합니다.
            // 이 호출은 Block 객체의 현재 상태(위치, 모양)를 네트워크로 전송합니다.
            networkListener.sendBlockRotation(block); 
            
            System.out.println(playerId + ": 네트워크에 블록 회전 정보 전송 완료. 블록 종류: " + block.getKind());
        }
        
        // 2. 로그 기록
        System.out.println(playerId + ": 이벤트 처리 - 블록 회전 완료. 현재 위치: (" + block.getX() + ", " + block.getY() + ")");
    }
    
    private void handleGameOverEvent() {
        System.out.println(playerId + ": 게임 오버!");
        
        // GameModel을 통해 게임 오버 처리
        // gameModel.changeState(GameState.GAME_OVER); // 이미 처리됨
        
        if (networkListener != null) {
            // 게임 오버 이벤트를 네트워크로 전송 (필요시)
        }
    }
    
    private void handleBlockSpawnedEvent(Block block) {
        // UI 업데이트 등 추가 처리 (필요시)
    }
    
    private void handleBlockLockedEvent(Block block) {
        // UI 업데이트 등 추가 처리 (필요시)
    }
    
    private void handleAttackReceivedEvent(AttackLine[] attackLines) {
        System.out.println(playerId + ": 공격 라인 " + attackLines.length + "개 받음");
    }
    
    // === 공격 라인 생성 ===
    private AttackLine[] generateAttackLines(int clearedLines) {
        // 테트리스 룰에 따른 공격 라인 생성
        int attackCount = switch (clearedLines) {
            case 1 -> 0;  // Single - 공격 없음
            case 2 -> 1;  // Double - 1줄 공격
            case 3 -> 2;  // Triple - 2줄 공격
            case 4 -> 4;  // Tetris - 4줄 공격
            default -> Math.max(0, clearedLines - 1);
        };
        
        AttackLine[] attacks = new AttackLine[attackCount];
        for (int i = 0; i < attackCount; i++) {
            attacks[i] = new AttackLine(1); // 기본 공격 강도
        }
        
        return attacks;
    }
    
    // === 공격 받기 처리 ===
    public void receiveAttack(AttackLine[] attackLines) {
        gameStateLock.writeLock().lock();
        try {
            // GameModel에 공격을 적용하는 메서드가 필요합니다. (GameModel에 구현되어 있어야 함)
            gameModel.applyAttackLines(attackLines); 
            
            // 공격 수신 이벤트를 큐에 넣어 UI 등에 알립니다.
            gameEventQueue.offer(new GameEvent(GameEvent.Type.ATTACK_RECEIVED, attackLines));
            
            System.out.println(playerId + ": 네트워크로부터 " + attackLines.length + "개의 공격 라인 수신 및 적용");
        } finally {
            gameStateLock.writeLock().unlock();
        }
    }
    
    // === 게임 상태 관리 ===
    private void handlePausedState() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(100);
    }
    
    private void togglePause() {
        if (isPaused.get()) {
            resumeGame();
        } else {
            pauseGame();
        }
    }
    
    // === 외부 인터페이스 ===
    public void addPlayerInput(PlayerInput input) {
        if (input != null) {
            inputQueue.offer(input);
        }
    }

    /**
     * Apply a player input immediately for optimistic client-side prediction.
     * This method updates the InputState and runs a single gameplay step under
     * the same lock used by the game loop so the local UI reflects the input
     * without waiting for the next tick. Use with caution.
     */
    public void applyImmediateInput(PlayerInput input) {
        if (input == null) return;
        gameStateLock.writeLock().lock();
        try {
            convertPlayerInputToInputState(input);
            // Run one step to reflect the change immediately
            try {
                gameModel.stepGameplay();
                if (gameModel.getActiveBlock() == null) {
                    gameModel.spawnIfNeeded();
                }
            } catch (Exception ex) {
                System.err.println("[GameThread] applyImmediateInput: stepGameplay failed: " + ex.getMessage());
            }
        } finally {
            gameStateLock.writeLock().unlock();
        }
    }
    
    public void pauseGame() {
        isPaused.set(true);
        gameModel.pauseGame();
        System.out.println(playerId + " 게임 일시정지");
    }
    
    public void resumeGame() {
        isPaused.set(false);
        gameModel.resumeGame();
        System.out.println(playerId + " 게임 재개");
    }
    
    public void stopGame() {
        isRunning.set(false);
        System.out.println(playerId + " 게임 종료 요청");
    }
    
    public GameState getCurrentGameState() {
        gameStateLock.readLock().lock();
        try {
            return gameModel.getCurrentState();
        } finally {
            gameStateLock.readLock().unlock();
        }
    }
    
    public Score getCurrentScore() {
        gameStateLock.readLock().lock();
        try {
            return gameModel.getScore();
        } finally {
            gameStateLock.readLock().unlock();
        }
    }
    
    public Board getBoard() {
        return board; // 읽기 전용 참조
    }
    
    public Block getActiveBlock() {
        gameStateLock.readLock().lock();
        try {
            return gameModel.getActiveBlock();
        } finally {
            gameStateLock.readLock().unlock();
        }
    }
    
    public void setNetworkListener(GameEventListener listener) {
        this.networkListener = listener;
    }
    
    public void setGameSpeed(int level) {
        // GameModel을 통해 속도 설정 (GameplayEngine.setGravityLevel 호출)
        // 현재 GameModel에 해당 메서드가 없으므로 추가 필요
        System.out.println(playerId + " 게임 속도 변경: Level " + level);
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public boolean isLocalPlayer() {
        return isLocalPlayer;
    }
    
    // === 유틸리티 메서드 ===
    private void maintainTickInterval(long currentTime) throws InterruptedException {
        long sleepTime = gameTickInterval - (System.currentTimeMillis() - currentTime);
        if (sleepTime > 0) {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        }
    }
    
    private void cleanup() {
        inputQueue.clear();
        gameEventQueue.clear();
        System.out.println("GameThread [" + playerId + "] 정리 완료");
    }
    
    // === 디버그/모니터링 ===
    public int getInputQueueSize() {
        return inputQueue.size();
    }
    
    public int getEventQueueSize() {
        return gameEventQueue.size();
    }
    
    public boolean isRunning() {
        return isRunning.get();
    }
    
    public boolean isPaused() {
        return isPaused.get();
    }
}
