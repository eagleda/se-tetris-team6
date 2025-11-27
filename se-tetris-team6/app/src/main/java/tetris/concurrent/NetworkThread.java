package tetris.concurrent;

import java.io.IOException;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// =================================================================
// 임시 더미 클래스/인터페이스 (실제 프로젝트 구조에 맞게 교체 필요)
// =================================================================

// GameMessage, MessageType은 소공_멀티_기능.pdf를 기반으로 정의
class GameMessage implements java.io.Serializable {
    public enum MessageType {
        CONNECTION_REQUEST, CONNECTION_ACCEPTED, DISCONNECT,
        PLAYER_INPUT, ATTACK_LINES, BOARD_STATE,
        PING, PONG, LAG_WARNING, ERROR
    }
    private final MessageType type;
    private final String senderId;
    private final Object payload;

    public GameMessage(MessageType type, String senderId, Object payload) {
        this.type = type;
        this.senderId = senderId;
        this.payload = payload;
    }
    public MessageType getType() { return type; }
    public String getSenderId() { return senderId; }
    public Object getPayload() { return payload; }
}

// NetworkManager (GameThread와 NetworkThread 사이의 인터페이스)
interface NetworkManager {
    void handleReceivedMessage(GameMessage message);
    void handleConnectionLost();
    void handleLatencyWarning(long latency);
    void handleNetworkError(Exception error);
    void handleConnectionEstablished();
}

class NetworkStats {
    public final long currentLatency;
    public final int sendQueueSize;
    public NetworkStats(long latency, int size) {
        this.currentLatency = latency;
        this.sendQueueSize = size;
    }
}

// =================================================================
// NetworkThread 구현 시작
// =================================================================

/**
 * 네트워크 통신을 담당하는 전용 스레드
 * - 네트워크 메시지 송수신 처리
 * - 게임 상태 동기화
 * - 지연시간 측정 및 모니터링
 * - 연결 상태 관리 및 재연결 처리
 * - 메시지 큐 관리 및 우선순위 처리
 */
public class NetworkThread implements Runnable {
    // === 상수 설정 (NetworkProtocol.java 참조) ===
    private static final long GAME_SYNC_INTERVAL = 50; // 50ms마다 동기화 시도
    private static final long PING_INTERVAL = 1000;    // 1초마다 핑
    private static final long MAX_LAG_THRESHOLD = 200; // 200ms 이상이면 랙 경고
    private static final int MAX_RETRY_COUNT = 3;     // 최대 재연결 시도 횟수
    private static final long RECONNECT_DELAY = 1000;  // 1초 후 재시도

    // === 네트워크 관리 ===
    private final NetworkManager networkManager;     // 네트워크 매니저 참조
    private final AtomicBoolean isRunning = new AtomicBoolean(true); // 스레드 실행 상태
    private final AtomicBoolean isConnected = new AtomicBoolean(false); // 네트워크 연결 상태

    // === 메시지 큐 관리 ===
    private final BlockingQueue<GameMessage> outgoingQueue = new LinkedBlockingQueue<>();    // 송신 대기 메시지
    private final BlockingQueue<GameMessage> incomingQueue = new LinkedBlockingQueue<>();    // 수신된 메시지 (GameThread가 가져감)
    private final BlockingQueue<GameMessage> priorityQueue = new LinkedBlockingQueue<>();    // 우선순위 메시지 (핑, 에러 등)

    // === 동기화 관리 ===
    // 맵 대신 단일 피어와의 동기화 시간만 관리하는 것으로 가정
    private long lastSyncTime;                          // 마지막 동기화 시간
    private final long syncInterval = GAME_SYNC_INTERVAL; // 동기화 간격

    // === 지연시간 관리 ===
    private volatile long currentLatency = 0;               // 현재 지연시간
    private final Queue<Long> latencyHistory = new LinkedList<>(); // 지연시간 히스토리
    private long lastPingTime;                 // 마지막 핑 시간

    // === 재연결 관리 ===
    private int reconnectAttempts = 0;             // 재연결 시도 횟수
    private long lastReconnectTime;            // 마지막 재연결 시도 시간

    // === 소켓 I/O (실제 구현 시 ServerHandler/ClientHandler와 유사) ===
    // private Socket socket;
    // private ObjectOutputStream outputStream;
    // private ObjectInputStream inputStream;


