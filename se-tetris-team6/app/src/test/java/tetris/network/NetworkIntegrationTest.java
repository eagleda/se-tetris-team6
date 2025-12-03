package tetris.network;

import tetris.network.client.GameClient;
import tetris.network.server.GameServer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*; // assertTrue, assertNotNull 등을 사용하기 위해 필요
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Step 2: 기본 서버-클라이언트 연결 및 핸드셰이크 통합 테스트
 * - GameServer를 시작하고, GameClient가 연결하여 메시지를 주고받는지 검증합니다.
 * 
 * 주의: 이 테스트를 실행하려면 프로젝트에 JUnit 5 의존성이 추가되어 있어야 합니다.
 */
public class NetworkIntegrationTest {

    private static final String TEST_IP = "127.0.0.1";

    /**
     * 서버 시작, 클라이언트 연결, 핸드셰이크 성공 여부를 통합 테스트합니다.
     */
    @Test
    void testServerClientConnectionAndHandshake() {
        System.out.println("--- 네트워크 통합 테스트 시작: 연결 및 핸드셰이크 ---");

        GameServer server = new GameServer();
        GameClient client = new GameClient();
        Thread serverThread = null;
        int port = findFreePort();

        // 1. 핸드셰이크 완료를 기다리기 위한 Latch 생성 (카운트 1)
        CountDownLatch handshakeLatch = new CountDownLatch(1);

        try {
            // 1. 서버 시작 스레드
            serverThread = new Thread(() -> {
                try {
                    server.startServer(port);
                } catch (IOException e) {
                    System.err.println("[TEST ERROR] Server failed to start: " + e.getMessage());
                }
            }, "TestServerThread");
            serverThread.start();

            // 서버가 소켓을 열 시간을 잠시 기다림 (필수는 아니지만 안정성을 위해)
            Thread.sleep(100);

            // 서버가 포트를 바인딩할 시간을 잠시 줍니다.
            TimeUnit.MILLISECONDS.sleep(500);

            // 2. 클라이언트 연결 시도
            boolean connected = client.connectToServer(TEST_IP, port, handshakeLatch);

            assertTrue(connected, "클라이언트가 서버에 연결해야 합니다.");

            // 3. Latch가 0이 될 때까지 최대 5초간 기다림 (핸드셰이크 완료 대기)
            boolean handshakeCompleted = handshakeLatch.await(5, TimeUnit.SECONDS);

            assertTrue(handshakeCompleted, "핸드셰이크가 5초 내에 완료되어야 합니다. (타임아웃 발생)");

            // 4. 연결 상태 최종 확인
            assertTrue(client.isConnected(), "클라이언트 연결 상태가 true여야 합니다.");

            // [검증 1] 소켓 연결 성공 여부
            assertTrue(connected, "1. 소켓 연결 실패: GameClient.connectToServer()가 false를 반환했습니다.");
            System.out.println("[TEST INFO] 1. 소켓 연결 성공.");

            // 3. 핸드셰이크가 완료될 시간을 충분히 줍니다.
            System.out.println("[TEST INFO] 2. 서버의 CONNECTION_ACCEPTED 응답 대기 중 (1초)..");
            TimeUnit.SECONDS.sleep(1);

            // [검증 2] 핸드셰이크 성공 여부 (ID 할당 확인)
            assertTrue(client.isConnected(), "2. 핸드셰이크 실패: 클라이언트가 연결 상태가 아닙니다.");
            assertNotNull(client.getPlayerId(), "2. 핸드셰이크 실패: Player ID가 할당되지 않았습니다.");
            assertTrue(client.getPlayerId().startsWith("Player-"), "2. 핸드셰이크 실패: Player ID 형식이 'Player-'로 시작하지 않습니다.");
            
            System.out.println("[TEST SUCCESS] 2. 핸드셰이크 성공. 할당된 ID: " + client.getPlayerId());

        } catch (Exception e) {
            // 테스트 중 발생한 모든 예외는 테스트 실패로 처리
            fail("테스트 중 예상치 못한 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        } finally {
            // 4. 정리 (서버와 클라이언트 연결 종료)
            if (client != null) {
                client.disconnect();
            }
            if (server != null) {
                server.stopServer();
            }
            
            // 서버 스레드가 완전히 종료될 때까지 대기
            if (serverThread != null) {
                try {
                    serverThread.join(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("--- 네트워크 통합 테스트 종료 ---");
        }
    }

    /** 사용 가능한 임시 포트를 할당한다. 실패 시 55555 반환. */
    private int findFreePort() {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return 55555;
        }
    }
}
