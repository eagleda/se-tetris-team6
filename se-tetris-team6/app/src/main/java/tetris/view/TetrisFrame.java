package tetris.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import tetris.controller.GameController;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;


public class TetrisFrame extends JFrame {
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

    private static JPanel prevPanel;
    private static JPanel currPanel;

    private final GameModel gameModel;
    private GameController gameController;

    public TetrisFrame() {
        this.setTitle(FRAME_TITLE);
        this.setSize(FRAME_SIZE);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        // layeredPane 설정 (패널을 겹쳐서 배치 가능)
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(FRAME_SIZE);
        this.add(layeredPane);

        this.gameModel = new GameModel();

        // 각 패널 설정
        setupMainPanel();
        setupGamePanel();
        setupSettingPanel();
        setupScoreboardPanel();
        setupPausePanel();

        gameModel.bindUiBridge(new GameModel.UiBridge() {
            @Override
            public void showPauseOverlay() {
                SwingUtilities.invokeLater(() -> showPauseOverlayPanel());
            }

            @Override
            public void hidePauseOverlay() {
                SwingUtilities.invokeLater(() -> hidePauseOverlayPanel());
            }

            @Override
            public void refreshBoard() {
                SwingUtilities.invokeLater(() -> gamePanel.repaint());
            }
        });

        gameController = new GameController(gamePanel, gameModel);

        // 시작 화면 설정
        this.setVisible(true);
        prevPanel = null;
        currPanel = mainPanel;
        displayPanel(mainPanel);

        // 전역 키 바인딩 설정 (포커스와 무관하게 동작)
        installRootKeyBindings();
    }

    private void setupMainPanel() {
        mainPanel = new MainPanel();
        layeredPane.add(mainPanel);

        mainPanel.gameButton.addActionListener(e -> {
            displayPanel(gamePanel);
             // 2. Controller에게 게임 시작을 명령
            gameController.startGame();
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
        gamePanel.bindGameModel(gameModel);
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupSettingPanel() {
        settingPanel = new SettingPanel();
        layeredPane.add(settingPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupPausePanel() {
        pausePanel = new PausePanel();
        pausePanel.setBounds(0, 0, FRAME_SIZE.width, FRAME_SIZE.height);
        layeredPane.add(pausePanel, JLayeredPane.PALETTE_LAYER);
        pausePanel.setVisible(false);

        // 버튼 기능 추가
        pausePanel.continueButton.addActionListener(e -> {
            gameModel.resumeGame();
        });
        pausePanel.goMainButton.addActionListener(e -> {
            displayPanel(mainPanel);
            gameModel.quitToMenu();
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
        prevPanel = currPanel;
        currPanel = panel;
        if (prevPanel != null) {
            prevPanel.setVisible(false);
        }
        panel.setVisible(true);
        panel.requestFocusInWindow();
        layeredPane.moveToFront(panel);
        layeredPane.repaint();
    }

    private void showPauseOverlayPanel() {
        pausePanel.setVisible(true);
        layeredPane.moveToFront(pausePanel);
        pausePanel.requestFocusInWindow();
        layeredPane.repaint();
    }

    private void hidePauseOverlayPanel() {
        pausePanel.setVisible(false);
        if (currPanel != null) {
            layeredPane.moveToFront(currPanel);
            currPanel.requestFocusInWindow();
        }
        layeredPane.repaint();
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
                GameState state = gameModel.getCurrentState();
                if (state == null) {
                    return;
                }
                if (state == GameState.PLAYING) {
                    gameModel.pauseGame();
                } else if (state == GameState.PAUSED) {
                    gameModel.resumeGame();
                }
            }
        });

        // MainPanel 복귀
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "goMainPanel");
        am.put("goMainPanel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayPanel(mainPanel);
                gameModel.quitToMenu();
            }
        });
    }

}
