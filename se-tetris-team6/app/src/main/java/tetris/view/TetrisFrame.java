package tetris.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class TetrisFrame extends JFrame implements KeyListener {
    private static final String FRAME_TITLE = "Tetris Game - Team 06";
    protected static final Dimension FRAME_SIZE = new Dimension(700, 900);

    // 프레임 레이아웃
    private JLayeredPane layeredPane;

    // 패널 참조
    protected static MainPanel mainPanel;
    protected static GamePanel gamePanel;
    protected static SettingPanel settingPanel;
    protected static ScoreboardPanel scoreboardPanel;
    protected static PausePanel pausePanel;

    public TetrisFrame() {
        this.setTitle(FRAME_TITLE);
        this.setSize(FRAME_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // layeredPane 설정
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(FRAME_SIZE);
        this.add(layeredPane);

        // 각 패널 설정
        setupMainPanel();
        setupGamePanel();
        setupSettingPanel();
        setupScoreboardPanel();
        setupPausePanel();

        // 시작 화면 설정
        this.setVisible(true);
        displayPanel(mainPanel);

        // 키 리스너 설정
        this.addKeyListener((KeyListener) this);
    }

    private void setupMainPanel() {
        mainPanel = new MainPanel();
        layeredPane.add(mainPanel);
        mainPanel.addButton("Game Start", Color.gray, e -> {
            displayPanel(gamePanel);
            hidePanel(mainPanel);
            hidePanel(pausePanel);
        });
        mainPanel.addButton("Setting", Color.gray, e -> {
            displayPanel(settingPanel);
            hidePanel(mainPanel);
            hidePanel(pausePanel);
        });
        mainPanel.addButton("Scoreboard", Color.gray, e -> {
            displayPanel(scoreboardPanel);
            hidePanel(mainPanel);
            hidePanel(pausePanel);
        });
        mainPanel.layoutButtons();
    }

    private void setupGamePanel() {
        gamePanel = new GamePanel();
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupSettingPanel() {
        settingPanel = new SettingPanel();
        layeredPane.add(settingPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupPausePanel() {
        pausePanel = new PausePanel();
        layeredPane.add(pausePanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupScoreboardPanel() {
        scoreboardPanel = new ScoreboardPanel();
        layeredPane.add(scoreboardPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void displayPanel(JPanel panel) {
        panel.setVisible(true);
        layeredPane.moveToFront(panel);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        layeredPane.repaint();
        this.requestFocusInWindow();
    }

    private void hidePanel(JPanel panel) {
        panel.setVisible(false);
        panel.setFocusable(false);
        layeredPane.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> { // esc키 -> 중단 화면
                if (!pausePanel.isVisible()) {
                    displayPanel(pausePanel);
                } else {
                    hidePanel(pausePanel);
                }
            }
            case KeyEvent.VK_HOME -> { // home키 -> 메인 화면
                displayPanel(mainPanel);
                hidePanel(pausePanel);
            }
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // 필요시 구현
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // 필요시 구현
    }
}
