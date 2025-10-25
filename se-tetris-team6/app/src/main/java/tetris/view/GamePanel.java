package tetris.view;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Graphics;

public class GamePanel extends JPanel {
    public GamePanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.red);
        this.setOpaque(true);
        this.setVisible(false);

        JLabel label = new JLabel("game panel");
        this.add(label, BorderLayout.CENTER);

    }

     // paintComponent 메소드를 오버라이드하여 게임 보드를 직접 그립니다.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

} //View는 이제 입력 처리를 직접 하지 않습니다.