package tetris.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class MainPanel extends JPanel {
    private static final Dimension BUTTON_SIZE = new Dimension(120, 50);
    private static final int BUTTON_MARGIN_Y = 30;

    protected final ArrayList<JButton> buttons = new ArrayList<>();
    private final SpringLayout layout;

    public MainPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.black);
        this.setOpaque(true);
        this.setVisible(false);
        layout = new SpringLayout();
        this.setLayout(layout);
    }

    // 버튼 추가
    public void addButton(String text, Color color, ActionListener action) {
        JButton button = new JButton();
        button.setText(text);
        button.setBackground(color);
        button.setFocusable(false);
        button.setPreferredSize(BUTTON_SIZE);
        button.addActionListener(action);
        buttons.add(button);
        this.add(button);
    }

    // 버튼 위치 조정
    public void layoutButtons() {
        if (!buttons.isEmpty()) {
            // 첫 번째 버튼을 패널 크기의 1/5(가로), 1/8(세로) 위치에 배치
            JButton firstButton = buttons.get(0);
            int xPosition = this.getWidth() / 5;
            int yPosition = this.getHeight() / 8;
            layout.putConstraint(SpringLayout.WEST, firstButton, xPosition, SpringLayout.WEST, this);
            layout.putConstraint(SpringLayout.NORTH, firstButton, yPosition, SpringLayout.NORTH, this);

            // 나머지 버튼들을 첫 번째 버튼 아래에 배치
            JButton prevButton = firstButton;
            for (int i = 1; i < buttons.size(); i++) {
                JButton nextButton = buttons.get(i);
                layout.putConstraint(SpringLayout.WEST, nextButton, xPosition, SpringLayout.WEST, this);
                layout.putConstraint(SpringLayout.NORTH, nextButton, BUTTON_MARGIN_Y, SpringLayout.SOUTH, prevButton);
                prevButton = nextButton;
            }
        }
    }

}
