package tetris.network;

import tetris.concurrent.GameThread;
import tetris.concurrent.NetworkThread;
import tetris.concurrent.GameMessage;
import tetris.concurrent.GameMessage.MessageType;
import tetris.concurrent.GameEventListener; // GameThread에서 사용하는 public 인터페이스 가정

// =================================================================
// 임시 더미 클래스/인터페이스 (컴파일 가능하도록 public으로 선언)
// =================================================================

// NetworkThread가 NetworkManager에게 콜백을 보낼 때 사용하는 인터페이스
// 기존의 NetThreadManager 역할을 대체
public interface INetworkThreadCallback {
    void handleReceivedMessage(GameMessage message);
    void handleConnectionEstablished();
    void handleConnectionLost();
    void handleLatencyWarning(long latency);
    void handleNetworkError(Exception error);
}

public class GameServer { /* ... */ }
public class GameClient { /* ... */ }
public class NetworkSettings { /* ... */ }
public class ConnectionInfo { /* ... */ }
public class NetworkStatus { /* ... */ }
public class PlayerInput {
    public enum Type { MOVE_LEFT, MOVE_RIGHT, ROTATE, SOFT_DROP, HARD_DROP, PAUSE }
    private final Type type;
    public PlayerInput(Type type) { this.type = type; }
    public Type getType() { return type; }
}
public class AttackLine { /* ... */ }
public class GameState { /* ... */ }

public enum NetworkMode {
    OFFLINE, SERVER, CLIENT
}

public interface NetworkEventListener {
    void onConnected();
    void onDisconnected();
    void onConnectionError(String error);
    void onLatencyWarning(long latency);
}

public interface GameDataListener {
    void onOpponentInput(PlayerInput input);
    void onIncomingAttack(AttackLine[] lines);
    void onGameStateUpdate(GameState state);
    void onGameStart();
    void onGameEnd(String winner);
}

// =================================================================
// NetworkManager 구현 시작
// =================================================================

/**
 * 네트워크 기능의 통합 관리자 및 외부 인터페이스
 * - 서버/클라이언트 모드 통합 관리
 * - 게임 로직과 네트워크 계층 간의 인터페이스 제공
 * - 네트워크 상태 모니터링 및 에러 처리
 */
public class NetworkManager implements INetworkThreadCallback {
    
    // === 모드 관리 ===
    private NetworkMode currentMode = NetworkMode.OFFLINE;
    private GameServer server;
    private GameClient client;

    // === 스레드 관리 ===
    private NetworkThread networkThread;
    private GameThread localGameThread;

    // === 리스너 인터페이스 ===
    private NetworkEventListener eventListener;
    private GameDataListener gameDataListener;

    // === 설정 관리 ===
    private NetworkSettings settings;
    private final String localPlayerId = "LocalPlayer";

    // === 생성자 ===
    public NetworkManager(GameThread localGameThread) {
        this.localGameThread = localGameThread;
        // GameThread가 NetworkManager를 통해 이벤트를 보낼 수 있도록 설정
        // GameEventListener는 tetris.concurrent 패키지에 public으로 존재한다고 가정
        this.localGameThread.setNetworkListener(
            (AttackLine[] lines) -> sendAttackLines(lines)
        );
    }

    // === 주요 메서드들 ===

    public boolean startAsServer(int port) {
        if (networkThread != null) networkThread.shutdown();
        // NetworkThread 초기화 및 실행
        networkThread = new NetworkThread(this);
        new Thread(networkThread).start();
        currentMode = NetworkMode.SERVER;
        return true;
    }

    public boolean connectAsClient(String serverIP, int port) {
        if (networkThread != null) networkThread.shutdown();
        // NetworkThread 초기화 및 실행
        networkThread = new NetworkThread(this);
        new Thread(networkThread).start();
        currentMode = NetworkMode.CLIENT;
        return true;
    }

    public void disconnect() {
        if (networkThread != null) {
            networkThread.shutdown();
            networkThread = null;
        }
        currentMode = NetworkMode.OFFLINE;
        if (eventListener != null) {
            eventListener.onDisconnected();
        }
    }

    public NetworkMode getCurrentMode() {
        return currentMode;
    }

    public boolean isConnected() {
        return networkThread != null && networkThread.isConnected();
    }

    // === 게임 데이터 전송 메서드들 (GameThread -> NetworkThread) ===

    public void sendPlayerInput(PlayerInput input) {
        if (isConnected() && networkThread != null) {
            GameMessage message = new GameMessage(
                MessageType.PLAYER_INPUT,
                localPlayerId,
                input
            );
            networkThread.sendMessage(message);
        }
    }

    public void sendAttackLines(AttackLine[] lines) {
        if (isConnected() && networkThread != null) {
            GameMessage message = new GameMessage(
                MessageType.ATTACK_LINES,
                localPlayerId,
                lines
            );
            networkThread.sendMessage(message);
        }
    }

    public void syncGameState(GameState state) {
        if (isConnected() && networkThread != null) {
            GameMessage message = new GameMessage(
                MessageType.BOARD_STATE,
                localPlayerId,
                state
            );
            networkThread.sendMessage(message);
        }
    }
    
    public void sendGameStart() {
        if (isConnected() && networkThread != null) {
            GameMessage message = new GameMessage(
                MessageType.GAME_START,
                localPlayerId,
                null
            );
            networkThread.sendMessage(message);
        }
    }

    // === 수신 데이터 처리 메서드들 (INetworkThreadCallback 구현) ===

    @Override
    public void handleReceivedMessage(GameMessage message) {
        if (gameDataListener == null) return;

        switch (message.getType()) {
            case PLAYER_INPUT:
                PlayerInput input = (PlayerInput) message.getPayload();
                gameDataListener.onOpponentInput(input);
                break;
            case ATTACK_LINES:
                AttackLine[] lines = (AttackLine[]) message.getPayload();
                gameDataListener.onIncomingAttack(lines);
                break;
            case BOARD_STATE:
                GameState state = (GameState) message.getPayload();
                gameDataListener.onGameStateUpdate(state);
                break;
            case GAME_START:
                gameDataListener.onGameStart();
                break;
            default:
                break;
        }
    }

    // === NetworkThread의 이벤트 처리 (INetworkThreadCallback 구현) ===

    @Override
    public void handleConnectionEstablished() {
        if (eventListener != null) {
            eventListener.onConnected();
        }
    }

    @Override
    public void handleConnectionLost() {
        if (eventListener != null) {
            eventListener.onDisconnected();
        }
    }

    @Override
    public void handleLatencyWarning(long latency) {
        if (eventListener != null) {
            eventListener.onLatencyWarning(latency);
        }
    }

    @Override
    public void handleNetworkError(Exception error) {
        if (eventListener != null) {
            eventListener.onConnectionError(error.getMessage());
        }
    }

    // === 리스너 등록 ===

    public void setNetworkEventListener(NetworkEventListener listener) {
        this.eventListener = listener;
    }

    public void setGameDataListener(GameDataListener listener) {
        this.gameDataListener = listener;
    }

    // === 상태 정보 ===

    public long getCurrentLatency() {
        return networkThread != null ? networkThread.getCurrentLatency() : 0;
    }

    public int getConnectedPlayerCount() {
        return isConnected() ? 2 : 1;
    }

    public NetworkStatus getNetworkStatus() {
        return null;
    }

    // === 설정 관리 ===

    public void saveRecentConnection(String ip, int port) { /* ... */ }
    public ConnectionInfo getRecentConnection() { return null; }
    public void updateSettings(NetworkSettings settings) { this.settings = settings; }
}
