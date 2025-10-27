package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

public class MainPanel extends JPanel implements KeyListener {
    public static MainPanel instance;

    public JButton gameButton;
    public JButton settingButton;
    public JButton scoreboardButton;

    public Color NORMAL_COLOR = Color.white;
    public Color HIGHLIGHT_COLOR = Color.gray;
    private List<JButton> buttons;
    private int currentFocusIndex = 0;

    public MainPanel() {
        instance = this;

        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.black);
        this.setOpaque(true);
        this.setVisible(false);
        this.setLayout(new GridBagLayout());

        // GridBagLayout 설정
        GridBagConstraints gbc = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                weightx = 1.0;
                weighty = 1.0;
                anchor = GridBagConstraints.CENTER;
                fill = GridBagConstraints.BOTH;
                insets = new Insets(10, 200, 10, 200);
            }
        };

        // 버튼 생성
        gameButton = getDefaultStyleButton("Game");
        settingButton = getDefaultStyleButton("Setting");
        scoreboardButton = getDefaultStyleButton("Scoreboard");

        buttons = new ArrayList<>();
        buttons.add(gameButton);
        buttons.add(settingButton);
        buttons.add(scoreboardButton);

        // Add Components to GridBagLayout
        for (int i = 0; i < 2; i++) {
            addComponentVertical(new EmptySpace(), gbc);
        }
        addComponentVertical(gameButton, gbc);
        addComponentVertical(settingButton, gbc);
        addComponentVertical(scoreboardButton, gbc);
        for (int i = 0; i < 8; i++) {
            addComponentVertical(new EmptySpace(), gbc);
        }

        // KeyListener 설정
        this.addKeyListener(this);
    }

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        this.add(component, gbc);
        gbc.gridy++;
    }

    private JButton getDefaultStyleButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 18));
        return button;
    }

    public void focusButton(int direction) {
        buttons.get(currentFocusIndex).setBackground(NORMAL_COLOR);
        currentFocusIndex = (currentFocusIndex + direction + buttons.size()) % buttons.size();
        buttons.get(currentFocusIndex).setBackground(HIGHLIGHT_COLOR);
    }

    public void clickFocusButton() {
        buttons.get(currentFocusIndex).doClick();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Controller.handleKeyPress(e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> focusButton(-1);
            case KeyEvent.VK_DOWN -> focusButton(1);
            case KeyEvent.VK_ENTER -> clickFocusButton();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
