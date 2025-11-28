package tetris.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tetris.domain.model.GameState;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.protocol.PlayerInput;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * P2P 대전 모드의 서버 역할을 담당
 * - 클라이언트 연결 대기 및 수락
 * - 연결된 클라이언트들 관리
 * - 게임 상태 동기화 및 메시지 중계
 * - 서버 생명주기 관리 (시작, 정지, 재시작)
 */
public class GameServer {
    // === 네트워크 관련 ===
    private ServerSocket serverSocket;          // 서버 소켓
    private boolean isRunning;                  // 서버 실행 상태
    private int port;                          // 서버 포트

    // === 클라이언트 관리 ===
    private List<ServerHandler> connectedClients = new CopyOnWriteArrayList<>();  // 연결된 클라이언트 목록
    private ExecutorService clientThreadPool = Executors.newCachedThreadPool();      // 클라이언트 처리용 스레드 풀

    // === 게임 관리 ===
    private GameState currentGameState;         // 현재 게임 상태
    private String selectedGameMode;            // 선택된 게임 모드
    private boolean gameInProgress;             // 게임 진행 중 여부
    private tetris.multiplayer.controller.MultiPlayerController gameController;  // 서버 측 게임 컨트롤러
    private tetris.multiplayer.model.MultiPlayerGame multiplayerGame;            // 멀티플레이어 게임 인스턴스
    private java.util.Timer gameTickTimer;      // 게임 틱 타이머

    // === 주요 메서드들 ===

    // 서버 시작 - 지정된 포트에서 클라이언트 연결 대기
    public void startServer(int port) throws IOException {
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.isRunning = true;
        System.out.println("Server started on port " + port);

        // 클라이언트 연결 수락을 위한 별도 스레드 시작
        new Thread(() -> {
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    acceptClient(clientSocket);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    // Return number of connected clients
    public int getConnectedCount() {
        return connectedClients.size();
    }

    // Mark host ready (host pressed Start)
    private volatile boolean hostReady = false;
    private final java.util.Map<ServerHandler, Boolean> clientReady = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile boolean started = false;

    public void setHostReady(boolean ready) {
        this.hostReady = ready;
        checkAndStartIfReady();
    }

    // Called by ServerHandler when client signals ready
    public void setClientReady(ServerHandler handler, boolean ready) {
        if (handler == null) return;
        clientReady.put(handler, ready);
        checkAndStartIfReady();
    }

    private void checkAndStartIfReady() {
        // For simple 1v1: if hostReady and at least one client ready -> start
        boolean anyClientReady = clientReady.values().stream().anyMatch(Boolean::booleanValue);
        if (hostReady && anyClientReady && !started) {
            System.out.println("All players ready. Broadcasting GAME_START");
            // broadcast selected mode (could be null)
            broadcastMessage(new tetris.network.protocol.GameMessage(tetris.network.protocol.MessageType.GAME_START, "SERVER", selectedGameMode));
            started = true;
        }
    }

    public boolean isStarted() { return started; }


    // 서버 중지 - 모든 연결 종료 및 리소스 정리
    public void stopServer() {
        this.isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("GameServer stopped.");
            }
            clientThreadPool.shutdownNow();
            // 모든 connectedClients에게 DISCONNECT 메시지 전송 및 연결 종료 로직 추가 예정
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    // 클라이언트 연결 수락 - 새로운 클라이언트가 접속했을 때
    private void acceptClient(Socket clientSocket) {
        System.out.println("New client connected: " + clientSocket.getInetAddress());
        ServerHandler handler = new ServerHandler(clientSocket, this);
        // connectedClients.add(handler); 핸드셰이크 성공 후 추가하는 것이 더 안전
        clientThreadPool.submit(handler); // 핸들러를 스레드 풀에서 실행
    }


    /**
     * ServerHandler가 연결 성공을 알릴 때 호출됩니다.
     * 
     */
    public void notifyClientConnected(ServerHandler handler) {
        connectedClients.add(handler);
        System.out.println("Client connected successfully. Total clients: " + connectedClients.size());
    }

    // 클라이언트 연결 해제 처리
    public void removeClient(ServerHandler client) {
        connectedClients.remove(client);
        System.out.println("Client disconnected. Current clients: " + connectedClients.size());
    }

     // 모든 클라이언트에게 메시지 브로드캐스트
    public void broadcastMessage(GameMessage message) {
        for (ServerHandler handler : connectedClients) {
            handler.sendMessage(message);
        }
    }

    // 특정 클라이언트에게만 메시지 전송
    public void sendToClient(String clientId, GameMessage message){
        /* Step 3 구현 예정 */ }

    // 게임 모드 선택 (서버가 결정)
    public void selectGameMode(String mode){
        /* Step 4 구현 예정 */ }

    public void setSelectedGameMode(String mode) {
        this.selectedGameMode = mode;
    }

    public String getSelectedGameMode() { return this.selectedGameMode; }

    // 게임 시작 - 모든 클라이언트가 준비되었을 때
    public void startGame(){
        if (gameInProgress) return;
        gameInProgress = true;
        // 게임 틱 타이머 시작 (60 FPS)
        if (gameTickTimer != null) gameTickTimer.cancel();
        gameTickTimer = new java.util.Timer("GameServer-Tick", true);
        gameTickTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (gameController != null) {
                    gameController.tick();
                    synchronizeGameState();
                }
            }
        }, 0, 16); // ~60 FPS
    }

