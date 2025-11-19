package tetris.view.GameComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import tetris.domain.GameModel;

public class ScorePanel extends JPanel {

    private JTextPane scoreText;
    private JTextPane speedText;

    private GameModel gameModel;

    public ScorePanel() {
        // 패널 레이아웃 및 외형 설정
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.BLACK);

        // 테두리
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        JTextPane titleText = new JTextPane();
        titleText.setText("Score");
        titleText.setEditable(false);
        titleText.setOpaque(false);
        titleText.setForeground(Color.GREEN);
        titleText.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleText.setFocusable(false);
        add(titleText, BorderLayout.NORTH);

        scoreText = new JTextPane();
        scoreText.setText("0");
        scoreText.setEditable(false);
        scoreText.setOpaque(false);
        scoreText.setForeground(Color.WHITE);
        scoreText.setFont(new Font("SansSerif", Font.BOLD, 24));
        scoreText.setFocusable(false);
        add(scoreText, BorderLayout.CENTER);

        speedText = new JTextPane();
        speedText.setText("Speed Lv. 0");
        speedText.setEditable(false);
        speedText.setOpaque(false);
        speedText.setForeground(Color.LIGHT_GRAY);
        speedText.setFont(new Font("SansSerif", Font.BOLD, 14));
        speedText.setFocusable(false);
        add(speedText, BorderLayout.SOUTH);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameModel == null) return;
        int pts = gameModel.getScore().getPoints();
        updateScoreDisplay(pts);
        updateSpeedDisplay(gameModel.getSpeedLevel());
    }
}
