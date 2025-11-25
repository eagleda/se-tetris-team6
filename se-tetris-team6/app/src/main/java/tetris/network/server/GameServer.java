package tetris.network.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * P2P 대전 모드의 서버 역할을 담당
 * - 클라이언트 연결 대기 및 수락
 * - 연결된 클라이언트들 관리
 * - 게임 상태 동기화 및 메시지 중계
 * - 서버 생명주기 관리 (시작, 정지, 재시작)
 */
public class GameServer {
    // === 네트워크 관련 ===
    private ServerSocket serverSocket;          // 서버 소켓
    private boolean isRunning;                  // 서버 실행 상태
    private int port;                          // 서버 포트

    // === 클라이언트 관리 ===
    private List<ServerHandler> connectedClients;  // 연결된 클라이언트 목록
    private ExecutorService clientThreadPool;      // 클라이언트 처리용 스레드 풀

    // === 게임 관리 ===
    private GameState currentGameState;         // 현재 게임 상태
    private String selectedGameMode;            // 선택된 게임 모드
    private boolean gameInProgress;             // 게임 진행 중 여부

    // === 주요 메서드들 ===

    // 서버 시작 - 지정된 포트에서 클라이언트 연결 대기
    public void startServer(int port) throws IOException;

    // 서버 중지 - 모든 연결 종료 및 리소스 정리
    public void stopServer();

    // 클라이언트 연결 수락 - 새로운 클라이언트가 접속했을 때
    private void acceptClient(Socket clientSocket);

    // 모든 클라이언트에게 메시지 브로드캐스트
    public void broadcastMessage(GameMessage message);

    // 특정 클라이언트에게만 메시지 전송
    public void sendToClient(String clientId, GameMessage message);

    // 클라이언트 연결 해제 처리
    public void removeClient(ServerHandler client);

    // 게임 모드 선택 (서버가 결정)
    public void selectGameMode(String mode);

    // 게임 시작 - 모든 클라이언트가 준비되었을 때
    public void startGame();

    // 게임 상태 동기화 - 주기적으로 호출
    private void synchronizeGameState();

    // 서버 상태 정보 반환 (연결된 클라이언트 수, 게임 상태 등)
    public ServerStatus getServerStatus();
}
