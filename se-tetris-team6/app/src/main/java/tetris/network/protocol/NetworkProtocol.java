package tetris.network.protocol;

/**
 * 네트워크 통신 규약과 상수들을 정의
 * - 포트 번호, 타임아웃 설정
 * - 메시지 크기 제한
 * - 재시도 횟수, 지연시간 임계값
 * - 프로토콜 버전 관리
 */
public class NetworkProtocol {
    // === 기본 설정 ===
    public static final int DEFAULT_PORT = 12345;
    public static final int PROTOCOL_VERSION = 1;
    public static final String CHARSET = "UTF-8";

    // === 타임아웃 설정 ===
    public static final int CONNECTION_TIMEOUT = 10000;    // 10초
    public static final int READ_TIMEOUT = 5000;           // 5초
    public static final int PING_INTERVAL = 1000;          // 1초마다 핑
    public static final int MAX_LAG_THRESHOLD = 200;       // 200ms 이상이면 랙 경고

    // === 재시도 설정 ===
    public static final int MAX_RETRY_COUNT = 3;
    public static final int RETRY_DELAY = 1000;            // 1초 후 재시도

    // === 메시지 크기 제한 ===
    public static final int MAX_MESSAGE_SIZE = 1024 * 10;  // 10KB
    public static final int BUFFER_SIZE = 4096;            // 4KB 버퍼

    // === 게임 설정 ===
    public static final int MAX_PLAYERS = 2;               // 최대 2명
    public static final int GAME_SYNC_INTERVAL = 50;       // 50ms마다 동기화

    // 유틸리티 메서드들
    // - 메시지 유효성 검사
    // - 프로토콜 버전 확인
    // - 에러 코드 정의
}
