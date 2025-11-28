package tetris.multiplayer.handler;

import java.util.function.Consumer;
import tetris.domain.GameModel;
// GameState import not required here
import tetris.domain.handler.GameHandler;

/**
 * Common contract for multiplayer handlers used by Local and Networked modes.
 */
public interface MultiplayerHandler extends GameHandler {
    void dispatchToPlayer(int playerId, Consumer<GameModel> action);
    int getPendingLines(int playerId);
}
