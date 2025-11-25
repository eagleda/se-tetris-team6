package tetris.network.server;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 서버에서 개별 클라이언트와의 통신을 담당
 * - 각 클라이언트마다 하나씩 생성되는 핸들러
 * - 클라이언트로부터 메시지 수신 및 처리
 * - 클라이언트에게 메시지 전송
 * - 연결 상태 모니터링 및 예외 처리
 */
public class ServerHandler implements Runnable {
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

    // 생성자 - 클라이언트 소켓과 서버 참조 받음
    public ServerHandler(Socket clientSocket, GameServer server);

    // 스레드 실행 메서드 - 클라이언트 메시지 수신 루프
    @Override
    public void run();

    // 클라이언트로부터 메시지 수신 및 처리
    private void handleMessage(GameMessage message);

    // 클라이언트에게 메시지 전송
    public void sendMessage(GameMessage message);

    // 연결 초기화 - 스트림 설정 및 클라이언트 ID 할당
    private void initializeConnection();

    // 핑 처리 - 지연시간 측정 및 연결 상태 확인
    private void handlePing(GameMessage pingMessage);

    // 게임 입력 처리 - 클라이언트의 키 입력을 다른 클라이언트에게 전달
    private void handlePlayerInput(GameMessage inputMessage);

    // 공격 처리 - 한 플레이어의 공격을 상대방에게 전달
    private void handleAttackLines(GameMessage attackMessage);

    // 연결 종료 처리 - 리소스 정리 및 서버에 알림
    public void disconnect();

    // 연결 상태 확인
    public boolean isConnected();

    // 클라이언트 ID 반환
    public String getClientId();
}
