package tetris.view.GameComponent;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.domain.GameModel;
import tetris.multiplayer.session.NetworkMultiplayerSession;
import tetris.multiplayer.handler.MultiplayerHandler;

/**
 * 네트워크 멀티플레이어 게임 화면 레이아웃 (온라인 P2P 전용)
 * 
 * 구조:
 * - 호스트(방장): 게임 로직 처리 + 상태 브로드캐스트
 * - 클라이언트(손님): 키입력 전송 + 받은 상태로 화면 업데이트만
 * 
 * 호스트가 주기적으로 스냅샷을 브로드캐스트하면 클라이언트는 그 상태로만 렌더링합니다.
 */
public class NetworkMultiGameLayout extends JPanel {
    // 플레이어 1 (자신)
    private GamePanel gamePanel_1;
    private NextBlockPanel nextBlockPanel_1;
    private ScorePanel scoreboard_1;
    private AttackQueuePanel attackQueuePanel_1;

    // 플레이어 2 (상대)
    private GamePanel gamePanel_2;
    private NextBlockPanel nextBlockPanel_2;
    private ScorePanel scoreboard_2;
    private AttackQueuePanel attackQueuePanel_2;

    // 중앙 Timer
    private TimerPanel timerPanel;
    
    // 주기적인 화면 갱신을 위한 타이머
    private Timer repaintTimer;

