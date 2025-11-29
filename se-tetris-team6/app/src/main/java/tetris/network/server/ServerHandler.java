package tetris.network.server;

import java.net.Socket;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger; // 추가: 스레드 안전한 카운터 사용

/**
 * 서버에서 개별 클라이언트와의 통신을 담당
 * - 각 클라이언트마다 하나씩 생성되는 핸들러
 * - 클라이언트로부터 메시지 수신 및 처리
 * - 클라이언트에게 메시지 전송
 * - 연결 상태 모니터링 및 예외 처리
 */
    public class ServerHandler implements Runnable {

        // 정적 ID 카운터 추가: 클라이언트는 Player-2부터 할당되도록 시작
        private static final AtomicInteger clientCounter = new AtomicInteger(2);

        // 생성자 - 클라이언트 소켓과 서버 참조 받음
        public ServerHandler(Socket clientSocket, GameServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientId = "UNASSIGNED";

        try {
            // ObjectOutputStream을 먼저 초기화하여 Deadlock을 피하고, 필드명 통일
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error initializing streams for client: " + e.getMessage());
            disconnect();
        }
    }

    // === 네트워크 관련 ===
    private Socket clientSocket;               // 클라이언트와의 소켓 연결
    private ObjectInputStream inputStream;     // 메시지 수신용 스트림
    private ObjectOutputStream outputStream;   // 메시지 송신용 스트림

    // === 클라이언트 정보 ===
    private String clientId;                   // 클라이언트 고유 ID
    private boolean isConnected;               // 연결 상태
    private long lastPingTime;                 // 마지막 핑 시간

    // === 서버 참조 ===
    private GameServer server;                 // 부모 서버 참조

    // === 주요 메서드들 ===

    // 스레드 실행 메서드 - 클라이언트 메시지 수신 루프
    @Override
    public void run() {
        try {
            initializeConnection();
            
            // Step 2에서는 연결 수락 후 바로 종료해도 무방하나, 메시지 루프 구조를 잡습니다.
            while (isConnected) {
                // 클라이언트로부터 메시지 수신 대기
                GameMessage message = (GameMessage) inputStream.readObject();
                handleMessage(message); // 메시지 처리 로직 (Step 3에서 상세 구현)
            }
        } catch (EOFException e) {
            System.out.println("Client closed connection gracefully.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ServerHandler error for client " + clientId + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    // 클라이언트에게 메시지 전송
    public void sendMessage(GameMessage message) {
        try {
            if (outputStream != null) {
            outputStream.writeObject(message);
            // 💡 핵심 수정: 버퍼링된 데이터를 즉시 전송합니다.
            outputStream.flush(); 
            System.out.println("ServerHandler sent message: " + message.getType());
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client " + clientId + ": " + e.getMessage());
            disconnect();
        }
    }

    // 연결 초기화 - 스트림 설정 및 클라이언트 ID 할당
    private void initializeConnection() throws IOException, ClassNotFoundException {

        // 1. 클라이언트의 CONNECTION_REQUEST를 받습니다.
        GameMessage request = (GameMessage) inputStream.readObject();

        if (request.getType() == MessageType.CONNECTION_REQUEST) {
            // 2. 클라이언트 ID를 할당 (테스트 요구사항에 맞춰 "Player-" 접두사 사용)
            this.clientId = "Player-" + clientCounter.getAndIncrement(); // MODIFIED
            
            // 3. CONNECTION_ACCEPTED 메시지를 클라이언트에게 전송합니다.
            GameMessage acceptance = new GameMessage(MessageType.CONNECTION_ACCEPTED, "SERVER", this.clientId);
            sendMessage(acceptance);
            // 4. 서버에 연결 완료 알림
            try {
                server.notifyClientConnected(this);
            } catch (Exception ex) {
                System.err.println("Error notifying server of new client: " + ex.getMessage());
            }
            System.out.println("Connection accepted for client: " + this.clientId);
            this.isConnected = true;
        } else {
            // 요청 타입이 잘못된 경우
            throw new IOException("Invalid connection request type.");
        }
    }

    // 연결 종료 처리 - 리소스 정리 및 서버에 알림
    public void disconnect() {
        if (isConnected) {
            isConnected = false;
            try {
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) { /* ignore */ }
            server.removeClient(this);
        }
    }

    // 연결 상태 확인
    public boolean isConnected(){return isConnected;}

    // 클라이언트 ID 반환
    public String getClientId(){return clientId;}


    // 클라이언트로부터 메시지 수신 및 처리
    private void handleMessage(GameMessage message){
        if (message == null) return;
        switch (message.getType()) {
            case PLAYER_READY:
                System.out.println("ServerHandler: PLAYER_READY from " + clientId);
                server.setClientReady(this, true);
                break;
            case DISCONNECT:
                System.out.println("ServerHandler: DISCONNECT from " + clientId);
                disconnect();
                break;
            case PLAYER_INPUT:
            case ATTACK_LINES:
                // 클라이언트 입력을 호스트에게 전달
                System.out.println("[ServerHandler] received " + message.getType() + " from clientId=" + clientId + " senderId=" + message.getSenderId() + " payload=" + message.getPayload());
                server.notifyHostOfMessage(message);
                break;
            default:
                // 기본 동작: 서버가 다른 클라이언트에게 그대로 브로드캐스트
                server.broadcastMessage(message);
                break;
        }
    }

         // 핑 처리 - 지연시간 측정 및 연결 상태 확인
    private void handlePing(GameMessage pingMessage){
        /* Step 3 구현 예정 */ }

    // 게임 입력 처리 - 클라이언트의 키 입력을 다른 클라이언트에게 전달
    private void handlePlayerInput(GameMessage inputMessage){
        /* Step 3 구현 예정 */ }

    // 공격 처리 - 한 플레이어의 공격을 상대방에게 전달
    private void handleAttackLines(GameMessage attackMessage){
        /* Step 3 구현 예정 */ }

    
}