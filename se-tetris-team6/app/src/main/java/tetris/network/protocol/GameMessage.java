package tetris.network.protocol;

public class GameMessage {
    
}


/**
 * 네트워크를 통해 주고받는 모든 메시지의 기본 클래스
 * - 직렬화 가능해야 Socket으로 전송 가능
 * - 메시지 타입, 발신자, 데이터, 타임스탬프 포함
 * - 패킷 순서 보장을 위한 시퀀스 번호 관리
 */