    // === 주요 메서드들 ===

    // 생성자 - 네트워크 매니저 참조 받음
    public NetworkThread(NetworkManager networkManager) {
        this.networkManager = networkManager;
        this.lastPingTime = System.currentTimeMillis();
        this.lastSyncTime = System.currentTimeMillis();
    }

    // 스레드 메인 실행 루프
    @Override
    public void run() {
        System.out.println("NetworkThread 시작됨.");
        // 초기 연결 시도
        attemptConnection();

        while (isRunning.get()) {
            if (isConnected.get()) {
                // 1. 우선순위 메시지 처리 (PING/PONG)
                processPriorityMessages();

                // 2. 송신 메시지 처리
                processSendQueue();

                // 3. 수신 메시지 처리 (여기서는 수신 스레드가 별도로 있다고 가정하고 큐에서 처리)
                // 실제로는 별도의 수신 스레드가 소켓에서 읽어와 incomingQueue에 넣습니다.
                processReceiveQueue();

                // 4. 지연시간 및 연결 상태 모니터링
                monitorConnection();

                // 5. 게임 상태 동기화 (필요한 경우)
                synchronizeGameState();

            } else {
                // 연결이 끊어진 경우 재연결 시도
                attemptReconnection();
            }

            // 루프 속도 제어
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isRunning.set(false);
            }
        }
        System.out.println("NetworkThread 종료됨.");
    }

    // 초기 연결 시도 (클라이언트 역할 가정)
    private void attemptConnection() {
        // TODO: 실제 소켓 연결 로직 구현
        // 현재는 더미로 연결 성공을 시뮬레이션
        try {
            // 소켓 연결 및 스트림 초기화 성공 가정
            TimeUnit.MILLISECONDS.sleep(500); // 연결 시도 시간
            isConnected.set(true);
            onConnectionEstablished();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    // 송신 메시지 처리 - 큐에서 메시지를 가져와 전송
    private void processSendQueue() {
        GameMessage message;
        while ((message = outgoingQueue.poll()) != null) {
            try {
                // TODO: 실제 소켓 outputStream.writeObject(message) 호출
                System.out.println("NetThread SEND: " + message.getType());
                // outputStream.writeObject(message);
                // outputStream.flush();
            } catch (Exception e) {
                // 전송 실패 시 연결 끊김 처리
                onNetworkError(e);
                break;
            }
        }
    }

    // 수신 메시지 처리 - 네트워크에서 받은 메시지 처리
    private void processReceiveQueue() {
        GameMessage message;
        while ((message = incomingQueue.poll()) != null) {
            // 수신된 메시지를 NetworkManager를 통해 GameThread로 전달
            networkManager.handleReceivedMessage(message);
        }
    }

    // 우선순위 메시지 처리 - 핑, 에러, 연결 관련 메시지
    private void processPriorityMessages() {
        GameMessage message;
        while ((message = priorityQueue.poll()) != null) {
            switch (message.getType()) {
                case PING:
                    // PING 수신 시 PONG으로 응답
                    sendPriorityMessage(new GameMessage(GameMessage.MessageType.PONG, "CLIENT", message.getPayload()));
                    break;
                case PONG:
                    // PONG 수신 시 지연시간 측정 완료
                    long pingTime = (Long) message.getPayload();
                    long latency = System.currentTimeMillis() - pingTime;
                    currentLatency = latency;
                    latencyHistory.offer(latency);
                    // 랙 경고 확인
                    if (latency > MAX_LAG_THRESHOLD) {
                        onLatencyWarning(latency);
                    }
                    break;
                case ERROR:
                    // 에러 메시지 처리
                    System.err.println("Received ERROR from server: " + message.getPayload());
                    break;
                case DISCONNECT:
                    // 연결 종료 요청 처리
                    onConnectionLost();
                    break;
                default:
                    // 우선순위 큐에 잘못 들어온 메시지는 무시
                    break;
            }
        }
    }

    // 게임 상태 동기화 - 주기적으로 호출
    private void synchronizeGameState() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncTime >= syncInterval) {
            // TODO: GameThread로부터 현재 GameState를 받아와 BOARD_STATE 메시지로 전송
            // 이 기능은 P2P 동기화 방식에 따라 다르게 구현됨 (입력 동기화 방식이면 생략 가능)
            // sendMessage(new GameMessage(GameMessage.MessageType.BOARD_STATE, "CLIENT", gameStateSnapshot));
            lastSyncTime = currentTime;
        }
    }

    // 지연시간 측정 - 핑-퐁 메커니즘
    private void measureLatency() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPingTime >= PING_INTERVAL) {
            // PING 메시지 전송 (페이로드에 현재 시간 포함)
            sendPriorityMessage(new GameMessage(GameMessage.MessageType.PING, "CLIENT", currentTime));
            lastPingTime = currentTime;
        }
    }

    // 연결 상태 모니터링
    private void monitorConnection() {
        measureLatency();
        // TODO: 소켓의 상태를 확인하여 강제 종료 여부 판단
        // if (socket.isClosed() || !socket.isConnected()) { onConnectionLost(); }
    }

    // 재연결 시도
    private void attemptReconnection() {
        long currentTime = System.currentTimeMillis();
        if (reconnectAttempts < MAX_RETRY_COUNT && currentTime - lastReconnectTime >= RECONNECT_DELAY) {
            lastReconnectTime = currentTime;
            reconnectAttempts++;
            System.out.println("재연결 시도 중... (" + reconnectAttempts + "/" + MAX_RETRY_COUNT + ")");

            // TODO: 실제 연결 시도 로직
            try {
                // socket = new Socket(...)
                // 스트림 재초기화
                // Handshake 재시도
                TimeUnit.MILLISECONDS.sleep(500);

                // 연결 성공 시
                isConnected.set(true);
                reconnectAttempts = 0;
                onConnectionEstablished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // catch (IOException e) { 연결 실패 }
        } else if (reconnectAttempts >= MAX_RETRY_COUNT) {
            System.err.println("최대 재연결 횟수 초과. 연결 복구 실패.");
            // 치명적인 오류 처리
            shutdown();
        }
    }

    // === 외부 인터페이스 ===

    // 메시지 전송 요청 - 게임 스레드에서 호출
    public void sendMessage(GameMessage message) {
        outgoingQueue.offer(message);
    }

    // 우선순위 메시지 전송 - 즉시 처리가 필요한 메시지
    public void sendPriorityMessage(GameMessage message) {
        priorityQueue.offer(message);
    }

    // 수신된 메시지 가져오기 - 게임 스레드에서 호출 (GameThread가 직접 가져가는 방식)
    public GameMessage getReceivedMessage() {
        return incomingQueue.poll();
    }

    // 현재 지연시간 반환
    public long getCurrentLatency() {
        return currentLatency;
    }

    // 연결 상태 확인
    public boolean isConnected() {
        return isConnected.get();
    }

    // 네트워크 통계 정보 반환
    public NetworkStats getNetworkStats() {
        return new NetworkStats(currentLatency, outgoingQueue.size());
    }

    // 스레드 종료
    public void shutdown() {
        isRunning.set(false);
        // TODO: 소켓 및 스트림 정리
        // try { socket.close(); } catch (IOException e) { /* ignore */ }
    }

    // === 이벤트 처리 ===

    // 연결 성공 이벤트
    private void onConnectionEstablished() {
        System.out.println("네트워크 연결 성공!");
        networkManager.handleConnectionEstablished();
    }

    // 연결 끊김 이벤트
    private void onConnectionLost() {
        isConnected.set(false);
        System.err.println("네트워크 연결 끊김! 재연결 시도...");
        networkManager.handleConnectionLost();
        lastReconnectTime = System.currentTimeMillis(); // 재연결 타이머 시작
    }

    // 지연 경고 이벤트 - 200ms 초과 시
    private void onLatencyWarning(long latency) {
        System.out.println("!!! 랙 경고: 지연시간 " + latency + "ms");
        networkManager.handleLatencyWarning(latency);
        // TODO: GameThread에 랙 경고를 보내 게임 속도를 늦추거나 보정 로직을 실행하도록 요청
    }

    // 네트워크 에러 이벤트
    private void onNetworkError(Exception error) {
        System.err.println("치명적인 네트워크 에러 발생: " + error.getMessage());
        // 에러 발생 시 연결 끊김 처리
        onConnectionLost();
    }
}
