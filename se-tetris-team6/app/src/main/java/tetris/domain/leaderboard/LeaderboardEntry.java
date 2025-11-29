package tetris.domain.leaderboard;

import java.util.Objects;

import tetris.domain.GameMode;

/** Simple leaderboard entry containing a player name, points, and mode. */
public final class LeaderboardEntry {
    private final String name;
    private final int points;
    private final GameMode mode;

    public LeaderboardEntry(String name, int points) {
        this(name, points, GameMode.STANDARD);
    }

    public LeaderboardEntry(String name, int points, GameMode mode) {
        this.name = Objects.requireNonNull(name, "name");
        this.points = points;
        this.mode = mode == null ? GameMode.STANDARD : mode;
    }

    public String getName() { return name; }
    public int getPoints() { return points; }
    public GameMode getMode() { return mode; }

    public LeaderboardEntry withMode(GameMode newMode) {
        return new LeaderboardEntry(name, points, newMode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeaderboardEntry other)) return false;
        return points == other.points
                && name.equals(other.name)
                && mode == other.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, points, mode);
    }
}
