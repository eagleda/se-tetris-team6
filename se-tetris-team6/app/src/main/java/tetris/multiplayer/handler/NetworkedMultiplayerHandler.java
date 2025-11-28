package tetris.multiplayer.handler;

import java.util.Objects;
import java.util.function.Consumer;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.controller.MultiPlayerController;
import tetris.multiplayer.model.MultiPlayerGame;

/**
 * Handler used for networked multiplayer where only one local GameModel is
 * ticked locally and the opponent model is driven by network updates.
 */
public final class NetworkedMultiplayerHandler implements MultiplayerHandler {

    private final MultiPlayerGame game;
    private final MultiPlayerController controller;
    private final GameState state;
    private final int localPlayerId;

    public NetworkedMultiplayerHandler(MultiPlayerGame game,
                                       MultiPlayerController controller,
                                       GameState state,
                                       int localPlayerId) {
        this.game = Objects.requireNonNull(game, "game");
        this.controller = Objects.requireNonNull(controller, "controller");
        this.state = Objects.requireNonNull(state, "state");
        if (localPlayerId != 1 && localPlayerId != 2) throw new IllegalArgumentException("localPlayerId must be 1 or 2");
        this.localPlayerId = localPlayerId;
    }

    @Override
    public GameState getState() { return state; }

    @Override
    public void enter(tetris.domain.GameModel model) {
        // mark ready flags false
        game.player(1).setReady(false);
        game.player(2).setReady(false);
        // register hooks only for the local model
        registerHookForLocal();
    }

    @Override
    public void update(tetris.domain.GameModel model) {
        // Only tick the local player's GameModel; opponent is updated via network
        game.modelOf(localPlayerId).update();
        // If local player's game over, mark loser locally and notify central model
        if (game.modelOf(localPlayerId).getCurrentState() == GameState.GAME_OVER) {
            game.markLoser(localPlayerId);
            model.changeState(GameState.GAME_OVER);
            model.showMultiplayerResult(game.getWinnerId() == null ? -1 : game.getWinnerId());
        }
    }

    @Override
    public void exit(tetris.domain.GameModel model) {
        unregisterHookForLocal();
    }

    @Override
    public void dispatchToPlayer(int playerId, Consumer<GameModel> action) {
        if (playerId != localPlayerId) {
            // ignore attempts to dispatch to remote player
            return;
        }
        controller.withPlayer(playerId, action);
    }

    @Override
    public int getPendingLines(int playerId) { return controller.getPendingLines(playerId); }

    private void registerHookForLocal() {
        int pid = localPlayerId;
        game.player(pid).getModel().addMultiplayerHook(createHook(pid));
    }

    private void unregisterHookForLocal() {
        int pid = localPlayerId;
        try { game.player(pid).getModel().removeMultiplayerHook(createHook(pid)); } catch (Exception ignore) {}
    }

    private tetris.domain.GameModel.MultiplayerHook createHook(int playerId) {
        return new tetris.domain.GameModel.MultiplayerHook() {
            @Override
            public void onPieceLocked(tetris.multiplayer.model.LockedPieceSnapshot snapshot, int[] clearedRows, int boardWidth) {
                controller.onPieceLocked(playerId, snapshot, clearedRows);
            }

            @Override
            public void beforeNextSpawn() {
                controller.injectAttackBeforeNextSpawn(playerId);
            }
        };
    }
}
