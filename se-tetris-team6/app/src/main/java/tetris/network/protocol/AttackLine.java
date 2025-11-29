package tetris.network.protocol;

import java.io.Serializable;

/**
 * 상대방에게 전송할 공격 라인 정보를 담는 데이터 클래스.
 * (예: 공격 라인의 개수, 특수 블록 정보 등)
 */
public class AttackLine implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 필드 이름을 공격력(strength)으로 명확히 변경
    private final int strength; 
    
    public AttackLine(int strength) {
        this.strength = strength;
    }
    
    public int getStrength() {
        return strength;
    }
}