package tetris.data.leaderboard;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import tetris.domain.GameMode;
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
    public synchronized List<LeaderboardEntry> loadTop(int n, GameMode mode) {
        List<LeaderboardEntry> filtered = new ArrayList<>();
        for (LeaderboardEntry entry : readAllEntries()) {
            if (entry.getMode() == mode) {
                filtered.add(entry);
            }
        }
        filtered.sort(Comparator.comparingInt(LeaderboardEntry::getPoints).reversed());
        if (n >= filtered.size()) return Collections.unmodifiableList(filtered);
        return Collections.unmodifiableList(new ArrayList<>(filtered.subList(0, n)));
    }

    @Override
    public synchronized void saveEntry(LeaderboardEntry entry) {
        List<LeaderboardEntry> standard = new ArrayList<>();
        List<LeaderboardEntry> item = new ArrayList<>();
        for (LeaderboardEntry existing : readAllEntries()) {
            if (existing.getMode() == GameMode.ITEM) {
                item.add(existing);
            } else {
                standard.add(existing.withMode(GameMode.STANDARD));
            }
        }

        if (entry.getMode() == GameMode.ITEM) {
            item.add(entry);
        } else {
            standard.add(entry.withMode(GameMode.STANDARD));
        }

        Comparator<LeaderboardEntry> comparator = Comparator.comparingInt(LeaderboardEntry::getPoints).reversed();
        standard.sort(comparator);
        item.sort(comparator);
        trimToCapacity(standard);
        trimToCapacity(item);

        StringBuilder sb = new StringBuilder();
        writeEntries(sb, standard);
        if (!standard.isEmpty() && !item.isEmpty()) sb.append('\n');
        writeEntries(sb, item);
        prefs.put(KEY_ENTRIES, sb.toString());
        try { prefs.flush(); } catch (Exception ex) { /* best-effort */ }
    }

    @Override
    public synchronized void reset() {
        prefs.remove(KEY_ENTRIES);
        try { prefs.flush(); } catch (Exception ex) { /* best-effort */ }
    }

    private List<LeaderboardEntry> readAllEntries() {
        String raw = prefs.get(KEY_ENTRIES, "");
        if (raw == null || raw.isBlank()) {
            return new ArrayList<>();
        }
        String[] lines = raw.split("\n");
        List<LeaderboardEntry> list = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) {
                continue;
            }
            try {
                String name = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name());
                int points = Integer.parseInt(parts[1]);
                GameMode mode = parts.length >= 3 ? GameMode.valueOf(parts[2]) : GameMode.STANDARD;
                list.add(new LeaderboardEntry(name, points, mode));
            } catch (Exception ignore) {
                // malformed line -> skip
            }
        }
        return list;
    }

    private void trimToCapacity(List<LeaderboardEntry> list) {
        while (list.size() > capacity) {
            list.remove(list.size() - 1);
        }
    }

    private void writeEntries(StringBuilder sb, List<LeaderboardEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e = entries.get(i);
            String enc = URLEncoder.encode(e.getName(), StandardCharsets.UTF_8);
            sb.append(enc).append('|').append(e.getPoints()).append('|').append(e.getMode().name());
            if (i < entries.size() - 1) {
                sb.append('\n');
            }
        }
    }
}
