package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

public class MainPanel extends JPanel {
    public JButton gameButton;
    public JButton itemGameButton;
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
                fill = GridBagConstraints.BOTH;
                insets = new Insets(10, 200, 10, 200);
            }
        };

        // 버튼 생성
        gameButton = new JButton() {
            {
                setText("Game");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        itemGameButton = new JButton() {
            {
                setText("Item Mode");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        settingButton = new JButton() {
            {
                setText("Setting");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        scoreboardButton = new JButton() {
            {
                setText("Scoreboard");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };

        // Add Components to GridBagLayout
        for (int i = 0; i < 2; i++) {
            addComponentVertical(new EmptySpace(), gbc);
        }
        addComponentVertical(gameButton, gbc);
        addComponentVertical(itemGameButton, gbc);
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
