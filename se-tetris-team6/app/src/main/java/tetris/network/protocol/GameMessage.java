package tetris.network.protocol;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 네트워크를 통해 주고받는 모든 메시지의 기본 클래스
 * - 직렬화 가능해야 Socket으로 전송 가능
 * - 메시지 타입, 발신자, 데이터, 타임스탬프 포함
 * - 패킷 순서 보장을 위한 시퀀스 번호 관리
 */
public class GameMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final AtomicInteger sequenceGenerator = new AtomicInteger(0);

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
    public GameMessage(MessageType type, String senderId, Object payload) {
        this.type = type;
        this.senderId = senderId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
        this.sequenceNumber = sequenceGenerator.getAndIncrement();
    }

    // Getters
    public MessageType getType() { return type; }
    public String getSenderId() { return senderId; }
    public Object getPayload() { return payload; }
    public long getTimestamp() { return timestamp; }
    public int getSequenceNumber() { return sequenceNumber; }

    // toString() 메서드 (디버깅용)
    @Override
    public String toString() {
        return "GameMessage{" +
                "type=" + type +
                ", senderId='" + senderId + '\'' +
                ", payload=" + payload +
                ", timestamp=" + timestamp +
                ", sequenceNumber=" + sequenceNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMessage that = (GameMessage) o;
        return timestamp == that.timestamp &&
                sequenceNumber == that.sequenceNumber &&
                type == that.type &&
                Objects.equals(senderId, that.senderId) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, senderId, payload, timestamp, sequenceNumber);
    }
}
