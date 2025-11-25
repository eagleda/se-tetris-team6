package tetris.network.client;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

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
    public boolean connectToServer(String ip, int port);

    // 서버와 연결 해제
    public void disconnect();

    // 게임 메시지 전송 (비동기)
    public void sendMessage(GameMessage message);

    // 플레이어 입력 전송 (키보드 입력)
    public void sendPlayerInput(PlayerInput input);

    // 공격 라인 전송 (줄 삭제 시)
    public void sendAttackLines(AttackLine[] lines);

    // 게임 시작 준비 완료 신호
    public void sendReadySignal();

    // 수신된 메시지 처리 - 메인 게임 루프에서 호출
    public void processIncomingMessages();

    // 연결 상태 확인
    public boolean isConnected();

    // 지연시간 측정
    public long getLatency();

    // 게임 상태 리스너 등록
    public void setGameStateListener(GameStateListener listener);

    // 최근 접속 IP 저장/불러오기
    public void saveRecentIP(String ip);
    public String getRecentIP();

    // 재연결 시도
    private void attemptReconnection();

    // 연결 상태 모니터링 (별도 스레드)
    private void monitorConnection();
}
