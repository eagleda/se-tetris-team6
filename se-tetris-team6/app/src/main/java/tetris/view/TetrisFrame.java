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
import tetris.multiplayer.session.NetworkMultiplayerSession;
import tetris.network.client.GameClient;
import tetris.view.GameComponent.SingleGameLayout;
import tetris.view.GameComponent.GameOverPanel;
import tetris.view.GameComponent.MultiGameLayout;
import tetris.network.server.GameServer;

public class TetrisFrame extends JFrame {
    private static final String FRAME_TITLE = "Tetris Game - Team 06";

    // 프레임 레이아웃
    private JLayeredPane layeredPane;

    // 패널 참조
    // 모든 패널과 모델/컨트롤러를 인스턴스 변수로 변경 (static 제거)
    protected MainPanel mainPanel;
    protected SingleGameLayout singleGameLayout;
    protected MultiGameLayout localMultiGameLayout;
    protected tetris.view.GameComponent.NetworkMultiGameLayout onlineMultiGameLayout;
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
    // 온라인(네트워크) 세션용 브리지 상태
    private NetworkMultiplayerSession boundOnlineSession;
    private GameModel.UiBridge onlineP1UiBridge;
    private GameModel.UiBridge onlineP2UiBridge;
    // Optional in-process server when user chooses to host a game
    private GameServer hostedServer;
    // 호스트 대기 다이얼로그 참조 (연결 타임아웃 시 닫기 위해)
    private javax.swing.JDialog hostWaitingDialog;
    // 호스트 게임 대기 중 상태
    private boolean isHostWaitingForGameStart = false;
    // 네트워크 상태 오버레이
    private NetworkStatusOverlay networkStatusOverlay;
    // 네트워크 상태 업데이트 타이머
    private javax.swing.Timer networkUpdateTimer;

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
        setupLocalMultiGameLayout();
        setupOnlineMultiGameLayout();

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
                    if (localMultiGameLayout != null)
                        localMultiGameLayout.repaint();
                    if (onlineMultiGameLayout != null)
                        onlineMultiGameLayout.repaint();
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

            @Override
            public void showMultiplayerResult(int winnerId, int localPlayerId) {
                SwingUtilities.invokeLater(() -> {
                    String message;
                    if (winnerId <= 0) {
                        message = "Match Finished";
                    } else if (winnerId == localPlayerId) {
                        // 온라인 멀티플레이: localPlayerId 기준
                        message = "You Win !";
                    } else {
                        // 온라인 멀티플레이: 상대가 이김
                        message = "You Lose !";
                    }
                    if (gameOverPanel != null) {
                        gameOverPanel.showMultiplayerResult(message, false);
                        layeredPane.moveToFront(gameOverPanel);
                    }
                });
            }

