package tetris.data.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tetris.domain.GameMode;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;

/** A simple in-memory leaderboard that keeps top entries by points. */
public final class InMemoryLeaderboardRepository implements LeaderboardRepository {

    private final List<LeaderboardEntry> entries = new ArrayList<>();
    private final int capacity;

    public InMemoryLeaderboardRepository() {
        this(10);
    }

    public InMemoryLeaderboardRepository(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    @Override
    public synchronized List<LeaderboardEntry> loadTop(int n, GameMode mode) {
        List<LeaderboardEntry> filtered = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
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
        saveAndHighlight(entry);
    }

    @Override
    public synchronized LeaderboardResult saveAndHighlight(LeaderboardEntry entry) {
        entries.add(entry);
        // sort by mode then points
        entries.sort((a, b) -> {
            int modeCmp = a.getMode().compareTo(b.getMode());
            if (modeCmp != 0) return modeCmp;
            return Integer.compare(b.getPoints(), a.getPoints());
        });

        trimMode(GameMode.STANDARD);
        trimMode(GameMode.ITEM);
        List<LeaderboardEntry> target = new ArrayList<>();
        for (LeaderboardEntry e : entries) {
            if (e.getMode() == entry.getMode()) {
                target.add(e);
            }
        }
        int highlight = target.indexOf(entry);
        System.out.printf("[LB][Memory] mode=%s size=%d highlight=%d name=%s pts=%d%n",
                entry.getMode(), target.size(), highlight, entry.getName(), entry.getPoints());
        return new LeaderboardResult(Collections.unmodifiableList(target), highlight);
    }

    @Override
    public synchronized void reset() {
        entries.clear();
    }

    private void trimMode(GameMode mode) {
        int count = 0;
        List<LeaderboardEntry> toRemove = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            if (entry.getMode() == mode) {
                count++;
                if (count > capacity) {
                    toRemove.add(entry);
                }
            }
        }
        entries.removeAll(toRemove);
    }
}
