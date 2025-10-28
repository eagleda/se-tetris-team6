package tetris.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import tetris.view.SettingComponent.ResolutionPanel;

public class TetrisFrame extends JFrame {
    public static TetrisFrame instance;

    public static final String FRAME_TITLE = "Tetris Game - Team 06";
    public static Dimension FRAME_SIZE = ResolutionPanel.RESOLUTIONS[0];

    // 프레임 레이아웃
    private final JLayeredPane layeredPane;

    // 패널 참조
    public static MainPanel mainPanel;
    public static GamePanel gamePanel;
    public static SettingPanel settingPanel;
    public static ScoreboardPanel scoreboardPanel;
    public static PausePanel pausePanel;

    private static JPanel prevPanel;
    private static JPanel currPanel;

    public TetrisFrame() {
        instance = this;

        this.setTitle(FRAME_TITLE);
        this.setSize(FRAME_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // layeredPane 설정 (패널을 겹쳐서 배치 가능)
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
        prevPanel = null;
        currPanel = mainPanel;
        displayPanel(mainPanel);
    }

    private void setupMainPanel() {
        mainPanel = new MainPanel();
        layeredPane.add(mainPanel);

        mainPanel.gameButton.addActionListener(e -> {
            displayPanel(gamePanel);
        });
        mainPanel.settingButton.addActionListener(e -> {
            displayPanel(settingPanel);
        });
        mainPanel.scoreboardButton.addActionListener(e -> {
            displayPanel(scoreboardPanel);
        });
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

        // 버튼 기능 추가
        pausePanel.continueButton.addActionListener(e -> {
            displayPanel(prevPanel);
        });
        pausePanel.goMainButton.addActionListener(e -> {
            displayPanel(mainPanel);
        });
        pausePanel.exitButton.addActionListener(e -> {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
    }

    private void setupScoreboardPanel() {
        scoreboardPanel = new ScoreboardPanel();
        layeredPane.add(scoreboardPanel, JLayeredPane.DEFAULT_LAYER);
    }

    public void displayPanel(JPanel panel) {
        prevPanel = currPanel;
        currPanel = panel;
        prevPanel.setVisible(false);
        panel.setVisible(true);
        panel.requestFocusInWindow();
        layeredPane.moveToFront(panel);
        layeredPane.repaint();
    }

    public void togglePausePanel() {
        if (!pausePanel.isVisible()) {
            displayPanel(pausePanel);
        } else {
            displayPanel(prevPanel);
        }
    }

    public void changeResolution(Dimension newSize) {
        // 1. FRAME_SIZE 전역 변수 업데이트
        FRAME_SIZE = newSize;

        // 2. 프레임 크기 변경
        this.setSize(newSize);

        // 3. layeredPane 크기 조정
        layeredPane.setPreferredSize(newSize);
        layeredPane.setBounds(0, 0, newSize.width, newSize.height);

        // 4. 모든 자식 패널 크기 조정
        for (Component component : layeredPane.getComponents()) {
            if (component instanceof JPanel panel) {
                panel.setSize(newSize);
                panel.setBounds(0, 0, newSize.width, newSize.height);
                panel.revalidate();
            }
        }

        // 5. 프레임을 화면 중앙으로 재배치
        this.setLocationRelativeTo(null);

        // 6. 레이아웃 갱신
        this.revalidate();
        this.repaint();
    }
}
