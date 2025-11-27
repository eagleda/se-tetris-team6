package tetris.concurrent;

import java.util.Objects;

/**
 * Minimal value object used by {@link GameThread} to fan out gameplay events to
 * other systems (UI, networking, etc.). The implementation intentionally keeps
 * the surface small so that unfinished multiplayer features can compile while
 * still allowing future extensions without breaking callers.
 */
public final class GameEvent {
    /**
     * Coarse event categories. Additional payload data can be attached for
     * richer scenarios without changing the enum.
     */
    public enum Type {
        GENERIC,
        LINE_CLEAR,
        ATTACK_RECEIVED,
        GAME_OVER
    }

    private final Type type;
    private final Object payload;

    public GameEvent(Type type, Object payload) {
        this.type = Objects.requireNonNull(type, "type");
        this.payload = payload;
    }

    public Type getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }
}
