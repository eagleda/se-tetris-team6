package tetris.network.protocol;

import java.io.Serializable;

/**
 * 네트워크를 통해 주고받는 모든 메시지의 기본 클래스
 * - 직렬화 가능해야 Socket으로 전송 가능
 * - 메시지 타입, 발신자, 데이터, 타임스탬프 포함
 * - 패킷 순서 보장을 위한 시퀀스 번호 관리
 */
public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // 메시지 타입 (연결, 게임상태, 입력 등)
    private MessageType type;

    // 발신자 ID (서버는 "SERVER", 클라이언트는 고유 ID)
    private String senderId;

    // 실제 전송할 데이터 (게임 상태, 입력, 공격 정보 등)
    private Object payload;

    // 메시지 생성 시간 (지연시간 측정용)
    private long timestamp;

    // 패킷 순서 보장용 시퀀스 번호
    private int sequenceNumber;

    // 생성자, getter, setter 메서드들
    // toString() 메서드 (디버깅용)
}
