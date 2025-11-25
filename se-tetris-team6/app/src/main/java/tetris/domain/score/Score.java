package tetris.domain.score;

/**
 * 점수, 레벨, 삭제한 줄 수를 표현하는 불변 도메인 모델.
 */
public final class Score {

    private final int points;
    private final int level;
    private final int clearedLines;

    private Score(int points, int level, int clearedLines) {
        this.points = Math.max(0, points);
        this.level = Math.max(0, level);
        this.clearedLines = Math.max(0, clearedLines);
    }

    public static Score zero() {
        return new Score(0, 0, 0);
    }

    public static Score of(int points, int level, int clearedLines) {
        return new Score(points, level, clearedLines);
    }

    public int getPoints() {
        return points;
    }

    public int getLevel() {
        return level;
    }

    public int getClearedLines() {
        return clearedLines;
    }

    public Score withAdditionalPoints(int delta) {
        if (delta <= 0) {
            return this;
        }
        return new Score(points + delta, level, clearedLines);
    }

    public Score withClearedLinesAdded(int delta) {
        if (delta <= 0) {
            return this;
        }
        return new Score(points, level, clearedLines + delta);
    }

    public Score withLevel(int newLevel) {
        if (newLevel < 0 || newLevel == level) {
            return this;
        }
        return new Score(points, newLevel, clearedLines);
    }

    public Score minusPoints(int delta) {
        if (delta <= 0) {
            return this;
        }
        int nextPoints = Math.max(0, points - delta);
        return new Score(nextPoints, level, clearedLines);
    }
}
