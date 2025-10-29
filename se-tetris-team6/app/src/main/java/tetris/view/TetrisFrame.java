package tetris.view;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
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
import tetris.controller.ScoreController;
import tetris.domain.GameModel;
import tetris.domain.model.GameState;

public class TetrisFrame extends JFrame {

    private GameModel gameModel;

    private static final String FRAME_TITLE = "Tetris Game - Team 06";
    protected static final Dimension FRAME_SIZE = new Dimension(700, 900);

    // 프레임 레이아웃
    private JLayeredPane layeredPane;

    // 패널 참조
    // 모든 패널과 모델/컨트롤러를 인스턴스 변수로 변경 (static 제거)
    protected MainPanel mainPanel;
    protected GamePanel gamePanel;
    protected SettingPanel settingPanel;
    protected ScoreboardPanel scoreboardPanel;
    protected PausePanel pausePanel;
    private static JPanel prevPanel;
    private static JPanel currPanel;

    private GameController gameController;
    private ScoreController scoreController;

    public TetrisFrame() {
        super(FRAME_TITLE);
        initializeModelsAndControllers();
        initializeFrame();
        // 전역 키 바인딩 설정 (포커스와 무관하게 동작)
        installRootKeyBindings();

        // 각 패널 설정
        setupMainPanel();
        setupSettingPanel();
        setupScoreboardPanel();
        setupPausePanel();
        setupGamePanel();

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

        scoreController = new ScoreController(
            gameModel.getScoreRepository(),
            gameModel.getScoreEngine(),
            scoreboardPanel
        );

        // 시작 화면 설정
        this.setVisible(true);
        prevPanel = null;
        currPanel = mainPanel;
        displayPanel(mainPanel);
        this.setVisible(true);

    }

    private void initializeModelsAndControllers() {
        gameModel = new GameModel();
        gameController = new GameController(gameModel);
        scoreController = null;
    }

    private void initializeFrame() {
        setSize(FRAME_SIZE);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(FRAME_SIZE);
        this.add(layeredPane);
    }

    private void setupMainPanel() {
        mainPanel = new MainPanel();
        mainPanel.setVisible(false); // 초기에 모든 패널은 보이지 않도록 설정
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

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
        settingPanel.setVisible(false);
        layeredPane.add(settingPanel, JLayeredPane.DEFAULT_LAYER);
        // create and bind setting controller so settings persist and apply at runtime
        new tetris.controller.SettingController(
            gameModel.getScoreRepository(),
            settingPanel,
            gameController,
            this
        );
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
        pausePanel.exitButton
                .addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    private void setupScoreboardPanel() {
        scoreboardPanel = new ScoreboardPanel();
        scoreboardPanel.setVisible(false);
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
     * Adjusts frame size and layer preferred sizes so the UI reflects the selection.
     */
    public void applyScreenSize(tetris.domain.setting.Setting.ScreenSize size) {
        if (size == null) return;
        switch (size) {
            case SMALL:
                setSize(new Dimension(560, 720));
                layeredPane.setPreferredSize(new Dimension(560, 720));
                break;
            case MEDIUM:
                setSize(new Dimension(700, 900));
                layeredPane.setPreferredSize(new Dimension(700, 900));
                break;
            case LARGE:
                setSize(new Dimension(900, 1200));
                layeredPane.setPreferredSize(new Dimension(900, 1200));
                break;
            default:
                break;
        }
        // reposition and resize existing panels
        layeredPane.setSize(getSize());
        layeredPane.revalidate();
        layeredPane.repaint();
    }
}
