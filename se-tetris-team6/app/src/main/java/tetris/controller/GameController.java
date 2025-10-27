package tetris.controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import tetris.domain.GameModel;
import tetris.domain.model.GameState;
import tetris.view.GamePanel;

/**
 * Controller 역할을 수행하는 클래스.
 * View(GamePanel)와 Model(추후 생성할 GameModel)을 연결합니다.
 */
public class GameController {

    private final GamePanel gamePanel; // View 참조
    private final GameModel gameModel; // Model 참조

    // 생성자에서 View와 Model을 주입받습니다.
    public GameController(GamePanel gamePanel, GameModel gameModel) {
        this.gamePanel = gamePanel;
        this.gameModel = gameModel;

        // GamePanel의 KeyListener가 보내는 이벤트를 Controller가 처리하도록 위임합니다.
        // 이 방법 대신, GamePanel 생성자에서 직접 KeyListener를 추가할 수도 있습니다.
        this.gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        
        // GamePanel이 키 입력을 받을 수 있도록 포커스를 요청합니다.
        this.gamePanel.setFocusable(true);
    }

    /**
     * 키보드 입력을 처리하는 메소드
     * @param e KeyEvent
     */
    private void handleKeyPress(KeyEvent e) {
        // 현재는 GamePanel이 아닌 PausePanel이 활성화된 경우 입력을 무시합니다.
        // if (TetrisFrame.pausePanel.isVisible()) {
        //     return;
        // }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                gameModel.moveBlockLeft();
                break;
            case KeyEvent.VK_RIGHT:
                gameModel.moveBlockRight();
                break;
            case KeyEvent.VK_DOWN:
                gameModel.moveBlockDown();
                break;
            case KeyEvent.VK_UP: // 보통 회전 키로 사용
                gameModel.rotateBlockClockwise();
                break;
            case KeyEvent.VK_SPACE:
                gameModel.hardDropBlock();
                break;
            case KeyEvent.VK_C:
                gameModel.holdCurrentBlock();
                break;
            case KeyEvent.VK_P:
                gameModel.pauseGame();
                break;
            case KeyEvent.VK_R:
                gameModel.restartGame();
                break;
        }
        
        // Model의 상태가 변경되었으므로 View를 다시 그리도록 요청합니다.
        gamePanel.repaint();
    }

    // 게임 시작, 게임 오버 등 로직을 처리할 메소드들을 추가할 수 있습니다.
    public void startGame() {
        System.out.println("Controller: 게임을 시작합니다.");
        gameModel.changeState(GameState.PLAYING);
    }
}

// --- GamePanel 수정 제안 ---
// 기존 GamePanel에서 KeyListener 구현부를 제거하고, Controller에게 위임합니다.

/*
import javax.swing.*;
import java.awt.*;
// import java.awt.event.KeyListener; // 더 이상 직접 구현할 필요 없음

public class GamePanel extends JPanel { // implements KeyListener 제거
    
    // Controller 참조 (필요하다면)
    // private GameController controller;

    public GamePanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.red);
        this.setOpaque(true);
        this.setVisible(false);

        JLabel label = new JLabel("game panel");
        this.add(label, BorderLayout.CENTER);

        // KeyListener 로직은 Controller가 담당하므로 여기서는 제거합니다.
    }

    // Controller를 설정하는 메소드
    // public void setController(GameController controller) {
    //     this.controller = controller;
    // }
    
    // paintComponent 메소드를 오버라이드하여 게임 보드를 직접 그려야 합니다.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 예: g.drawString("Score: " + gameModel.getScore(), 20, 20);
        // 예: gameModel.getBoard().draw(g);
    }

    // KeyListener 관련 메소드 제거
    // @Override public void keyPressed(KeyEvent e) {}
    // @Override public void keyReleased(KeyEvent e) {}
    // @Override public void keyTyped(KeyEvent e) {}
}
*/
