package tetris.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.checkerframework.checker.nullness.qual.NonNull;

import tetris.controller.GameController;
import tetris.controller.GameOverController;
import tetris.controller.ScoreController;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.model.GameState;
import tetris.domain.setting.Setting;
import tetris.view.GameComponent.GameLayout;
import tetris.view.GameComponent.GameOverPanel;

public class TetrisFrame extends JFrame {
    private static final String FRAME_TITLE = "Tetris Game - Team 06";

    // 프레임 레이아웃
    private JLayeredPane layeredPane;

    // 패널 참조
    // 모든 패널과 모델/컨트롤러를 인스턴스 변수로 변경 (static 제거)
    protected MainPanel mainPanel;
    protected GameLayout gameLayout;
    protected SettingPanel settingPanel;
    protected ScoreboardPanel scoreboardPanel;
    protected PausePanel pausePanel;
    protected GameOverPanel gameOverPanel;
    private GameOverController gameOverController;
    private static JPanel prevPanel;
    private static JPanel currPanel;

    private GameModel gameModel;
    private GameController gameController;
    private ScoreController scoreController;

    public TetrisFrame(GameModel gameModel) {
        super(FRAME_TITLE);
        this.gameModel = Objects.requireNonNull(gameModel, "gameModel");
        initializeControllers();
        initializeFrame();
        // 전역 키 바인딩 설정 (포커스와 무관하게 동작)
        installRootKeyBindings();

        // 각 패널 설정
        setupMainPanel();
        setupSettingPanel();
        setupScoreboardPanel();
        setupPausePanel();
        setupGameOverPanel();
        setupGameLayout();

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
                SwingUtilities.invokeLater(() -> {
                    if (gameLayout != null)
                        gameLayout.repaint();
                });
            }

