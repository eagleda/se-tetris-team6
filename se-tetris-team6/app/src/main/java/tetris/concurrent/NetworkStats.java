package tetris.concurrent;

/** Simple DTO summarising networking telemetry so callers can display it. */
public final class NetworkStats {
    private final long messagesSent;
    private final long messagesReceived;
    private final long currentLatency;

    public NetworkStats(long messagesSent, long messagesReceived, long currentLatency) {
        this.messagesSent = messagesSent;
        this.messagesReceived = messagesReceived;
        this.currentLatency = currentLatency;
    }

    public long getMessagesSent() {
        return messagesSent;
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getCurrentLatency() {
        return currentLatency;
    }
}
