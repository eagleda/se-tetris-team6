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

// === 프로토콜 및 콜백 임포트 ===
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;
import tetris.network.INetworkThreadCallback; // <--- 이 부분이 핵심 수정
// ============================

// =================================================================
// 임시 더미 클래스 (NetworkThread에서 사용하는 유틸리티)
// =================================================================

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
 * ...
 */
public class NetworkThread implements Runnable {
    // === 상수 설정 (NetworkProtocol.java 참조) ===
    private static final long GAME_SYNC_INTERVAL = 50; // 50ms마다 동기화 시도
    private static final long PING_INTERVAL = 1000;    // 1초마다 핑
    private static final long MAX_LAG_THRESHOLD = 200; // 200ms 이상이면 랙 경고
    private static final int MAX_RETRY_COUNT = 3;     // 최대 재연결 시도 횟수
    private static final long RECONNECT_DELAY = 1000;  // 1초 후 재시도

    // === 네트워크 관리 ===
    // NetworkManager 대신 INetworkThreadCallback 인터페이스를 사용합니다.
    private final INetworkThreadCallback callback;     // 콜백 인터페이스 참조
    private final AtomicBoolean isRunning = new AtomicBoolean(true); // 스레드 실행 상태
    private final AtomicBoolean isConnected = new AtomicBoolean(false); // 네트워크 연결 상태

    // === 메시지 큐 관리 ===
    private final BlockingQueue<GameMessage> outgoingQueue = new LinkedBlockingQueue<>();    // 송신 대기 메시지
    private final BlockingQueue<GameMessage> incomingQueue = new LinkedBlockingQueue<>();    // 수신된 메시지 (GameThread가 가져감)
    private final BlockingQueue<GameMessage> priorityQueue = new LinkedBlockingQueue<>();    // 우선순위 메시지 (핑, 에러 등)

    // === 동기화 관리 ===
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

    // 생성자 - INetworkThreadCallback 구현체(NetworkManager) 참조 받음
    public NetworkThread(INetworkThreadCallback callback) { // <--- 생성자 수정
        this.callback = callback;
        this.lastPingTime = System.currentTimeMillis();
        this.lastSyncTime = System.currentTimeMillis();
    }

    // 스레드 메인 실행 루프 (이하 생략 - 로직은 동일)
    @Override
    public void run() {
        System.out.println("NetworkThread 시작됨.");
        attemptConnection();

        while (isRunning.get()) {
            if (isConnected.get()) {
                processPriorityMessages();
                processSendQueue();
                processReceiveQueue();
                monitorConnection();
                synchronizeGameState();

            } else {
                attemptReconnection();
            }

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
            // 수신된 메시지를 콜백을 통해 NetworkManager로 전달
            callback.handleReceivedMessage(message); // <--- 여기서 callback 사용
        }
    }

    // 우선순위 메시지 처리 - 핑, 에러, 연결 관련 메시지
    private void processPriorityMessages() {
        GameMessage message;
        while ((message = priorityQueue.poll()) != null) {
            switch (message.getType()) {
                case PING:
                    sendPriorityMessage(new GameMessage(MessageType.PONG, "CLIENT", message.getPayload()));
                    break;
                case PONG:
                    long pingTime = (Long) message.getPayload();
                    long latency = System.currentTimeMillis() - pingTime;
                    currentLatency = latency;
                    latencyHistory.offer(latency);
                    if (latency > MAX_LAG_THRESHOLD) {
                        onLatencyWarning(latency);
                    }
                    break;
                case ERROR:
                    System.err.println("Received ERROR from server: " + message.getPayload());
                    break;
                case DISCONNECT:
                    onConnectionLost();
                    break;
                default:
                    break;
            }
        }
    }

    // 게임 상태 동기화 - 주기적으로 호출
    private void synchronizeGameState() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncTime >= syncInterval) {
            lastSyncTime = currentTime;
        }
    }

    // 지연시간 측정 - 핑-퐁 메커니즘
    private void measureLatency() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPingTime >= PING_INTERVAL) {
            sendPriorityMessage(new GameMessage(MessageType.PING, "CLIENT", currentTime));
            lastPingTime = currentTime;
        }
    }

    // 연결 상태 모니터링
    private void monitorConnection() {
        measureLatency();
        // TODO: 소켓의 상태를 확인하여 강제 종료 여부 판단
    }

    // 재연결 시도
    private void attemptReconnection() {
        long currentTime = System.currentTimeMillis();
        if (reconnectAttempts < MAX_RETRY_COUNT && currentTime - lastReconnectTime >= RECONNECT_DELAY) {
            lastReconnectTime = currentTime;
            reconnectAttempts++;
            System.out.println("재연결 시도 중... (" + reconnectAttempts + "/" + MAX_RETRY_COUNT + ")");

            try {
                TimeUnit.MILLISECONDS.sleep(500);

                // 연결 성공 시
                isConnected.set(true);
                reconnectAttempts = 0;
                onConnectionEstablished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else if (reconnectAttempts >= MAX_RETRY_COUNT) {
            System.err.println("최대 재연결 횟수 초과. 연결 복구 실패.");
            shutdown();
        }
    }

    // === 외부 인터페이스 ===

    public void sendMessage(GameMessage message) {
        outgoingQueue.offer(message);
    }

    public void sendPriorityMessage(GameMessage message) {
        priorityQueue.offer(message);
    }

    public GameMessage getReceivedMessage() {
        return incomingQueue.poll();
    }

    public long getCurrentLatency() {
        return currentLatency;
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public NetworkStats getNetworkStats() {
        return new NetworkStats(currentLatency, outgoingQueue.size());
    }

    public void shutdown() {
        isRunning.set(false);
    }

    // === 이벤트 처리 ===

    private void onConnectionEstablished() {
        System.out.println("네트워크 연결 성공!");
        callback.handleConnectionEstablished(); // <--- 여기서 callback 사용
    }

    private void onConnectionLost() {
        isConnected.set(false);
        System.err.println("네트워크 연결 끊김! 재연결 시도...");
        callback.handleConnectionLost(); // <--- 여기서 callback 사용
        lastReconnectTime = System.currentTimeMillis();
    }

    private void onLatencyWarning(long latency) {
        System.out.println("!!! 랙 경고: 지연시간 " + latency + "ms");
        callback.handleLatencyWarning(latency); // <--- 여기서 callback 사용
    }

    private void onNetworkError(Exception error) {
        System.err.println("치명적인 네트워크 에러 발생: " + error.getMessage());
        onConnectionLost();
        callback.handleNetworkError(error); // <--- 여기서 callback 사용
    }
}