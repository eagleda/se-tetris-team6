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
    // 플레이어 1 (자신) - 화면의 왼쪽 영역
    private GamePanel gamePanel_1;
    private NextBlockPanel nextBlockPanel_1;
    private ScorePanel scoreboard_1;
    private AttackQueuePanel attackQueuePanel_1;

    // 플레이어 2 (상대) - 화면의 오른쪽 영역
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
     * - 로컬 플레이어: 항상 좌측에 표시
     * - 원격 플레이어: 항상 우측에 표시
     */
    public void bindOnlineMultiplayerSession(NetworkMultiplayerSession session) {
        System.out.println("[NetworkMultiGameLayout] bindOnlineMultiplayerSession called - session=" + (session != null ? "ACTIVE" : "NULL"));
        if (session == null) {
            return;
        }
        
        // 1. 로컬 플레이어 ID 확인
        MultiplayerHandler handler = session.handler();
        int localPlayerId = 0;
        if (handler instanceof tetris.multiplayer.handler.NetworkedMultiplayerHandler) {
            localPlayerId = ((tetris.multiplayer.handler.NetworkedMultiplayerHandler) handler).getLocalPlayerId();
        }

        // 2. 왼쪽/오른쪽 플레이어 ID 결정 (로컬 플레이어가 항상 왼쪽)
        int leftPlayerId = 1;
        int rightPlayerId = 2;
        if (localPlayerId == 2) {
            leftPlayerId = 2;
            rightPlayerId = 1;
        }

        System.out.println("[NetworkMultiGameLayout] Determined localPlayerId=" + localPlayerId + ", leftId=" + leftPlayerId + ", rightId=" + rightPlayerId);

        // 3. 왼쪽/오른쪽 모델 결정
        GameModel leftModel = (leftPlayerId == 1) ? session.playerOneModel() : session.playerTwoModel();
        GameModel rightModel = (rightPlayerId == 1) ? session.playerOneModel() : session.playerTwoModel();

        // 4. 패널 교체: 왼쪽 패널은 로컬, 오른쪽 패널은 원격으로 설정
        if (leftPlayerId == localPlayerId) {
            replaceLeftWithLocal();
            replaceRightWithRemote();
        } else {
            // 이 경우는 로컬 플레이어가 P1이고 P2가 왼쪽으로 할당된 경우 (발생하지 않아야 함)
            replaceLeftWithRemote();
            replaceRightWithLocal();
        }

        // 5. 모델 바인딩 (leftModel -> gamePanel_1, rightModel -> gamePanel_2)
        bindPlayerModels(leftModel, rightModel);

        // 6. 공격 대기열 바인딩
        // - 서버(호스트): handler를 통해 실제 공격 대기열 가져오기
        // - 클라이언트: 스냅샷에서 받은 공격 대기열 데이터 표시
        final int lp = leftPlayerId;
        final int rp = rightPlayerId;
        
        if (localPlayerId == 1) {
            // 서버인 경우: handler에서 실제 공격 대기열 데이터 가져오기
            attackQueuePanel_1.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(lp));
            attackQueuePanel_2.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(rp));
        } else {
            // 클라이언트인 경우: 스냅샷에서 받은 데이터 표시
            attackQueuePanel_1.bindAttackLinesSupplier(() -> leftModel.getSnapshotAttackLines());
            attackQueuePanel_2.bindAttackLinesSupplier(() -> rightModel.getSnapshotAttackLines());
        }

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
