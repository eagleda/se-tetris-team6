package tetris.domain.model;

import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * 점수 규칙 집계기.
 * 블록 하강, 고정, 라인 삭제 등의 이벤트를 받아 {@link ScoreData}를 갱신하고
 * 점수 변경 사실을 콜백으로 전달합니다.
 */
public final class ScoreRuleEngine {

    private static final int BASE_DROP_POINTS = 1;

    private final ScoreData score;
    private final IntConsumer scoreChanged;

    public ScoreRuleEngine(ScoreData score, IntConsumer scoreChanged) {
        this.score = Objects.requireNonNull(score, "score");
        this.scoreChanged = scoreChanged;
    }

    /**
     * 블록이 1칸 하강할 때마다 호출.
     * 자동/수동 조작과 무관하게 1점을 지급합니다.
     */
    public void onBlockDescend() {
        applyScore(BASE_DROP_POINTS);
    }

    /**
     * 블록이 보드에 고정될 때 호출.
     * (현재는 별도의 점수를 부여하지 않지만 확장 지점을 남겨둡니다.)
     */
    public void onBlockLocked() {
        // 확장 지점: 빠른 잠금 보너스 등
    }

    /**
     * 라인 삭제 시 호출. 삭제 줄 수에 비례해 점수를 지급합니다.
     */
    public void onLinesCleared(int clearedLines) {
        if (clearedLines <= 0) {
            return;
        }
        int base = switch (clearedLines) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> 1200;
        };
        applyScore(base);
        score.addClearedLines(clearedLines);
    }

    private void applyScore(int delta) {
        if (delta <= 0) {
            return;
        }
        score.addScore(delta);
        if (scoreChanged != null) {
            scoreChanged.accept(score.getScore());
        }
    }
}
