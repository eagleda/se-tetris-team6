package tetris.network;

import tetris.network.protocol.AttackLine;
import tetris.network.protocol.PlayerInput;
import tetris.domain.model.Block; // Block 객체 임포트 필요

public interface GameEventListener {
    // 네트워크 스레드(NetworkManager)로 공격 라인을 전송하는 인터페이스
    void sendAttackLines(AttackLine[] attackLines);
    // (추가) 로컬 플레이어의 입력을 네트워크로 전송하는 인터페이스
    void sendPlayerInput(PlayerInput input);
    
    // 블록 회전 정보를 네트워크로 전송하는 메서드 추가
    void sendBlockRotation(Block block); 
}
