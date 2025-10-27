package tetris.controller;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * Controller 역할을 수행하는 클래스.
 * View(GamePanel)와 Model(추후 생성할 GameModel)을 연결합니다.
 */
public class GameController {

    private final GameModel gameModel; // Model 참조

    // 설정 가능한 키 바인딩 (기본값)
    private Map<String, Integer> keyBindings;

     // 키 반복 입력 제어를 위한 상태 추적
    private Map<Integer, Long> lastKeyPressTime;
    private static final long KEY_REPEAT_DELAY = 100; // 100ms
    private static final long MOVEMENT_REPEAT_DELAY = 150; // 이동키는 조금 더 느리게

     // 게임 일시정지 토글을 위한 상태
    private boolean pauseKeyPressed = false;

    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.lastKeyPressTime = new HashMap<>();
        initializeDefaultKeyBindings();
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
     * @param e KeyEvent
     */
    public void handleKeyPress(int keyCode) {
    
        if (gameModel.getGameState() != GameState.PLAYING) {
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                // gameModel.moveBlockLeft();
                System.out.println("Controller: 왼쪽 키 입력 감지");
                break;
            case KeyEvent.VK_RIGHT:
                // gameModel.moveBlockRight();
                System.out.println("Controller: 오른쪽 키 입력 감지");
                break;
            case KeyEvent.VK_DOWN:
                // gameModel.moveBlockDown();
                System.out.println("Controller: 아래쪽 키 입력 감지");
                break;
            case KeyEvent.VK_UP: // 보통 회전 키로 사용
                // gameModel.rotateBlock();
                System.out.println("Controller: 위쪽 키(회전) 입력 감지");
                break;
            case KeyEvent.VK_SPACE:
                // gameModel.hardDrop();
                System.out.println("Controller: 스페이스 바(하드 드롭) 입력 감지");
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
        } 
        else if (keyCode == keyBindings.get("MOVE_RIGHT")) {
            gameModel.moveBlockRight();
            System.out.println("Controller: 블록 오른쪽 이동");
        } 
        else if (keyCode == keyBindings.get("MOVE_DOWN")) {
            gameModel.moveBlockDown();
            System.out.println("Controller: 블록 아래로 이동 (소프트 드롭)");
        } 
        else if (keyCode == keyBindings.get("ROTATE_CW")) {
            gameModel.rotateBlockClockwise();
            System.out.println("Controller: 블록 시계방향 회전");
        } 
        else if (keyCode == keyBindings.get("ROTATE_CCW")) {
            gameModel.rotateBlockCounterClockwise();
            System.out.println("Controller: 블록 반시계방향 회전");
        } 
        else if (keyCode == keyBindings.get("HARD_DROP")) {
            gameModel.hardDropBlock();
            System.out.println("Controller: 하드 드롭 (즉시 하강)");
        } 
        else if (keyCode == keyBindings.get("HOLD")) {
            gameModel.holdCurrentBlock();
            System.out.println("Controller: 블록 홀드");
        }
        else if (keyCode == keyBindings.get("RESTART")) {
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
        } 
        else if (keyCode == keyBindings.get("QUIT_GAME")) {
            gameModel.quitToMenu();
            System.out.println("Controller: 메뉴로 돌아가기");
        }
        else if (keyCode == keyBindings.get("RESTART")) {
            gameModel.restartGame();
            System.out.println("Controller: 게임 재시작");
        }
    }

    /**
     * 메뉴에서의 키 입력 처리
     */
    private void handleMenuInput(int keyCode) {
        if (keyCode == keyBindings.get("MENU_UP")) {
            gameModel.navigateMenuUp();
            System.out.println("Controller: 메뉴 위로 이동");
        } 
        else if (keyCode == keyBindings.get("MENU_DOWN")) {
            gameModel.navigateMenuDown();
            System.out.println("Controller: 메뉴 아래로 이동");
        } 
        else if (keyCode == keyBindings.get("MENU_SELECT")) {
            gameModel.selectCurrentMenuItem();
            System.out.println("Controller: 메뉴 항목 선택");
        } 
        else if (keyCode == keyBindings.get("MENU_BACK")) {
            // 메인 메뉴에서는 프로그램 종료, 서브 메뉴에서는 뒤로가기
            gameModel.handleMenuBack();
            System.out.println("Controller: 메뉴 뒤로가기/종료");
        }
    }

    /**
     * 게임 오버 화면에서의 키 입력 처리
     */
    private void handleGameOverInput(int keyCode) {
        if (keyCode == keyBindings.get("MENU_SELECT") || keyCode == KeyEvent.VK_ENTER) {
            gameModel.proceedFromGameOver();
            System.out.println("Controller: 게임 오버 화면에서 진행");
        } 
        else if (keyCode == keyBindings.get("RESTART")) {
            gameModel.restartGame();
            System.out.println("Controller: 게임 재시작");
        } 
        else if (keyCode == keyBindings.get("QUIT_GAME")) {
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
        } 
        else if (keyCode == keyBindings.get("MENU_DOWN")) {
            gameModel.navigateSettingsDown();
            System.out.println("Controller: 설정 메뉴 아래로 이동");
        } 
        else if (keyCode == keyBindings.get("MENU_SELECT")) {
            gameModel.selectCurrentSetting();
            System.out.println("Controller: 설정 항목 선택/변경");
        } 
        else if (keyCode == keyBindings.get("MENU_BACK")) {
            gameModel.exitSettings();
            System.out.println("Controller: 설정 화면 나가기");
        } 
        else if (keyCode == keyBindings.get("SETTINGS_RESET")) {
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
        }
        else if (keyCode == keyBindings.get("MENU_UP")) {
            gameModel.scrollScoreboardUp();
            System.out.println("Controller: 스코어보드 위로 스크롤");
        }
        else if (keyCode == keyBindings.get("MENU_DOWN")) {
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
        } 
        else if (keyCode == KeyEvent.VK_BACK_SPACE) {
            gameModel.deleteCharacterFromName();
            System.out.println("Controller: 이름에서 문자 삭제");
        } 
        else if (keyCode == KeyEvent.VK_ESCAPE) {
            gameModel.cancelNameInput();
            System.out.println("Controller: 이름 입력 취소");
        }
        else if (isValidNameCharacter(keyCode)) {
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
     * 키 해제 이벤트 처리 (키를 뗐을 때)
     */
    public void handleKeyRelease(int keyCode) {
        // 일시정지 키 해제 처리
        if (keyCode == keyBindings.get("PAUSE")) {
            pauseKeyPressed = false;
        }
    }
}