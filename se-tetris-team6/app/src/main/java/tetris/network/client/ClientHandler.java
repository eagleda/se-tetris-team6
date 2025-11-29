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

    private CountDownLatch handshakeLatch;

    // === 주요 메서드들 ===

    // 생성자 - 스트림과 클라이언트 참조 받음
    public ClientHandler(ObjectInputStream input, ObjectOutputStream output, GameClient client, CountDownLatch latch) {
    this.inputStream = input;
    this.outputStream = output;
    this.client = client;
    this.handshakeLatch = latch; // Latch 저장
}

    // 스레드 실행 메서드 - 서버 메시지 수신 루프
    @Override
    public void run() {
        try {
            while (client.isConnected()) { // 부모 클라이언트의 상태를 따름
                GameMessage message = (GameMessage) inputStream.readObject();
                handleMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("Server closed connection.");
        } catch (IOException | ClassNotFoundException e) {
            handleError(e);
        } finally {
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
                outputStream.writeObject(message);
                // 💡 핵심 수정: 버퍼링된 데이터를 즉시 전송합니다.
                outputStream.flush(); 
                System.out.println("ClientHandler sent message: " + message.getType());
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

    // 퐁 처리 - 지연시간 계산
    private void handlePong(GameMessage message){
        // payload may contain timestamp or latency info
        try {
            Object payload = message.getPayload();
            if (payload instanceof Long) {
                long sent = (Long) payload;
                long now = System.currentTimeMillis();
                long rtt = now - sent;
                // update optional latency metric on client if exposed
                System.out.println("PONG received, rtt=" + rtt + "ms");
            }
        } catch (Exception e) {
            // ignore parse issues
        }
    }

    // 주기적 핑 전송 - 지연시간 측정 및 연결 확인
    private void sendPing(){
        /* Step 3 구현 예정 */ }

    // 현재 지연시간 반환
    public long getLatency() {
        return currentLatency;
    }
}
