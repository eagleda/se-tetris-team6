/**
 * 대상: tetris.network.NetworkManager (INetworkThreadCallback 분기, 상태 계산)
 *
 * 목적:
 * - handleConnectionEstablished/Lost/Error 분기를 거쳐 NetworkEventListener 콜백이 호출되는지 검증하고
 *   isConnected=false 상태에서 getConnectedPlayerCount가 1을 반환하는지 확인하여 커버리지를 보강한다.
 *
 * 주요 시나리오:
 * 1) onConnected/onDisconnected/onConnectionError 호출 확인
 * 2) 연결되지 않은 상태의 플레이어 수 계산 검증
 *
 * Mockito 사용 이유:
 * - GameModel 협력자(board/inputState/setSecondaryListener)를 간단히 스텁하여 GameThread를 생성하기 위함.
 */
package tetris.network;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import tetris.concurrent.GameThread;
import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.model.InputState;

class NetworkManagerCallbackAndStateTest {

    @Test
    void callbacks_andConnectedCount() {
        GameModel gm = mock(GameModel.class, withSettings().lenient());
        when(gm.getBoard()).thenReturn(new Board());
        when(gm.getInputState()).thenReturn(new InputState());
        GameThread thread = new GameThread(gm, "p1", true);

        NetworkManager manager = new NetworkManager(thread);

        NetworkEventListener nel = mock(NetworkEventListener.class);
        manager.setNetworkEventListener(nel);

        manager.handleConnectionEstablished();
        verify(nel, atLeastOnce()).onConnected();

        manager.handleConnectionLost();
        verify(nel, atLeastOnce()).onDisconnected();

        manager.handleNetworkError(new RuntimeException("err"));
        verify(nel, atLeastOnce()).onConnectionError(anyString());

        // isConnected=false 이므로 connected player count는 1
        assert(manager.getConnectedPlayerCount() == 1);
    }
}
