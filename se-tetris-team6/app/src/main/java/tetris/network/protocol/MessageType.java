package tetris.network.protocol;

/**
 * 네트워크 메시지의 타입을 정의하는 열거형
 * - 연결 관련: 접속, 응답, 종료
 * - 게임 제어: 시작, 일시정지, 종료
 * - 게임 데이터: 플레이어 입력, 보드 상태, 공격
 * - 네트워크 상태: 핑퐁, 지연 경고
 */
public enum MessageType {
    // === 연결 관리 ===
    CONNECTION_REQUEST,     // 클라이언트 → 서버: 연결 요청
    CONNECTION_ACCEPTED,    // 서버 → 클라이언트: 연결 승인
    CONNECTION_REJECTED,    // 서버 → 클라이언트: 연결 거부
    DISCONNECT,            // 양방향: 연결 종료 알림

    // === 게임 제어 ===
    GAME_MODE_SELECT,      // 서버 → 클라이언트: 게임 모드 선택
    GAME_START,           // 양방향: 게임 시작 신호
    GAME_PAUSE,           // 양방향: 게임 일시정지
    GAME_END,             // 양방향: 게임 종료
    GAME_RESTART,         // 양방향: 재시작 요청

    // === 게임 데이터 ===
    PLAYER_INPUT,         // 클라이언트 → 서버: 키 입력 (이동, 회전 등)
    BOARD_STATE,          // 양방향: 보드 상태 동기화
    BLOCK_PLACEMENT,      // 양방향: 블록 배치 정보
    LINE_CLEAR,           // 양방향: 줄 삭제 정보
    ATTACK_LINES,         // 양방향: 공격 줄 전송
    SCORE_UPDATE,         // 양방향: 점수 업데이트

    // === 네트워크 상태 ===
    PING,                 // 지연시간 측정용 핑
    PONG,                 // 지연시간 측정용 퐁
    LAG_WARNING,          // 지연 경고
    SYNC_REQUEST,         // 동기화 요청

    // === 에러 처리 ===
    ERROR,                // 에러 메시지
    TIMEOUT               // 타임아웃 알림
}
