package tetris.controller;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Timer;
import tetris.domain.BlockGenerator;
import tetris.domain.GameDifficulty;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.domain.setting.Setting;
import tetris.multiplayer.handler.MultiplayerHandler;
import tetris.multiplayer.session.LocalMultiplayerSession;
import tetris.multiplayer.session.LocalMultiplayerSessionFactory;
// 이제부터 모델의 좌우 움직임이 안 되는 이유를 해결합니다.

/**
 * Controller 역할을 수행하는 클래스.
 * View(GamePanel)와 Model(추후 생성할 GameModel)을 연결합니다.
 */
public class GameController {

    private final GameModel gameModel; // Model 참조

    // 키 반복 입력 제어를 위한 상태 추적
    private Map<Integer, Long> lastKeyPressTime;
    private static final long KEY_REPEAT_DELAY = 15; // 15ms
    private static final long MOVEMENT_REPEAT_DELAY = 30; // 이동키는 조금 더 느리게

    // 게임 일시정지 토글을 위한 상태
    private boolean pauseKeyPressed = false;

    // 키 바인딩 맵
    private Map<String, Integer> keyBindings;
    private LocalMultiplayerSession localSession;
    private Timer localMultiplayerTimer;
    private tetris.network.client.GameClient networkClient; // 네트워크 클라이언트 참조
    private tetris.network.server.GameServer networkServer; // 네트워크 서버 참조 (호스트용)

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
        BlockGenerator generator = gameModel.getBlockGenerator();
        if (generator != null) {
            generator.setDifficulty(difficulty == null ? GameDifficulty.NORMAL : difficulty);
        }
    }

    public void applyColorBlindMode(boolean enabled) {
        gameModel.setColorBlindMode(enabled);
    }

    /**
     * 기본 키 바인딩 초기화
     * 설정에서 변경 가능하도록 Map으로 관리
     */
    private void initializeDefaultKeyBindings() {
        keyBindings = new HashMap<>();
        Setting defaults = Setting.defaults();

        // 게임 플레이 키 (싱글)
        putDefaultBinding("MOVE_LEFT", defaults.getKeyBinding("MOVE_LEFT"), KeyEvent.VK_LEFT);
        putDefaultBinding("MOVE_RIGHT", defaults.getKeyBinding("MOVE_RIGHT"), KeyEvent.VK_RIGHT);
        putDefaultBinding("SOFT_DROP", defaults.getKeyBinding("SOFT_DROP"), KeyEvent.VK_DOWN);
        putDefaultBinding("ROTATE_CW", defaults.getKeyBinding("ROTATE_CW"), KeyEvent.VK_UP);
        putDefaultBinding("ROTATE_CCW", defaults.getKeyBinding("ROTATE_CCW"), KeyEvent.VK_Z);
        putDefaultBinding("HARD_DROP", defaults.getKeyBinding("HARD_DROP"), KeyEvent.VK_SPACE);
        putDefaultBinding("HOLD", defaults.getKeyBinding("HOLD"), KeyEvent.VK_C);

        // 게임 제어 키
        putDefaultBinding("PAUSE", defaults.getKeyBinding("PAUSE"), KeyEvent.VK_P);
        putDefaultBinding("QUIT_GAME", defaults.getKeyBinding("QUIT_GAME"), KeyEvent.VK_Q);
        putDefaultBinding("RESTART", defaults.getKeyBinding("RESTART"), KeyEvent.VK_R);

        // 메뉴 네비게이션 키
        putDefaultBinding("MENU_UP", defaults.getKeyBinding("MENU_UP"), KeyEvent.VK_UP);
        putDefaultBinding("MENU_DOWN", defaults.getKeyBinding("MENU_DOWN"), KeyEvent.VK_DOWN);
        putDefaultBinding("MENU_SELECT", defaults.getKeyBinding("MENU_SELECT"), KeyEvent.VK_ENTER);
        putDefaultBinding("MENU_BACK", defaults.getKeyBinding("MENU_BACK"), KeyEvent.VK_ESCAPE);

        // 설정 화면 키
        putDefaultBinding("SETTINGS_RESET", defaults.getKeyBinding("SETTINGS_RESET"), KeyEvent.VK_DELETE);

        // 로컬 멀티 (P1)
        putDefaultBinding("P1_MOVE_LEFT", defaults.getKeyBinding("P1_MOVE_LEFT"), KeyEvent.VK_A);
        putDefaultBinding("P1_MOVE_RIGHT", defaults.getKeyBinding("P1_MOVE_RIGHT"), KeyEvent.VK_D);
        putDefaultBinding("P1_SOFT_DROP", defaults.getKeyBinding("P1_SOFT_DROP"), KeyEvent.VK_S);
        putDefaultBinding("P1_ROTATE_CW", defaults.getKeyBinding("P1_ROTATE_CW"), KeyEvent.VK_W);
        putDefaultBinding("P1_HARD_DROP", defaults.getKeyBinding("P1_HARD_DROP"), KeyEvent.VK_SPACE);

        // 로컬 멀티 (P2)
        putDefaultBinding("P2_MOVE_LEFT", defaults.getKeyBinding("P2_MOVE_LEFT"), KeyEvent.VK_LEFT);
        putDefaultBinding("P2_MOVE_RIGHT", defaults.getKeyBinding("P2_MOVE_RIGHT"), KeyEvent.VK_RIGHT);
        putDefaultBinding("P2_SOFT_DROP", defaults.getKeyBinding("P2_SOFT_DROP"), KeyEvent.VK_DOWN);
        putDefaultBinding("P2_ROTATE_CW", defaults.getKeyBinding("P2_ROTATE_CW"), KeyEvent.VK_UP);
        putDefaultBinding("P2_HARD_DROP", defaults.getKeyBinding("P2_HARD_DROP"), KeyEvent.VK_ENTER);
    }

    private void putDefaultBinding(String action, Integer configured, int fallback) {
        keyBindings.put(action, configured != null ? configured : fallback);
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
            deactivateLocalMultiplayer();
            gameModel.quitToMenu();
            System.out.println("Controller: 메뉴로 돌아가기");
            return;
        }

        if (localSession != null && routeLocalMultiplayerInput(keyCode)) {
            return;
        }

        // 블록 조작 키들
        if (keyCode == keyBindings.get("MOVE_LEFT")) {
            gameModel.moveBlockLeft();
            System.out.println("Controller: 블록 왼쪽 이동");
        } else if (keyCode == keyBindings.get("MOVE_RIGHT")) {
            gameModel.moveBlockRight();
            System.out.println("Controller: 블록 오른쪽 이동");
        } else if (keyCode == keyBindings.get("SOFT_DROP")) {
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
            pauseKeyPressed = false;
            lastKeyPressTime.clear();
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
            deactivateLocalMultiplayer();
            gameModel.quitToMenu();
            System.out.println("Controller: 메뉴로 돌아가기");
        } else if (keyCode == keyBindings.get("RESTART")) {
            gameModel.restartGame();
            pauseKeyPressed = false;
            lastKeyPressTime.clear();
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
            deactivateLocalMultiplayer();
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
            keyCode == keyBindings.get("SOFT_DROP")) {
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
    public void startStandardGame() {
        startGame(GameMode.STANDARD);
    }

    public void startItemGame() {
        startGame(GameMode.ITEM);
    }

    public void startGame(GameMode mode) {
        // 로컬 멀티 세션이 켜져 있었다면 먼저 정리하고 싱글 루프로 복귀한다.
        deactivateLocalMultiplayer();
        pauseKeyPressed = false;
        lastKeyPressTime.clear();
        gameModel.startGame(mode);
    }

    public void startGame() {
        startStandardGame();
    }

    /**
     * 로컬 멀티플레이를 시작하고 생성된 세션을 반환한다.
     * - UI는 반환된 세션으로 각 패널을 바인딩한다.
     */
    public LocalMultiplayerSession startLocalMultiplayerGame(GameMode mode) {
        deactivateLocalMultiplayer();
        LocalMultiplayerSession session = LocalMultiplayerSessionFactory.create(mode);
        localSession = session;
        gameModel.enableLocalMultiplayer(session);
        startLocalMultiplayerTick();
        pauseKeyPressed = false;
        lastKeyPressTime.clear();
        gameModel.startGame(mode);
        return session;
    }

    /**
     * Start a networked multiplayer session where only one side is local.
     * @param mode game mode
     * @param localIsPlayerOne true if this process controls player 1
     */
    public LocalMultiplayerSession startNetworkedMultiplayerGame(GameMode mode, boolean localIsPlayerOne) {
        deactivateLocalMultiplayer();
        
        // Create callback to send GAME_END message when local player loses
        Runnable sendGameEndCallback = () -> {
            try {
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("winnerId", localIsPlayerOne ? 2 : 1); // Opponent is the winner
                
                if (networkClient != null) {
                    tetris.network.protocol.GameMessage message = new tetris.network.protocol.GameMessage(
                        tetris.network.protocol.MessageType.GAME_END, 
                        "CLIENT", 
                        data
                    );
                    networkClient.sendMessage(message);
                } else if (networkServer != null) {
                    tetris.network.protocol.GameMessage message = new tetris.network.protocol.GameMessage(
                        tetris.network.protocol.MessageType.GAME_END, 
                        "SERVER", 
                        data
                    );
                    networkServer.sendHostMessage(message);
                }
            } catch (Exception e) {
                System.err.println("Failed to send GAME_END message: " + e.getMessage());
            }
        };
        
        LocalMultiplayerSession session = LocalMultiplayerSessionFactory.createNetworkedSession(mode, localIsPlayerOne, sendGameEndCallback);
        localSession = session;
        
        // Set up network event handler
        tetris.multiplayer.controller.NetworkMultiPlayerController networkController = session.networkController();
        if (networkController != null) {
            networkController.setNetworkHandler(new tetris.multiplayer.controller.NetworkMultiPlayerController.NetworkEventHandler() {
                @Override
                public void sendPieceLockedEvent(tetris.multiplayer.model.LockedPieceSnapshot snapshot, int[] clearedYs) {
                    sendNetworkPieceLocked(snapshot, clearedYs);
                }
                
                @Override
                public void sendGameState(tetris.domain.GameModel gameState) {
                    // Game state sync can be added here if needed
                }
                
                @Override
                public void sendGameOverEvent() {
                    sendGameEndCallback.run();
                }
            });
        }
        
        gameModel.enableLocalMultiplayer(session);
        startLocalMultiplayerTick();
        pauseKeyPressed = false;
        lastKeyPressTime.clear();
        gameModel.startGame(mode);
        return session;
    }
    
    /**
     * Send piece locked event over network
     */
    private void sendNetworkPieceLocked(tetris.multiplayer.model.LockedPieceSnapshot snapshot, int[] clearedYs) {
        try {
            tetris.network.protocol.AttackLine[] attackLines = null;
            if (clearedYs != null && clearedYs.length > 0) {
                // Convert cleared lines to attack lines
                attackLines = new tetris.network.protocol.AttackLine[clearedYs.length];
                for (int i = 0; i < clearedYs.length; i++) {
                    attackLines[i] = new tetris.network.protocol.AttackLine(1);
                }
            }
            
            if (attackLines != null && attackLines.length > 0) {
                if (networkClient != null) {
                    tetris.network.protocol.GameMessage message = new tetris.network.protocol.GameMessage(
                        tetris.network.protocol.MessageType.ATTACK_LINES,
                        "CLIENT",
                        attackLines
                    );
                    networkClient.sendMessage(message);
                } else if (networkServer != null) {
                    tetris.network.protocol.GameMessage message = new tetris.network.protocol.GameMessage(
                        tetris.network.protocol.MessageType.ATTACK_LINES,
                        "SERVER",
                        attackLines
                    );
                    networkServer.sendHostMessage(message);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send piece locked event: " + e.getMessage());
        }
    }

    /**
     * Set the network client for sending input to the opponent.
     */
    public void setNetworkClient(tetris.network.client.GameClient client) {
        this.networkClient = client;
    }

    /**
     * Set the network server for host to send messages to clients.
     */
    public void setNetworkServer(tetris.network.server.GameServer server) {
        this.networkServer = server;
    }

    /**
     * 키 릴리즈 처리 (일시정지 키 상태 초기화)
     */
    public void handleKeyRelease(int keyCode) {
        if (keyCode == keyBindings.get("PAUSE")) {
            pauseKeyPressed = false;
        }
    }

    private boolean routeLocalMultiplayerInput(int keyCode) {
        if (localSession == null || !gameModel.isLocalMultiplayerActive()) {
            return false;
        }
        MultiplayerHandler handler = localSession.handler();
        if (handler == null) {
            return false;
        }

        // 네트워크 모드인 경우, 로컬 플레이어만 처리
        boolean isNetworked = handler instanceof tetris.multiplayer.handler.NetworkedMultiplayerHandler;
        int localPlayerId = 0;
        if (isNetworked) {
            localPlayerId = ((tetris.multiplayer.handler.NetworkedMultiplayerHandler) handler).getLocalPlayerId();
        }

        // P1 입력 처리
        if (keyCode == keyFor("P1_MOVE_LEFT")) {
            if (!isNetworked || localPlayerId == 1) {
                handler.dispatchToPlayer(1, GameModel::moveBlockLeft);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.MOVE_LEFT));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P1_MOVE_RIGHT")) {
            if (!isNetworked || localPlayerId == 1) {
                handler.dispatchToPlayer(1, GameModel::moveBlockRight);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.MOVE_RIGHT));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P1_SOFT_DROP")) {
            if (!isNetworked || localPlayerId == 1) {
                handler.dispatchToPlayer(1, GameModel::moveBlockDown);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.SOFT_DROP));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P1_ROTATE_CW")) {
            if (!isNetworked || localPlayerId == 1) {
                handler.dispatchToPlayer(1, GameModel::rotateBlockClockwise);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.ROTATE));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P1_HARD_DROP")) {
            if (!isNetworked || localPlayerId == 1) {
                handler.dispatchToPlayer(1, GameModel::hardDropBlock);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.HARD_DROP));
                }
                return true;
            }
            return false;
        }

        // P2 입력 처리
        if (keyCode == keyFor("P2_MOVE_LEFT")) {
            if (!isNetworked || localPlayerId == 2) {
                handler.dispatchToPlayer(2, GameModel::moveBlockLeft);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.MOVE_LEFT));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P2_MOVE_RIGHT")) {
            if (!isNetworked || localPlayerId == 2) {
                handler.dispatchToPlayer(2, GameModel::moveBlockRight);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.MOVE_RIGHT));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P2_SOFT_DROP")) {
            if (!isNetworked || localPlayerId == 2) {
                handler.dispatchToPlayer(2, GameModel::moveBlockDown);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.SOFT_DROP));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P2_ROTATE_CW")) {
            if (!isNetworked || localPlayerId == 2) {
                handler.dispatchToPlayer(2, GameModel::rotateBlockClockwise);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.ROTATE));
                }
                return true;
            }
            return false;
        }
        if (keyCode == keyFor("P2_HARD_DROP")) {
            if (!isNetworked || localPlayerId == 2) {
                handler.dispatchToPlayer(2, GameModel::hardDropBlock);
                if (isNetworked) {
                    sendNetworkInput(new tetris.network.protocol.PlayerInput(tetris.network.protocol.InputType.HARD_DROP));
                }
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Send input to network (client or server).
     */
    private void sendNetworkInput(tetris.network.protocol.PlayerInput input) {
        if (networkClient != null) {
            networkClient.sendPlayerInput(input);
        } else if (networkServer != null) {
            networkServer.sendHostMessage(new tetris.network.protocol.GameMessage(
                tetris.network.protocol.MessageType.PLAYER_INPUT,
                "Player-1", // Host is always Player-1
                input
            ));
        }
    }

    private int keyFor(String action) {
        return keyBindings.getOrDefault(action, -1);
    }

    private void deactivateLocalMultiplayer() {
        if (localSession == null) {
            return;
        }
        stopLocalMultiplayerTick();
        gameModel.clearLocalMultiplayerSession();
        localSession = null;
    }

    /**
     * 로컬 멀티 모드에서 두 플레이어 GameModel을 동시에 진행시키기 위한 전용 틱 타이머.
     * - GAME CLOCK와 분리되어 LocalMultiplayerHandler.update()를 주기적으로 호출한다.
     */
    private void startLocalMultiplayerTick() {
        stopLocalMultiplayerTick();
        localMultiplayerTimer = new Timer(16, e -> {
            if (localSession == null || !gameModel.isLocalMultiplayerActive()) {
                stopLocalMultiplayerTick();
                return;
            }
            MultiplayerHandler handler = localSession.handler();
            if (handler != null) {
                handler.update(gameModel);
            }
        });
        localMultiplayerTimer.setRepeats(true);
        localMultiplayerTimer.start();
    }

    private void stopLocalMultiplayerTick() {
        if (localMultiplayerTimer != null) {
            localMultiplayerTimer.stop();
            localMultiplayerTimer = null;
        }
    }
}
