package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PausePanel extends JPanel {
    public JButton continueButton;
    public JButton goMainButton;
    public JButton exitButton;

    public PausePanel() {
        this.setSize(TetrisFrame.FRAME_SIZE);
        this.setBackground(Color.gray);
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
                insets = new Insets(30, 200, 30, 200);
            }
        };

        JLabel textLabel = new JLabel() {
            {
                setText("Pause Panel");
                setFont(new Font("SansSerif", Font.BOLD, 24));
                setForeground(Color.WHITE);
                setHorizontalAlignment(JLabel.CENTER);
            }
        };

        goMainButton = new JButton() {
            {
                setText("Main");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };

        continueButton = new JButton() {
            {
                setText("Continue");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        exitButton = new JButton() {
            {
                setText("Exit");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };

        // Add Components to GridBagLayout
        for (int i = 0; i < 3; i++) { // 빈 공간
            addComponentVertical(new EmptySpace(), gbc);
        }
        addComponentVertical(textLabel, gbc); // 텍스트 라벨
        addComponentVertical(continueButton, gbc); // 재게 버튼
        addComponentVertical(goMainButton, gbc); // 메인 버튼
        addComponentVertical(exitButton, gbc); // 게임 종료 버튼
        for (int i = 0; i < 3; i++) { // 빈 공간
            addComponentVertical(new EmptySpace(), gbc);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        this.add(component, gbc);
        gbc.gridy++;
    }
}
