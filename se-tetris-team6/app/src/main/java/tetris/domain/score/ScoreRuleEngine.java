package tetris.domain.score;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 점수 규칙 집계기.
 * - 블록 하강/라인 삭제 이벤트 등을 받아 점수를 계산합니다.
 * - 계산된 결과는 {@link ScoreRepository}를 통해 저장하며, 리스너에게 알립니다.
 */
public final class ScoreRuleEngine {

    private static final int BASE_DROP_POINTS = 1;

    private final ScoreRepository repository;
    private final List<Consumer<Score>> listeners = new CopyOnWriteArrayList<>();

    public ScoreRuleEngine(ScoreRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public void addListener(Consumer<Score> listener) {
        Objects.requireNonNull(listener, "listener");
        listeners.add(listener);
    }

    public void removeListener(Consumer<Score> listener) {
        listeners.remove(listener);
    }

    public void onBlockDescend() {
        apply(score -> score.withAdditionalPoints(BASE_DROP_POINTS));
    }

    public void onBlockLocked() {
        // 확장 지점: 빠른 잠금 보너스 등
    }

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
        apply(score -> score
            .withClearedLinesAdded(clearedLines)
            .withAdditionalPoints(base));
    }

    public void resetScore() {
        repository.reset();
        notifyListeners(repository.load());
    }

    private void apply(UnaryOperator<Score> operator) {
        Score current = repository.load();
        Score updated = operator.apply(current);
        repository.save(updated);
        notifyListeners(updated);
    }

    private void notifyListeners(Score updated) {
        for (Consumer<Score> listener : listeners) {
            listener.accept(updated);
        }
    }
}
