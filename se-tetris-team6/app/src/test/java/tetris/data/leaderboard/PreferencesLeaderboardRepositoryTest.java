package tetris.data.leaderboard;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import tetris.domain.leaderboard.LeaderboardEntry;

public class PreferencesLeaderboardRepositoryTest {

    private final String testNode = "se-tetris-team6/test-leaderboard";

    @AfterEach
    public void cleanup() throws Exception {
        Preferences.userRoot().node(testNode).removeNode();
    }

    @Test
    public void saveAndLoad_roundtrip() {
        Preferences prefs = Preferences.userRoot().node(testNode);
    PreferencesLeaderboardRepository repo = new PreferencesLeaderboardRepository(prefs, 10);

    repo.saveEntry(new LeaderboardEntry("Alice", 100, tetris.domain.GameMode.STANDARD));
    repo.saveEntry(new LeaderboardEntry("Bob", 200, tetris.domain.GameMode.STANDARD));

    List<LeaderboardEntry> top = repo.loadTop(10, tetris.domain.GameMode.STANDARD);
        assertEquals(2, top.size());
        assertEquals("Bob", top.get(0).getName());
        assertEquals(200, top.get(0).getPoints());
        assertEquals("Alice", top.get(1).getName());
    }

    @Test
    public void reset_clearsEntries() {
        Preferences prefs = Preferences.userRoot().node(testNode);
    PreferencesLeaderboardRepository repo = new PreferencesLeaderboardRepository(prefs, 10);
    repo.saveEntry(new LeaderboardEntry("X", 1, tetris.domain.GameMode.STANDARD));
    assertFalse(repo.loadTop(10, tetris.domain.GameMode.STANDARD).isEmpty());
    repo.reset();
    assertTrue(repo.loadTop(10, tetris.domain.GameMode.STANDARD).isEmpty());
    }

    @Test
    public void capacity_trimsToLimit() {
        Preferences prefs = Preferences.userRoot().node(testNode);
    PreferencesLeaderboardRepository repo = new PreferencesLeaderboardRepository(prefs, 3);
    repo.saveEntry(new LeaderboardEntry("A", 10, tetris.domain.GameMode.STANDARD));
    repo.saveEntry(new LeaderboardEntry("B", 20, tetris.domain.GameMode.STANDARD));
    repo.saveEntry(new LeaderboardEntry("C", 30, tetris.domain.GameMode.STANDARD));
    repo.saveEntry(new LeaderboardEntry("D", 40, tetris.domain.GameMode.STANDARD));

    List<LeaderboardEntry> top = repo.loadTop(10, tetris.domain.GameMode.STANDARD);
        assertEquals(3, top.size());
        assertEquals("D", top.get(0).getName());
        assertEquals("B", top.get(2).getName());
    }
}
