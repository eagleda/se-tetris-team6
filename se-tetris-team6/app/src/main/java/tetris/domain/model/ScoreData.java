package tetris.domain.model;

/**
 * 점수/레벨/라인 수를 추적하는 단순 데이터 클래스.
 */
public final class ScoreData {
    private int score;
    private int level;
    private int clearedLines;

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getClearedLines() {
        return clearedLines;
    }

    public void addScore(int delta) {
        score += delta;
        System.out.printf("[LOG] score updated: %d%n", score);
    }

    public void addClearedLines(int lines) {
        clearedLines += lines;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }

    public void reset() {
        score = 0;
        level = 0;
        clearedLines = 0;
    }
}
