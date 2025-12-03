package tetris.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import tetris.domain.BlockGenerator;
import tetris.domain.BlockKind;
import tetris.domain.GameModel;
import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.setting.Setting;
import tetris.domain.setting.SettingRepository;
import tetris.domain.setting.SettingService;

/*
 * 테스트 대상: tetris.view.TetrisFrame
 *
 * 역할 요약:
 * - 메인 프레임을 생성하고 컨트롤러/뷰/레이아웃을 구성하는 Swing 진입점.
 *
 * 테스트 전략:
 * - 헤드리스 환경이 아니면 프레임을 생성해 제목/레이어드페인이 초기화되는지 확인.
 * - 생성된 프레임은 테스트 후 dispose로 정리.
 */
class TetrisFrameTest {

    @Test
    void constructor_initializesFrameAndPanels() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 JFrame 생성 불가");

        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                new DummyLeaderboardRepo(), new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        final TetrisFrame[] holder = new TetrisFrame[1];

        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));

        TetrisFrame frame = holder[0];
        assertNotNull(frame);
        assertEquals("Tetris Game - Team 06", frame.getTitle());
        assertNotNull(frame.getLayeredPane());

        frame.dispose();
    }

    @Test
    void displayPanel_nullIsNoOp_andScoreboardLoadsWhenEntriesPresent() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 JFrame 생성 불가");

        DummyLeaderboardRepo lbRepo = new DummyLeaderboardRepo();
        lbRepo.entries = java.util.List.of(new LeaderboardEntry("AAA", 100));
        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                lbRepo, new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        final TetrisFrame[] holder = new TetrisFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));
        TetrisFrame frame = holder[0];

        SwingUtilities.invokeAndWait(() -> frame.displayPanel(null)); // should warn but not throw
        SwingUtilities.invokeAndWait(() -> frame.displayPanel(getPanel(frame, "scoreboardPanel")));

        javax.swing.DefaultListModel<?> standardModel =
                (javax.swing.DefaultListModel<?>) getField(getPanel(frame, "scoreboardPanel"), "standardModel");
        assertEquals(1, standardModel.size());

        frame.dispose();
    }

    @Test
    void applyScreenSize_changesFrameSize() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 JFrame 생성 불가");
        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                new DummyLeaderboardRepo(), new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        final TetrisFrame[] holder = new TetrisFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));
        TetrisFrame frame = holder[0];

        SwingUtilities.invokeAndWait(() -> frame.applyScreenSize(Setting.ScreenSize.SMALL));
        assertEquals(Setting.ScreenSize.SMALL.getDimension(), frame.getSize());

        frame.dispose();
    }

    @Test
    void displayPanel_withPendingHighlightLoadsScoreboard() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 JFrame 생성 불가");

        DummyLeaderboardRepo lbRepo = new DummyLeaderboardRepo();
        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                lbRepo, new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        LeaderboardResult result = new LeaderboardResult(
                List.of(new LeaderboardEntry("AAA", 100), new LeaderboardEntry("BBB", 200)), 0);

        final TetrisFrame[] holder = new TetrisFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));
        TetrisFrame frame = holder[0];

        SwingUtilities.invokeAndWait(() -> frame.setPendingLeaderboard(GameMode.STANDARD, result));
        SwingUtilities.invokeAndWait(() -> frame.displayPanel(getPanel(frame, "scoreboardPanel")));

        javax.swing.DefaultListModel<?> standardModel =
                (javax.swing.DefaultListModel<?>) getField(getPanel(frame, "scoreboardPanel"), "standardModel");
        assertEquals(2, standardModel.size());

        frame.dispose();
    }

    @Test
    void togglePausePanel_switchesBetweenPanels() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 JFrame 생성 불가");

        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                new DummyLeaderboardRepo(), new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        final TetrisFrame[] holder = new TetrisFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));
        TetrisFrame frame = holder[0];

        SwingUtilities.invokeAndWait(() -> frame.showMainPanel());
        SwingUtilities.invokeAndWait(() -> frame.togglePausePanel());

        Object curr = getStaticField(TetrisFrame.class, "currPanel");
        assertSame(getPanel(frame, "pausePanel"), curr);

        frame.dispose();
    }

    @Test
    void showMainAndScoreboardPanels_updateCurrPanel() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Headless 환경에서는 JFrame 생성 불가");

        DummyLeaderboardRepo lbRepo = new DummyLeaderboardRepo();
        lbRepo.entries = List.of(new LeaderboardEntry("AAA", 100));
        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                lbRepo, new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        final TetrisFrame[] holder = new TetrisFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));
        TetrisFrame frame = holder[0];

        SwingUtilities.invokeAndWait(() -> frame.showMainPanel());
        assertSame(getPanel(frame, "mainPanel"), getStaticField(TetrisFrame.class, "currPanel"));

        SwingUtilities.invokeAndWait(() -> frame.showScoreboardPanel());
        assertSame(getPanel(frame, "scoreboardPanel"), getStaticField(TetrisFrame.class, "currPanel"));

        frame.dispose();
    }

    @Test
    void setPendingLeaderboard_storesResult() throws Exception {
        DummyLeaderboardRepo lbRepo = new DummyLeaderboardRepo();
        GameModel model = new GameModel(new ConstGenerator(), new DummyScoreRepo(),
                lbRepo, new SettingService(new InMemorySettingRepository(), new DummyScoreRepo()));
        final TetrisFrame[] holder = new TetrisFrame[1];
        SwingUtilities.invokeAndWait(() -> holder[0] = new TetrisFrame(model));
        TetrisFrame frame = holder[0];

        LeaderboardResult result = new LeaderboardResult(List.of(new LeaderboardEntry("ZZZ", 999)), 0);
        frame.setPendingLeaderboard(GameMode.ITEM, result);

        LeaderboardResult stored = (LeaderboardResult) getField(frame, "pendingItemHighlight");
        assertSame(result, stored);

        frame.dispose();
    }

    private javax.swing.JPanel getPanel(TetrisFrame frame, String fieldName) {
        try {
            Field f = TetrisFrame.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (javax.swing.JPanel) f.get(frame);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getField(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object getStaticField(Class<?> type, String name) {
        try {
            Field f = type.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // === 테스트용 단순 구현체들 ===
    static class DummyScoreRepo implements ScoreRepository {
        private Score score = Score.zero();
        @Override public Score load() { return score; }
        @Override public void save(Score score) { this.score = score; }
        @Override public void reset() { this.score = Score.zero(); }
    }

    static class DummyLeaderboardRepo implements LeaderboardRepository {
        java.util.List<LeaderboardEntry> entries = java.util.Collections.emptyList();
        @Override public java.util.List<LeaderboardEntry> loadTop(int n, GameMode mode) { return entries; }
        @Override public void saveEntry(LeaderboardEntry entry) { }
        @Override public tetris.domain.leaderboard.LeaderboardResult saveAndHighlight(LeaderboardEntry entry) {
            return new tetris.domain.leaderboard.LeaderboardResult(entries, -1);
        }
        @Override public void reset() { }
    }

    static class InMemorySettingRepository implements SettingRepository {
        private Setting current = Setting.defaults();
        @Override public Setting load() { return current; }
        @Override public void save(Setting settings) { current = settings; }
        @Override public void resetToDefaults() { current = Setting.defaults(); }
    }

    static class ConstGenerator implements BlockGenerator {
        @Override public BlockKind nextBlock() { return BlockKind.I; }
        @Override public BlockKind peekNext() { return BlockKind.I; }
    }
}
