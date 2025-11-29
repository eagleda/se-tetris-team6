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
        
        // 서버 권한 방식: 서버(P1)만 게임 로직 실행
        if (localPlayerId == 1) {
            // 서버: 두 플레이어 모두 update
            GameModel player1Model = game.modelOf(1);
            GameModel player2Model = game.modelOf(2);
            
            if (player1Model != null) {
                player1Model.update();
            }
            if (player2Model != null) {
                player2Model.update();
            }
            
            // Check if any player's game over
            boolean p1GameOver = player1Model != null && player1Model.getCurrentState() == GameState.GAME_OVER;
            boolean p2GameOver = player2Model != null && player2Model.getCurrentState() == GameState.GAME_OVER;
            
            if (p1GameOver || p2GameOver) {
                gameEndHandled = true;
                
                if (p1GameOver) {
                    game.markLoser(1);
                } else {
                    game.markLoser(2);
                }
                
                model.changeState(GameState.GAME_OVER);
                model.showMultiplayerResult(game.getWinnerId() == null ? -1 : game.getWinnerId());
                
                if (!gameEndSent && sendGameEndCallback != null) {
                    sendGameEndCallback.run();
                    gameEndSent = true;
                }
            }
        } else {
            // 클라이언트(P2): 아무것도 하지 않음
            // 서버로부터 받은 스냅샷으로만 화면 업데이트
            // 게임 종료 확인만 수행
            GameModel localModel = game.modelOf(localPlayerId);
            if (localModel != null && localModel.getCurrentState() == GameState.GAME_OVER) {
                gameEndHandled = true;
                model.changeState(GameState.GAME_OVER);
                model.showMultiplayerResult(game.getWinnerId() == null ? -1 : game.getWinnerId());
            }
        }
    }

    @Override
    public void exit(tetris.domain.GameModel model) {
        unregisterHookForLocal();
    }

    @Override
    public void dispatchToPlayer(int playerId, Consumer<GameModel> action) {
        if (localPlayerId == 1) {
            // 서버: 모든 플레이어에게 dispatch 가능
            GameModel targetModel = game.modelOf(playerId);
            if (targetModel != null && action != null) {
                action.accept(targetModel);
            }
        } else {
            // 클라이언트: 로컬 플레이어에게만 dispatch (but 실제로는 키 입력만 서버로 전송)
            if (playerId != localPlayerId) {
                return;
            }
            controller.withLocalPlayer(action);
        }
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
