package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

import tetris.domain.GameModel;

/*
 * 테스트 대상: tetris.view.MainPanel
 *
 * 역할 요약:
 * - 메인 메뉴 버튼을 구성하고, 포커스 이동/선택 시나리오를 제공한다.
 *
 * 테스트 전략:
 * - 생성 시 버튼 목록이 기대 개수로 초기화되고 패널이 비가시 상태인지 확인.
 * - bindGameModel이 모델 참조를 저장하는지 검증.
 * - focusButton이 포커스 인덱스를 순환시키며 버튼 배경색을 변경하는지 확인.
 * - 보호된 훅(onSinglePlayConfirmed 등)이 오버라이드되어 호출될 수 있음을 검증.
 */
class MainPanelTest {

    @Test
    void constructor_initializesButtonsAndInvisible() throws Exception {
        MainPanel panel = new MainPanel();
        assertFalse(panel.isVisible());
        List<?> buttons = getButtons(panel);
        assertEquals(5, buttons.size());
        assertEquals(Color.black, panel.getBackground());
    }

    @Test
    void bindGameModel_storesReference() throws Exception {
        MainPanel panel = new MainPanel();
        GameModel mockModel = org.mockito.Mockito.mock(GameModel.class);
        panel.bindGameModel(mockModel);

        Field f = MainPanel.class.getDeclaredField("gameModel");
        f.setAccessible(true);
        assertSame(mockModel, f.get(panel));
    }

    @Test
    void focusButton_cyclesHighlightColors() throws Exception {
        MainPanel panel = new MainPanel();
        @SuppressWarnings("unchecked")
        List<java.awt.Component> buttons = (List<java.awt.Component>) getButtons(panel);

        panel.focusButton(1);
        assertEquals(Color.white, buttons.get(0).getBackground());
        assertEquals(Color.gray, buttons.get(1).getBackground());

        panel.focusButton(-1); // wrap back
        assertEquals(Color.gray, buttons.get(0).getBackground());
    }

    @Test
    void hooks_canBeOverriddenAndCalled() {
        RecordingMainPanel panel = new RecordingMainPanel();
        panel.onSinglePlayConfirmed("ITEM");
        panel.onMultiPlayConfirmed("TIME_LIMIT", true, true);
        panel.onLocalMultiPlayConfirmed("NORMAL");
        panel.onOnlineServerCancelled();
        panel.onOnlineClientCancelled();
        panel.onSettingMenuClicked();
        panel.onScoreboardMenuClicked();
        panel.onExitMenuClicked();

        assertEquals("ITEM", panel.lastSingleMode);
        // onLocalMultiPlayConfirmed 마지막 호출로 "NORMAL"로 덮어씀
        assertEquals("NORMAL", panel.lastMultiMode);
        assertTrue(panel.lastMultiOnline);
        assertTrue(panel.lastMultiServer);
        assertTrue(panel.serverCancelled);
        assertTrue(panel.clientCancelled);
        assertTrue(panel.settingClicked);
        assertTrue(panel.scoreboardClicked);
        assertTrue(panel.exitClicked);
    }

    @SuppressWarnings("unchecked")
    private List<?> getButtons(MainPanel panel) throws Exception {
        Field f = MainPanel.class.getDeclaredField("buttons");
        f.setAccessible(true);
        return (List<?>) f.get(panel);
    }

    /** 테스트용 훅 오버라이드 패널 */
    private static final class RecordingMainPanel extends MainPanel {
        String lastSingleMode;
        String lastMultiMode;
        boolean lastMultiOnline;
        boolean lastMultiServer;
        boolean serverCancelled;
        boolean clientCancelled;
        boolean settingClicked;
        boolean scoreboardClicked;
        boolean exitClicked;

        @Override protected void onSinglePlayConfirmed(String mode) { this.lastSingleMode = mode; }
        @Override protected void onMultiPlayConfirmed(String mode, boolean isOnline, boolean isServer) {
            this.lastMultiMode = mode;
            this.lastMultiOnline = isOnline;
            this.lastMultiServer = isServer;
        }
        @Override protected void onLocalMultiPlayConfirmed(String mode) { this.lastMultiMode = mode; }
        @Override protected void onOnlineServerCancelled() { this.serverCancelled = true; }
        @Override protected void onOnlineClientCancelled() { this.clientCancelled = true; }
        @Override protected String getServerAddress() { return "localhost"; }
        @Override protected void connectToServer(String address) { /* no-op */ }
        @Override protected void onSettingMenuClicked() { this.settingClicked = true; }
        @Override protected void onScoreboardMenuClicked() { this.scoreboardClicked = true; }
        @Override protected void onExitMenuClicked() { this.exitClicked = true; }
    }
}
