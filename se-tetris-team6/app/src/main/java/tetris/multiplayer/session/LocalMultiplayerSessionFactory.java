package tetris.multiplayer.session;

import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.infrastructure.GameModelFactory;
import tetris.multiplayer.controller.LocalMultiPlayerController;
import tetris.multiplayer.controller.NetworkMultiPlayerController;
// LocalMultiplayerHandler referenced fully-qualified below; no import needed here
import tetris.multiplayer.model.MultiPlayerGame;
import tetris.multiplayer.model.PlayerState;
import tetris.multiplayer.model.VersusRules;

/**
 * 로컬 2P 및 네트워크 멀티플레이 세션을 생성하는 팩토리 클래스.
 * - GameModelFactory 를 재사용해 싱글 모드와 동일한 규칙을 가져온다.
 * - 로컬 멀티플레이는 LocalMultiplayerSession을, 네트워크 멀티플레이는 NetworkMultiplayerSession을 반환한다.
 */
public final class LocalMultiplayerSessionFactory {

    private LocalMultiplayerSessionFactory() {
    }

    /**
     * 로컬 2P 전용 세션 생성
     */
    public static LocalMultiplayerSession create(GameMode mode) {
        GameModel p1 = GameModelFactory.createDefault();
        GameModel p2 = GameModelFactory.createDefault();
        PlayerState playerOne = new PlayerState(1, p1, true);
        PlayerState playerTwo = new PlayerState(2, p2, true);
        VersusRules rules = new VersusRules();
        MultiPlayerGame game = new MultiPlayerGame(playerOne, playerTwo, rules);
        LocalMultiPlayerController controller = new LocalMultiPlayerController(game);
        tetris.multiplayer.handler.LocalMultiplayerHandler handler = new tetris.multiplayer.handler.LocalMultiplayerHandler(game, controller, GameState.PLAYING);
        LocalMultiplayerSession session = new LocalMultiplayerSession(playerOne, playerTwo, game, controller, handler);
        session.restartPlayers(mode);
        return session;
    }

    /**
     * 네트워크 멀티플레이 전용 세션 생성
     * Create a networked session where only one side is local and the opponent
     * is driven by the network. The local player side is determined by
     * {@code localIsPlayerOne}.
     */
    public static NetworkMultiplayerSession createNetworkedSession(GameMode mode, boolean localIsPlayerOne, Runnable sendGameEndCallback) {
        GameModel p1 = GameModelFactory.createDefault();
        GameModel p2 = GameModelFactory.createDefault();
        PlayerState playerOne = new PlayerState(1, p1, localIsPlayerOne);
        PlayerState playerTwo = new PlayerState(2, p2, !localIsPlayerOne);
        VersusRules rules = new VersusRules();
        MultiPlayerGame game = new MultiPlayerGame(playerOne, playerTwo, rules);
        NetworkMultiPlayerController controller = new NetworkMultiPlayerController(game, localIsPlayerOne ? 1 : 2);
        tetris.multiplayer.handler.NetworkedMultiplayerHandler handler = new tetris.multiplayer.handler.NetworkedMultiplayerHandler(game, controller, GameState.PLAYING, localIsPlayerOne ? 1 : 2, sendGameEndCallback);
        NetworkMultiplayerSession session = new NetworkMultiplayerSession(playerOne, playerTwo, game, controller, handler);
        session.restartPlayers(mode);
        return session;
    }

    /**
     * 네트워크 세션(시드 지정): 양측 동일 시드로 블록 순서를 동기화.
     */
    public static NetworkMultiplayerSession createNetworkedSession(GameMode mode, boolean localIsPlayerOne, Runnable sendGameEndCallback, long seed) {
        GameModel p1 = GameModelFactory.createWithSeed(seed);
        GameModel p2 = GameModelFactory.createWithSeed(seed);
        PlayerState playerOne = new PlayerState(1, p1, localIsPlayerOne);
        PlayerState playerTwo = new PlayerState(2, p2, !localIsPlayerOne);
        VersusRules rules = new VersusRules();
        MultiPlayerGame game = new MultiPlayerGame(playerOne, playerTwo, rules);
        NetworkMultiPlayerController controller = new NetworkMultiPlayerController(game, localIsPlayerOne ? 1 : 2);
        tetris.multiplayer.handler.NetworkedMultiplayerHandler handler = new tetris.multiplayer.handler.NetworkedMultiplayerHandler(game, controller, GameState.PLAYING, localIsPlayerOne ? 1 : 2, sendGameEndCallback);
        NetworkMultiplayerSession session = new NetworkMultiplayerSession(playerOne, playerTwo, game, controller, handler);
        session.restartPlayers(mode);
        return session;
    }
}
