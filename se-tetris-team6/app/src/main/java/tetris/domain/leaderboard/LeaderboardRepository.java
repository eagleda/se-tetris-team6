package tetris.domain.leaderboard;

import java.util.List;

/** Repository abstraction for leaderboard/top scores. */
public interface LeaderboardRepository {
    /** Return the top N entries for the given mode, sorted descending by points. */
    List<LeaderboardEntry> loadTop(int n, tetris.domain.GameMode mode);

    /** Persist a new entry; repository may trim to a fixed capacity. */
    void saveEntry(LeaderboardEntry entry);

    /**
     * Persist a new entry and return the sorted list with the index of the newly inserted/updated
     * record for the corresponding mode. Highlight index is -1 if the record is not retained (e.g. trimmed).
     */
    LeaderboardResult saveAndHighlight(LeaderboardEntry entry);

    /** Reset leaderboard to initial empty state. */
    void reset();
}
