package tetris.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class TetrisFrame extends JFrame implements KeyListener {
    private static final String FRAME_TITLE = "Tetris Game - Team 06";
    protected static final Dimension FRAME_SIZE = new Dimension(700, 900);

    // 프레임 레이아웃
    private final JLayeredPane layeredPane;

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

        // layeredPane 설정 (겹쳐서 배치 가능)
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

        // 전역 키 바인딩 설정 (포커스와 무관하게 동작)
        installRootKeyBindings();
    }

    private void setupMainPanel() {
        mainPanel = new MainPanel();
        layeredPane.add(mainPanel);

        mainPanel.gameButton.addActionListener(e -> {
            hidePanel(mainPanel);
            displayPanel(gamePanel);
        });
        mainPanel.settingButton.addActionListener(e -> {
            hidePanel(mainPanel);
            displayPanel(settingPanel);
        });
        mainPanel.scoreboardButton.addActionListener(e -> {
            hidePanel(mainPanel);
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
            hidePanel(pausePanel);
        });
        pausePanel.goMainButton.addActionListener(e -> {
            hidePanel(pausePanel);
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

    private void displayPanel(JPanel panel) {
        panel.setVisible(true);
        layeredPane.moveToFront(panel);
        panel.setFocusable(true);
        panel.requestFocusInWindow();
        layeredPane.repaint();
    }

    private void hidePanel(JPanel panel) {
        panel.setVisible(false);
        panel.setFocusable(false);
        layeredPane.repaint();
        this.requestFocusInWindow();
    }

    // 전역 키 설정
    private void installRootKeyBindings() {
        InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getRootPane().getActionMap();

        // PausePanel 토글
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "togglePausePanel");
        am.put("togglePausePanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!pausePanel.isVisible())
                    displayPanel(pausePanel);
                else
                    hidePanel(pausePanel);
            }
        });

        // MainPanel 복귀
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "goMainPanel");
        am.put("goMainPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayPanel(mainPanel);
                hidePanel(pausePanel);
            }
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
