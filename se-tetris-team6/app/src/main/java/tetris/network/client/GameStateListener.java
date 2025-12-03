package tetris.network.client;

import tetris.network.protocol.GameMessage;

/**
 * 서버로부터 수신된 게임 상태 관련 메시지를 처리하기 위한 리스너 인터페이스.
 * GameClient가 메시지를 수신하면 이 리스너를 통해 UI/게임 로직에 전달합니다.
 */
public interface GameStateListener {
    
    /**
     * 상대방의 테트리스 보드 상태가 업데이트되었을 때 호출됩니다.
     * @param message 상대방의 보드 상태를 포함하는 GameMessage
     */
    void onOpponentBoardUpdate(GameMessage message);

    /**
     * 게임 모드 선택, 게임 시작 등 주요 게임 상태 변경 시 호출됩니다.
     * @param message 상태 변경 정보를 포함하는 GameMessage
     */
    void onGameStateChange(GameMessage message);

    /**
     * 호스트로부터 권위 있는 게임 상태 스냅샷을 수신했을 때 호출됩니다.
     * 클라이언트는 이 스냅샷을 적용하여 화면을 렌더링합니다.
     * @param snapshot 게임 보드, 블록, 점수, 시간 등을 포함하는 스냅샷
     */
    void onGameStateSnapshot(tetris.network.protocol.GameSnapshot snapshot);
    
    /**
     * 네트워크 연결 타임아웃 또는 연결 끊김 시 호출됩니다.
     * @param reason 연결 끊김 이유
     */
    default void onConnectionTimeout(String reason) {
        // 기본 구현은 비어있음
    }
    
    // Step 3, 4에서 필요한 다른 메서드들을 추가할 수 있습니다.
}