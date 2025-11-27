package tetris.multiplayer.model;

import java.util.Objects;
import tetris.domain.GameModel;

/**
 * 멀티플레이어 도메인에서 한 명의 플레이어를 표현하는 상태 객체.
 * - id: 1 또는 2 (플레이어 식별)
 * - model: 실제 게임 진행을 담당하는 {@link GameModel}
 * - local: 로컬 조작 여부 (P2P 확장 시 원격 플레이어 구분 용도)
 */
public final class PlayerState {

    private final int id;
    private final GameModel model;
    private final boolean local;
    private boolean ready;

    public PlayerState(int id, GameModel model, boolean local) {
        if (id != 1 && id != 2) {
            throw new IllegalArgumentException("player id must be 1 or 2: " + id);
        }
        this.id = id;
        this.model = Objects.requireNonNull(model, "model");
        this.local = local;
    }

    public int getId() {
        return id;
    }

    public GameModel getModel() {
        return model;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
