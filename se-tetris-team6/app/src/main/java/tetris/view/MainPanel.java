package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

public class MainPanel extends JPanel {
    private static final Dimension BUTTON_SIZE = new Dimension(150, 80);
    private static final int BUTTON_MARGIN_Y = 30;

    public JButton gameButton;
    public JButton settingButton;
    public JButton scoreboardButton;

    public MainPanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.black);
        this.setOpaque(true);
        this.setVisible(false);
        // layout = new SpringLayout();
        // this.setLayout(layout);
        this.setLayout(new GridBagLayout());

        // GridBagLayout 설정
        GridBagConstraints gbc = new GridBagConstraints() {
            {
                gridx = 0;
                gridy = 0;
                weightx = 1.0;
                weighty = 1.0;
                anchor = GridBagConstraints.CENTER;
            }
        };

        // 버튼 생성
        gameButton = new JButton() {
            {
                setText("Game");
                setFont(new Font("SansSerif", Font.BOLD, 18));
                setPreferredSize(BUTTON_SIZE);
            }
        };
        settingButton = new JButton() {
            {
                setText("Setting");
                setFont(new Font("SansSerif", Font.BOLD, 18));
                setPreferredSize(BUTTON_SIZE);
            }
        };
        scoreboardButton = new JButton() {
            {
                setText("Scoreboard");
                setFont(new Font("SansSerif", Font.BOLD, 18));
                setPreferredSize(BUTTON_SIZE);
            }
        };

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
    }

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        this.add(component, gbc);
        gbc.gridy++;
    }
}
