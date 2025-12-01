package tetris.network.client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CountDownLatch;

import tetris.network.protocol.GameMessage;

/**
 * 클라이언트에서 서버와의 통신을 담당
 * - 서버로부터 메시지 수신 및 처리
 * - 서버에게 메시지 전송
 * - 네트워크 예외 상황 처리
 * - 지연시간 측정 및 연결 품질 모니터링
 */
public class ClientHandler implements Runnable {
    // === 네트워크 관련 ===
    private ObjectInputStream inputStream;     // 서버로부터 메시지 수신
    private ObjectOutputStream outputStream;   // 서버에게 메시지 송신

    // === 클라이언트 참조 ===
    private GameClient client;                 // 부모 클라이언트 참조

    // === 지연시간 측정 ===
    private long lastPingTime;                 // 마지막 핑 전송 시간
    private long currentLatency;               // 현재 지연시간
    private boolean waitingForPong;            // 퐁 응답 대기 중
    // 마지막으로 적용한 스냅샷 시퀀스 (playerId 1..2)
    private final int[] lastAppliedSnapshotSeq = new int[] {-1, -1, -1};

    private CountDownLatch handshakeLatch;
    
    // === 연결 타임아웃 감지 ===
    private volatile long lastMessageTime;     // 마지막 메시지 수신 시간
    private static final long TIMEOUT_MS = 10000; // 10초 타임아웃
    private Thread timeoutWatchdog;

    // === 주요 메서드들 ===

    // 생성자 - 스트림과 클라이언트 참조 받음
    public ClientHandler(ObjectInputStream input, ObjectOutputStream output, GameClient client, CountDownLatch latch) {
    this.inputStream = input;
    this.outputStream = output;
    this.client = client;
    this.handshakeLatch = latch; // Latch 저장
    this.lastMessageTime = System.currentTimeMillis();
    startTimeoutWatchdog();
}

    // 스레드 실행 메서드 - 서버 메시지 수신 루프
    @Override
    public void run() {
        try {
            while (client.isConnected()) { // 부모 클라이언트의 상태를 따름
                GameMessage message = (GameMessage) inputStream.readObject();
                lastMessageTime = System.currentTimeMillis(); // 메시지 수신 시 타임스탬프 갱신
                handleMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("Server closed connection.");
            notifyServerDisconnected();
        } catch (IOException | ClassNotFoundException e) {
            handleError(e);
            notifyServerDisconnected();
        } finally {
            stopTimeoutWatchdog();
            client.disconnect();
        }
    }

    // 서버로부터 메시지 수신 및 처리
    private void handleMessage(GameMessage message) {
        switch (message.getType()) {
            case CONNECTION_ACCEPTED:
                handleConnectionAccepted(message);
                break;
            case DISCONNECT:
                System.out.println("Server requested disconnect.");
                client.disconnect();
                break;
            case OPPONENT_DISCONNECTED:
                handleOpponentDisconnected(message);
                break;
            case GAME_START:
                handleGameStart(message);
                break;
            case PLAYER_INPUT:
                handleOpponentInput(message);
                break;
            case ATTACK_LINES:
                handleIncomingAttack(message);
                break;
            case BOARD_STATE:
                // forward board state updates to client listener on EDT
                if (client.getGameStateListener() != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> client.getGameStateListener().onOpponentBoardUpdate(message));
                }
                break;
            case GAME_STATE:
                handleGameState(message);
                break;
            case PING:
                // 서버로부터 PING 받으면 PONG 응답
                sendMessage(new GameMessage(tetris.network.protocol.MessageType.PONG, client.getPlayerId(), null));
                break;
            case PONG:
                handlePong(message);
                break;
            case GAME_END:
                // game end - forward as a state change
                if (client.getGameStateListener() != null) {
                    javax.swing.SwingUtilities.invokeLater(() -> client.getGameStateListener().onGameStateChange(message));
                }
                break;
            // Step 3에서 다른 메시지 타입 처리 로직 추가 예정
            default:
                System.out.println("Received unhandled message type: " + message.getType());
        }
    }

