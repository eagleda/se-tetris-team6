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

import tetris.controller.GameController;
import tetris.domain.GameModel;


public class TetrisFrame extends JFrame {

    private GameModel gameModel;

    private static final String FRAME_TITLE = "Tetris Game - Team 06";
    protected static final Dimension FRAME_SIZE = new Dimension(700, 900);

    // 프레임 레이아웃
    private JLayeredPane layeredPane;

    // 패널 참조
    //모든 패널과 모델/컨트롤러를 인스턴스 변수로 변경 (static 제거)
    protected MainPanel mainPanel;
    protected GamePanel gamePanel;
    protected SettingPanel settingPanel;
    protected ScoreboardPanel scoreboardPanel;
    protected PausePanel pausePanel;

    private static JPanel currPanel;

      // <<< 1. Controller 참조 변수 추가
    private GameController gameController;


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

        // 시작 화면을 MainPanel로 설정
        displayPanel(mainPanel);
        this.setVisible(true);

    }
    
    private void initializeModelsAndControllers() {
        gameModel = new GameModel();
        gameController = new GameController(gameModel);
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
        gamePanel = new GamePanel(gameModel, gameController);
        gamePanel.setVisible(false);
        gameModel.addPropertyChangeListener(gamePanel); // Model과 View 연결
        layeredPane.add(gamePanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupSettingPanel() {
        settingPanel = new SettingPanel();
        settingPanel.setVisible(false);
        layeredPane.add(settingPanel, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupPausePanel() {
        pausePanel = new PausePanel();
        pausePanel.setVisible(false);
        // [개선 3] PausePanel은 다른 패널 위에 겹쳐야 하므로 더 높은 레이어에 추가
        layeredPane.add(pausePanel, JLayeredPane.PALETTE_LAYER);

        pausePanel.continueButton.addActionListener(e -> togglePause());
        pausePanel.goMainButton.addActionListener(e -> {
            togglePause(); // Pause 패널을 먼저 닫고
            displayPanel(mainPanel); // Main으로 이동
        });
        pausePanel.exitButton.addActionListener(e -> this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
    }

    private void setupScoreboardPanel() {
        scoreboardPanel = new ScoreboardPanel();
        scoreboardPanel.setVisible(false);
        layeredPane.add(scoreboardPanel, JLayeredPane.DEFAULT_LAYER);
    }

    // [개선 3] 더 안정적이고 명확한 패널 전환 메소드
    private void displayPanel(JPanel panelToShow) {
        if (currentPanel != null) {
            currentPanel.setVisible(false);
        }
        panelToShow.setVisible(true);
        panelToShow.requestFocusInWindow();
        layeredPane.moveToFront(panelToShow);
        currentPanel = panelToShow;
    }

    // [개선 4] Pause 전용 토글 메소드
    private void togglePause() {
        // 게임이 실행 중일 때만 Pause가 동작하도록 방어 코드 추가
        if (currentPanel == gamePanel || pausePanel.isVisible()) {
            boolean isPaused = !pausePanel.isVisible();
            pausePanel.setVisible(isPaused);
            
            if (isPaused) {
                gameController.pauseGame();
                layeredPane.moveToFront(pausePanel); // Pause 패널을 최상단으로
            } else {
                gameController.resumeGame();
                gamePanel.requestFocusInWindow(); // 다시 게임 패널에 포커스
            }
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
                if (!pausePanel.isVisible()) {
                    displayPanel(pausePanel);
                } else {
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
            }
        });
    }

}