            @Override
            public void showLocalMultiplayerResult(int winnerId) {
                SwingUtilities.invokeLater(() -> {
                    String message;
                    if (winnerId <= 0) {
                        message = "Match Finished";
                    } else {
                        // 로컬 멀티플레이: "Player X Wins" 형식
                        message = "Player " + winnerId + " Wins";
                    }
                    if (gameOverPanel != null) {
                        gameOverPanel.showMultiplayerResult(message, false);
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
    
    /**
     * 네트워크 멀티플레이 세션을 정리하고 연결을 해제합니다.
     * 게임 진행 중이라면 상대방에게 승리 신호를 보내고, 본인은 패배 처리합니다.
     */
    private void cleanupNetworkSession() {
        try {
            // 네트워크 멀티플레이 세션 종료
            if (gameModel.getActiveNetworkMultiplayerSession().isPresent()) {
                System.out.println("[UI] Cleaning up network multiplayer session...");
                NetworkMultiplayerSession session = gameModel.getActiveNetworkMultiplayerSession().get();
                
                // 게임 진행 중이라면 게임 종료 처리
                tetris.domain.GameModel localModel = session.game().modelOf(session.networkController().getLocalPlayerId());
                if (localModel != null && localModel.getCurrentState() == tetris.domain.model.GameState.PLAYING) {
                    System.out.println("[UI] Game is in progress. Player forfeited the game.");
                    
                    // 본인 ID와 상대방 ID 결정
                    int localPlayerId = session.networkController().getLocalPlayerId();
                    int opponentPlayerId = (localPlayerId == 1) ? 2 : 1;
                    
                    // 본인을 패자로, 상대방을 승자로 설정
                    session.game().markLoser(localPlayerId);
                    
                    // 양쪽 모델 모두 게임 오버 상태로 변경
                    tetris.domain.GameModel opponentModel = session.game().modelOf(opponentPlayerId);
                    localModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                    if (opponentModel != null) {
                        opponentModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                    }
                    
                    // 상대방에게 GAME_END 메시지 전송 (상대방이 승리)
                    java.util.Map<String, Object> payload = new java.util.HashMap<>();
                    payload.put("winnerId", opponentPlayerId);
                    payload.put("loserId", localPlayerId);
                    
                    tetris.network.protocol.GameMessage endMessage = new tetris.network.protocol.GameMessage(
                        tetris.network.protocol.MessageType.GAME_END,
                        "Player-" + localPlayerId,
                        payload
                    );
                    
                    // 서버인 경우
                    if (hostedServer != null) {
                        hostedServer.broadcastMessage(endMessage);
                        System.out.println("[UI][SERVER] Sent GAME_END signal (opponent wins) to client");
                    }
                    // 클라이언트인 경우
                    else if (session.networkClient() != null) {
                        session.networkClient().sendMessage(endMessage);
                        System.out.println("[UI][CLIENT] Sent GAME_END signal (opponent wins) to server");
                    }
                    
                    // 본인 화면에 패배 표시
                    gameModel.showMultiplayerResult(opponentPlayerId, localPlayerId);
                    System.out.println("[UI] Showing defeat screen for local player");
                    
                    // 약간의 지연을 주어 메시지가 전송되고 화면이 표시되도록 함
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                // 클라이언트 연결 종료 (DISCONNECT 메시지 전송)
                if (session.networkClient() != null) {
                    System.out.println("[UI][CLIENT] Disconnecting from server...");
                    session.networkClient().disconnect();
                }
                
                session.shutdown();
            }
            
            // 게임 컨트롤러의 네트워크 세션 정리 (중요: 네트워크 클라이언트 참조 해제)
            gameController.cleanupNetworkSession();
            
            // 서버 종료
            if (hostedServer != null) {
                System.out.println("[UI] Stopping hosted server...");
                hostedServer.stopServer();
                hostedServer = null;
            }
            
            // 게임 모델을 메뉴 상태로 전환
            gameModel.quitToMenu();
            
            System.out.println("[UI] Network session cleanup completed.");
        } catch (Exception e) {
            System.err.println("[UI] Error during network cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 네트워크 세션 정리를 위한 public 메서드 (GameOverController에서 호출용)
     */
    public void cleanupNetworkSessionPublic() {
        cleanupNetworkSession();
    }

    /**
     * 상대방 연결 끊김 처리 - 게임 승리 화면 표시
     */
    private void handleOpponentDisconnected(tetris.network.protocol.GameMessage message) {
        String disconnectedId = (String) message.getPayload();
        System.out.println("[TetrisFrame] Opponent " + disconnectedId + " disconnected.");
        
        NetworkMultiplayerSession session = gameModel.getActiveNetworkMultiplayerSession().orElse(null);
        if (session == null) return;
        
        // 상대방 모델 찾기
        int localPlayerId = session.networkController().getLocalPlayerId();
        int opponentId = (localPlayerId == 1) ? 2 : 1;
        
        tetris.domain.GameModel localModel = session.game().modelOf(localPlayerId);
        tetris.domain.GameModel opponentModel = session.game().modelOf(opponentId);
        
        // 이미 게임 종료 상태면 무시
        if (localModel.getCurrentState() == tetris.domain.model.GameState.GAME_OVER) {
            return;
        }
        
        // 상대방을 패자로 표시, 나는 승리
        session.game().markLoser(opponentId);
        
        // 양쪽 모두 게임 종료 상태로 변경
        localModel.changeState(tetris.domain.model.GameState.GAME_OVER);
        opponentModel.changeState(tetris.domain.model.GameState.GAME_OVER);
        
        // 승리 화면 표시 (localPlayerId가 승자)
        gameModel.showMultiplayerResult(localPlayerId, localPlayerId);
        
        // 화면 갱신
        if (onlineMultiGameLayout != null) {
            onlineMultiGameLayout.repaint();
        }
        
        System.out.println("[TetrisFrame] Game ended due to opponent disconnect. You win!");
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
            protected void onMultiPlayConfirmed(String mode, boolean isOnline, boolean isServer) {
                if (isOnline && isServer) {
                    final int port = 5000; // default port
                    try {
                        if (hostedServer == null) hostedServer = new GameServer();

                        // start server in background
                        new Thread(() -> {
                            try { hostedServer.startServer(port); } catch (Exception ex) {
                                System.err.println("[NET] Failed to start server: " + ex.getMessage());
                            }
                        }, "GameServer-Starter").start();

                        // Show a dialog with server IP and port and wait for client
                        java.awt.Window win = SwingUtilities.getWindowAncestor(TetrisFrame.this);
                        final javax.swing.JDialog dlg = new javax.swing.JDialog(win, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
                        hostWaitingDialog = dlg; // 인스턴스 변수에 저장
                        isHostWaitingForGameStart = true; // 대기 상태 설정
                        dlg.setTitle("Hosting: Server Address");
                        javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
                        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(12,12,12,12));
                        String ip = "<unknown>";
                        try { ip = java.net.InetAddress.getLocalHost().getHostAddress(); } catch (Exception ignore) {}
                        // mode selection for host
                        javax.swing.JPanel center = new javax.swing.JPanel();
                        center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));
                        final javax.swing.JLabel info = new javax.swing.JLabel("Server IP: " + ip + "  Port: " + port, javax.swing.SwingConstants.CENTER);
                        center.add(info);
                        center.add(javax.swing.Box.createVerticalStrut(8));
                        javax.swing.JRadioButton rNormal = new javax.swing.JRadioButton("Normal");
                        javax.swing.JRadioButton rItem = new javax.swing.JRadioButton("Item");
                        javax.swing.JRadioButton rTime = new javax.swing.JRadioButton("Time Limit");
                        javax.swing.ButtonGroup bg = new javax.swing.ButtonGroup();
                        bg.add(rNormal); bg.add(rItem); bg.add(rTime);
                        // Set the radio button based on the mode selected in the first dialog
                        if ("ITEM".equalsIgnoreCase(mode)) {
                            rItem.setSelected(true);
                        } else if ("TIME_LIMIT".equalsIgnoreCase(mode)) {
                            rTime.setSelected(true);
                        } else {
                            rNormal.setSelected(true);
                        }
                        javax.swing.JPanel modeRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,8,0));
                        modeRow.add(rNormal); modeRow.add(rItem); modeRow.add(rTime);
                        center.add(modeRow);
                        root.add(center, java.awt.BorderLayout.CENTER);
                        root.add(info, java.awt.BorderLayout.CENTER);

                        javax.swing.JPanel btns = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,8,6));
                        javax.swing.JButton stop = new javax.swing.JButton("Stop Server");
                        javax.swing.JButton start = new javax.swing.JButton("Start Game");
                        start.setEnabled(false);
                        btns.add(start);
                        btns.add(stop);
                        root.add(btns, java.awt.BorderLayout.SOUTH);

                        stop.addActionListener(ae -> {
                            try { hostedServer.stopServer(); } catch (Exception ex) { System.err.println(ex.getMessage()); }
                            isHostWaitingForGameStart = false; // 서버 중지 시 대기 상태 해제
                            hostWaitingDialog = null; // 다이얼로그 참조 해제
                            dlg.dispose();
                            showMainPanel();
                        });

                        // When host presses Start, mark host ready on server
                        start.addActionListener(ae -> {
                            String selected = rItem.isSelected() ? "ITEM" : rTime.isSelected() ? "TIME_LIMIT" : "NORMAL";
                            hostedServer.setSelectedGameMode(selected);
                            hostedServer.setHostReady(true);
                            start.setEnabled(false);
                            info.setText("Host ready — waiting for client...");
                        });

                        // Polling thread: enable Start when client connected, and update when client ready
                        new Thread(() -> {
                            try {
                                // 1) wait until a client connects
                                while (!Thread.currentThread().isInterrupted()) {
                                    int count = hostedServer.getConnectedCount();
                                    if (count >= 1) {
                                        javax.swing.SwingUtilities.invokeLater(() -> {
                                            info.setText("Client connected — press Start when ready. (Clients: " + count + ")");
                                            start.setEnabled(true);
                                        });
                                        break;
                                    }
                                    Thread.sleep(300);
                                }

                                // 2) after host pressed start, wait until server reports started
                                while (!Thread.currentThread().isInterrupted()) {
                                    if (hostedServer.isStarted()) {
                                        // start local multiplayer session on UI thread according to selected mode
                                        String selectedMode = hostedServer.getSelectedGameMode();
                                        javax.swing.SwingUtilities.invokeLater(() -> {
                                            isHostWaitingForGameStart = false; // 게임 시작 시 대기 상태 해제
                                            hostWaitingDialog = null; // 다이얼로그 참조 해제
                                            dlg.dispose();
                                            GameMode gameMode = TetrisFrame.this.resolveMenuMode(selectedMode);
                                            // Start a networked session as host (host is Player-1)
                                            System.out.println("[UI][SERVER] Starting networked multiplayer as Player-1 (Host) with mode: " + gameMode + " (" + selectedMode + ")");
                                            gameController.setNetworkServer(hostedServer); // 서버 연결
                                            NetworkMultiplayerSession session = gameController.startNetworkedMultiplayerGame(gameMode, true);
                                            // 호스트는 서버를 통해 클라이언트 메시지를 받음
                                            TetrisFrame.this.setupHostNetworkListener();
                                            System.out.println("[UI][SERVER] Binding online panel to session");
                                            TetrisFrame.this.bindOnlinePanelToCurrentSession();
                                            System.out.println("[UI][SERVER] Displaying onlineMultiGameLayout");
                                            TetrisFrame.this.displayPanel(onlineMultiGameLayout);
                                            // 네트워크 상태 오버레이 시작
                                            TetrisFrame.this.startNetworkStatusMonitoring();
                                            // IMPORTANT: Server also needs to restart players with correct mode after GAME_START broadcast
                                            System.out.println("[UI][SERVER] Re-starting players with mode: " + gameMode);
                                            session.restartPlayers(gameMode);
                                        });
                                        break;
                                    }
                                    Thread.sleep(200);
                                }
                            } catch (InterruptedException ignored) {}
                        }, "Host-Watcher").start();

                        dlg.getContentPane().add(root);
                        dlg.pack();
                        dlg.setResizable(false);
                        dlg.setLocationRelativeTo(win);
                        dlg.setVisible(true);

                    } catch (Exception ex) {
                        System.err.println("[UI] Could not start hosted server: " + ex.getMessage());
                    }
                } else if (isOnline && !isServer) {
                    // 주소 입력 다이얼로그 띄우기
                    java.awt.Window win = SwingUtilities.getWindowAncestor(TetrisFrame.this);
                    final javax.swing.JDialog dlg = new javax.swing.JDialog(win, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
                    dlg.setTitle("서버 연결");
                    javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
                    root.setBorder(javax.swing.BorderFactory.createEmptyBorder(12,12,12,12));
                    javax.swing.JPanel center = new javax.swing.JPanel();
                    center.setLayout(new javax.swing.BoxLayout(center, javax.swing.BoxLayout.Y_AXIS));
                    final javax.swing.JLabel info = new javax.swing.JLabel("서버 주소를 입력하세요 (예: 127.0.0.1:5000)", javax.swing.SwingConstants.CENTER);
                    center.add(info);
                    center.add(javax.swing.Box.createVerticalStrut(8));
                    java.util.List<String> recentHosts = tetris.network.client.GameClient.getRecentHosts();
                    final javax.swing.JTextField addressField = new javax.swing.JTextField(16);
                    if (!recentHosts.isEmpty()) {
                        addressField.setText(recentHosts.get(0));
                    }
                    center.add(addressField);
                    if (!recentHosts.isEmpty()) {
                        center.add(javax.swing.Box.createVerticalStrut(6));
                        javax.swing.JComboBox<String> recentCombo = new javax.swing.JComboBox<>(recentHosts.toArray(new String[0]));
                        recentCombo.setSelectedIndex(0);
                        recentCombo.addActionListener(ev -> {
                            Object sel = recentCombo.getSelectedItem();
                            if (sel != null) addressField.setText(sel.toString());
                        });
                        javax.swing.JPanel recentRow = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 0));
                        recentRow.add(new javax.swing.JLabel("최근 접속:"));
                        recentRow.add(recentCombo);
                        center.add(recentRow);
                    }
                    final javax.swing.JLabel errorLabel = new javax.swing.JLabel("");
                    errorLabel.setForeground(java.awt.Color.RED);
                    center.add(errorLabel);
                    root.add(center, java.awt.BorderLayout.CENTER);
                    javax.swing.JPanel btns = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,8,6));
                    javax.swing.JButton connectBtn = new javax.swing.JButton("연결");
                    javax.swing.JButton cancelBtn = new javax.swing.JButton("취소");
                    btns.add(connectBtn); btns.add(cancelBtn);
                    root.add(btns, java.awt.BorderLayout.SOUTH);
                    connectBtn.addActionListener(ae -> {
                        String address = addressField.getText().trim();
                        if (address.isEmpty()) {
                            errorLabel.setText("주소를 입력하세요.");
                            return;
                        }
                        try {
                            connectToServer(address);
                            dlg.dispose();
                        } catch (Exception ex) {
                            errorLabel.setText("서버 연결에 실패했습니다: " + ex.getMessage());
                            // 연결 실패 시 확인 버튼을 띄워 메인으로 복귀
                            javax.swing.JOptionPane.showMessageDialog(
                                dlg,
                                "서버 연결에 실패했습니다: " + ex.getMessage(),
                                "연결 오류",
                                javax.swing.JOptionPane.ERROR_MESSAGE
                            );
                            dlg.dispose();
                            showMainPanel();
                        }
                    });
                    cancelBtn.addActionListener(ae -> {
                        dlg.dispose();
                        showMainPanel();
                    });
                    dlg.getContentPane().add(root);
                    dlg.pack();
                    dlg.setResizable(false);
                    dlg.setLocationRelativeTo(win);
                    dlg.setVisible(true);
                } else {
                    // 로컬 멀티플레이 시작
                    onLocalMultiPlayConfirmed(mode);
                }
            }

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
                displayPanel(localMultiGameLayout);
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
                // Attempt to connect to remote host (address may be hostname:port or ip:port)
                String host = address == null ? "127.0.0.1" : address.trim();
                int port = 5000; // default
                if (host.contains(":")) {
                    String[] parts = host.split(":");
                    host = parts[0];
                    try { port = Integer.parseInt(parts[1]); } catch (NumberFormatException ignore) {}
                }

