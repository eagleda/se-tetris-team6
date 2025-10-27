package tetris.controller;

import java.awt.event.KeyEvent;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;

/**
 * Controller 역할을 수행하는 클래스.
 * View(GamePanel)와 Model(추후 생성할 GameModel)을 연결합니다.
 */
public class GameController {

    private final GameModel gameModel; // Model 참조

    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;
    }

    /**
     * 키보드 입력을 처리하는 메소드
     * @param e KeyEvent
     */
    public void handleKeyPress(int keyCode) {
    
        if (gameModel.getGameState() != GameState.PLAYING) {
            return;
        }

        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                // gameModel.moveBlockLeft();
                System.out.println("Controller: 왼쪽 키 입력 감지");
                break;
            case KeyEvent.VK_RIGHT:
                // gameModel.moveBlockRight();
                System.out.println("Controller: 오른쪽 키 입력 감지");
                break;
            case KeyEvent.VK_DOWN:
                // gameModel.moveBlockDown();
                System.out.println("Controller: 아래쪽 키 입력 감지");
                break;
            case KeyEvent.VK_UP: // 보통 회전 키로 사용
                // gameModel.rotateBlock();
                System.out.println("Controller: 위쪽 키(회전) 입력 감지");
                break;
            case KeyEvent.VK_SPACE:
                // gameModel.hardDrop();
                System.out.println("Controller: 스페이스 바(하드 드롭) 입력 감지");
                break;
        }
    
    }

    // 게임 시작, 게임 오버 등 로직을 처리할 메소드들을 추가할 수 있습니다.
    public void startGame() {
        System.out.println("Controller: 게임을 시작합니다.");
        // gameModel.initGame();
        // gamePanel.repaint();
    }
}