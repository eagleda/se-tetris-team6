package tetris.domain.leaderboard;

import java.util.List;

/** Repository abstraction for leaderboard/top scores. */
public interface LeaderboardRepository {
    /** Return the top N entries, sorted descending by points. */
    List<LeaderboardEntry> loadTop(int n);

    /** Persist a new entry; repository may trim to a fixed capacity. */
    void saveEntry(LeaderboardEntry entry);

    /** Reset leaderboard to initial empty state. */
    void reset();
}
