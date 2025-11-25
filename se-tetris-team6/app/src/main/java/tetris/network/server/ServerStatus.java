package tetris.network.server;

public class ServerStatus {
    private final int connectedClientCount;
    private final String selectedGameMode;
    private final boolean isGameInProgress;
    private final boolean isRunning;

    /**
     * 서버 상태 객체를 생성합니다.
     * @param connectedClientCount 현재 연결된 클라이언트 수
     * @param selectedGameMode 현재 선택된 게임 모드
     * @param isGameInProgress 게임 진행 여부
     * @param isRunning 서버 실행 여부
     */
    public ServerStatus(int connectedClientCount, String selectedGameMode, boolean isGameInProgress, boolean isRunning) {
        this.connectedClientCount = connectedClientCount;
        this.selectedGameMode = selectedGameMode;
        this.isGameInProgress = isGameInProgress;
        this.isRunning = isRunning;
    }

    // --- Getter 메서드 (Step 4에서 GameServer가 이 정보를 채워서 반환할 때 사용) ---
    public int getConnectedClientCount() {
        return connectedClientCount;
    }

    public String getSelectedGameMode() {
        return selectedGameMode;
    }

    public boolean isGameInProgress() {
        return isGameInProgress;
    }

    public boolean isRunning() {
        return isRunning;
    }
}