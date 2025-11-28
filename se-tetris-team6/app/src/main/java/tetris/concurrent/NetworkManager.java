package tetris.concurrent;

import tetris.network.protocol.GameMessage;

/**
 * Minimal abstraction around the real networking implementation. The
 * multiplayer prototype only needs the ability to send/receive messages and to
 * query the connection state, so keeping the surface tiny makes it easy to stub
 * in tests.
 */
public interface NetworkManager {

    /** Attempts to send the given message. */
    boolean send(GameMessage message);

    /** Receives the next message if available, otherwise returns {@code null}. */
    GameMessage receive();

    /** @return {@code true} when an active connection exists. */
    boolean isConnected();

    /** Releases any underlying resources. */
    void close();
}
