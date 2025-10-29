package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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

        JLabel titleLabel = new JLabel("TETRIS", SwingConstants.CENTER) {
            {
                setFont(new Font("SansSerif", Font.BOLD, 54));
                setForeground(Color.WHITE);
                setOpaque(false);
            }
        };

        // Add Components to GridBagLayout
        addComponentVertical(new EmptySpace(), gbc);
        addComponentVertical(titleLabel, gbc);
        addComponentVertical(new EmptySpace(), gbc);
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
