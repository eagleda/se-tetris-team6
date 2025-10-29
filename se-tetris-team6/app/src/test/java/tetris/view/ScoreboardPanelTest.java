package tetris.view;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import tetris.domain.leaderboard.LeaderboardEntry;

public class ScoreboardPanelTest {

    @Test
    public void renderLeaderboard_showsEntries() throws Exception {
        ScoreboardPanel panel = new ScoreboardPanel();

        var entries = List.of(
            new LeaderboardEntry("Alpha", 123, tetris.domain.GameMode.STANDARD),
            new LeaderboardEntry("Beta", 45, tetris.domain.GameMode.STANDARD)
        );

        SwingUtilities.invokeAndWait(() -> panel.renderLeaderboard(tetris.domain.GameMode.STANDARD, entries));

        // find the STANDARD JList in the component tree
        javax.swing.JList<?> found = findFirstJList(panel);
        assertNotNull(found, "Expected to find a JList in ScoreboardPanel");
        assertEquals(2, found.getModel().getSize());
        assertTrue(found.getModel().getElementAt(0).toString().contains("Alpha"));
        assertTrue(found.getModel().getElementAt(1).toString().contains("Beta"));
    }

    private javax.swing.JList<?> findFirstJList(java.awt.Container c) {
        for (java.awt.Component comp : c.getComponents()) {
            if (comp instanceof javax.swing.JList) return (javax.swing.JList<?>) comp;
            if (comp instanceof java.awt.Container) {
                javax.swing.JList<?> r = findFirstJList((java.awt.Container) comp);
                if (r != null) return r;
            }
        }
        return null;
    }
}
