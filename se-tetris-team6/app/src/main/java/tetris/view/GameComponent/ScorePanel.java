package tetris.view.GameComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
<<<<<<< HEAD
import java.util.Objects;
=======
import java.util.Random;
>>>>>>> 870d0047cfb1dc8314fb9ed388606a46b54b486f

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import tetris.domain.GameModel;

public class ScorePanel extends JPanel {

<<<<<<< HEAD
    private JTextPane scoreText;
=======
    private JTextPane ScoreText;
>>>>>>> 870d0047cfb1dc8314fb9ed388606a46b54b486f

    private GameModel gameModel;

    public ScorePanel() {
        // 패널 레이아웃 및 외형 설정
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.BLACK);

<<<<<<< HEAD
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
    }

    public void bindGameModel(GameModel model) {
        this.gameModel = Objects.requireNonNull(model, "model");
        repaint();
    }

    private void updateScoreDisplay(int points) {
        scoreText.setText(String.valueOf(points));
=======
        // 빨간 테두리: 패널이 할당된 영역을 감싸도록 표시
        setBorder(BorderFactory.createLineBorder(Color.RED, 3));

        JTextPane titleText = new JTextPane() {
            {
                setText("Scores");
                setEditable(false);
                setOpaque(false); // 배경은 패널 배경을 사용
                setForeground(Color.GREEN);
                setFont(new Font("SansSerif", Font.BOLD, 32));
                setFocusable(false);
            }
        };
        add(titleText, BorderLayout.NORTH);

        ScoreText = new JTextPane() {
            {
                setText("0");
                setEditable(false);
                setOpaque(false); // 배경은 패널 배경을 사용
                setForeground(Color.WHITE);
                setFont(new Font("SansSerif", Font.BOLD, 24));
                setFocusable(false);
            }
        };
        add(ScoreText, BorderLayout.CENTER);
    }

    public void bindGameModel(GameModel model) {
        this.gameModel = model;
        repaint();
    }

    private void updateScore(int score) {
        ScoreText.setText(String.valueOf(score));
>>>>>>> 870d0047cfb1dc8314fb9ed388606a46b54b486f
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
<<<<<<< HEAD
        if (gameModel == null) return;
        int pts = gameModel.getScore().getPoints();
        updateScoreDisplay(pts);
=======
        // TODO - GameModel.getScore() 메서드 구현
        // updateScore(gameModel.getScore())
        updateScore(new Random().nextInt(0, 100)); // 임시
>>>>>>> 870d0047cfb1dc8314fb9ed388606a46b54b486f
    }
}