    // 서버에게 메시지 전송

    public void sendMessage(GameMessage message) {
        try {
            if (outputStream != null) {
                synchronized (outputStream) {
                    outputStream.writeObject(message);
                    // 💡 핵심 수정: 버퍼링된 데이터를 즉시 전송합니다.
                    outputStream.flush(); 
                }
                try {
                    int seq = message == null ? -1 : message.getSequenceNumber();
                    System.out.println("ClientHandler sent message: " + message.getType() + " seq=" + seq + " identity=" + System.identityHashCode(message));
                } catch (Exception ignore) {
                    System.out.println("ClientHandler sent message: " + message.getType());
                }
            }
        } catch (IOException e) {
            System.err.println("Error sending message from client: " + e.getMessage());
        }
    }


    // 연결 승인 처리 - 서버가 연결을 승인했을 때
    private void handleConnectionAccepted(GameMessage message) {
        // 서버가 할당해 준 클라이언트 ID를 저장
        client.setPlayerId((String) message.getPayload()); 
        System.out.println("Connection accepted. My ID is: " + client.getPlayerId());
        // 이 시점에서 UI에 '연결 성공'을 표시하거나 다음 단계로 넘어갈 수 있습니다.

         // **핸드셰이크 완료 신호 전송**
        if (handshakeLatch != null) {
            handshakeLatch.countDown();
        }
    }

    

    // 에러 처리 - 네트워크 오류 발생 시
    private void handleError(Exception e) {
        System.err.println("ClientHandler network error: " + e.getMessage());
        client.disconnect();
    }