            @Override
            public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) {
                SwingUtilities.invokeLater(() -> {
                    if (gameOverController != null) {
                        gameOverController.show(score, canEnterName);
                        layeredPane.moveToFront(gameOverPanel);
                    }
                });
            }

            @Override
            public void showNameEntryOverlay(tetris.domain.score.Score score) {
                SwingUtilities.invokeLater(() -> {
                    if (gameOverController != null) {
                        gameOverController.show(score, true);
                        layeredPane.moveToFront(gameOverPanel);
                    }
                });
            }
        });

        scoreController = new ScoreController(
                gameModel.getScoreRepository(),
                gameModel.getScoreEngine(),
                scoreboardPanel);

        // 시작 화면 설정
        this.setVisible(true);
        displayPanel(mainPanel);
        this.setVisible(true);

        // 화면 크기 설정
        applyScreenSize(Setting.ScreenSize.MEDIUM);
    }

    private void setupGameOverPanel() {
        gameOverPanel = new GameOverPanel();
        layeredPane.add(gameOverPanel, JLayeredPane.PALETTE_LAYER);
        // create controller
        gameOverController = new GameOverController(
                gameModel.getScoreRepository(),
                gameModel.getLeaderboardRepository(),
                gameOverPanel,
                this);
    }

    private void initializeControllers() {
        gameController = new GameController(gameModel);
        scoreController = null;
    }

    private void initializeFrame() {
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        this.add(layeredPane);
    }

    private void setupMainPanel() {
        mainPanel = new MainPanel();
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        mainPanel.gameButton.addActionListener(e -> {
            displayPanel(gameLayout);
            gameController.startStandardGame();
        });
        mainPanel.itemGameButton.addActionListener(e -> {
            displayPanel(gameLayout);
            gameController.startItemGame();
        });
        mainPanel.settingButton.addActionListener(e -> {
            displayPanel(settingPanel);
        });
        mainPanel.scoreboardButton.addActionListener(e -> {
            // ensure leaderboard is refreshed before showing
            showScoreboardPanel();
        });
    }

    private void setupGameLayout() {
        gameLayout = new GameLayout();
        gameLayout.setVisible(false);
        gameLayout.bindGameModel(gameModel);
        layeredPane.add(gameLayout, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupSettingPanel() {
        settingPanel = new SettingPanel();
        layeredPane.add(settingPanel, JLayeredPane.DEFAULT_LAYER);
        // create and bind setting controller so settings persist and apply at runtime
        new tetris.controller.SettingController(
                gameModel.getScoreRepository(),
                settingPanel,
                gameController,
                this);
    }

    private void setupPausePanel() {
        pausePanel = new PausePanel();
        layeredPane.add(pausePanel, JLayeredPane.PALETTE_LAYER);

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
        pausePanel.exitButton
                .addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    private void setupScoreboardPanel() {
        scoreboardPanel = new ScoreboardPanel();
        layeredPane.add(scoreboardPanel, JLayeredPane.DEFAULT_LAYER);
        scoreboardPanel.setBackAction(e -> {
            displayPanel(mainPanel);
            gameModel.quitToMenu();
        });
    }

    public void displayPanel(JPanel panel) {
        if (currPanel != null && currPanel != prevPanel) {
            currPanel.setVisible(false);
        }
        prevPanel = currPanel;
        currPanel = panel;
        // If we're about to show the scoreboard, refresh its contents from the
        // leaderboard repo
        if (panel == scoreboardPanel) {
            try {
                List<LeaderboardEntry> standard = gameModel.loadTopScores(GameMode.STANDARD, 10);
                List<LeaderboardEntry> item = gameModel.loadTopScores(GameMode.ITEM, 10);
                scoreboardPanel.renderLeaderboard(GameMode.STANDARD, standard);
                scoreboardPanel.renderLeaderboard(GameMode.ITEM, item);
            } catch (Exception ex) {
                // ignore; show existing data if loading fails
            }
        }
        // if (prevPanel != null)
        // prevPanel.setVisible(false);
        panel.setVisible(true);
        panel.requestFocusInWindow();
        layeredPane.moveToFront(panel);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private void showPauseOverlayPanel() {
        displayPanel(pausePanel);
    }

    private void hidePauseOverlayPanel() {
        displayPanel(prevPanel);
    }

    // showPauseOverlayPanel, hidePauseOverlayPanel 대체 가능
    public void togglePausePanel() {
        if (!pausePanel.equals(currPanel)) {
            displayPanel(pausePanel);
        } else {
            displayPanel(prevPanel);
        }
    }

    /**
     * Convenience to show the main menu panel.
     */
    public void showMainPanel() {
        displayPanel(mainPanel);
    }

    /** Convenience to show the scoreboard panel. */
    public void showScoreboardPanel() {
        displayPanel(scoreboardPanel);
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

        // 방향키로 버튼 이동 및 엔터로 클릭
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "moveUpButton");
        am.put("moveUpButton", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainPanel.equals(currPanel))
                    mainPanel.focusButton(-1);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveDownButton");
        am.put("moveDownButton", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainPanel.equals(currPanel))
                    mainPanel.focusButton(1);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "clickFocusButton");
        am.put("clickFocusButton", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainPanel.equals(currPanel))
                    mainPanel.clickFocusButton();
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(event -> {
                    if (gameController == null) {
                        return false;
                    }
                    if (event.getID() == KeyEvent.KEY_PRESSED) {
                        gameController.handleKeyPress(event.getKeyCode());
                    } else if (event.getID() == KeyEvent.KEY_RELEASED) {
                        gameController.handleKeyRelease(event.getKeyCode());
                    }
                    return false;
                });
    }

    /**
     * Apply a screen size selection to the frame and contained layered pane/panels.
     * Adjusts frame size and layer preferred sizes so the UI reflects the
     * selection.
     */
    public void applyScreenSize(tetris.domain.setting.Setting.ScreenSize size) {
        if (size == null)
            return;
        changeResolution(size.getDimension());
    }

    // 화면 크기 변경 메서드 추가
    public void changeResolution(Dimension size) {
        // 프레임 크기 변경
        this.setSize(size);

        // layeredPane 크기 조정
        layeredPane.setPreferredSize(size);
        layeredPane.setBounds(0, 0, size.width, size.height);

        // 모든 자식 패널 크기 조정
        for (Component component : layeredPane.getComponents()) {
            if (component instanceof JPanel panel) {
                panel.setPreferredSize(size);
                panel.setBounds(0, 0, size.width, size.height);
                panel.revalidate();
            }
        }

        // 프레임을 화면 중앙으로 재배치
        this.setLocationRelativeTo(null);

        // 레이아웃 갱신
        this.revalidate();
        this.repaint();
    }

    public GameModel getGameModel() {
        return gameModel;
    }
}
