package tetris.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PausePanel extends JPanel {
    public PausePanel() {
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

        JButton continueButton = new JButton() {
            {
                setText("Continue");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        continueButton.addActionListener(e -> onContinueClicked());

        JButton goMainButton = new JButton() {
            {
                setText("Main");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        goMainButton.addActionListener(e -> onGoMainClicked());

        JButton exitButton = new JButton() {
            {
                setText("Exit");
                setFont(new Font("SansSerif", Font.BOLD, 18));
            }
        };
        exitButton.addActionListener(e -> onExitClicked());

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

    private void addComponentVertical(Component component, GridBagConstraints gbc) {
        if (component instanceof JButton button) {
            button.setFont(new Font("SansSerif", Font.BOLD, 18));
            Dimension fixed = new Dimension(240, 44);
            button.setPreferredSize(fixed);
            button.setMaximumSize(fixed);

            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrapper.setOpaque(false);
            wrapper.add(button);
            this.add(wrapper, gbc);
        } else {
            this.add(component, gbc);
        }
        gbc.gridy++;
    }

    protected void onContinueClicked() {
        // Override to handle continue action
    }

    protected void onGoMainClicked() {
        // Override to handle go main action
    }

    protected void onExitClicked() {
        // Override to handle exit action
    }
}