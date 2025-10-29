package tetris.domain.leaderboard;

import java.util.Objects;

/** Simple leaderboard entry containing a player name and points. */
public final class LeaderboardEntry {
    private final String name;
    private final int points;

    public LeaderboardEntry(String name, int points) {
        this.name = Objects.requireNonNull(name, "name");
        this.points = points;
    }

    public String getName() { return name; }
    public int getPoints() { return points; }
}
