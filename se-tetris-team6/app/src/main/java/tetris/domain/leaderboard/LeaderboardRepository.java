package tetris.domain.leaderboard;

import java.util.List;

/** Repository abstraction for leaderboard/top scores. */
public interface LeaderboardRepository {
    /** Return the top N entries for the given mode, sorted descending by points. */
    List<LeaderboardEntry> loadTop(int n, tetris.domain.GameMode mode);

    /** Persist a new entry; repository may trim to a fixed capacity. */
    void saveEntry(LeaderboardEntry entry);

    /** Reset leaderboard to initial empty state. */
    void reset();
}
