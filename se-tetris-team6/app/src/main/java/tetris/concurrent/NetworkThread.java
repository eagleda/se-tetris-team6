package tetris.concurrent;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

/**
 * 네트워크 스레드의 최소 구현체. 연결 관리/재연결, 송수신 큐 등 필수 구성만
 * 남겨 multiplayer 코드를 컴파일 가능하게 한다.
 */
public class NetworkThread implements Runnable {
    private static final Duration LOOP_SLEEP = Duration.ofMillis(10);
    private static final Duration SYNC_INTERVAL = Duration.ofSeconds(1);
    private static final long LATENCY_WARNING_THRESHOLD = 200L;

    private final NetworkManager networkManager;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    private final BlockingQueue<GameMessage> outgoingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<GameMessage> incomingQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<GameMessage> priorityQueue = new LinkedBlockingQueue<>();

    private final Map<String, Long> lastSyncTime = new ConcurrentHashMap<>();
    private final Queue<Long> latencyHistory = new ArrayDeque<>();

    private volatile long currentLatency = 0L;
    private volatile long lastPingTime = 0L;
    private volatile int reconnectAttempts = 0;
    private volatile long lastReconnectTime = 0L;

    private volatile long messagesSent = 0L;
    private volatile long messagesReceived = 0L;

    public NetworkThread(NetworkManager networkManager) {
        this.networkManager = Objects.requireNonNull(networkManager, "networkManager");
    }

    @Override
    public void run() {
        if (!isRunning.compareAndSet(false, true)) {
            return;
        }

        try {
            while (isRunning.get()) {
                processPriorityMessages();
                processSendQueue();
                processReceiveQueue();
                synchronizeGameState();
                measureLatency();
                monitorConnection();
                sleepLoop();
            }
        } finally {
            isRunning.set(false);
            networkManager.close();
        }
    }

    private void processSendQueue() {
        GameMessage message;
        while ((message = outgoingQueue.poll()) != null) {
            if (networkManager.send(message)) {
                messagesSent++;
            } else {
                onNetworkError(new IllegalStateException("Failed to send message"));
                break;
            }
        }
    }

    private void processReceiveQueue() {
        GameMessage message;
        while ((message = networkManager.receive()) != null) {
            messagesReceived++;
            incomingQueue.offer(message);
            if (message.getType() == MessageType.PONG && lastPingTime > 0) {
                currentLatency = System.currentTimeMillis() - lastPingTime;
                latencyHistory.add(currentLatency);
                lastPingTime = 0L;
            }
        }
    }

    private void processPriorityMessages() {
        GameMessage priority;
        while ((priority = priorityQueue.poll()) != null) {
            outgoingQueue.offer(priority);
        }
    }

    private void synchronizeGameState() {
        long now = System.currentTimeMillis();
        lastSyncTime.entrySet().removeIf(entry -> now - entry.getValue() > SYNC_INTERVAL.toMillis());
    }

    private void measureLatency() {
        if (!isConnected.get() || lastPingTime != 0L) {
            return;
        }
        GameMessage ping = new GameMessage(MessageType.PING, "SYSTEM", null);
        if (networkManager.send(ping)) {
            messagesSent++;
            lastPingTime = System.currentTimeMillis();
        }
    }

    private void monitorConnection() {
        boolean connected = networkManager.isConnected();
        if (connected && !isConnected.getAndSet(true)) {
            onConnectionEstablished();
        } else if (!connected && isConnected.getAndSet(false)) {
            onConnectionLost();
            attemptReconnection();
        } else if (!connected) {
            attemptReconnection();
        }
    }

    private void attemptReconnection() {
        long now = System.currentTimeMillis();
        if (now - lastReconnectTime < 1_000L) {
            return;
        }
        lastReconnectTime = now;
        reconnectAttempts++;
        priorityQueue.offer(new GameMessage(MessageType.CONNECTION_REQUEST, "SYSTEM", reconnectAttempts));
    }

    public void sendMessage(GameMessage message) {
        if (message != null) {
            outgoingQueue.offer(message);
        }
    }

    public void sendPriorityMessage(GameMessage message) {
        if (message != null) {
            priorityQueue.offer(message);
        }
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
        return new NetworkStats(messagesSent, messagesReceived, currentLatency);
    }

    public void shutdown() {
        isRunning.set(false);
    }

    private void onConnectionEstablished() {
        reconnectAttempts = 0;
    }

    private void onConnectionLost() {
        latencyHistory.clear();
    }

    private void onLatencyWarning(long latency) {
        // 향후 UI 경고 시스템과 연결될 수 있도록 hook만 남겨둔다.
    }

    private void onNetworkError(Exception error) {
        if (error != null && currentLatency > LATENCY_WARNING_THRESHOLD) {
            onLatencyWarning(currentLatency);
        }
    }

    private void sleepLoop() {
        try {
            TimeUnit.MILLISECONDS.sleep(LOOP_SLEEP.toMillis());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}