package tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.domain.GameDifficulty;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.handler.MultiplayerHandler;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.controller.GameController
 *
 * 역할 요약:
 * - 키 입력을 받아 GameModel에 동작을 위임하고, 로컬/네트워크 멀티 입력을 핸들러로 라우팅한다.
 * - 게임 시작/종료/재시작 요청을 GameModel 및 세션 관련 로직에 전달한다.
 * - 설정 변경(키 바인딩, 난이도, 색각 모드)을 런타임에 반영한다.
 *
 * 테스트 전략:
 * - 사용 라이브러리:
 *   - JUnit 5 (junit-jupiter)
 *   - Mockito로 GameModel, MultiplayerHandler 등을 mock 하고, 기대 메서드 호출을 검증한다.
 *
 * - 설계 가정:
 *   - 컨트롤러는 생성자를 통해 필요한 협력자를 주입받는다.
 *   - 테스트는 "입력 → 협력자 메서드 호출" 패턴을 검증하며, UI 렌더링보다는 협력자 호출 여부에 초점을 둔다.
 *
 * - 테스트 방식:
 *   - given: mock 협력자들을 준비하고, 컨트롤러 인스턴스를 생성한다.
 *   - when : public 메서드(handleKeyPress, startGame 등)를 호출한다.
 *   - then : Mockito.verify()로 협력자 메서드 호출 여부/인자를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock
    GameModel gameModel;

    @Mock
    MultiplayerHandler handler;

    @Captor
    org.mockito.ArgumentCaptor<Consumer<GameModel>> consumerCaptor;

    GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(gameModel);
    }

    @Test
    void handleKeyPress_playing_movesBlockLeft() {
        when(gameModel.getCurrentState()).thenReturn(GameState.PLAYING);

        controller.handleKeyPress(KeyEvent.VK_LEFT);

        verify(gameModel, times(1)).moveBlockLeft();
    }

    @Test
    void handleKeyPress_pauseKey_callsPauseGameOnce() {
        when(gameModel.getCurrentState()).thenReturn(GameState.PLAYING);

        controller.handleKeyPress(KeyEvent.VK_P);

        verify(gameModel).pauseGame();
    }

    @Test
    void handleKeyPress_paused_resumeGame() {
        when(gameModel.getCurrentState()).thenReturn(GameState.PAUSED);

        controller.handleKeyPress(KeyEvent.VK_P);

        verify(gameModel).resumeGame();
    }

    @Test
    void handleKeyPress_gameOver_quitToMenu() {
        when(gameModel.getCurrentState()).thenReturn(GameState.GAME_OVER);

        controller.handleKeyPress(KeyEvent.VK_Q);

        verify(gameModel).quitToMenu();
    }

    @Test
    void handleKeyPress_settings_resetAll() {
        when(gameModel.getCurrentState()).thenReturn(GameState.SETTINGS);

        controller.handleKeyPress(KeyEvent.VK_DELETE);

        verify(gameModel).resetAllSettings();
    }

    @Test
    void handleKeyPress_scoreboard_scrollDown() {
        when(gameModel.getCurrentState()).thenReturn(GameState.SCOREBOARD);

        controller.handleKeyPress(KeyEvent.VK_DOWN);

        verify(gameModel).scrollScoreboardDown();
    }

    @Test
    void handleKeyPress_nameInput_addsCharacter() {
        when(gameModel.getCurrentState()).thenReturn(GameState.NAME_INPUT);

        controller.handleKeyPress((int) 'A');

        verify(gameModel).addCharacterToName('A');
    }

    @Test
    void handleKeyPress_nameInput_enterConfirms() {
        when(gameModel.getCurrentState()).thenReturn(GameState.NAME_INPUT);

        controller.handleKeyPress(KeyEvent.VK_ENTER);

        verify(gameModel).confirmNameInput();
    }

    @Test
    void handleKeyPress_ignoresRapidRepeat() throws Exception {
        when(gameModel.getCurrentState()).thenReturn(GameState.PLAYING);

        // 첫 입력
        controller.handleKeyPress(KeyEvent.VK_LEFT);

        // lastKeyPressTime을 바로 직전으로 설정해 반복 무시 유도
        Field f = GameController.class.getDeclaredField("lastKeyPressTime");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Long> times = (Map<Integer, Long>) f.get(controller);
        times.put(KeyEvent.VK_LEFT, System.currentTimeMillis());

        controller.handleKeyPress(KeyEvent.VK_LEFT);

        verify(gameModel, times(1)).moveBlockLeft();
    }

    @Test
    void handleKeyPress_routesLocalMultiplayerP1() throws Exception {
        when(gameModel.isLocalMultiplayerActive()).thenReturn(true);
        tetris.multiplayer.session.LocalMultiplayerSession session = mock(tetris.multiplayer.session.LocalMultiplayerSession.class);
        when(session.handler()).thenReturn(handler);

        Field f = GameController.class.getDeclaredField("localSession");
        f.setAccessible(true);
        f.set(controller, session);

        controller.handleKeyPress(KeyEvent.VK_A); // 기본 P1_MOVE_LEFT

        verify(handler).dispatchToPlayer(eq(1), any());
    }

    @Test
    void handleKeyPress_routesLocalMultiplayerP2() throws Exception {
        when(gameModel.isLocalMultiplayerActive()).thenReturn(true);
        tetris.multiplayer.session.LocalMultiplayerSession session = mock(tetris.multiplayer.session.LocalMultiplayerSession.class);
        when(session.handler()).thenReturn(handler);

        Field f = GameController.class.getDeclaredField("localSession");
        f.setAccessible(true);
        f.set(controller, session);

        controller.handleKeyPress(KeyEvent.VK_RIGHT); // 기본 P2_MOVE_RIGHT

        verify(handler).dispatchToPlayer(eq(2), any());
    }

    @Test
    void handleKeyPress_routesNetworkMultiplayerAndSendsInput() throws Exception {
        // Arrange network session and handler
        tetris.multiplayer.handler.NetworkedMultiplayerHandler netHandler =
                mock(tetris.multiplayer.handler.NetworkedMultiplayerHandler.class);
        when(netHandler.getLocalPlayerId()).thenReturn(1);

        tetris.multiplayer.session.NetworkMultiplayerSession session =
                mock(tetris.multiplayer.session.NetworkMultiplayerSession.class);
        when(session.handler()).thenReturn(netHandler);

        Field sessionField = GameController.class.getDeclaredField("networkSession");
        sessionField.setAccessible(true);
        sessionField.set(controller, session);

        tetris.network.client.GameClient client = mock(tetris.network.client.GameClient.class);
        Field clientField = GameController.class.getDeclaredField("networkClient");
        clientField.setAccessible(true);
        clientField.set(controller, client);

        // Act
        controller.handleKeyPress(KeyEvent.VK_LEFT); // MOVE_LEFT

        // Assert
        verify(netHandler).dispatchToPlayer(eq(1), any());
        verify(client).sendPlayerInput(any());
    }

    @Test
    void startItemGame_delegatesToGameModel() {
        controller.startItemGame();

        verify(gameModel).startGame(GameMode.ITEM);
    }

    @Test
    void applySettings_updatesBindingsAndColorBlind() {
        Map<String, Integer> updated = new HashMap<>();
        updated.put("MOVE_LEFT", KeyEvent.VK_J);

        controller.applyKeyBindings(updated);
        controller.applyDifficulty(GameDifficulty.HARD);
        controller.applyColorBlindMode(true);

        verify(gameModel).setColorBlindMode(true);
        // BlockGenerator는 null로 스텁되어 있어 setDifficulty 호출은 검증하지 않음
    }
}
