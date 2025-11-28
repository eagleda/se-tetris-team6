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

import tetris.controller.GameController;
import tetris.controller.GameOverController;
import tetris.controller.ScoreController;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.model.GameState;
import tetris.domain.setting.Setting;
import tetris.multiplayer.session.LocalMultiplayerSession;
import tetris.view.GameComponent.SingleGameLayout;
import tetris.view.GameComponent.GameOverPanel;
import tetris.view.GameComponent.MultiGameLayout;

public class TetrisFrame extends JFrame {
    private static final String FRAME_TITLE = "Tetris Game - Team 06";

    // 프레임 레이아웃
    private JLayeredPane layeredPane;

    // 패널 참조
    // 모든 패널과 모델/컨트롤러를 인스턴스 변수로 변경 (static 제거)
    protected MainPanel mainPanel;
    protected SingleGameLayout singleGameLayout;
    protected MultiGameLayout multiGameLayout;
    protected SettingPanel settingPanel;
    protected ScoreboardPanel scoreboardPanel;
    protected PausePanel pausePanel;
    protected GameOverPanel gameOverPanel;

    private static JPanel prevPanel;
    private static JPanel currPanel;

    private GameModel gameModel;
    private GameController gameController;
    private ScoreController scoreController;
    private GameOverController gameOverController;
    private LeaderboardResult pendingStandardHighlight;
    private LeaderboardResult pendingItemHighlight;
    // 로컬 멀티 세션에 GameModel UiBridge를 붙일 때 재사용하기 위한 상태값.
    // (한 번 연결된 세션을 기억해 중복 바인딩을 피하고, 세션 종료 시 해제한다.)
    private LocalMultiplayerSession boundLocalSession;
    private GameModel.UiBridge localP1UiBridge;
    private GameModel.UiBridge localP2UiBridge;

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
        setupSingleGameLayout();
        setupMultiGameLayout();

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
                    ensureLocalSessionUiBridges();
                    if (singleGameLayout != null)
                        singleGameLayout.repaint();
                    if (multiGameLayout != null)
                        multiGameLayout.repaint();
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
        mainPanel = new MainPanel() {
            @Override
            protected void onSinglePlayConfirmed(String mode) {
                displayPanel(singleGameLayout);
                GameMode selectedMode = TetrisFrame.this.resolveMenuMode(mode);
                gameController.startGame(selectedMode);
                TetrisFrame.this.bindMultiPanelToCurrentSession();
            }

            @Override
            protected void onLocalMultiPlayConfirmed(String mode) {
                GameMode selectedMode = TetrisFrame.this.resolveMenuMode(mode);
                gameController.startLocalMultiplayerGame(selectedMode);
                TetrisFrame.this.bindMultiPanelToCurrentSession();
                displayPanel(multiGameLayout);
            }

            @Override
            protected void onOnlineServerCancelled() {
                showMainPanel();
            }

            @Override
            protected void onOnlineClientCancelled() {
                showMainPanel();
            }

            @Override
            protected String getServerAddress() {
                try {
                    // return gameModel.getNetworkAddress();
                    throw new Exception();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void connectToServer(String address) throws Exception {
                displayPanel(multiGameLayout);
                // gameModel.startOnlineMultiplayerGame(address);
            }

            @Override
            protected void onSettingMenuClicked() {
                displayPanel(settingPanel);
            }

            @Override
            protected void onScoreboardMenuClicked() {
                displayPanel(scoreboardPanel);
            }

            @Override
            protected void onExitMenuClicked() {
                TetrisFrame.this.dispatchEvent(new WindowEvent(TetrisFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        };
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupSingleGameLayout() {
        singleGameLayout = new SingleGameLayout();
        singleGameLayout.setVisible(false);
        singleGameLayout.bindGameModel(gameModel);
        layeredPane.add(singleGameLayout, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupMultiGameLayout() {
        multiGameLayout = new MultiGameLayout();
        multiGameLayout.setVisible(false);
        bindMultiPanelToCurrentSession();
        layeredPane.add(multiGameLayout, JLayeredPane.DEFAULT_LAYER);
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
        pausePanel = new PausePanel() {
            @Override
            protected void onContinueClicked() {
                System.out.println("[UI] PausePanel: Continue clicked");
                gameModel.resumeGame();
            }

            @Override
            protected void onGoMainClicked() {
                System.out.println("[UI] PausePanel: Main clicked");
                pausePanel.setVisible(false);
                // 다음 전환 시 메인 패널로 돌아가도록 이전 패널을 메인으로 지정
                prevPanel = mainPanel;
                gameModel.quitToMenu();
            }

            @Override
            protected void onExitClicked() {
                System.out.println("[UI] PausePanel: Exit clicked");
                TetrisFrame.this.dispatchEvent(new WindowEvent(TetrisFrame.this, WindowEvent.WINDOW_CLOSING));
            }
        };
        layeredPane.add(pausePanel, JLayeredPane.PALETTE_LAYER);
    }

    private void setupScoreboardPanel() {
        scoreboardPanel = new ScoreboardPanel();
        layeredPane.add(scoreboardPanel, JLayeredPane.DEFAULT_LAYER);
        scoreboardPanel.setBackAction(e -> {
            displayPanel(mainPanel);
            gameModel.quitToMenu();
        });
        scoreboardPanel.setResetAction(e -> {
            int result = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "정말로 모든 스코어보드 기록을 초기화하시겠습니까?",
                    "Reset Scores",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            if (result == javax.swing.JOptionPane.YES_OPTION) {
                gameModel.getLeaderboardRepository().reset();
                // 초기화 후 리스트를 비우고 하이라이트도 제거
                scoreboardPanel.renderLeaderboard(GameMode.STANDARD, java.util.Collections.emptyList(), -1);
                scoreboardPanel.renderLeaderboard(GameMode.ITEM, java.util.Collections.emptyList(), -1);
            }
        });
    }

    public void displayPanel(JPanel panel) {
        if (panel == null) {
            System.out.println("[UI][WARN] displayPanel called with null panel — aborting swap");
            return;
        }
        String fromName = currPanel == null ? "null" : resolvePanelName(currPanel);
        String toName = resolvePanelName(panel);
        System.out.printf("[UI] displayPanel: from=%s to=%s%n", fromName, toName);
        if (currPanel != null && currPanel != prevPanel) {
            System.out.printf("[UI] hiding current panel: %s%n", currPanel.getClass().getSimpleName());
            currPanel.setVisible(false);
        }
        prevPanel = currPanel;
        currPanel = panel;
        // If we're about to show the scoreboard, refresh its contents from the
        // leaderboard repo
        if (panel == scoreboardPanel) {
            try {
                System.out.println("[UI] loading scoreboard data (with pending highlight if any)");
                LeaderboardResult std = pendingStandardHighlight;
                LeaderboardResult itm = pendingItemHighlight;
                List<LeaderboardEntry> standard = std != null
                        ? std.entries()
                        : gameModel.loadTopScores(GameMode.STANDARD, 10);
                List<LeaderboardEntry> item = itm != null
                        ? itm.entries()
                        : gameModel.loadTopScores(GameMode.ITEM, 10);
                int stdHighlight = std != null ? std.highlightIndex() : -1;
                int itemHighlight = itm != null ? itm.highlightIndex() : -1;
                System.out.printf("[UI] scoreboard render standard size=%d highlight=%d, item size=%d highlight=%d%n",
                        standard.size(), stdHighlight, item.size(), itemHighlight);
                scoreboardPanel.renderLeaderboard(GameMode.STANDARD, standard, stdHighlight);
                scoreboardPanel.renderLeaderboard(GameMode.ITEM, item, itemHighlight);
                pendingStandardHighlight = null;
                pendingItemHighlight = null;
            } catch (Exception ex) {
                // ignore; show existing data if loading fails
                System.out.printf("[UI][WARN] scoreboard load failed: %s%n", ex.getMessage());
            }
        }
        if (panel == multiGameLayout) {
            bindMultiPanelToCurrentSession();
        }
        // if (prevPanel != null)
        // prevPanel.setVisible(false);
        panel.setVisible(true);
        System.out.printf("[UI] now showing panel: %s%n", toName);
        panel.requestFocusInWindow();
        layeredPane.moveToFront(panel);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    private void showPauseOverlayPanel() {
        displayPanel(pausePanel);
    }

    private void hidePauseOverlayPanel() {
        if (prevPanel != null) {
            displayPanel(prevPanel);
        } else {
            displayPanel(mainPanel);
        }
    }

    /**
    * 익명 서브클래스인 경우에도 의미 있는 이름을 반환한다.
    */
    private String resolvePanelName(JPanel panel) {
        if (panel == null) return "null";
        String name = panel.getClass().getSimpleName();
        if (name == null || name.isBlank()) {
            Class<?> sup = panel.getClass().getSuperclass();
            if (sup != null) {
                name = sup.getSimpleName();
            }
        }
        return name == null || name.isBlank() ? panel.getClass().getName() : name;
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

    /**
     * 이름 입력 직후 새 기록을 강조하기 위해, 스코어보드 전환 전에 하이라이트 정보를 보관한다.
     */
    public void setPendingLeaderboard(GameMode mode, LeaderboardResult result) {
        if (mode == GameMode.ITEM) {
            pendingItemHighlight = result;
        } else {
            pendingStandardHighlight = result;
        }
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
                switch (state) {
                    // case MENU:
                    //     break;
                    case PLAYING:
                        gameModel.pauseGame();
                        break;
                    case PAUSED:
                        gameModel.resumeGame();
                        break;
                    default:
                        displayPanel(prevPanel);
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

    /**
     * 멀티 패널은 싱글/로컬 멀티 전환 시마다 데이터 소스를 재연결해야 한다.
     * 세션이 없으면 기본 GameModel을 공유하고, 있으면 세션 내 플레이어 모델을 그린다.
     */
    private void bindMultiPanelToCurrentSession() {
        if (multiGameLayout == null)
            return;
        ensureLocalSessionUiBridges();
        if (boundLocalSession != null) {
            multiGameLayout.bindLocalMultiplayerSession(boundLocalSession);
        } else {
            multiGameLayout.bindGameModel(gameModel);
        }
    }

    private GameMode resolveMenuMode(String mode) {
        if (mode == null)
            return GameMode.STANDARD;
        if ("ITEM".equalsIgnoreCase(mode)) {
            return GameMode.ITEM;
        }
        if ("TIME_LIMIT".equalsIgnoreCase(mode)) {
            System.out.println("[UI][WARN] Time Limit mode is not implemented; falling back to STANDARD");
        }
        return GameMode.STANDARD;
    }

    /**
     * 멀티 세션이 활성화된 경우 P1/P2 모델에도 UiBridge를 붙여 MultiGameLayout repaint가 가능하게 한다.
     * - 세션이 새로 생기면 bindLocalSessionUiBridges를 호출하고,
     * - 세션이 종료되면 clearLocalSessionUiBridges로 즉시 정리한다.
     */
    private void ensureLocalSessionUiBridges() {
        LocalMultiplayerSession session = gameModel.getActiveLocalMultiplayerSession().orElse(null);
        if (session == null) {
            clearLocalSessionUiBridges();
        } else if (session != boundLocalSession) {
            bindLocalSessionUiBridges(session);
        }
    }

    private void bindLocalSessionUiBridges(LocalMultiplayerSession session) {
        clearLocalSessionUiBridges();
        boundLocalSession = session;
        // 각 플레이어 모델도 도메인 이벤트 → UiBridge → MultiGameLayout.repaint() 경로를 갖도록 한다.
        localP1UiBridge = createLocalUiBridge();
        localP2UiBridge = createLocalUiBridge();
        session.playerOneModel().bindUiBridge(localP1UiBridge);
        session.playerTwoModel().bindUiBridge(localP2UiBridge);
    }

    private void clearLocalSessionUiBridges() {
        if (boundLocalSession != null) {
            // 세션이 종료되면 즉시 브리지를 제거해 다음 세션에서 중복 repaint나 NPE가 발생하지 않도록 한다.
            try {
                boundLocalSession.playerOneModel().clearUiBridge();
            } catch (Exception ignore) {
            }
            try {
                boundLocalSession.playerTwoModel().clearUiBridge();
            } catch (Exception ignore) {
            }
        }
        boundLocalSession = null;
        localP1UiBridge = null;
        localP2UiBridge = null;
    }

    /**
     * 로컬 멀티 전용 UiBridge 구현.
     * - GameModel이 refreshBoard를 호출하면 Swing EDT에서 MultiGameLayout.repaint()만 수행한다.
     * - 나머지 오버레이 관련 메서드는 싱글 주 GameModel이 이미 처리하므로 비워 둔다.
     */
    private GameModel.UiBridge createLocalUiBridge() {
        return new GameModel.UiBridge() {
            private void requestMultiRepaint() {
                if (multiGameLayout == null)
                    return;
                SwingUtilities.invokeLater(() -> multiGameLayout.repaint());
            }

            @Override
            public void showPauseOverlay() {
                // 로컬 멀티 보드는 메인 UI의 일시정지 창을 그대로 사용하므로 별도 처리 없음.
            }

            @Override
            public void hidePauseOverlay() {
                // 상동
            }

            @Override
            public void refreshBoard() {
                requestMultiRepaint();
            }

            @Override
            public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) {
                // 개별 플레이어 오버레이는 메인 GameModel이 총괄하므로 여기서는 단순 무시한다.
            }

            @Override
            public void showNameEntryOverlay(tetris.domain.score.Score score) {
                // 동일하게 로컬 멀티에서는 이름 입력을 통합 UI에서 처리한다.
            }
        };
    }
}
