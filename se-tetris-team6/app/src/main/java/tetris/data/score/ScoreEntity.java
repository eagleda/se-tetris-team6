package tetris.data.score;

import tetris.domain.score.Score;

/**
 * 점수를 보관하는 내부 데이터 모델.
 */
final class ScoreEntity {

    private int points;
    private int level;
    private int clearedLines;

    static ScoreEntity initial() {
        return fromDomain(Score.zero());
    }

    static ScoreEntity fromDomain(Score score) {
        ScoreEntity entity = new ScoreEntity();
        entity.points = score.getPoints();
        entity.level = score.getLevel();
        entity.clearedLines = score.getClearedLines();
        return entity;
    }

    Score toDomain() {
        return Score.of(points, level, clearedLines);
    }

    void overwrite(Score score) {
        this.points = score.getPoints();
        this.level = score.getLevel();
        this.clearedLines = score.getClearedLines();
    }
}
