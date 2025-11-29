package tetris.multiplayer.handler;

import java.util.Objects;
import java.util.function.Consumer;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.multiplayer.controller.NetworkMultiPlayerController;
import tetris.multiplayer.model.MultiPlayerGame;

/**
 * Handler used for networked multiplayer where only one local GameModel is
 * ticked locally and the opponent model is driven by network updates.
 */
public final class NetworkedMultiplayerHandler implements MultiplayerHandler {

    private final MultiPlayerGame game;
    private final NetworkMultiPlayerController controller;
    private final GameState state;
    private final int localPlayerId;
    private final Runnable sendGameEndCallback;
    private boolean gameEndSent = false;
    private boolean gameEndHandled = false;

    public NetworkedMultiplayerHandler(MultiPlayerGame game,
                                       NetworkMultiPlayerController controller,
                                       GameState state,
                                       int localPlayerId,
                                       Runnable sendGameEndCallback) {
        this.game = Objects.requireNonNull(game, "game");
        this.controller = Objects.requireNonNull(controller, "controller");
        this.state = Objects.requireNonNull(state, "state");
        if (localPlayerId != 1 && localPlayerId != 2) throw new IllegalArgumentException("localPlayerId must be 1 or 2");
        this.localPlayerId = localPlayerId;
        this.sendGameEndCallback = sendGameEndCallback;
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
        // reset game end flags
        gameEndSent = false;
        gameEndHandled = false;
    }

    @Override
    public void update(tetris.domain.GameModel model) {
        // If game end has been handled, stop updating to prevent infinite loop
        if (gameEndHandled) {
            return;
        }
        
        // 네트워크 모드에서는 로컬 플레이어만 update
        // 상대방은 네트워크 스냅샷으로만 업데이트됨
        GameModel localModel = game.modelOf(localPlayerId);
        if (localModel != null) {
            localModel.update();
        }
        
        // Check if local player's game over
        if (localModel.getCurrentState() == GameState.GAME_OVER) {
            // Mark that we've handled game end to prevent re-processing
            gameEndHandled = true;
            
            game.markLoser(localPlayerId);
            model.changeState(GameState.GAME_OVER);
            model.showMultiplayerResult(game.getWinnerId() == null ? -1 : game.getWinnerId());
            
            // Send GAME_END message to opponent (only once)
            if (!gameEndSent && sendGameEndCallback != null) {
                sendGameEndCallback.run();
                gameEndSent = true;
            }
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
        controller.withLocalPlayer(action);
    }

    @Override
    public int getPendingLines(int playerId) { return controller.getPendingLines(playerId); }

    @Override
    public java.util.List<tetris.multiplayer.model.AttackLine> getPendingAttackLines(int playerId) {
        return controller.getPendingAttackLines(playerId);
    }

    /**
     * Returns the local player ID (1 or 2) for this networked session.
     */
    public int getLocalPlayerId() {
        return localPlayerId;
    }

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
                controller.onLocalPieceLocked(snapshot, clearedRows);
            }

            @Override
            public void beforeNextSpawn() {
                controller.injectAttackBeforeNextSpawn(playerId);
            }
        };
    }
}
