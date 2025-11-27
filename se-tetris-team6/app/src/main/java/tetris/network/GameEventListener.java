package tetris.network;

import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;

public interface GameEventListener {
    // 네트워크 스레드(NetworkManager)로 공격 라인을 전송하는 인터페이스
    void sendAttackLines(AttackLine[] attackLines);
    // (추가) 로컬 플레이어의 입력을 네트워크로 전송하는 인터페이스
    void sendPlayerInput(PlayerInput input); 
}
