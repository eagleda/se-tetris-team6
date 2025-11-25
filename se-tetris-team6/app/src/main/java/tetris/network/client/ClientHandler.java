package tetris.network.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 클라이언트에서 서버와의 통신을 담당
 * - 서버로부터 메시지 수신 및 처리
 * - 서버에게 메시지 전송
 * - 네트워크 예외 상황 처리
 * - 지연시간 측정 및 연결 품질 모니터링
 */
public class ClientHandler implements Runnable {
    // === 네트워크 관련 ===
    private ObjectInputStream inputStream;     // 서버로부터 메시지 수신
    private ObjectOutputStream outputStream;   // 서버에게 메시지 송신

    // === 클라이언트 참조 ===
    private GameClient client;                 // 부모 클라이언트 참조

    // === 지연시간 측정 ===
    private long lastPingTime;                 // 마지막 핑 전송 시간
    private long currentLatency;               // 현재 지연시간
    private boolean waitingForPong;            // 퐁 응답 대기 중

    // === 주요 메서드들 ===

    // 생성자 - 스트림과 클라이언트 참조 받음
    public ClientHandler(ObjectInputStream input, ObjectOutputStream output, GameClient client);

    // 스레드 실행 메서드 - 서버 메시지 수신 루프
    @Override
    public void run();

    // 서버로부터 메시지 수신 및 처리
    private void handleMessage(GameMessage message);

    // 서버에게 메시지 전송
    public void sendMessage(GameMessage message);

    // 연결 승인 처리 - 서버가 연결을 승인했을 때
    private void handleConnectionAccepted(GameMessage message);

    // 게임 모드 선택 처리 - 서버가 게임 모드를 알려줄 때
    private void handleGameModeSelect(GameMessage message);

    // 게임 시작 처리 - 서버가 게임 시작 신호를 보낼 때
    private void handleGameStart(GameMessage message);

    // 상대방 입력 처리 - 상대방의 키 입력을 받을 때
    private void handleOpponentInput(GameMessage message);

    // 공격 받기 처리 - 상대방의 공격 라인을 받을 때
    private void handleIncomingAttack(GameMessage message);

    // 퐁 처리 - 지연시간 계산
    private void handlePong(GameMessage message);

    // 주기적 핑 전송 - 지연시간 측정 및 연결 확인
    private void sendPing();

    // 에러 처리 - 네트워크 오류 발생 시
    private void handleError(Exception e);

    // 현재 지연시간 반환
    public long getLatency();
}
