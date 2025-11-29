package tetris.data.score;

import java.util.Objects;

import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;

/**
 * 애플리케이션 메모리에 점수 상태를 저장하는 단순 구현체.
 */
public final class InMemoryScoreRepository implements ScoreRepository {

    private ScoreEntity state;

    public InMemoryScoreRepository() {
        this.state = ScoreEntity.initial();
    }

    @Override
    public synchronized Score load() {
        return state.toDomain();
    }

    @Override
    public synchronized void save(Score score) {
        Objects.requireNonNull(score, "score");
        state.overwrite(score);
    }

    @Override
    public synchronized void reset() {
        state = ScoreEntity.initial();
    }
}
