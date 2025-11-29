package tetris.network;

import tetris.concurrent.GameThread;
import tetris.concurrent.NetworkThread;
import tetris.domain.model.Block;
import tetris.domain.model.GameState;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;
import tetris.network.NetworkMode;
import tetris.network.NetworkEventListener;
import tetris.network.GameDataListener;
import tetris.network.INetworkThreadCallback;
import tetris.network.server.GameServer;
import tetris.network.client.GameClient;

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
    private final String localPlayerId = "LocalPlayer";

    // === 생성자 ===
    public NetworkManager(GameThread localGameThread) {
    this.localGameThread = localGameThread;
    
    // GameThread가 NetworkManager를 통해 이벤트를 보낼 수 있도록 GameEventListener를 구현하여 설정
    // GameEventListener는 두 개의 메서드를 가지므로 익명 클래스를 사용해야 함
        this.localGameThread.setNetworkListener(new GameEventListener() {
        @Override
        public void sendAttackLines(AttackLine[] lines) {
            sendAttackLines(lines);
        }

        @Override
        public void sendPlayerInput(PlayerInput input) {
            sendPlayerInput(input);
        }

        @Override
        public void sendBlockRotation(Block block) {
                // 안전한 기본 동작: 블록 회전 정보를 간단한 직렬화 가능한 배열로 전송
                if (isConnected() && networkThread != null && block != null) {
                    // payload: [x, y, kindOrdinal]
                    int[] payload = new int[] { block.getX(), block.getY(), block.getKind().ordinal() };
                    GameMessage message = new GameMessage(MessageType.BLOCK_PLACEMENT, localPlayerId, payload);
                    networkThread.sendMessage(message);
                }
        }
    });
}

    // === 주요 메서드들 ===

    public boolean startAsServer(int port) {
        // 기존 NetworkThread는 클라이언트용입니다. 서버 모드는 GameServer를 사용해 시작합니다.
        try {
            if (server == null) {
                server = new GameServer();
            }
            server.startServer(port);
            currentMode = NetworkMode.SERVER;
            return true;
        } catch (Exception e) {
            System.err.println("서버 시작 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean connectAsClient(String serverIP, int port) {
        if (networkThread != null) networkThread.shutdown();
        // NetworkThread 초기화 및 실행 (호스트/포트 전달)
        networkThread = new NetworkThread(this, serverIP, port);
        new Thread(networkThread).start();
        currentMode = NetworkMode.CLIENT;
        return true;
    }

    public void disconnect() {
        if (networkThread != null) {
            networkThread.shutdown();
            networkThread = null;
        }
        // 서버가 실행 중이면 중지
        if (server != null) {
            try {
                server.stopServer();
            } catch (Exception e) {
                System.err.println("서버 중지 중 오류: " + e.getMessage());
            }
            server = null;
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
            try {
                System.out.println("[NetworkManager] sendPlayerInput: localPlayerId=" + localPlayerId + " payload=" + input + " seq=" + message.getSequenceNumber() + " identity=" + System.identityHashCode(message));
            } catch (Exception ignore) {}
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
                javax.swing.SwingUtilities.invokeLater(() -> gameDataListener.onOpponentInput(input));
                break;
            case ATTACK_LINES:
                AttackLine[] lines = (AttackLine[]) message.getPayload();
                javax.swing.SwingUtilities.invokeLater(() -> gameDataListener.onIncomingAttack(lines));
                break;
            case BOARD_STATE:
                GameState state = (GameState) message.getPayload();
                javax.swing.SwingUtilities.invokeLater(() -> gameDataListener.onGameStateUpdate(state));
                break;
            case GAME_START:
                javax.swing.SwingUtilities.invokeLater(() -> gameDataListener.onGameStart());
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

}
