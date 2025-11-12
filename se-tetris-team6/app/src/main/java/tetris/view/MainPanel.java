package tetris.view;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public JButton gameButton;
    public JButton itemGameButton;
    public JButton settingButton;
    public JButton scoreboardButton;

    public Color NORMAL_COLOR = Color.white;
    public Color HIGHLIGHT_COLOR = Color.gray;
    private List<JButton> buttons;
    private int currentFocusIndex = 0;

    public MainPanel() {
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

        // 게임 타이틀
        JLabel titleText = new JLabel("TETRIS", SwingConstants.CENTER);
        titleText.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleText.setForeground(Color.BLUE);
        titleText.setHorizontalAlignment(SwingConstants.CENTER);

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
        addComponentVertical(titleText, gbc);
        addComponentVertical(gameButton, gbc);
        addComponentVertical(itemGameButton, gbc);
        addComponentVertical(settingButton, gbc);
        addComponentVertical(scoreboardButton, gbc);
        for (int i = 0; i < 8; i++) {
            addComponentVertical(new EmptySpace(), gbc);
        }

        // Add button to buttons
        buttons = new ArrayList<>();
        buttons.add(gameButton);
        buttons.add(itemGameButton);
        buttons.add(settingButton);
        buttons.add(scoreboardButton);
    }

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        this.add(component, gbc);
        gbc.gridy++;
    }

    public void focusButton(int direction) {
        buttons.get(currentFocusIndex).setBackground(NORMAL_COLOR);
        currentFocusIndex = (currentFocusIndex + direction + buttons.size()) % buttons.size();
        buttons.get(currentFocusIndex).setBackground(HIGHLIGHT_COLOR);
    }

    public void clickFocusButton() {
        buttons.get(currentFocusIndex).doClick();
    }
}
