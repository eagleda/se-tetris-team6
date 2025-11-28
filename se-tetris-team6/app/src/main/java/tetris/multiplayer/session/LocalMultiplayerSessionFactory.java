package tetris.multiplayer.session;

import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.infrastructure.GameModelFactory;
import tetris.multiplayer.controller.MultiPlayerController;
// LocalMultiplayerHandler referenced fully-qualified below; no import needed here
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.model.PlayerState;
import tetris.multiplayer.model.VersusRules;

/**
 * 로컬 2P 세션을 한 번에 만들어 주는 단순 팩토리.
 * - GameModelFactory 를 재사용해 싱글 모드와 동일한 규칙을 가져온다.
 * - 나중에 의존성을 주입하고 싶다면 이 클래스를 교체하면 된다.
 */
public final class LocalMultiplayerSessionFactory {

    private LocalMultiplayerSessionFactory() {
    }

    public static LocalMultiplayerSession create(GameMode mode) {
        GameModel p1 = GameModelFactory.createDefault();
        GameModel p2 = GameModelFactory.createDefault();
        PlayerState playerOne = new PlayerState(1, p1, true);
        PlayerState playerTwo = new PlayerState(2, p2, true);
        VersusRules rules = new VersusRules();
        MultiPlayerGame game = new MultiPlayerGame(playerOne, playerTwo, rules);
        MultiPlayerController controller = new MultiPlayerController(game);
        tetris.multiplayer.handler.LocalMultiplayerHandler handler = new tetris.multiplayer.handler.LocalMultiplayerHandler(game, controller, GameState.PLAYING);
        LocalMultiplayerSession session = new LocalMultiplayerSession(playerOne, playerTwo, game, controller, handler);
        session.restartPlayers(mode);
        return session;
    }

    /**
     * Create a networked session where only one side is local and the opponent
     * is driven by the network. The local player side is determined by
     * {@code localIsPlayerOne}.
     */
    public static LocalMultiplayerSession createNetworkedSession(GameMode mode, boolean localIsPlayerOne) {
        GameModel p1 = GameModelFactory.createDefault();
        GameModel p2 = GameModelFactory.createDefault();
        PlayerState playerOne = new PlayerState(1, p1, localIsPlayerOne);
        PlayerState playerTwo = new PlayerState(2, p2, !localIsPlayerOne);
        VersusRules rules = new VersusRules();
        MultiPlayerGame game = new MultiPlayerGame(playerOne, playerTwo, rules);
        MultiPlayerController controller = new MultiPlayerController(game);
        tetris.multiplayer.handler.NetworkedMultiplayerHandler handler = new tetris.multiplayer.handler.NetworkedMultiplayerHandler(game, controller, GameState.PLAYING, localIsPlayerOne ? 1 : 2);
        LocalMultiplayerSession session = new LocalMultiplayerSession(playerOne, playerTwo, game, controller, handler);
        session.restartPlayers(mode);
        return session;
    }
}
