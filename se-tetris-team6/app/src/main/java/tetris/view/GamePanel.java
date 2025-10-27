package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tetris.controller.GameController;
import tetris.domain.GameModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * View 역할을 수행하는 클래스.
 * Model의 데이터를 기반으로 화면을 그리고, 사용자의 입력을 Controller에 전달합니다.
 * Model의 변경을 감지하기 위해 PropertyChangeListener를 구현합니다. (Observer 패턴)
 */

public class GamePanel extends JPanel implements PropertyChangeListener {

    private final GameModel gameModel;
    private final GameController gameController;

    public GamePanel(GameModel gameModel, GameController gameController) {
        this.gameModel = gameModel;
        this.gameController = gameController;

        // 1. View가 스스로 Model의 변경을 구독(Observe)합니다.
        this.gameModel.addPropertyChangeListener(this);

        // 2. View가 스스로 키 입력을 받을 준비를 합니다.
        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 3. 입력을 받으면 Controller에게 그대로 전달합니다.
                GamePanel.this.gameController.handleKeyPress(e.getKeyCode());
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Model의 어떤 속성이 변경되었는지에 따라 선택적으로 동작할 수도 있습니다.
        // 지금은 어떤 변경이든 화면을 다시 그리도록 합니다.
        if ("gameState".equals(evt.getPropertyName()) || "board".equals(evt.getPropertyName())) {
            repaint();
        }
    }

    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //여기에 gameModel의 데이터를 가져와 화면을 그리는 로직을 구현합니다.
    }

} //View는 이제 입력 처리를 직접 하지 않습니다.