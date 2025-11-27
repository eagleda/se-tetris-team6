package tetris.network;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import tetris.concurrent.GameThread;
import tetris.network.NetworkManager;
import tetris.network.protocol.PlayerInput;
import tetris.network.protocol.AttackLine;
import tetris.network.protocol.InputType;

// 임시 더미 클래스 (실제 구현 시 tetris.domain.model.* 등으로 대체)
class ThreadSafeGameModel {
    private int blockX = 5; // 블록의 X 좌표 (테스트용)
    private final String playerId;
    private final java.util.concurrent.BlockingQueue<AttackLine[]> incomingAttacks = new java.util.concurrent.LinkedBlockingQueue<>();
    
    public ThreadSafeGameModel(String playerId) { this.playerId = playerId; }
    
    // P1의 입력을 P2의 모델이 받아 처리하는 메서드 (NetworkManager -> GameThread -> Model)
    public boolean moveBlock(tetris.concurrent.Direction direction) { 
        if (direction == tetris.concurrent.Direction.LEFT) {
            blockX--;
            return true;
        }
        // ... 다른 방향 처리
        return false;
    }
    
    public int getBlockX() { return blockX; }
    
    // GameThread.java에 receiveAttack(AttackLine[]) 메서드가 NetworkManager로부터 호출됨.
    // 이 메서드는 이 Model의 applyAttack을 호출해야 합니다.
    public void applyAttack(AttackLine[] attacks) {
        incomingAttacks.offer(attacks);
    }
    
    public int getIncomingAttackCount() {
        return incomingAttacks.size();
    }
    
    public AttackLine[] pollIncomingAttack() {
        return incomingAttacks.poll();
    }
}

// 실제 GameServer, GameClient 클래스가 필요합니다.
// 현재는 NetworkManager가 서버/클라이언트 역할을 모두 수행한다고 가정합니다.
public class NetworkEndTest {

    // 테스트에 필요한 구성 요소
    private final int TEST_PORT = 12345;
    private NetworkManager client1Manager;
    private NetworkManager client2Manager;
    private GameThread client1GameThread;
    private GameThread client2GameThread;
    private ThreadSafeGameModel client1Model; 
    private ThreadSafeGameModel client2Model;

    @BeforeEach
    void setup() throws Exception {
        // 1. 게임 모델 및 스레드 설정
        client1Model = new ThreadSafeGameModel("P1");
        client2Model = new ThreadSafeGameModel("P2");
        
        // GameThread는 NetworkManager에 의해 시작될 수 있도록 준비
        // GameThread 생성자: (gameModel, playerId, isLocal)
        client1GameThread = new GameThread(client1Model, "P1", true);
        client2GameThread = new GameThread(client2Model, "P2", true);
        
        // 2. NetworkManager 설정 (GameThread와 연결)
        client1Manager = new NetworkManager(client1GameThread);
        client2Manager = new NetworkManager(client2GameThread);
        
        // GameThread 시작 (실제 게임 루프 시작)
        new Thread(client1GameThread).start();
        new Thread(client2GameThread).start();
        
        // 3. 네트워크 연결 시도 (P1이 서버, P2가 클라이언트 역할)
        // **주의: 이 부분은 실제 소켓 구현이 필요합니다.**
        client1Manager.startAsServer(TEST_PORT);
        client2Manager.connectAsClient("localhost", TEST_PORT);
        
        // 4. 연결이 완료될 때까지 대기 (E2E 테스트에서 가장 중요한 부분)
        // 실제로는 NetworkEventListener를 통해 연결 성공 이벤트를 받아야 합니다.
        Thread.sleep(1500); // 임시 대기
        
        assertTrue(client1Manager.isConnected(), "Client 1 (Server) should be connected.");
        assertTrue(client2Manager.isConnected(), "Client 2 (Client) should be connected.");
    }

    @AfterEach
    void teardown() {
        client1Manager.disconnect();
        client2Manager.disconnect();
        client1GameThread.stopGame();
        client2GameThread.stopGame();
    }

    @Test
    @DisplayName("P1의 입력이 P2의 모델에 반영되는지 검증")
    void testInputSynchronization() throws InterruptedException {
        // 1. P2의 현재 블록 위치 확인 (P2의 모델은 P1의 블록을 추적해야 함)
        int initialX = client2Model.getBlockX(); // 현재는 P2 자신의 블록으로 테스트 (상대방 블록 추적 로직 필요)

        // 2. P1의 GameThread에 입력 강제 호출 (MOVE_LEFT)
        PlayerInput moveLeft = new PlayerInput(InputType.MOVE_LEFT);
        client1GameThread.addPlayerInput(moveLeft);
        
        // 3. 네트워크 전송 및 P2의 GameThread 처리 대기
        Thread.sleep(200); 

        // 4. P2의 모델에서 P1의 블록 위치가 왼쪽으로 이동했는지 검증
        // **주의: 현재 P2의 Model은 P1의 블록을 추적하는 로직이 없으므로, 이 테스트는 P2가 P1의 입력을 받아 P2의 보드에 적용하는지 확인하는 것으로 대체합니다.**
        // P2의 모델이 P1의 입력을 받으면 P2의 블록이 움직여야 함 (P2의 GameThread가 P1의 입력을 처리해야 함)
        int finalX = client2Model.getBlockX();
        
        // P1의 입력이 P2에게 전달되어 P2의 GameThread가 P2의 Model을 움직이게 했는지 확인
        // (이것은 P2P 동기화 방식에 따라 다름. 입력 동기화 방식이라면 P2의 Model이 움직여야 함)
        assertEquals(initialX - 1, finalX, "Client 1의 입력이 Client 2의 모델에 반영되어야 합니다.");
    }

    @Test
    @DisplayName("P1의 줄 삭제가 P2에게 공격으로 전송되는지 검증")
    void testAttackSynchronization() throws InterruptedException {
        // 1. P1에서 줄 삭제 시뮬레이션 -> NetworkManager.sendAttackLines 호출 유도
        
        // GameThread의 handleLineClear(LineClearResult) 메서드를 호출할 방법이 없으므로,
        // NetworkManager의 sendAttackLines를 직접 호출하여 시뮬레이션합니다.
        AttackLine[] attack = {new AttackLine(1), new AttackLine(1)};
        client1Manager.sendAttackLines(attack);
        
        // 2. 네트워크 전송 및 P2의 GameThread 처리 대기
        Thread.sleep(200);
        
        // 3. P2의 모델/GameThread에 공격 라인이 추가되었는지 검증
        // P2의 GameThread는 NetworkManager로부터 메시지를 받아 Model.applyAttack을 호출해야 합니다.
        
        assertEquals(1, client2Model.getIncomingAttackCount(), "Client 2의 모델에 1개의 공격 큐가 추가되어야 합니다.");
        AttackLine[] receivedAttack = client2Model.pollIncomingAttack();
        assertEquals(2, receivedAttack.length, "수신된 공격 라인의 개수가 2개여야 합니다.");
    }
}