    // 상대방 연결 끊김 처리
    private void handleOpponentDisconnected(GameMessage message) {
        String disconnectedId = (String) message.getPayload();
        System.out.println("Opponent " + disconnectedId + " disconnected from the game.");
        
        // 게임 상태 리스너에게 알림 (승리 처리)
        if (client.getGameStateListener() != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                client.getGameStateListener().onGameStateChange(message);
            });
        }
    }

    // 서버 연결 끊김 알림
    private void notifyServerDisconnected() {
        System.out.println("[ClientHandler] Server disconnected - notifying game state listener");
        // Mark client as disconnected
        client.setDisconnected();
        
        if (client.getGameStateListener() != null) {
            GameMessage disconnectMsg = new GameMessage(
                tetris.network.protocol.MessageType.OPPONENT_DISCONNECTED,
                "SERVER",
                "Server"
            );
            javax.swing.SwingUtilities.invokeLater(() -> {
                client.getGameStateListener().onGameStateChange(disconnectMsg);
            });
        }
    }

    // 게임 모드 선택 처리 - 서버가 게임 모드를 알려줄 때
    private void handleGameModeSelect(GameMessage message){
        /* Step 4 구현 예정 */
    }

    // 게임 시작 처리 - 서버가 게임 시작 신호를 보낼 때
    private void handleGameStart(GameMessage message){
        // 서버가 보낸 게임 시작 신호 수신: 페이로드로 {mode, seed}
        Object payload = message.getPayload();
        String mode = null;
        Long seed = null;
        if (payload instanceof java.util.Map<?,?> map) {
            Object m = map.get("mode");
            Object s = map.get("seed");
            if (m instanceof String) mode = (String)m;
            if (s instanceof Number) seed = ((Number)s).longValue();
        } else if (payload instanceof String) {
            mode = (String) payload;
        }
        System.out.println("Received GAME_START from server. mode=" + mode + ", seed=" + seed);
        // 전달받은 정보를 GameClient에 기록하여 UI가 확인할 수 있게 함
        client.setStartReceived(true);
        client.setStartMode(mode);
        if (seed != null) client.setStartSeed(seed);
    }

    // 상대방 입력 처리 - 상대방의 키 입력을 받을 때
    private void handleOpponentInput(GameMessage message){
        // PlayerInput payload is expected
                if (client.getGameStateListener() != null) {
            javax.swing.SwingUtilities.invokeLater(() -> client.getGameStateListener().onGameStateChange(message));
        } else {
            System.out.println("Opponent input received but no GameStateListener registered: " + message);
        }
    }

    // 공격 받기 처리 - 상대방의 공격 라인을 받을 때
    private void handleIncomingAttack(GameMessage message){
        if (client.getGameStateListener() != null) {
            javax.swing.SwingUtilities.invokeLater(() -> client.getGameStateListener().onGameStateChange(message));
        } else {
            System.out.println("Incoming attack received but no GameStateListener registered: " + message);
        }
    }

    // 게임 상태 스냅샷 처리 - 호스트의 권위 있는 게임 상태를 수신
    private void handleGameState(GameMessage message) {
        // 페이로드로 GameSnapshot 객체가 전달됨
        Object payload = message.getPayload();
        if (payload instanceof tetris.network.protocol.GameSnapshot) {
            tetris.network.protocol.GameSnapshot snapshot = (tetris.network.protocol.GameSnapshot) payload;
            int playerId = snapshot.playerId();
            int seq = message.getSequenceNumber();
            // Ignore older or duplicate snapshots for the same player
            if (playerId >= 1 && playerId <= 2) {
                int last = lastAppliedSnapshotSeq[playerId];
                if (seq <= last) {
                    System.out.println("[ClientHandler] Ignoring old/duplicate snapshot for player=" + playerId + " seq=" + seq + " last=" + last);
                    return;
                }
                lastAppliedSnapshotSeq[playerId] = seq;
            }
            if (client.getGameStateListener() != null) {
                javax.swing.SwingUtilities.invokeLater(() -> client.getGameStateListener().onGameStateSnapshot(snapshot));
            } else {
                System.out.println("GameState snapshot received but no listener registered.");
            }
        } else {
            System.out.println("GAME_STATE payload is not a GameSnapshot: " + payload);
        }
    }

    // 퐁 처리 - 지연시간 계산
    private void handlePong(GameMessage message){
        // GameClient의 handlePong 호출하여 RTT 계산
        client.handlePong();
    }

    // 주기적 핑 전송 - 지연시간 측정 및 연결 확인
    private void sendPing(){
        /* Step 3 구현 예정 */ }

    // 현재 지연시간 반환
    public long getLatency() {
        return currentLatency;
    }
    
    // 타임아웃 감시 스레드 시작
    private void startTimeoutWatchdog() {
        timeoutWatchdog = new Thread(() -> {
            try {
                while (client.isConnected()) {
                    Thread.sleep(1000); // 1초마다 체크
                    long elapsed = System.currentTimeMillis() - lastMessageTime;
                    if (elapsed > TIMEOUT_MS) {
                        System.err.println("[ClientHandler] Connection timeout detected (no message for " + elapsed + "ms)");
                        notifyConnectionTimeout("서버로부터 10초 이상 응답이 없습니다.");
                        client.disconnect();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                // 정상 종료
            }
        }, "ClientHandler-TimeoutWatchdog");
        timeoutWatchdog.setDaemon(true);
        timeoutWatchdog.start();
    }
    
    // 타임아웃 감시 스레드 중지
    private void stopTimeoutWatchdog() {
        if (timeoutWatchdog != null) {
            timeoutWatchdog.interrupt();
        }
    }
    
    // 연결 타임아웃 알림
    private void notifyConnectionTimeout(String reason) {
        System.out.println("[ClientHandler] Connection timeout - notifying game state listener");
        client.setDisconnected();
        
        if (client.getGameStateListener() != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                client.getGameStateListener().onConnectionTimeout(reason);
            });
        }
    }
}
