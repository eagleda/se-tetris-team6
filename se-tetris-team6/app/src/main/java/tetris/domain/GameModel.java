package tetris.domain;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import tetris.domain.model.GameState;

/**
 * Model 역할을 수행하는 클래스.
 * 게임의 모든 데이터(상태, 보드, 블록, 점수 등)와 핵심 비즈니스 로직을 가집니다.
 */
public class GameModel {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private GameState gameState;
    // ... private int[][] board;
    // ... private Block currentBlock;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    // 상태를 변경하는 메소드들은 변경 후 반드시 "신호"를 보내야 합니다.
    public void moveBlockLeft() {
        // ... 블록 이동 로직 ...
        // 로직 수행 후, "board" 속성이 변경되었음을 알림
        this.pcs.firePropertyChange("board", null, this.board);
    }
    
    public void setGameState(GameState newState) {
        GameState oldState = this.gameState;
        this.gameState = newState;
        // "gameState" 속성이 변경되었음을 알림
        this.pcs.firePropertyChange("gameState", oldState, newState);
    }
    
    public GameState getGameState() {
        return this.gameState;
    }

    // ... moveBlockRight, rotateBlock, initGame 등 모든 로직 구현 ...
}
