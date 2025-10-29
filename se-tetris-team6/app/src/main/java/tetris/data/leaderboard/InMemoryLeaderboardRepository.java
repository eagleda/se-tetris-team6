package tetris.data.leaderboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;

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
    public synchronized List<LeaderboardEntry> loadTop(int n) {
        List<LeaderboardEntry> copy = new ArrayList<>(entries);
        copy.sort(Comparator.comparingInt(LeaderboardEntry::getPoints).reversed());
        if (n >= copy.size()) return Collections.unmodifiableList(copy);
        return Collections.unmodifiableList(copy.subList(0, n));
    }

    @Override
    public synchronized void saveEntry(LeaderboardEntry entry) {
        entries.add(entry);
        entries.sort(Comparator.comparingInt(LeaderboardEntry::getPoints).reversed());
        // trim to capacity
        while (entries.size() > capacity) {
            entries.remove(entries.size() - 1);
        }
    }

    @Override
    public synchronized void reset() {
        entries.clear();
    }
}
