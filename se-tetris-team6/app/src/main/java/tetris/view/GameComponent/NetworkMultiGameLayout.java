package tetris.view.GameComponent;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import tetris.domain.GameModel;
import tetris.multiplayer.session.LocalMultiplayerSession;

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

    public NetworkMultiGameLayout() {
        super(new GridBagLayout());
        setOpaque(true);
        setVisible(true);

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
     * - 자신(P1, 좌측)과 상대(P2, 우측) 모델을 각각 표시합니다.
     * - 공격 대기열은 LocalMultiplayerSession의 handler를 통해 실시간으로 갱신됩니다.
     */
    public void bindOnlineMultiplayerSession(LocalMultiplayerSession session) {
        System.out.println("[NetworkMultiGameLayout] bindOnlineMultiplayerSession called - session=" + (session != null ? "ACTIVE" : "NULL"));
        if (session == null) {
            return;
        }
        System.out.println("[NetworkMultiGameLayout] Binding player models - P1=" + session.playerOneModel() + ", P2=" + session.playerTwoModel());
        bindPlayerModels(session.playerOneModel(), session.playerTwoModel());
        // 각 패널이 해당 플레이어의 공격 패턴(구멍 위치 포함)을 바로 읽어오도록 공급자를 연결합니다.
        attackQueuePanel_1.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(1));
        attackQueuePanel_2.bindAttackLinesSupplier(() -> session.handler().getPendingAttackLines(2));
        System.out.println("[NetworkMultiGameLayout] Session binding complete, repainting");
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
}
