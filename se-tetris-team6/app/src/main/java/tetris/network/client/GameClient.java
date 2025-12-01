package tetris.network.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import tetris.network.protocol.AttackLine;
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
    private static final int MAX_RECENT_HOSTS = 5;
    private static final String PREF_KEY_RECENT_HOSTS = "tetris.recent.hosts";
    private static final Preferences PREFS = Preferences.userRoot().node("tetris");

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

    private CountDownLatch handshakeLatch;
    // Game start signal tracking
    private volatile boolean startReceived = false;
    private volatile String startMode = null;
    private volatile Long startSeed = null;
    
    // === 핑 측정 관련 ===
    private volatile long lastPingTime = 0;        // 마지막 PING 전송 시간
    private volatile long currentPing = -1;        // 현재 핑 (ms), -1이면 측정 중 또는 연결 안됨
    private volatile boolean waitingForPong = false; // PONG 응답 대기 중
    private Thread pingThread;                     // 핑 측정 스레드

    // === 주요 메서드들 ===

    // 서버에 연결 시도
    public boolean connectToServer(String ip, int port, CountDownLatch latch) {
        this.handshakeLatch = latch;
        this.serverIP = ip;
        this.serverPort = port;
        try {
            this.serverSocket = new Socket(ip, port);
        
            // 2. 스트림 초기화 및 flush (이전 단계에서 수정했다고 가정)
            ObjectOutputStream output = new ObjectOutputStream(serverSocket.getOutputStream());
            output.flush(); // 💡 중요: 헤더 전송
            ObjectInputStream input = new ObjectInputStream(serverSocket.getInputStream());

            // 3. CONNECTION_REQUEST 전송 (핸드셰이크 시작)
            GameMessage request = new GameMessage(MessageType.CONNECTION_REQUEST, "CLIENT", null);
            
            // 💡 핵심 수정: 핸들러 스레드를 시작하기 전에 직접 메시지를 보냅니다.
            output.writeObject(request);
            output.flush(); 

            // 4. ClientHandler 초기화 및 시작
            this.clientHandler = new ClientHandler(input, output, this, handshakeLatch);
            handlerThread = new Thread(clientHandler);
            handlerThread.start(); // <--- 이제 ClientHandler는 서버의 응답을 기다립니다.

            this.isConnected = true;
            System.out.println("Successfully connected to server at " + ip + ":" + port);
            // persist recent host:port on successful connect
            addRecentHost(ip + ":" + port);
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
        stopPingMeasurement();
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

    // Send READY signal to server
    public void sendReady() {
        if (isConnected && clientHandler != null) {
            GameMessage ready = new GameMessage(MessageType.PLAYER_READY, this.playerId == null ? "CLIENT" : this.playerId, null);
            clientHandler.sendMessage(ready);
            System.out.println("GameClient: sent PLAYER_READY");
        }
    }

    public boolean isStartReceived() { return startReceived; }
    public String getStartMode() { return startMode; }
    public Long getStartSeed() { return startSeed; }
    public void setStartReceived(boolean v) { this.startReceived = v; }
    public void setStartMode(String m) { this.startMode = m; }
    public void setStartSeed(Long s) { this.startSeed = s; }
    
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
        if (!isConnected || clientHandler == null || input == null) return;
        GameMessage msg = new GameMessage(tetris.network.protocol.MessageType.PLAYER_INPUT, this.playerId == null ? "CLIENT" : this.playerId, input);
        try {
            System.out.println("[Client] sendPlayerInput: playerId=" + this.playerId + " payload=" + input + " seq=" + msg.getSequenceNumber() + " identity=" + System.identityHashCode(msg));
        } catch (Exception ignore) {
            System.out.println("[Client] sendPlayerInput: playerId=" + this.playerId + " payload=" + input);
        }
        clientHandler.sendMessage(msg);
    }

    // 공격 라인 전송 (줄 삭제 시)
    public void sendAttackLines(AttackLine[] lines){
        if (!isConnected || clientHandler == null || lines == null) return;
        GameMessage msg = new GameMessage(tetris.network.protocol.MessageType.ATTACK_LINES, this.playerId == null ? "CLIENT" : this.playerId, lines);
        clientHandler.sendMessage(msg);
    }

    // 게임 상태 스냅샷 전송 (클라이언트도 자신의 게임 상태를 호스트에게 전송)
    public void sendGameStateSnapshot(tetris.network.protocol.GameSnapshot snapshot) {
        if (!isConnected || clientHandler == null || snapshot == null) return;
        GameMessage msg = new GameMessage(
            tetris.network.protocol.MessageType.GAME_STATE,
            this.playerId == null ? "CLIENT" : this.playerId,
            snapshot
        );
        clientHandler.sendMessage(msg);
        System.out.println("[Client] Sent game state snapshot to server");
    }

    // 게임 시작 준비 완료 신호
    public void sendReadySignal(){
        sendReady();
    }

    // 수신된 메시지 처리 - 메인 게임 루프에서 호출
    public void processIncomingMessages(){
        // Currently messages are handled on-the-fly by ClientHandler and forwarded
        // to the registered GameStateListener. No queued processing required here.
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
        this.gameStateListener = listener;
    }

    public GameStateListener getGameStateListener() {
        return this.gameStateListener;
    }

    // 최근 접속 IP 저장/불러오기
    public void saveRecentIP(String ip){
        addRecentHost(ip);
    }
    public String getRecentIP(){
        List<String> recents = getRecentHosts();
        if (!recents.isEmpty()) return recents.get(0);
        return "127.0.0.1";
    }

    /**
     * 최근 연결 시도한 host:port 목록을 반환 (최신순).
     */
    public static List<String> getRecentHosts() {
        try {
            String raw = PREFS.get(PREF_KEY_RECENT_HOSTS, "");
            if (raw == null || raw.isBlank()) return Collections.emptyList();
            String[] parts = raw.split(",");
            List<String> list = new ArrayList<>();
            for (String p : parts) {
                String v = p.trim();
                if (!v.isEmpty()) list.add(v);
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 최근 host:port를 저장 (중복 제거 후 최신순, 최대 MAX_RECENT_HOSTS).
     */
    public static void addRecentHost(String hostPort) {
        if (hostPort == null || hostPort.isBlank()) return;
        try {
            List<String> list = new ArrayList<>(getRecentHosts());
            list.removeIf(s -> s.equalsIgnoreCase(hostPort));
            list.add(0, hostPort);
            if (list.size() > MAX_RECENT_HOSTS) {
                list = list.subList(0, MAX_RECENT_HOSTS);
            }
            String joined = String.join(",", list);
            PREFS.put(PREF_KEY_RECENT_HOSTS, joined);
        } catch (Exception ignore) {
            // ignore persistence errors
        }
    }

    // 재연결 시도
    private void attemptReconnection(){
        /*todo */
    }

    // 연결 상태 모니터링 (별도 스레드)
    private void monitorConnection(){
        /*todo */
    }

    // 서버 연결 끊김 알림 (ClientHandler에서 호출)
    public void setDisconnected() {
        this.isConnected = false;
    }
    
    // === 핑 측정 메서드 ===
    
    /**
     * 핑 측정 시작 - 주기적으로 PING 메시지를 서버로 전송
     */
    public void startPingMeasurement() {
        if (pingThread != null && pingThread.isAlive()) {
            return; // 이미 실행 중
        }
        
        pingThread = new Thread(() -> {
            while (isConnected && !Thread.currentThread().isInterrupted()) {
                try {
                    // PING 전송
                    if (!waitingForPong) {
                        lastPingTime = System.currentTimeMillis();
                        waitingForPong = true;
                        sendMessage(new GameMessage(MessageType.PING, this.playerId, null));
                    }
                    
                    // 2초마다 측정
                    Thread.sleep(2000);
                    
                    // 타임아웃 체크 (5초 이상 응답 없으면)
                    if (waitingForPong && (System.currentTimeMillis() - lastPingTime) > 5000) {
                        currentPing = -1; // 연결 불안정
                        waitingForPong = false;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "PingMeasurement");
        pingThread.setDaemon(true);
        pingThread.start();
    }
    
    /**
     * 핑 측정 중지
     */
    public void stopPingMeasurement() {
        if (pingThread != null) {
            pingThread.interrupt();
            pingThread = null;
        }
        currentPing = -1;
        waitingForPong = false;
    }
    
    /**
     * 현재 핑 값 반환 (ms)
     * @return 핑 값, -1이면 측정 중이거나 연결 안됨
     */
    public long getCurrentPing() {
        return currentPing;
    }
    
    /**
     * PONG 응답 처리 - ClientHandler에서 호출
     */
    public void handlePong() {
        if (waitingForPong) {
            long rtt = System.currentTimeMillis() - lastPingTime;
            currentPing = rtt;
            waitingForPong = false;
        }
    }

}
