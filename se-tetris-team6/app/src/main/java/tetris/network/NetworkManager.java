package tetris.network;

/**
 * 네트워크 기능의 통합 관리자 및 외부 인터페이스
 * - 서버/클라이언트 모드 통합 관리
 * - 게임 로직과 네트워크 계층 간의 인터페이스 제공
 * - 네트워크 상태 모니터링 및 에러 처리
 * - 설정 관리 및 최근 연결 정보 저장
 */
public class NetworkManager {
    // === 모드 관리 ===
    private NetworkMode currentMode;           // 현재 모드 (SERVER/CLIENT/OFFLINE)
    private GameServer server;                 // 서버 인스턴스
    private GameClient client;                 // 클라이언트 인스턴스

    // === 리스너 인터페이스 ===
    private NetworkEventListener eventListener;    // 네트워크 이벤트 리스너
    private GameDataListener gameDataListener;     // 게임 데이터 리스너

    // === 설정 관리 ===
    private NetworkSettings settings;          // 네트워크 설정

    // === 주요 메서드들 ===

    // 서버 모드로 시작
    public boolean startAsServer(int port);

    // 클라이언트 모드로 서버에 연결
    public boolean connectAsClient(String serverIP, int port);

    // 네트워크 연결 종료
    public void disconnect();

    // 현재 네트워크 모드 반환
    public NetworkMode getCurrentMode();

    // 연결 상태 확인
    public boolean isConnected();

    // === 게임 데이터 전송 메서드들 ===

    // 플레이어 입력 전송
    public void sendPlayerInput(PlayerInput input);

    // 공격 라인 전송
    public void sendAttackLines(AttackLine[] lines);

    // 게임 상태 동기화
    public void syncGameState(GameState state);

    // 게임 시작 신호
    public void sendGameStart();

    // === 리스너 등록 ===

    // 네트워크 이벤트 리스너 등록 (연결, 끊김, 에러 등)
    public void setNetworkEventListener(NetworkEventListener listener);

    // 게임 데이터 리스너 등록 (상대방 입력, 공격 등)
    public void setGameDataListener(GameDataListener listener);

    // === 상태 정보 ===

    // 현재 지연시간 반환
    public long getCurrentLatency();

    // 연결된 플레이어 수 반환
    public int getConnectedPlayerCount();

    // 네트워크 상태 정보 반환
    public NetworkStatus getNetworkStatus();

    // === 설정 관리 ===

    // 최근 접속 IP 저장
    public void saveRecentConnection(String ip, int port);

    // 최근 접속 정보 불러오기
    public ConnectionInfo getRecentConnection();

    // 네트워크 설정 변경
    public void updateSettings(NetworkSettings settings);
}

// === 열거형 및 인터페이스 정의 ===

enum NetworkMode {
    OFFLINE, SERVER, CLIENT
}

interface NetworkEventListener {
    void onConnected();
    void onDisconnected();
    void onConnectionError(String error);
    void onLatencyWarning(long latency);
}

interface GameDataListener {
    void onOpponentInput(PlayerInput input);
    void onIncomingAttack(AttackLine[] lines);
    void onGameStateUpdate(GameState state);
    void onGameStart();
    void onGameEnd(String winner);
}
