package tetris.multiplayer.session;

import java.util.Objects;

import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.multiplayer.controller.LocalMultiPlayerController;
import tetris.multiplayer.handler.MultiplayerHandler;
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.model.PlayerState;

/**
 * 로컬 2P 멀티플레이 세션 전용 클래스.
 * 로컬 멀티플레이어에 필요한 참조(플레이어 모델, 컨트롤러, 핸들러)를 한 군데 묶어 둔 값 객체.
 * UI나 컨트롤러가 동일한 세션을 공유해야 하므로 불변 필드만 노출한다.
 */
public final class LocalMultiplayerSession {

    private final PlayerState player1;
    private final PlayerState player2;
    private final MultiPlayerGame game;
    private final LocalMultiPlayerController controller;
    private final MultiplayerHandler handler;

    public LocalMultiplayerSession(PlayerState player1,
                                   PlayerState player2,
                                   MultiPlayerGame game,
                                   LocalMultiPlayerController controller,
                                   MultiplayerHandler handler) {
        this.player1 = Objects.requireNonNull(player1, "player1");
        this.player2 = Objects.requireNonNull(player2, "player2");
        this.game = Objects.requireNonNull(game, "game");
        this.controller = Objects.requireNonNull(controller, "controller");
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    /** P1이 조작할 GameModel */
    public GameModel playerOneModel() {
        return player1.getModel();
    }

    /** P2가 조작할 GameModel */
    public GameModel playerTwoModel() {
        return player2.getModel();
    }

    /** 멀티플레이 상태 머신에 장착할 핸들러 */
    public MultiplayerHandler handler() {
        return handler;
    }

    /** Whether player one is local to this process */
    public boolean isPlayerOneLocal() { return player1.isLocal(); }

    /** Whether player two is local to this process */
    public boolean isPlayerTwoLocal() { return player2.isLocal(); }

    /** 로컬 멀티플레이 컨트롤러 반환 */
    public LocalMultiPlayerController controller() {
        return controller;
    }

    public MultiPlayerGame game() {
        return game;
    }

    /**
     * 모든 플레이어 모델을 동일한 모드로 재시작한다.
     * - GameModel#startGame 이 내부 상태를 초기화하므로 별도 클리어 작업이 필요 없다.
     */
    public void restartPlayers(GameMode mode) {
        GameMode resolved = mode == null ? GameMode.STANDARD : mode;
        playerOneModel().startGame(resolved);
        playerTwoModel().startGame(resolved);
    }

    /**
     * 메뉴로 돌아갈 때 플레이어 모델도 MENU 상태로 돌려 놓는다.
     */
    public void shutdown() {
        playerOneModel().quitToMenu();
        playerTwoModel().quitToMenu();
    }
}
