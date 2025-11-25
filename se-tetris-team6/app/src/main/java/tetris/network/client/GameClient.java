package tetris.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.protocol.PlayerInput;

/**
 * P2P 대전 모드의 클라이언트 역할을 담당
 * - 서버에 연결 및 연결 유지
 * - 게임 데이터 송수신
 * - 로컬 게임과 네트워크 게임 상태 동기화
 * - 연결 상태 모니터링 및 재연결 처리
 */
public class GameClient {
    // === 네트워크 관련 ===
    private Socket serverSocket;               // 서버와의 소켓 연결
    private ClientHandler clientHandler;       // 메시지 처리 핸들러
    private boolean isConnected;               // 연결 상태
    private Thread handlerThread;

    // === 서버 정보 ===
    private String serverIP;                   // 서버 IP 주소
    private int serverPort;                    // 서버 포트

    // === 메시지 큐 ===
    private BlockingQueue<GameMessage> outgoingMessages;  // 송신 대기 메시지
    private BlockingQueue<GameMessage> incomingMessages;  // 수신된 메시지

    // === 게임 상태 ===
    private String playerId;                   // 내 플레이어 ID
    private GameStateListener gameStateListener;  // 게임 상태 변경 리스너

    // === 주요 메서드들 ===

    // 서버에 연결 시도
    public boolean connectToServer(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        try {
            // 1. 소켓 연결
            this.serverSocket = new Socket(ip, port);
            
            // 2. 스트림 초기화 (서버와 마찬가지로 Output을 먼저)
            ObjectOutputStream output = new ObjectOutputStream(serverSocket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(serverSocket.getInputStream());

            // 3. ClientHandler 초기화 및 시작
            this.clientHandler = new ClientHandler(input, output, this);
            handlerThread = new Thread(clientHandler);
            handlerThread.start();

            // 4. CONNECTION_REQUEST 전송 (핸드셰이크 시작)
            GameMessage request = new GameMessage(MessageType.CONNECTION_REQUEST, "CLIENT", null);
            clientHandler.sendMessage(request);

            this.isConnected = true;
            System.out.println("Successfully connected to server at " + ip + ":" + port);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            this.isConnected = false;
            return false;
        }
    }

    // 서버와 연결 해제
    public void disconnect() {
        this.isConnected = false;
        if (clientHandler != null) {
            // 클라이언트 핸들러에게 연결 종료 메시지 전송 후 종료 요청
            clientHandler.sendMessage(new GameMessage(MessageType.DISCONNECT, this.playerId, null));
        }
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) { /* ignore */ }
        
        if (handlerThread != null) {
            handlerThread.interrupt();
        }
    }

    // 게임 메시지 전송 (비동기)
    public void sendMessage(GameMessage message) {
        if (isConnected && clientHandler != null) {
            clientHandler.sendMessage(message);
        }
    }
    
    // 플레이어 ID 설정
    public void setPlayerId(String id) {
        this.playerId = id;
    }

    // 플레이어 ID 반환
    public String getPlayerId() {
        return playerId;
    }

    // 플레이어 입력 전송 (키보드 입력)
    public void sendPlayerInput(PlayerInput input){
        /*todo */
    }

    // 공격 라인 전송 (줄 삭제 시)
    public void sendAttackLines(AttackLine[] lines){
        /*todo */
    }

    // 게임 시작 준비 완료 신호
    public void sendReadySignal(){
        /*todo */
    }

    // 수신된 메시지 처리 - 메인 게임 루프에서 호출
    public void processIncomingMessages(){
        /*todo */
    }

    // 연결 상태 확인
    public boolean isConnected(){
        return isConnected;
    }

    // 지연시간 측정
    public long getLatency(){
        return 0; /* Step 4 구현 예정 */
    }

    // 게임 상태 리스너 등록
    public void setGameStateListener(GameStateListener listener){
        /*todo */
    }

    // 최근 접속 IP 저장/불러오기
    public void saveRecentIP(String ip){
        /*todo */
    }
    public String getRecentIP(){
        return "127.0.0.1"; /* Step 4 구현 예정 */
    }

    // 재연결 시도
    private void attemptReconnection(){
        /*todo */
    }

    // 연결 상태 모니터링 (별도 스레드)
    private void monitorConnection(){
        /*todo */
    }


}
