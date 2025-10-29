package tetris.domain.score;

/**
 * 점수 상태를 로드/저장/초기화하는 저장소 추상화.
 */
public interface ScoreRepository {

    /**
     * 현재 점수 상태를 반환합니다.
     */
    Score load();

    /**
     * 점수 상태를 저장합니다.
     */
    void save(Score score);

    /**
     * 점수를 초기 상태로 리셋합니다.
     */
    void reset();
}
