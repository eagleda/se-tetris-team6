package tetris.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 네트워크 통신을 담당하는 전용 스레드
 * - 네트워크 메시지 송수신 처리
 * - 게임 상태 동기화
 * - 지연시간 측정 및 모니터링
 * - 연결 상태 관리 및 재연결 처리
 * - 메시지 큐 관리 및 우선순위 처리
 */
public class NetworkThread implements Runnable {
    // === 네트워크 관리 ===
    private NetworkManager networkManager;     // 네트워크 매니저 참조
    private AtomicBoolean isRunning;           // 스레드 실행 상태
    private AtomicBoolean isConnected;         // 네트워크 연결 상태

    // === 메시지 큐 관리 ===
    private BlockingQueue<GameMessage> outgoingQueue;    // 송신 대기 메시지
    private BlockingQueue<GameMessage> incomingQueue;    // 수신된 메시지
    private BlockingQueue<GameMessage> priorityQueue;    // 우선순위 메시지 (핑, 에러 등)

    // === 동기화 관리 ===
    private Map<String, Long> lastSyncTime;             // 플레이어별 마지막 동기화 시간
    private long syncInterval;                          // 동기화 간격

    // === 지연시간 관리 ===
    private long currentLatency;               // 현재 지연시간
    private Queue<Long> latencyHistory;        // 지연시간 히스토리
    private long lastPingTime;                 // 마지막 핑 시간

    // === 재연결 관리 ===
    private int reconnectAttempts;             // 재연결 시도 횟수
    private long lastReconnectTime;            // 마지막 재연결 시도 시간

    // === 주요 메서드들 ===

    // 생성자 - 네트워크 매니저 참조 받음
    public NetworkThread(NetworkManager networkManager);

    // 스레드 메인 실행 루프
    @Override
    public void run();

    // 송신 메시지 처리 - 큐에서 메시지를 가져와 전송
    private void processSendQueue();

    // 수신 메시지 처리 - 네트워크에서 받은 메시지 처리
    private void processReceiveQueue();

    // 우선순위 메시지 처리 - 핑, 에러, 연결 관련 메시지
    private void processPriorityMessages();

    // 게임 상태 동기화 - 주기적으로 호출
    private void synchronizeGameState();

    // 지연시간 측정 - 핑-퐁 메커니즘
    private void measureLatency();

    // 연결 상태 모니터링
    private void monitorConnection();

    // 재연결 시도
    private void attemptReconnection();

    // === 외부 인터페이스 ===

    // 메시지 전송 요청 - 게임 스레드에서 호출
    public void sendMessage(GameMessage message);

    // 우선순위 메시지 전송 - 즉시 처리가 필요한 메시지
    public void sendPriorityMessage(GameMessage message);

    // 수신된 메시지 가져오기 - 게임 스레드에서 호출
    public GameMessage getReceivedMessage();

    // 현재 지연시간 반환
    public long getCurrentLatency();

    // 연결 상태 확인
    public boolean isConnected();

    // 네트워크 통계 정보 반환
    public NetworkStats getNetworkStats();

    // 스레드 종료
    public void shutdown();

    // === 이벤트 처리 ===

    // 연결 성공 이벤트
    private void onConnectionEstablished();

    // 연결 끊김 이벤트
    private void onConnectionLost();

    // 지연 경고 이벤트 - 200ms 초과 시
    private void onLatencyWarning(long latency);

    // 네트워크 에러 이벤트
    private void onNetworkError(Exception error);
}