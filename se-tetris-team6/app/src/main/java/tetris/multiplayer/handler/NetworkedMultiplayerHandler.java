package tetris.multiplayer.handler;

import java.util.Objects;
import java.util.function.Consumer;

import tetris.domain.GameModel;
import tetris.domain.GameModel.MultiplayerHook;
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
    private MultiplayerHook p1Hook;
    private MultiplayerHook p2Hook;

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
            // 매 틱마다 두 플레이어의 스냅샷을 브로드캐스트하여 클라이언트 UI를 실시간 동기화
            try {
                if (player1Model != null && player1Model.getCurrentState() == GameState.PLAYING) {
                    controller.sendGameState(player1Model);
                }
                if (player2Model != null && player2Model.getCurrentState() == GameState.PLAYING) {
                    controller.sendGameState(player2Model);
                }
            } catch (Exception e) {
                System.err.println("[NetworkedMultiplayerHandler] Failed to broadcast tick snapshots: " + e.getMessage());
            }

            if (maybeHandleTimeLimit(model)) {
                return;
            }

            // Check if any player's game over
            boolean p1GameOver = player1Model != null && player1Model.getCurrentState() == GameState.GAME_OVER;
            boolean p2GameOver = player2Model != null && player2Model.getCurrentState() == GameState.GAME_OVER;

            if (p1GameOver || p2GameOver) {
                int loserId;
                if (p1GameOver) {
                    loserId = 1;
                    game.markLoser(1);
                } else {
                    loserId = 2;
                    game.markLoser(2);
                }
                
                System.out.println("[NetworkedMultiplayerHandler][SERVER] Player " + loserId + " lost, sending GAME_END to all");

                concludeGame(model);
                return; // 게임 종료 시 이후 브로드캐스트 생략
            }
        } else {
            // 클라이언트(P2): 아무것도 하지 않음
            // 서버로부터 받은 스냅샷으로만 화면 업데이트
            // 게임 종료 확인만 수행
            GameModel localModel = game.modelOf(localPlayerId);
            if (localModel != null && localModel.getCurrentState() == GameState.GAME_OVER) {
                if (!gameEndHandled) {
                    gameEndHandled = true;
                    model.changeState(GameState.GAME_OVER);
                    
                    // 승자 결정: 게임이 종료되었으면 승자 ID 확인
                    int winnerId = game.getWinnerId() == null ? -1 : game.getWinnerId();
                    
                    // 승자가 결정되지 않았다면 로컬 플레이어가 패배한 것으로 간주
                    if (winnerId == -1 || winnerId == 0) {
                        // 상대방을 승자로 설정
                        int opponentId = (localPlayerId == 1) ? 2 : 1;
                        winnerId = opponentId;
                        game.markLoser(localPlayerId);
                    }
                    
                    model.showMultiplayerResult(winnerId, localPlayerId);
                    System.out.println("[NetworkedMultiplayerHandler][CLIENT] Game ended - Winner: " + winnerId + ", LocalPlayer: " + localPlayerId + ", Result: " + (winnerId == localPlayerId ? "WIN" : "LOSE"));
                }
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

    private boolean maybeHandleTimeLimit(GameModel model) {
        if (game.isGameOver()) {
            return true;
        }
        GameModel reference = game.modelOf(1);
        if (reference == null || !reference.isTimeLimitMode()) {
            return false;
        }
        if (!reference.isTimeLimitExpired()) {
            return false;
        }
        int comparison = game.compareScores();
        if (comparison > 0) {
            game.markLoser(2);
            System.out.println("[NetworkedMultiplayerHandler][SERVER] Time limit - Player 2 lost (lower score)");
        } else if (comparison < 0) {
            game.markLoser(1);
            System.out.println("[NetworkedMultiplayerHandler][SERVER] Time limit - Player 1 lost (lower score)");
        } else {
            game.endWithDraw();
            System.out.println("[NetworkedMultiplayerHandler][SERVER] Time limit - Draw (equal scores)");
        }
        concludeGame(model);
        return true;
    }

    private void concludeGame(GameModel model) {
        if (gameEndHandled) {
            return;
        }
        gameEndHandled = true;
        model.changeState(GameState.GAME_OVER);
        
        int winnerId = game.getWinnerId() == null ? -1 : game.getWinnerId();
        int loserId = game.getLoserId() == null ? -1 : game.getLoserId();
        
        // 서버(로컬)에게 결과 표시
        model.showMultiplayerResult(winnerId, localPlayerId);
        
        System.out.println("[NetworkedMultiplayerHandler][SERVER] Game concluded - Winner: " + winnerId + ", Loser: " + loserId + ", LocalPlayer: " + localPlayerId + ", Result: " + (winnerId == localPlayerId ? "WIN" : "LOSE"));

        // GAME_END 메시지 전송 (클라이언트에게 패배/승리 정보 전달)
        if (!gameEndSent && sendGameEndCallback != null) {
            sendGameEndCallback.run();
            gameEndSent = true;
        }
    }

    private void registerHookForLocal() {
        // 서버(플레이어1)인 경우 두 플레이어 모두 Hook 등록하여
        // 공격 대기열 처리와 beforeNextSpawn 로직을 동일하게 적용한다.
        if (localPlayerId == 1) {
            p1Hook = createHook(1);
            p2Hook = createHook(2);
            game.player(1).getModel().addMultiplayerHook(p1Hook);
            game.player(2).getModel().addMultiplayerHook(p2Hook);
        } else {
            // 클라이언트(P2)는 서버 권한 모델이므로 로컬 모델에 공격 로직 Hook을 등록하지 않습니다.
            // 모든 공격 처리 및 적용은 서버가 수행하고 스냅샷을 통해 동기화됩니다.
        }
    }

    private void unregisterHookForLocal() {
        if (localPlayerId == 1) {
            if (p1Hook != null) {
                try { game.player(1).getModel().removeMultiplayerHook(p1Hook); } catch (Exception ignore) {}
                p1Hook = null;
            }
            if (p2Hook != null) {
                try { game.player(2).getModel().removeMultiplayerHook(p2Hook); } catch (Exception ignore) {}
                p2Hook = null;
            }
        } else {
            // 클라이언트(P2)는 Hook을 등록하지 않았으므로 해제할 필요가 없습니다.
        }
    }

    private tetris.domain.GameModel.MultiplayerHook createHook(int playerId) {
        return new tetris.domain.GameModel.MultiplayerHook() {
            @Override
            public void onPieceLocked(tetris.multiplayer.model.LockedPieceSnapshot snapshot, int[] clearedRows, int boardWidth) {
                // Hook의 playerId 매개변수를 사용하여 정확한 플레이어의 공격으로 등록합니다.
                // P1 모델의 Hook이 호출되면 playerId=1로 공격 등록 (상대방 P2 버퍼에 추가)
                // P2 모델의 Hook이 호출되면 playerId=2로 공격 등록 (상대방 P1 버퍼에 추가)
                GameModel model = game.modelOf(playerId);
                game.onPieceLocked(playerId, snapshot, clearedRows, boardWidth);
                
                // 로컬 플레이어만 네트워크로 이벤트 전송
                if (playerId == localPlayerId) {
                    controller.sendPieceLockedEvent(snapshot, clearedRows);
                    if (clearedRows.length > 0) {
                        controller.sendGameState(model);
                    }
                }
            }

            @Override
            public void beforeNextSpawn() {
                // 서버(P1)에서 P1 또는 P2의 공격 대기열을 처리합니다.
                controller.injectAttackBeforeNextSpawn(playerId);
            }
        };
    }
}