    public NetworkMultiGameLayout() {
        super(new GridBagLayout());
        setOpaque(true);
        setVisible(true);
        
        // 주기적인 repaint 타이머 시작 (30fps)
        repaintTimer = new Timer(33, e -> {
            if (isVisible()) {
                repaint();
            }
        });
        repaintTimer.start();

        // 각 요소 객체 생성 및 배치
        gamePanel_1 = new GamePanel();
        addToRegion(gamePanel_1, 0, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        nextBlockPanel_1 = new NextBlockPanel();
        addToRegion(nextBlockPanel_1, 6, 0, 2, 3, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        scoreboard_1 = new ScorePanel();
        addToRegion(scoreboard_1, 6, 3, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        attackQueuePanel_1 = new AttackQueuePanel();
        addToRegion(attackQueuePanel_1, 6, 5, 2, 6, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

        gamePanel_2 = new GamePanel();
        addToRegion(gamePanel_2, 11, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        nextBlockPanel_2 = new NextBlockPanel();
        addToRegion(nextBlockPanel_2, 8, 0, 2, 3, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        scoreboard_2 = new ScorePanel();
        addToRegion(scoreboard_2, 8, 3, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        attackQueuePanel_2 = new AttackQueuePanel();
        addToRegion(attackQueuePanel_2, 8, 5, 2, 6, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

        timerPanel = new TimerPanel();
        addToRegion(timerPanel, 6, 4, 4, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);

        revalidate();
        repaint();
    }

    /**
     * Show the central timer panel.
     */
    public void showTimer() {
        setTimerVisible(true);
    }

    /**
     * Hide the central timer panel.
     */
    public void hideTimer() {
        setTimerVisible(false);
    }

    /**
     * Set visibility of the central timer panel.
     */
    public void setTimerVisible(boolean visible) {
        if (timerPanel == null)
            return;
        timerPanel.setVisible(visible);
        revalidate();
        repaint();
    }

    /**
     * 네트워크 멀티플레이어 세션을 바인딩합니다.
     * 
     * 화면 구성:
     * - 호스트(P1): 좌측에 자신(P1), 우측에 클라이언트(P2 - 빈 화면 또는 최소 정보)
     * - 클라이언트(P2): 좌측에 호스트(P1 - 네트워크로 받은 상태), 우측에 자신(P2)
     * 
     * 네트워크 동기화:
     * - 호스트는 자신(P1)의 게임 상태를 매 틱마다 브로드캐스트
     * - 클라이언트는 받은 호스트 상태를 P1 화면에 표시
     */
    public void bindOnlineMultiplayerSession(NetworkMultiplayerSession session) {
        System.out.println("[NetworkMultiGameLayout] bindOnlineMultiplayerSession called - session=" + (session != null ? "ACTIVE" : "NULL"));
        if (session == null) {
            return;
        }
        
        // Determine left/right by asking the session's handler for which player is local.
        MultiplayerHandler handler = session.handler();
        int localPlayerId = 0;
        if (handler instanceof tetris.multiplayer.handler.NetworkedMultiplayerHandler) {
            localPlayerId = ((tetris.multiplayer.handler.NetworkedMultiplayerHandler) handler).getLocalPlayerId();
        }
<<<<<<< HEAD
        
        System.out.println("[NetworkMultiGameLayout] LocalPlayerId=" + localPlayerId);
        
        // 🔧 FIX: 로컬 플레이어는 항상 왼쪽, 원격 플레이어는 항상 오른쪽에 표시
        // localPlayerId에 따라 모델 바인딩도 함께 교체
        GameModel localModel;
        GameModel remoteModel;
        
        if (localPlayerId == 1) {
            localModel = session.playerOneModel();   // P1 = 로컬
            remoteModel = session.playerTwoModel();  // P2 = 원격
=======

        int leftPlayerId = 1;
        int rightPlayerId = 2;
        if (localPlayerId == 2) {
            leftPlayerId = 2;
            rightPlayerId = 1;
        }

        System.out.println("[NetworkMultiGameLayout] Determined localPlayerId=" + localPlayerId + ", leftId=" + leftPlayerId + ", rightId=" + rightPlayerId);

        GameModel leftModel = (leftPlayerId == 1) ? session.playerOneModel() : session.playerTwoModel();
        GameModel rightModel = (rightPlayerId == 1) ? session.playerOneModel() : session.playerTwoModel();

        // Replace panels appropriately: left panel shows local, right panel remote
        if (leftPlayerId == localPlayerId) {
>>>>>>> 49270cf72e766fd0fb497a5fa00491955c79a2bd
            replaceLeftWithLocal();
            replaceRightWithRemote();
        } else {
            localModel = session.playerTwoModel();   // P2 = 로컬
            remoteModel = session.playerOneModel();  // P1 = 원격
            replaceLeftWithLocal();   // P2를 왼쪽에
            replaceRightWithRemote(); // P1을 오른쪽에
        }

<<<<<<< HEAD
        System.out.println("[NetworkMultiGameLayout] Binding - Local(left)=" + localModel + ", Remote(right)=" + remoteModel);

        // 왼쪽=로컬, 오른쪽=원격으로 바인딩
        bindPlayerModels(localModel, remoteModel);
        
        // 🔧 FIX: 공격 대기열도 로컬/원격 플레이어 ID에 맞춰 바인딩
        if (localPlayerId == 1) {
            attackQueuePanel_1.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(1)); // 왼쪽=P1
            attackQueuePanel_2.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(2)); // 오른쪽=P2
        } else {
            attackQueuePanel_1.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(2)); // 왼쪽=P2
            attackQueuePanel_2.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(1)); // 오른쪽=P1
        }
=======
        // Bind models according to left/right decided above
        bindPlayerModels(leftModel, rightModel);

        // Bind attack queues to the correct player IDs for left/right panels
        final int lp = leftPlayerId;
        final int rp = rightPlayerId;
        attackQueuePanel_1.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(lp));
        attackQueuePanel_2.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(rp));
>>>>>>> 49270cf72e766fd0fb497a5fa00491955c79a2bd
        System.out.println("[NetworkMultiGameLayout] Session binding complete, repainting");
        String out = tetris.view.PvPGameRenderer.render(session.playerOneModel(), session.playerTwoModel(), true, true, "상태 메시지");
        System.out.println(out);
        repaint();
    }

    /**
     * 자신과 상대 모델을 각각 바인딩합니다.
     * - selfModel: 좌측(P1) 표시
     * - opponentModel: 우측(P2) 표시
     * - 클라이언트는 스냅샷을 받아 opponentModel에 적용하고, 화면에는 그 상태만 표시됩니다.
     */
    public void bindOnlineMultiplayer(GameModel selfModel, GameModel opponentModel) {
        bindPlayerModels(selfModel, opponentModel != null ? opponentModel : selfModel);
        attackQueuePanel_1.bindGameModel(selfModel);
        attackQueuePanel_2.bindGameModel(opponentModel != null ? opponentModel : selfModel);
        repaint();
    }

    private void bindPlayerModels(GameModel playerOne, GameModel playerTwo) {
        // 좌측 UI는 P1 모델, 우측 UI는 P2 모델을 그대로 바라보도록 분리합니다.
        gamePanel_1.bindGameModel(playerOne);
        gamePanel_2.bindGameModel(playerTwo);
        nextBlockPanel_1.bindGameModel(playerOne);
        nextBlockPanel_2.bindGameModel(playerTwo);
        scoreboard_1.bindGameModel(playerOne);
        scoreboard_2.bindGameModel(playerTwo);
        // 중앙 타이머 패널은 P1 기준으로 공유(필요 시 P2 전용 UI를 추가할 수 있습니다).
        timerPanel.bindGameModel(playerOne);

        // 모든 컴포넌트가 보이도록 명시적으로 설정
        gamePanel_1.setVisible(true);
        gamePanel_2.setVisible(true);
        nextBlockPanel_1.setVisible(true);
        nextBlockPanel_2.setVisible(true);
        scoreboard_1.setVisible(true);
        scoreboard_2.setVisible(true);
        attackQueuePanel_1.setVisible(true);
        attackQueuePanel_2.setVisible(true);
        timerPanel.setVisible(true);

        // 레이아웃 재검증 및 다시 그리기
        revalidate();
        repaint();
    }

    private void addToRegion(Component comp, int x, int y, int w, int h, int fill, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;

        gbc.weightx = gbc.gridwidth;
        gbc.weighty = gbc.gridheight;

        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.ipadx = 5;
        gbc.ipady = 5;

        comp.setVisible(true);
        this.add(comp, gbc);
    }

    // Helper methods to replace panels at runtime with Local/Remote variants
    private void replaceLeftWithLocal() {
        try {
            this.remove(gamePanel_1);
        } catch (Exception ignore) {}
        gamePanel_1 = new LocalPlayerPanel();
        // left region coordinates: 0,0,6,11
        addToRegion(gamePanel_1, 0, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        revalidate();
        repaint();
    }

    private void replaceLeftWithRemote() {
        try {
            this.remove(gamePanel_1);
        } catch (Exception ignore) {}
        gamePanel_1 = new RemotePlayerPanel();
        addToRegion(gamePanel_1, 0, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        revalidate();
        repaint();
    }

    private void replaceRightWithLocal() {
        try {
            this.remove(gamePanel_2);
        } catch (Exception ignore) {}
        gamePanel_2 = new LocalPlayerPanel();
        // right region coordinates: 11,0,6,11
        addToRegion(gamePanel_2, 11, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        revalidate();
        repaint();
    }

    private void replaceRightWithRemote() {
        try {
            this.remove(gamePanel_2);
        } catch (Exception ignore) {}
        gamePanel_2 = new RemotePlayerPanel();
        addToRegion(gamePanel_2, 11, 0, 6, 11, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
        revalidate();
        repaint();
    }
}
