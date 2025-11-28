package tetris.view.GameComponent;

import java.util.Objects;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import tetris.domain.GameModel;

public class ScorePanel extends JPanel {

    private JTextPane scoreText;
    private JTextPane speedText;
    private JTextPane slowBuffTimerText;
    private JTextPane doubleScoreBuffTimerText;

    private GameModel gameModel;

    public ScorePanel() {
        // 패널 레이아웃 및 외형 설정
        setLayout(new java.awt.GridBagLayout());
        setOpaque(true);
        setBackground(Color.BLACK);

        // 테두리
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(4, 4, 4, 4);
        gbc.fill = java.awt.GridBagConstraints.NONE;

        // titleText: 좌상단
        JTextPane titleText = new JTextPane();
        titleText.setText("Score");
        titleText.setEditable(false);
        titleText.setOpaque(false);
        titleText.setForeground(Color.GREEN);
        titleText.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleText.setFocusable(false);
        titleText.setPreferredSize(new Dimension(80, 32));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(titleText, gbc);

        // scoreText: titleText 우측
        scoreText = new JTextPane();
        scoreText.setText("0");
        scoreText.setEditable(false);
        scoreText.setOpaque(false);
        scoreText.setForeground(Color.WHITE);
        scoreText.setFont(new Font("SansSerif", Font.BOLD, 24));
        scoreText.setFocusable(false);
        scoreText.setPreferredSize(new Dimension(80, 32));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(scoreText, gbc);

        // speedText, slowBuffTimerText, doubleScoreBuffTimerText: 아래쪽에 수평 배치
        speedText = new JTextPane();
        speedText.setText("Speed Lv. 0");
        speedText.setEditable(false);
        speedText.setOpaque(false);
        speedText.setForeground(Color.LIGHT_GRAY);
        speedText.setFont(new Font("SansSerif", Font.BOLD, 12));
        speedText.setFocusable(false);
        speedText.setPreferredSize(new Dimension(90, 24));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(speedText, gbc);

        slowBuffTimerText = new JTextPane();
        slowBuffTimerText.setText("");
        slowBuffTimerText.setEditable(false);
        slowBuffTimerText.setOpaque(false);
        slowBuffTimerText.setForeground(Color.CYAN);
        slowBuffTimerText.setFont(new Font("SansSerif", Font.BOLD, 12));
        slowBuffTimerText.setFocusable(false);
        slowBuffTimerText.setPreferredSize(new Dimension(90, 24));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(slowBuffTimerText, gbc);

        doubleScoreBuffTimerText = new JTextPane();
        doubleScoreBuffTimerText.setText("");
        doubleScoreBuffTimerText.setEditable(false);
        doubleScoreBuffTimerText.setOpaque(false);
        doubleScoreBuffTimerText.setForeground(Color.ORANGE);
        doubleScoreBuffTimerText.setFont(new Font("SansSerif", Font.BOLD, 12));
        doubleScoreBuffTimerText.setFocusable(false);
        doubleScoreBuffTimerText.setPreferredSize(new Dimension(120, 24));
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(doubleScoreBuffTimerText, gbc);
    }

    public void bindGameModel(GameModel model) {
        this.gameModel = Objects.requireNonNull(model, "model");
        repaint();
    }

    private void updateScoreDisplay(int points) {
        scoreText.setText(String.valueOf(points));
    }

    private void updateSpeedDisplay(int level) {
        speedText.setText(String.format("Speed Lv. %d", level));
    }

    private void updateBuffTimers() {
        if (gameModel == null) return;

        long slowBuffRemaining = gameModel.getSlowBuffRemainingTimeMs();
        if (slowBuffRemaining > 0) {
            slowBuffTimerText.setText(String.format("Slow: %.1f s", slowBuffRemaining / 1000.0));
        } else {
            slowBuffTimerText.setText("");
        }

        long doubleScoreBuffRemaining = gameModel.getDoubleScoreBuffRemainingTimeMs();
        if (doubleScoreBuffRemaining > 0) {
            doubleScoreBuffTimerText.setText(String.format("Double Score: %.1f s", doubleScoreBuffRemaining / 1000.0));
        } else {
            doubleScoreBuffTimerText.setText("");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameModel == null) return;
        updateScoreDisplay(gameModel.getScore().getPoints());
        updateSpeedDisplay(gameModel.getSpeedLevel());
        updateBuffTimers();
    }

    @Override
    public Dimension getMaximumSize() {
        return getSize();
    }
}
