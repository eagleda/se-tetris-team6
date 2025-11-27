package tetris.network;

import tetris.network.protocol.PlayerInput;
import tetris.domain.model.GameState;
import tetris.network.protocol.AttackLine;


public interface GameDataListener {
    void onOpponentInput(PlayerInput input);
    void onIncomingAttack(AttackLine[] lines);
    void onGameStateUpdate(GameState state);
    void onGameStart();
    void onGameEnd(String winner);
}
