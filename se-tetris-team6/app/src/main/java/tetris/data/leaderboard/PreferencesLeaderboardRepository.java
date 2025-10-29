package tetris.data.leaderboard;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;

/**
 * Preferences-backed leaderboard. Stores entries as encoded lines under a single
 * Preferences key. Each line is: urlencoded(name)|points
 */
public final class PreferencesLeaderboardRepository implements LeaderboardRepository {

    private static final String NODE = "se-tetris-team6/leaderboard";
    private static final String KEY_ENTRIES = "entries";
    private final Preferences prefs;
    private final int capacity;

    public PreferencesLeaderboardRepository() {
        this(Preferences.userRoot().node(NODE), 10);
    }

    public PreferencesLeaderboardRepository(Preferences prefs, int capacity) {
        this.prefs = prefs;
        this.capacity = Math.max(1, capacity);
    }

    @Override
    public synchronized List<LeaderboardEntry> loadTop(int n) {
        String raw = prefs.get(KEY_ENTRIES, "");
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        String[] lines = raw.split("\n");
        List<LeaderboardEntry> list = new ArrayList<>();
        for (String line : lines) {
            int sep = line.lastIndexOf('|');
            if (sep <= 0) continue;
            try {
                String enc = line.substring(0, sep);
                String name = URLDecoder.decode(enc, StandardCharsets.UTF_8.name());
                int pts = Integer.parseInt(line.substring(sep + 1));
                list.add(new LeaderboardEntry(name, pts));
            } catch (Exception ex) {
                // ignore malformed lines
            }
        }
        list.sort(Comparator.comparingInt(LeaderboardEntry::getPoints).reversed());
        if (n >= list.size()) return Collections.unmodifiableList(list);
        return Collections.unmodifiableList(new ArrayList<>(list.subList(0, n)));
    }

    @Override
    public synchronized void saveEntry(LeaderboardEntry entry) {
        List<LeaderboardEntry> current = new ArrayList<>(loadTop(capacity));
        current.add(entry);
        current.sort(Comparator.comparingInt(LeaderboardEntry::getPoints).reversed());
        while (current.size() > capacity) current.remove(current.size() - 1);
        // serialize
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < current.size(); i++) {
            LeaderboardEntry e = current.get(i);
            String enc = URLEncoder.encode(e.getName(), StandardCharsets.UTF_8);
            sb.append(enc).append('|').append(e.getPoints());
            if (i < current.size() - 1) sb.append('\n');
        }
        prefs.put(KEY_ENTRIES, sb.toString());
        try { prefs.flush(); } catch (Exception ex) { /* best-effort */ }
    }

    @Override
    public synchronized void reset() {
        prefs.remove(KEY_ENTRIES);
        try { prefs.flush(); } catch (Exception ex) { /* best-effort */ }
    }
}