                // Create client and connect with handshake latch
                final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
                final GameClient client = new GameClient();
                boolean ok = client.connectToServer(host, port, latch);
                if (!ok) {
                    javax.swing.JOptionPane.showMessageDialog(this, "서버에 연결할 수 없습니다.", "연결 실패", javax.swing.JOptionPane.ERROR_MESSAGE);
                    showMainPanel();
                    return;
                }

                // Wait briefly for handshake acceptance
                boolean accepted = false;
                try { accepted = latch.await(5, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                if (!accepted) {
                    javax.swing.JOptionPane.showMessageDialog(this, "서버의 응답이 없습니다.", "연결 실패", javax.swing.JOptionPane.ERROR_MESSAGE);
                    client.disconnect();
                    showMainPanel();
                    return;
                }

                // Delegate network message handling to GameController.
                // GameController implements GameStateListener and will be registered
                // by setNetworkClient. This avoids UI-level direct manipulation of
                // opponent models and centralizes input routing and network handling.
                gameController.setNetworkClient(client);
                
                // Show waiting-for-host dialog with Ready button
                java.awt.Window win = SwingUtilities.getWindowAncestor(this);
                final javax.swing.JDialog dlg = new javax.swing.JDialog(win, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
                dlg.setTitle("Connected to Server");
                javax.swing.JPanel root = new javax.swing.JPanel(new java.awt.BorderLayout());
                root.setBorder(javax.swing.BorderFactory.createEmptyBorder(12,12,12,12));
                final javax.swing.JLabel info = new javax.swing.JLabel("Connected to " + host + ":" + port + " — waiting for host to start.", javax.swing.SwingConstants.CENTER);
                root.add(info, java.awt.BorderLayout.CENTER);
                javax.swing.JPanel btns = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER,8,6));
                javax.swing.JButton ready = new javax.swing.JButton("Ready");
                javax.swing.JButton cancel = new javax.swing.JButton("Disconnect");
                btns.add(ready); btns.add(cancel);
                root.add(btns, java.awt.BorderLayout.SOUTH);
                
                // 연결 대기 중 상태 추적
                final boolean[] isWaitingForGameStart = {true};
                
                // Register connection timeout listener
                client.setGameStateListener(new tetris.network.client.GameStateListener() {
                    @Override
                    public void onOpponentBoardUpdate(tetris.network.protocol.GameMessage message) {}
                    
                    @Override
                    public void onGameStateChange(tetris.network.protocol.GameMessage message) {}
                    
                    @Override
                    public void onGameStateSnapshot(tetris.network.protocol.GameSnapshot snapshot) {}
                    
                    @Override
                    public void onConnectionTimeout(String reason) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            // 게임 접속 대기 중인 경우
                            if (isWaitingForGameStart[0]) {
                                // 대기 다이얼로그 닫기
                                dlg.dispose();
                                
                                // 네트워크 세션 정리
                                gameController.cleanupNetworkSession();
                                
                                // 연결 실패 팝업
                                javax.swing.JOptionPane.showMessageDialog(
                                    TetrisFrame.this,
                                    "서버 연결이 끊겼습니다.\n" + reason,
                                    "연결 실패",
                                    javax.swing.JOptionPane.ERROR_MESSAGE
                                );
                                
                                // 메인 화면으로 복귀
                                showMainPanel();
                            } 
                            // 게임 플레이 중인 경우
                            else if (gameModel.getCurrentState() == tetris.domain.model.GameState.PLAYING) {
                                // 게임 종료 상태로 전환
                                gameModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                                
                                // 네트워크 세션 정리
                                gameController.cleanupNetworkSession();
                                
                                // 에러 메시지 팝업
                                javax.swing.JOptionPane.showMessageDialog(
                                    TetrisFrame.this,
                                    "네트워크 연결이 끊겼습니다.\n" + reason,
                                    "연결 오류",
                                    javax.swing.JOptionPane.ERROR_MESSAGE
                                );
                                
                                // 메인 화면으로 돌아가기
                                showMainPanel();
                            }
                        });
                    }
                });

                ready.addActionListener(ae -> {
                    client.sendReady();
                    ready.setEnabled(false);
                    info.setText("Ready sent. Waiting for host to start...");
                });

                cancel.addActionListener(ae -> {
                    client.disconnect();
                    dlg.dispose();
                    showMainPanel();
                });

                // Poll for GAME_START signal from server
                new Thread(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            if (client.isStartReceived()) {
                                String mode = client.getStartMode();
                                javax.swing.SwingUtilities.invokeLater(() -> {
                                    // 게임 시작 - 대기 상태 해제
                                    isWaitingForGameStart[0] = false;
                                    dlg.dispose();
                                    // Start a networked session with provided mode. Determine whether
                                    // this client controls Player-1 based on assigned playerId.
                                    GameMode gameMode = TetrisFrame.this.resolveMenuMode(mode);
                                    boolean localIsPlayerOne = "Player-1".equals(client.getPlayerId());
                                    Long seed = client.getStartSeed();
                                    System.out.println("[UI][CLIENT] Starting networked multiplayer as Player-" + (localIsPlayerOne ? "1" : "2") + " with mode: " + gameMode + " (" + mode + ")");
                                    gameController.setNetworkClient(client); // 네트워크 클라이언트 연결
                                    if (seed != null) {
                                        gameController.startNetworkedMultiplayerGame(gameMode, localIsPlayerOne, seed);
                                    } else {
                                        gameController.startNetworkedMultiplayerGame(gameMode, localIsPlayerOne);
                                    }
                                    System.out.println("[UI][CLIENT] Binding multi panel to session");
                                    bindOnlinePanelToCurrentSession();
                                    System.out.println("[UI][CLIENT] Displaying onlineMultiGameLayout");
                                    displayPanel(onlineMultiGameLayout);
                                    // 네트워크 상태 모니터링 시작
                                    startNetworkStatusMonitoring();
                                });
                                break;
                            }
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException ignored) {}
                }, "Client-Start-Watcher").start();

                dlg.getContentPane().add(root);
                dlg.pack();
                dlg.setResizable(false);
                dlg.setLocationRelativeTo(win);
                dlg.setVisible(true);
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

    private void setupLocalMultiGameLayout() {
        localMultiGameLayout = new MultiGameLayout();
        localMultiGameLayout.setVisible(false);
        bindMultiPanelToCurrentSession();
        layeredPane.add(localMultiGameLayout, JLayeredPane.DEFAULT_LAYER);
    }

    private void setupOnlineMultiGameLayout() {
        onlineMultiGameLayout = new tetris.view.GameComponent.NetworkMultiGameLayout();
        onlineMultiGameLayout.setVisible(false);
        layeredPane.add(onlineMultiGameLayout, JLayeredPane.DEFAULT_LAYER);
        
        // 네트워크 상태 오버레이 추가
        networkStatusOverlay = new NetworkStatusOverlay();
        networkStatusOverlay.setVisible(false);
        networkStatusOverlay.setBounds(0, 0, 800, 50); // 상단에 배치
        layeredPane.add(networkStatusOverlay, JLayeredPane.PALETTE_LAYER); // 게임 화면 위에 표시
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
                // 네트워크 세션 정리 (연결 종료)
                TetrisFrame.this.cleanupNetworkSession();
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

    /**
     * Setup network listener for the host to receive client messages.
     */
    private void setupHostNetworkListener() {
        if (hostedServer == null) return;
        hostedServer.setGameStateListener(new tetris.network.client.GameStateListener() {
            @Override
            public void onOpponentBoardUpdate(tetris.network.protocol.GameMessage message) {
                NetworkMultiplayerSession session = gameModel.getActiveNetworkMultiplayerSession().orElse(null);
                if (session == null) return;
                ensureOnlineSessionUiBridges();
                if (onlineMultiGameLayout != null) onlineMultiGameLayout.repaint();
            }

            @Override
            public void onGameStateSnapshot(tetris.network.protocol.GameSnapshot snapshot) {
                // 호스트는 스냅샷을 수신하지 않음 (자신이 송신함), 하지만 인터페이스 구현 필요
                System.out.println("[TetrisFrame] Host received snapshot (unexpected, ignoring).");
            }

            @Override
            public void onGameStateChange(tetris.network.protocol.GameMessage message) {
                NetworkMultiplayerSession session = gameModel.getActiveNetworkMultiplayerSession().orElse(null);
                if (session == null) return;
                // Host is Player-1, so opponent is Player-2
                tetris.domain.GameModel opponent = session.playerTwoModel();
                switch (message.getType()) {
                    case PLAYER_INPUT:
                        Object payload = message.getPayload();
                        System.out.println("[Host][Listener] Received PLAYER_INPUT from sender=" + message.getSenderId() + ", seq=" + message.getSequenceNumber());
                        if (payload instanceof tetris.network.protocol.PlayerInput pi) {
                            // Log opponent active block before applying input
                            tetris.domain.model.Block before = opponent.getActiveBlock();
                            String beforeInfo = before == null ? "<no-active>" : (before.getKind().name() + "(id=" + (before.getKind().ordinal()+1) + ")@x=" + before.getX() + ",y=" + before.getY() + ",rot=" + before.getRotation());
                            System.out.println("[Host][Listener] Opponent BEFORE input -> " + beforeInfo + " | input=" + pi.inputType());

                            switch (pi.inputType()) {
                                case MOVE_LEFT -> opponent.moveBlockLeft();
                                case MOVE_RIGHT -> opponent.moveBlockRight();
                                case SOFT_DROP -> opponent.moveBlockDown();
                                case ROTATE -> opponent.rotateBlockClockwise();
                                case ROTATE_CCW -> opponent.rotateBlockCounterClockwise();
                                case HARD_DROP -> opponent.hardDropBlock();
                                case HOLD -> opponent.holdCurrentBlock();
                                default -> {}
                            }

                            // Log opponent active block after applying input
                            tetris.domain.model.Block after = opponent.getActiveBlock();
                            String afterInfo = after == null ? "<no-active>" : (after.getKind().name() + "(id=" + (after.getKind().ordinal()+1) + ")@x=" + after.getX() + ",y=" + after.getY() + ",rot=" + after.getRotation());
                            System.out.println("[Host][Listener] Opponent AFTER input  -> " + afterInfo);

                            // Repaint to show opponent's updated state
                            if (onlineMultiGameLayout != null) {
                                onlineMultiGameLayout.repaint();
                            }

                            // Immediately broadcast authoritative snapshots so client sees its own input reflected without waiting for next tick.
                            if (hostedServer != null && session != null) {
                                try {
                                    tetris.domain.GameModel p1Model = session.playerOneModel();
                                    tetris.domain.GameModel p2Model = session.playerTwoModel();
                                    tetris.network.protocol.GameSnapshot s1 = p1Model.toSnapshot(1);
                                    tetris.network.protocol.GameSnapshot s2 = p2Model.toSnapshot(2);
                                    // System.out.println("[Host][Listener] Broadcasting snapshots -> p1(currentId=" + s1.currentBlockId() + ",nextId=" + s1.nextBlockId() + ") p2(currentId=" + s2.currentBlockId() + ",nextId=" + s2.nextBlockId() + ")");
                                    hostedServer.broadcastDualSnapshots(s1, s2);
                                } catch (Exception ex) {
                                    System.err.println("[Host] Failed to broadcast snapshots after remote input: " + ex.getMessage());
                                }
                            }
                        }
                        break;
                    case ATTACK_LINES:
                        Object pl = message.getPayload();
                        if (pl instanceof tetris.network.protocol.AttackLine[] networkLines) {
                            // Apply network attack lines directly to opponent
                            opponent.applyAttackLines(networkLines);
                            
                                    // Repaint to show attack lines
                                    if (onlineMultiGameLayout != null) {
                                        onlineMultiGameLayout.repaint();
                                    }
                        }
                        break;
                    case GAME_END:
                        System.out.println("[UI] Received GAME_END message");
                        NetworkMultiplayerSession sess = gameModel.getActiveNetworkMultiplayerSession().orElse(null);
                        if (sess != null) {
                            // Terminate game for both players
                            tetris.domain.GameModel localModel = sess.playerOneModel();  // Host is always player 1
                            tetris.domain.GameModel opponentModel = sess.playerTwoModel();
                            
                            // Only process if not already in GAME_OVER state (prevent infinite loop)
                            if (localModel.getCurrentState() != tetris.domain.model.GameState.GAME_OVER) {
                                // Get winnerId and loserId from message payload
                                Object payloadObj = message.getPayload();
                                Integer winnerId = null;
                                Integer loserId = null;
                                
                                if (payloadObj instanceof java.util.Map) {
                                    Object winnerIdObj = ((java.util.Map<?, ?>) payloadObj).get("winnerId");
                                    Object loserIdObj = ((java.util.Map<?, ?>) payloadObj).get("loserId");
                                    
                                    if (winnerIdObj instanceof Number) {
                                        winnerId = ((Number) winnerIdObj).intValue();
                                    }
                                    if (loserIdObj instanceof Number) {
                                        loserId = ((Number) loserIdObj).intValue();
                                    }
                                }
                                
                                System.out.println("[UI] GAME_END - winnerId: " + winnerId + ", loserId: " + loserId);
                                
                                // Mark loser based on loserId (preferred) or winnerId
                                if (loserId != null) {
                                    sess.game().markLoser(loserId);
                                } else if (winnerId != null) {
                                    int calculatedLoserId = (winnerId == 1) ? 2 : 1;
                                    sess.game().markLoser(calculatedLoserId);
                                } else {
                                    // Fallback: client (player 2) sent GAME_END, so they lost
                                    sess.game().markLoser(2);
                                }
                                
                                // Change states to GAME_OVER
                                localModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                                opponentModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                                
                                // Show result and update display
                                int localId = sess.networkController().getLocalPlayerId();
                                int finalWinnerId = sess.game().getWinnerId() == null ? -1 : sess.game().getWinnerId();
                                int finalLoserId = sess.game().getLoserId() == null ? -1 : sess.game().getLoserId();
                                
                                gameModel.showMultiplayerResult(finalWinnerId, localId);
                                
                                String result = (finalLoserId == localId) ? "LOSE" : (finalWinnerId == localId) ? "WIN" : "UNKNOWN";
                                System.out.println("[UI] GAME_END processed - Winner: " + finalWinnerId + ", Loser: " + finalLoserId + ", LocalPlayer: " + localId + ", Result: " + result);
                                
                                // Force repaint to show final state
                                if (onlineMultiGameLayout != null) {
                                    onlineMultiGameLayout.repaint();
                                }
                                
                                // 게임 종료 후 약간의 지연 후 네트워크 세션 정리
                                javax.swing.Timer cleanupTimer = new javax.swing.Timer(2000, evt -> {
                                    System.out.println("[UI] Game ended, scheduling network cleanup...");
                                    // 세션 종료는 Back to Menu 버튼으로 사용자가 트리거하도록 함
                                });
                                cleanupTimer.setRepeats(false);
                                cleanupTimer.start();
                            }
                        }
                        break;
                    case OPPONENT_DISCONNECTED:
                        TetrisFrame.this.handleOpponentDisconnected(message);
                        break;
                    default:
                        break;
                }
            }
            
            @Override
            public void onConnectionTimeout(String reason) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    // 대기 중이라면 다이얼로그 닫고 연결 실패 팝업
                    if (isHostWaitingForGameStart && hostWaitingDialog != null) {
                        hostWaitingDialog.dispose();
                        hostWaitingDialog = null;
                        isHostWaitingForGameStart = false;
                        
                        // 서버 종료
                        if (hostedServer != null) {
                            try {
                                hostedServer.stopServer();
                            } catch (Exception e) {
                                System.err.println("Error stopping server: " + e.getMessage());
                            }
                            hostedServer = null;
                        }
                        
                        // 연결 실패 메시지 팝업
                        javax.swing.JOptionPane.showMessageDialog(
                            TetrisFrame.this,
                            "클라이언트 연결이 실패했습니다.\n" + reason,
                            "연결 실패",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                        
                        // 메인 화면으로 돌아가기
                        showMainPanel();
                        return;
                    }
                    
                    // 게임 플레이 중이라면 게임 종료 처리
                    if (gameModel.getCurrentState() == tetris.domain.model.GameState.PLAYING) {
                        gameModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                    }
                    
                    // 네트워크 세션 정리
                    gameController.cleanupNetworkSession();
                    
                    // 서버 종료
                    if (hostedServer != null) {
                        try {
                            hostedServer.stopServer();
                        } catch (Exception e) {
                            System.err.println("Error stopping server: " + e.getMessage());
                        }
                        hostedServer = null;
                    }
                    
                    // 에러 메시지 팝업
                    javax.swing.JOptionPane.showMessageDialog(
                        TetrisFrame.this,
                        "클라이언트 연결이 끊겼습니다.\n" + reason,
                        "연결 오류",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                    
                    // 메인 화면으로 돌아가기
                    showMainPanel();
                });
            }
        });
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
        if (panel == localMultiGameLayout) {
            bindMultiPanelToCurrentSession();
        } else if (panel == onlineMultiGameLayout) {
            bindOnlinePanelToCurrentSession();
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
        stopNetworkStatusMonitoring(); // 네트워크 모니터링 중지
        displayPanel(mainPanel);
    }

    /** Convenience to show the scoreboard panel. */
    public void showScoreboardPanel() {
        displayPanel(scoreboardPanel);
    }
    
    /**
     * 네트워크 상태 모니터링 시작 - 온라인 게임 시작 시 호출
     */
    private void startNetworkStatusMonitoring() {
        if (networkStatusOverlay != null) {
            networkStatusOverlay.setVisible(true);
        }
        
        NetworkMultiplayerSession session = gameController.getNetworkSession();
        
        // 클라이언트인 경우
        if (session != null && session.networkClient() != null) {
            session.networkClient().startPingMeasurement();
        }
        
        // 호스트(서버)인 경우
        if (hostedServer != null) {
            hostedServer.startPingMeasurement();
        }
        
        // 주기적으로 UI 업데이트 (500ms마다)
        if (networkUpdateTimer != null) {
            networkUpdateTimer.stop();
        }
        networkUpdateTimer = new javax.swing.Timer(500, e -> updateNetworkStatus());
        networkUpdateTimer.start();
    }
    
    /**
     * 네트워크 상태 모니터링 중지 - 게임 종료 시 호출
     */
    private void stopNetworkStatusMonitoring() {
        if (networkStatusOverlay != null) {
            networkStatusOverlay.setVisible(false);
        }
        
        if (networkUpdateTimer != null) {
            networkUpdateTimer.stop();
            networkUpdateTimer = null;
        }
        
        NetworkMultiplayerSession session = gameController.getNetworkSession();
        
        // 클라이언트에서 핑 측정 중지
        if (session != null && session.networkClient() != null) {
            session.networkClient().stopPingMeasurement();
        }
        
        // 호스트(서버)에서 핑 측정 중지
        if (hostedServer != null) {
            hostedServer.stopPingMeasurement();
        }
    }
    
    /**
     * 네트워크 상태 UI 업데이트
     */
    private void updateNetworkStatus() {
        if (networkStatusOverlay == null) return;
        
        long ping = -1;
        NetworkMultiplayerSession session = gameController.getNetworkSession();
        
        // 클라이언트인 경우
        if (session != null && session.networkClient() != null) {
            ping = session.networkClient().getCurrentPing();
        }
        // 호스트(서버)인 경우
        else if (hostedServer != null) {
            ping = hostedServer.getCurrentPing();
        }
        
        networkStatusOverlay.updateStatus(ping);
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
                // 네트워크 세션 정리 (연결 종료)
                cleanupNetworkSession();
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
        if (localMultiGameLayout == null)
            return;
        ensureLocalSessionUiBridges();
        if (boundLocalSession != null) {
            System.out.println("[UI] Binding MultiGameLayout to LocalMultiplayerSession");
            localMultiGameLayout.bindLocalMultiplayerSession(boundLocalSession);
        } else {
            System.out.println("[UI][WARN] No active session found, binding single GameModel to both panels");
            localMultiGameLayout.bindGameModel(gameModel);
        }
    }

    /**
     * 온라인 멀티용 패널 바인딩: NetworkMultiGameLayout을 사용하여 네트워크 세션 바인딩.
     */
    private void bindOnlinePanelToCurrentSession() {
        if (onlineMultiGameLayout == null)
            return;
        NetworkMultiplayerSession session = gameModel.getActiveNetworkMultiplayerSession().orElse(null);
        if (session == null) {
            System.out.println("[UI][WARN] No active network session found for online layout; skipping binding");
            return;
        }
        // Ensure UI bridges are attached so GameModel.refreshBoard() calls repaint
        ensureOnlineSessionUiBridges();

        // NetworkMultiGameLayout의 bindOnlineMultiplayerSession 메서드 호출
        onlineMultiGameLayout.bindOnlineMultiplayerSession(session);

        // EDT에서 명시적으로 레이아웃 갱신 보장
        SwingUtilities.invokeLater(() -> {
            onlineMultiGameLayout.revalidate();
            onlineMultiGameLayout.repaint();
        });

        System.out.println("[UI] Bound online multiplayer session to NetworkMultiGameLayout");
    }

    private void ensureOnlineSessionUiBridges() {
        NetworkMultiplayerSession session = gameModel.getActiveNetworkMultiplayerSession().orElse(null);
        System.out.println("[UI] ensureOnlineSessionUiBridges - session=" + (session != null ? "ACTIVE" : "NULL"));
        if (session == null) {
            clearOnlineSessionUiBridges();
        } else if (session != boundOnlineSession) {
            bindOnlineSessionUiBridges(session);
        }
    }

    private void bindOnlineSessionUiBridges(NetworkMultiplayerSession session) {
        clearOnlineSessionUiBridges();
        boundOnlineSession = session;
        onlineP1UiBridge = createOnlineUiBridge();
        onlineP2UiBridge = createOnlineUiBridge();
        try { session.playerOneModel().bindUiBridge(onlineP1UiBridge); } catch (Exception ignore) {}
        try { session.playerTwoModel().bindUiBridge(onlineP2UiBridge); } catch (Exception ignore) {}
    }

    private void clearOnlineSessionUiBridges() {
        if (boundOnlineSession != null) {
            try { boundOnlineSession.playerOneModel().clearUiBridge(); } catch (Exception ignore) {}
            try { boundOnlineSession.playerTwoModel().clearUiBridge(); } catch (Exception ignore) {}
        }
        boundOnlineSession = null;
        onlineP1UiBridge = null;
        onlineP2UiBridge = null;
    }

    private GameModel.UiBridge createOnlineUiBridge() {
        return new GameModel.UiBridge() {
            @Override
            public void showPauseOverlay() { }

            @Override
            public void hidePauseOverlay() { }

            @Override
            public void refreshBoard() {
                if (onlineMultiGameLayout == null) return;
                SwingUtilities.invokeLater(() -> onlineMultiGameLayout.repaint());
            }

            @Override
            public void showGameOverOverlay(tetris.domain.score.Score score, boolean canEnterName) { }

            @Override
            public void showNameEntryOverlay(tetris.domain.score.Score score) { }
        };
    }

    private GameMode resolveMenuMode(String mode) {
        if (mode == null)
            return GameMode.STANDARD;
        if ("ITEM".equalsIgnoreCase(mode)) {
            return GameMode.ITEM;
        }
        if ("TIME_LIMIT".equalsIgnoreCase(mode)) {
            return GameMode.TIME_LIMIT;
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
        System.out.println("[UI] ensureLocalSessionUiBridges - session=" + (session != null ? "ACTIVE" : "NULL"));
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
                if (localMultiGameLayout == null)
                    return;
                SwingUtilities.invokeLater(() -> localMultiGameLayout.repaint());
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

            @Override
            public void showMultiplayerResult(int winnerId, int localPlayerId) {
                // 로컬 뷰는 메인 UI 오버레이를 사용하므로 무시
            }
        };
    }
}
