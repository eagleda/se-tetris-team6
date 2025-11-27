package tetris.network;

public interface NetworkEventListener {
    void onConnected();
    void onDisconnected();
    void onConnectionError(String error);
    void onLatencyWarning(long latency);
}