    // 게임 상태 동기화 - 주기적으로 호출
    private void synchronizeGameState(){
        if (multiplayerGame == null) return;
        try {
            // Player 1 (서버 호스트) 보드 상태 브로드캐스트
            tetris.domain.GameModel p1Model = multiplayerGame.modelOf(1);
            BoardStateData p1State = new BoardStateData(
                1,
                p1Model.getBoard(),
                null, // 현재 피스는 추후 구현
                p1Model.getScore(),
                p1Model.getCurrentState()
            );
            broadcastMessage(new GameMessage(MessageType.BOARD_STATE, "SERVER", p1State));
            
            // Player 2 (클라이언트) 보드 상태 브로드캐스트
            tetris.domain.GameModel p2Model = multiplayerGame.modelOf(2);
            BoardStateData p2State = new BoardStateData(
                2,
                p2Model.getBoard(),
                null, // 현재 피스는 추후 구현
                p2Model.getScore(),
                p2Model.getCurrentState()
            );
            broadcastMessage(new GameMessage(MessageType.BOARD_STATE, "SERVER", p2State));
        } catch (Exception e) {
            System.err.println("Error synchronizing game state: " + e.getMessage());
        }
    }

    // 게임 컨트롤러 설정
    public void setGameController(tetris.multiplayer.controller.MultiPlayerController controller) {
        this.gameController = controller;
    }

    public void setMultiplayerGame(tetris.multiplayer.model.MultiPlayerGame game) {
        this.multiplayerGame = game;
    }

    // 플레이어 입력 처리 (클라이언트로부터)
    public void handlePlayerInput(int playerId, PlayerInput input) {
        if (gameController == null || multiplayerGame == null) return;
        gameController.withPlayer(playerId, model -> {
            if (input.inputType() == tetris.network.protocol.InputType.MOVE_LEFT) {
                model.moveBlockLeft();
            } else if (input.inputType() == tetris.network.protocol.InputType.MOVE_RIGHT) {
                model.moveBlockRight();
            } else if (input.inputType() == tetris.network.protocol.InputType.SOFT_DROP) {
                model.moveBlockDown();
            } else if (input.inputType() == tetris.network.protocol.InputType.ROTATE) {
                model.rotateBlockClockwise();
            } else if (input.inputType() == tetris.network.protocol.InputType.ROTATE_CCW) {
                model.rotateBlockCounterClockwise();
            } else if (input.inputType() == tetris.network.protocol.InputType.HARD_DROP) {
                model.hardDropBlock();
            }
        });
    }

    // 서버 상태 정보 반환 (연결된 클라이언트 수, 게임 상태 등)
    public ServerStatus getServerStatus() { 
        return null; /* Step 4 구현 예정 */ }

    // 보드 상태 데이터 클래스
    public static class BoardStateData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int playerId;
        public final int[][] boardGrid;  // Board 객체 대신 int[][]
        public final Object currentPiece;
        public final int score;  // Score 객체 대신 int
        public final GameState gameState;

        public BoardStateData(int playerId, tetris.domain.Board board, Object currentPiece, tetris.domain.score.Score score, GameState gameState) {
            this.playerId = playerId;
            this.boardGrid = board != null ? board.gridView() : new int[20][10];
            this.currentPiece = currentPiece;
            this.score = score != null ? score.getPoints() : 0;
            this.gameState = gameState;
        }
    }
}
