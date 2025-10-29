package tetris;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tetris.data.leaderboard.PreferencesLeaderboardRepository;
import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;

class PreferencesLeaderboardRepositoryTest {

    private Preferences prefs;
    private LeaderboardRepository repository;

    @BeforeEach
    void setUp() {
        String node = "se-tetris-team6/test/leaderboard/" + UUID.randomUUID();
        prefs = Preferences.userRoot().node(node);
        repository = new PreferencesLeaderboardRepository(prefs, 3);
    }

    @AfterEach
    void tearDown() throws Exception {
        prefs.removeNode();
        prefs.flush();
    }

    @Test
    void savesAndLoadsEntriesByMode() {
        repository.saveEntry(new LeaderboardEntry("Alice", 1000, GameMode.STANDARD));
        repository.saveEntry(new LeaderboardEntry("Bob", 800, GameMode.STANDARD));
        repository.saveEntry(new LeaderboardEntry("Cara", 1200, GameMode.ITEM));
        repository.saveEntry(new LeaderboardEntry("Dan", 400, GameMode.ITEM));

        List<LeaderboardEntry> standard = repository.loadTop(10, GameMode.STANDARD);
        List<LeaderboardEntry> item = repository.loadTop(10, GameMode.ITEM);

        assertEquals(2, standard.size());
        assertEquals("Alice", standard.get(0).getName());
        assertEquals(1000, standard.get(0).getPoints());

        assertEquals(2, item.size());
        assertEquals("Cara", item.get(0).getName());
        assertEquals(1200, item.get(0).getPoints());
    }

    @Test
    void respectsCapacitySeparatelyPerMode() {
        repository.saveEntry(new LeaderboardEntry("S1", 100, GameMode.STANDARD));
        repository.saveEntry(new LeaderboardEntry("S2", 200, GameMode.STANDARD));
        repository.saveEntry(new LeaderboardEntry("S3", 300, GameMode.STANDARD));
        repository.saveEntry(new LeaderboardEntry("S4", 400, GameMode.STANDARD));

        repository.saveEntry(new LeaderboardEntry("I1", 100, GameMode.ITEM));
        repository.saveEntry(new LeaderboardEntry("I2", 200, GameMode.ITEM));
        repository.saveEntry(new LeaderboardEntry("I3", 300, GameMode.ITEM));
        repository.saveEntry(new LeaderboardEntry("I4", 400, GameMode.ITEM));

        List<LeaderboardEntry> standard = repository.loadTop(10, GameMode.STANDARD);
        List<LeaderboardEntry> item = repository.loadTop(10, GameMode.ITEM);

        assertEquals(3, standard.size(), "standard entries should be trimmed to capacity");
        assertEquals("S4", standard.get(0).getName());
        assertEquals("S2", standard.get(standard.size() - 1).getName());

        assertEquals(3, item.size(), "item entries should be trimmed to capacity");
        assertEquals("I4", item.get(0).getName());
        assertEquals("I2", item.get(item.size() - 1).getName());
    }

    @Test
    void resetClearsAllEntries() throws Exception {
        repository.saveEntry(new LeaderboardEntry("Alice", 500, GameMode.STANDARD));
        repository.reset();

        assertTrue(repository.loadTop(10, GameMode.STANDARD).isEmpty());
        assertTrue(repository.loadTop(10, GameMode.ITEM).isEmpty());
    }
}
