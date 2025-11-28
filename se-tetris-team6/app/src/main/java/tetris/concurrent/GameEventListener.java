package tetris.concurrent;

/**
 * Lightweight listener interface so {@link GameThread} can notify other
 * components (e.g. networking layer) without introducing hard dependencies.
 */
public interface GameEventListener {
    void onGameEvent(GameEvent event);
}
