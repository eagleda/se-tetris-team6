package tetris.multiplayer.handler;

import java.util.Objects;
import java.util.function.Consumer;

import tetris.domain.GameModel;
import tetris.domain.GameModel.MultiplayerHook;
import tetris.domain.handler.GameHandler;
import tetris.domain.model.GameState;
import tetris.multiplayer.controller.LocalMultiPlayerController;
import tetris.multiplayer.model.MultiPlayerGame;

/**
 * Local 2P 모드를 기존 {@link GameHandler} 상태 머신에 붙여주는 핸들러.
 * - GameModel에 멀티 훅을 등록해 락/스폰 이벤트를 자동 수신한다.
 * - update()에서 두 모델을 동시에 틱 처리하고 승패를 감지한다.
 */
public final class LocalMultiplayerHandler implements MultiplayerHandler {

    private final MultiPlayerGame game;
    private final LocalMultiPlayerController controller;
    private final GameState state;
    private MultiplayerHook p1Hook;
    private MultiplayerHook p2Hook;

    public LocalMultiplayerHandler(MultiPlayerGame game,
                                   LocalMultiPlayerController controller,
                                   GameState state) {
        this.game = Objects.requireNonNull(game, "game");
        this.controller = Objects.requireNonNull(controller, "controller");
        this.state = Objects.requireNonNull(state, "state");
    }

    @Override
    public GameState getState() {
        return state;
    }

    @Override
    public void enter(GameModel model) {
        game.player(1).setReady(false);
        game.player(2).setReady(false);
        registerHooks();
    }

    @Override
    public void update(GameModel model) {
        controller.tick();
        // 개별 플레이어가 먼저 GAME_OVER가 되면 즉시 패배자로 표시한다.
        if (!game.isGameOver()) {
            if (game.modelOf(1).getCurrentState() == GameState.GAME_OVER) {
                System.out.println("[LOG][LocalMulti] Player1 reached GAME_OVER → mark loser");
                game.markLoser(1);
            } else if (game.modelOf(2).getCurrentState() == GameState.GAME_OVER) {
                System.out.println("[LOG][LocalMulti] Player2 reached GAME_OVER → mark loser");
                game.markLoser(2);
            }
        }

        if (game.isGameOver() && model.getCurrentState() != GameState.GAME_OVER) {
            int winnerId = game.getWinnerId() == null ? -1 : game.getWinnerId();
            System.out.printf("[LOG][LocalMulti] Game over detected, winner: %d%n", winnerId);
            // 두 플레이어 모델 모두 GAME_OVER 상태로 정지시킨다.
            controller.withPlayer(1, m -> m.changeState(GameState.GAME_OVER));
            controller.withPlayer(2, m -> m.changeState(GameState.GAME_OVER));
            model.changeState(GameState.GAME_OVER);
            // 로컬 멀티플레이용 메시지를 직접 전달
            model.showLocalMultiplayerResult(winnerId);
        }
    }

    @Override
    public void exit(GameModel model) {
        unregisterHooks();
    }

    public void dispatchToPlayer(int playerId, Consumer<GameModel> action) {
        controller.withPlayer(playerId, action);
    }

    public int getPendingLines(int playerId) {
        return controller.getPendingLines(playerId);
    }

    /**
     * 특정 플레이어가 받을 공격 줄의 실제 패턴을 반환한다.
     */
    public java.util.List<tetris.multiplayer.model.AttackLine> getPendingAttackLines(int playerId) {
        return controller.getPendingAttackLines(playerId);
    }

    private void registerHooks() {
        // GameModel 훅은 중복 등록을 피하기 위해 항상 기존 값을 제거하고 새로 등록한다.
        unregisterHooks();
        p1Hook = createHook(1);
        p2Hook = createHook(2);
        game.player(1).getModel().addMultiplayerHook(p1Hook);
        game.player(2).getModel().addMultiplayerHook(p2Hook);
    }

    private void unregisterHooks() {
        if (p1Hook != null) {
            game.player(1).getModel().removeMultiplayerHook(p1Hook);
            p1Hook = null;
        }
        if (p2Hook != null) {
            game.player(2).getModel().removeMultiplayerHook(p2Hook);
            p2Hook = null;
        }
    }

    private MultiplayerHook createHook(int playerId) {
        return new MultiplayerHook() {
            @Override
            public void onPieceLocked(tetris.multiplayer.model.LockedPieceSnapshot snapshot,
                    int[] clearedRows,
                    int boardWidth) {
                // 해당 플레이어가 줄을 삭제하면 VersusRules로 위임해 공격을 계산한다.
                controller.onPieceLocked(playerId, snapshot, clearedRows);
            }

            @Override
            public void beforeNextSpawn() {
                // 스폰 직전에 대기 중인 공격 줄을 주입한다.
                controller.injectAttackBeforeNextSpawn(playerId);
            }
        };
    }
}
