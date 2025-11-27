package tetris.network;

import tetris.network.protocol.GameMessage;

// NetworkThread가 NetworkManager에게 콜백을 보낼 때 사용하는 인터페이스
// 기존의 NetThreadManager 역할을 대체
public interface INetworkThreadCallback {
    void handleReceivedMessage(GameMessage message);
    void handleConnectionEstablished();
    void handleConnectionLost();
    void handleLatencyWarning(long latency);
    void handleNetworkError(Exception error);
}