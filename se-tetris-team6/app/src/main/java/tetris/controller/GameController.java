package tetris.controller;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import tetris.domain.BlockGenerator;
import tetris.domain.GameDifficulty;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
// 이제부터 모델의 좌우 움직임이 안 되는 이유를 해결합니다.

/**
 * Controller 역할을 수행하는 클래스.
 * View(GamePanel)와 Model(추후 생성할 GameModel)을 연결합니다.
 */
public class GameController {

    private final GameModel gameModel; // Model 참조

    // 키 반복 입력 제어를 위한 상태 추적
    private Map<Integer, Long> lastKeyPressTime;
    private static final long KEY_REPEAT_DELAY = 100; // 100ms
    private static final long MOVEMENT_REPEAT_DELAY = 150; // 이동키는 조금 더 느리게

    // 게임 일시정지 토글을 위한 상태
    private boolean pauseKeyPressed = false;

    // 키 바인딩 맵
    private Map<String, Integer> keyBindings;

    // 생성자에서 View와 Model을 주입받습니다.
    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.lastKeyPressTime = new HashMap<>();
        initializeDefaultKeyBindings();
        applyDifficulty(GameDifficulty.NORMAL);
    }

    /**
     * Apply updated key bindings at runtime. Only keys present in the map are updated.
     * Map keys are action names (same as used throughout this class) -> KeyEvent codes.
     */
    public void applyKeyBindings(java.util.Map<String, Integer> updated) {
        if (updated == null) return;
        for (java.util.Map.Entry<String, Integer> e : updated.entrySet()) {
            if (e.getValue() == null) continue;
            keyBindings.put(e.getKey(), e.getValue());
        }
    }

    public void applyDifficulty(GameDifficulty difficulty) {
        if (difficulty == null) {
            difficulty = GameDifficulty.NORMAL;
        }
        BlockGenerator generator = gameModel.getBlockGenerator();
        if (generator != null) {
            generator.setDifficulty(difficulty);
        }
    }

    /**
     * 기본 키 바인딩 초기화
     * 설정에서 변경 가능하도록 Map으로 관리
     */
    private void initializeDefaultKeyBindings() {
        keyBindings = new HashMap<>();

        // 게임 플레이 키
        keyBindings.put("MOVE_LEFT", KeyEvent.VK_LEFT);
        keyBindings.put("MOVE_RIGHT", KeyEvent.VK_RIGHT);
        keyBindings.put("MOVE_DOWN", KeyEvent.VK_DOWN);
        keyBindings.put("ROTATE_CW", KeyEvent.VK_UP);           // 시계방향 회전
        keyBindings.put("ROTATE_CCW", KeyEvent.VK_Z);           // 반시계방향 회전 (추가)
        keyBindings.put("HARD_DROP", KeyEvent.VK_SPACE);        // 하드 드롭
        keyBindings.put("HOLD", KeyEvent.VK_C);                 // 홀드 기능 (추가)

        // 게임 제어 키
        keyBindings.put("PAUSE", KeyEvent.VK_P);                // 일시정지/재개
        keyBindings.put("QUIT_GAME", KeyEvent.VK_Q);            // 게임 종료
        keyBindings.put("RESTART", KeyEvent.VK_R);              // 게임 재시작

        // 메뉴 네비게이션 키
        keyBindings.put("MENU_UP", KeyEvent.VK_UP);
        keyBindings.put("MENU_DOWN", KeyEvent.VK_DOWN);
        keyBindings.put("MENU_SELECT", KeyEvent.VK_ENTER);
        keyBindings.put("MENU_BACK", KeyEvent.VK_ESCAPE);

        // 설정 화면 키
        keyBindings.put("SETTINGS_RESET", KeyEvent.VK_DELETE);  // 설정 초기화
    }

    /**
     * 키보드 입력을 처리하는 메소드
     * @param keyCode 키 코드
     */
    public void handleKeyPress(int keyCode) {
        long currentTime = System.currentTimeMillis();

        // 키 반복 입력 무시
        if (shouldIgnoreKeyRepeat(keyCode, currentTime)) {
            return;
        }

        GameState currentState = gameModel.getCurrentState();

        switch (currentState) {
            case PLAYING:
                handleGamePlayInput(keyCode);
                break;
            case PAUSED:
                handlePausedInput(keyCode);
                break;
            case GAME_OVER:
                handleGameOverInput(keyCode);
                break;
            case SETTINGS:
                handleSettingsInput(keyCode);
                break;
            case SCOREBOARD:
                handleScoreboardInput(keyCode);
                break;
            case NAME_INPUT:
                handleNameInputInput(keyCode);
                break;
            default:
                // 기본 처리
                break;
        }

        // 키 입력 시간 기록
        lastKeyPressTime.put(keyCode, currentTime);
    }

    /**
     * 게임 플레이 중 키 입력 처리
     */
    private void handleGamePlayInput(int keyCode) {
        // 일시정지 키는 항상 우선 처리
        if (keyCode == keyBindings.get("PAUSE")) {
            if (!pauseKeyPressed) {
                gameModel.pauseGame();
                pauseKeyPressed = true;
                System.out.println("Controller: 게임 일시정지");
            }
            return;
        }

        // 게임 종료 키
        if (keyCode == keyBindings.get("QUIT_GAME")) {
            gameModel.quitToMenu();
            System.out.println("Controller: 메뉴로 돌아가기");
            return;
        }

        // 블록 조작 키들
        if (keyCode == keyBindings.get("MOVE_LEFT")) {
            gameModel.moveBlockLeft();
            System.out.println("Controller: 블록 왼쪽 이동");
        } else if (keyCode == keyBindings.get("MOVE_RIGHT")) {
            gameModel.moveBlockRight();
            System.out.println("Controller: 블록 오른쪽 이동");
        } else if (keyCode == keyBindings.get("MOVE_DOWN")) {
            gameModel.moveBlockDown();
            System.out.println("Controller: 블록 아래로 이동 (소프트 드롭)");
        } else if (keyCode == keyBindings.get("ROTATE_CW")) {
            gameModel.rotateBlockClockwise();
            System.out.println("Controller: 블록 시계방향 회전");
        } else if (keyCode == keyBindings.get("ROTATE_CCW")) {
            gameModel.rotateBlockCounterClockwise();
            System.out.println("Controller: 블록 반시계방향 회전");
        } else if (keyCode == keyBindings.get("HARD_DROP")) {
            gameModel.hardDropBlock();
            System.out.println("Controller: 하드 드롭 (즉시 하강)");
        } else if (keyCode == keyBindings.get("HOLD")) {
            gameModel.holdCurrentBlock();
            System.out.println("Controller: 블록 홀드");
        } else if (keyCode == keyBindings.get("RESTART")) {
            gameModel.restartGame();
            System.out.println("Controller: 게임 재시작");
        }
    }

    /**
     * 일시정지 상태에서의 키 입력 처리
     */
    private void handlePausedInput(int keyCode) {
        if (keyCode == keyBindings.get("PAUSE")) {
            if (!pauseKeyPressed) {
                gameModel.resumeGame();
                pauseKeyPressed = true;
                System.out.println("Controller: 게임 재개");
            }
        } else if (keyCode == keyBindings.get("QUIT_GAME")) {
            gameModel.quitToMenu();
            System.out.println("Controller: 메뉴로 돌아가기");
        } else if (keyCode == keyBindings.get("RESTART")) {
            gameModel.restartGame();
            System.out.println("Controller: 게임 재시작");
        }
    }

    /**
     * 게임 오버 화면에서의 키 입력 처리
     */
    private void handleGameOverInput(int keyCode) {
        if (keyCode == keyBindings.get("MENU_SELECT") || keyCode == KeyEvent.VK_ENTER) {
            gameModel.proceedFromGameOver();
            System.out.println("Controller: 게임 오버 화면에서 진행");
        } else if (keyCode == keyBindings.get("RESTART")) {
            gameModel.restartGame();
            System.out.println("Controller: 게임 재시작");
        } else if (keyCode == keyBindings.get("QUIT_GAME")) {
            gameModel.quitToMenu();
            System.out.println("Controller: 메뉴로 돌아가기");
        }
    }

    /**
     * 설정 화면에서의 키 입력 처리
     */
    private void handleSettingsInput(int keyCode) {
        if (keyCode == keyBindings.get("MENU_UP")) {
            gameModel.navigateSettingsUp();
            System.out.println("Controller: 설정 메뉴 위로 이동");
        } else if (keyCode == keyBindings.get("MENU_DOWN")) {
            gameModel.navigateSettingsDown();
            System.out.println("Controller: 설정 메뉴 아래로 이동");
        } else if (keyCode == keyBindings.get("MENU_SELECT")) {
            gameModel.selectCurrentSetting();
            System.out.println("Controller: 설정 항목 선택/변경");
        } else if (keyCode == keyBindings.get("MENU_BACK")) {
            gameModel.exitSettings();
            System.out.println("Controller: 설정 화면 나가기");
        } else if (keyCode == keyBindings.get("SETTINGS_RESET")) {
            gameModel.resetAllSettings();
            System.out.println("Controller: 모든 설정 초기화");
        }
    }

    /**
     * 스코어보드에서의 키 입력 처리
     */
    private void handleScoreboardInput(int keyCode) {
        if (keyCode == keyBindings.get("MENU_BACK") || keyCode == keyBindings.get("MENU_SELECT")) {
            gameModel.exitScoreboard();
            System.out.println("Controller: 스코어보드 나가기");
        } else if (keyCode == keyBindings.get("MENU_UP")) {
            gameModel.scrollScoreboardUp();
            System.out.println("Controller: 스코어보드 위로 스크롤");
        } else if (keyCode == keyBindings.get("MENU_DOWN")) {
            gameModel.scrollScoreboardDown();
            System.out.println("Controller: 스코어보드 아래로 스크롤");
        }
    }

    /**
     * 이름 입력 화면에서의 키 입력 처리
     */
    private void handleNameInputInput(int keyCode) {
        if (keyCode == KeyEvent.VK_ENTER) {
            gameModel.confirmNameInput();
            System.out.println("Controller: 이름 입력 완료");
        } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            gameModel.deleteCharacterFromName();
            System.out.println("Controller: 이름에서 문자 삭제");
        } else if (keyCode == KeyEvent.VK_ESCAPE) {
            gameModel.cancelNameInput();
            System.out.println("Controller: 이름 입력 취소");
        } else if (isValidNameCharacter(keyCode)) {
            char character = (char) keyCode;
            gameModel.addCharacterToName(character);
            System.out.println("Controller: 이름에 문자 추가 - " + character);
        }
    }

    /**
     * 키 반복 입력을 무시할지 결정
     */
    private boolean shouldIgnoreKeyRepeat(int keyCode, long currentTime) {
        if (!lastKeyPressTime.containsKey(keyCode)) {
            return false;
        }

        long lastTime = lastKeyPressTime.get(keyCode);
        long delay = getKeyRepeatDelay(keyCode);

        return (currentTime - lastTime) < delay;
    }

    /**
     * 키별 반복 입력 지연 시간 반환
     */
    private long getKeyRepeatDelay(int keyCode) {
        // 이동 키들은 조금 더 느린 반복
        if (keyCode == keyBindings.get("MOVE_LEFT") ||
            keyCode == keyBindings.get("MOVE_RIGHT") ||
            keyCode == keyBindings.get("MOVE_DOWN")) {
            return MOVEMENT_REPEAT_DELAY;
        }

        // 회전, 드롭 등은 기본 지연
        return KEY_REPEAT_DELAY;
    }

    /**
     * 이름 입력에 유효한 문자인지 확인
     */
    private boolean isValidNameCharacter(int keyCode) {
        return (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) ||
               (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) ||
               keyCode == KeyEvent.VK_SPACE;
    }

    /**
     * 게임 시작 메소드
     */
    public void startGame() {
        System.out.println("[LOG] GameController.startGame()");
        gameModel.changeState(GameState.PLAYING);
    }

    /**
     * 키 릴리즈 처리 (일시정지 키 상태 초기화)
     */
    public void handleKeyRelease(int keyCode) {
        if (keyCode == keyBindings.get("PAUSE")) {
            pauseKeyPressed = false;
        }
    }
}
