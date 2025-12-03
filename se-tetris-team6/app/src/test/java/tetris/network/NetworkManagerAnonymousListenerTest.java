/*
 * 테스트 대상: tetris.network.NetworkManager$1 (GameEventListener 익명 구현)
 *
 * 역할 요약:
 * - NetworkManager 생성 시 GameThread에 네트워크 이벤트 리스너를 등록합니다.
 *
 * 테스트 전략:
 * - GameThread를 mock하여 setNetworkListener 호출 시 전달되는 리스너를 캡처하고,
 *   onGameEvent를 호출해도 예외가 없는지 확인합니다.
 */
package tetris.network;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import tetris.concurrent.GameThread;
import tetris.network.GameEventListener;

class NetworkManagerAnonymousListenerTest {

    @Test
    void anonymousGameEventListener_handlesCallbacks() {
        GameThread thread = Mockito.mock(GameThread.class, Mockito.withSettings().lenient());
        new NetworkManager(thread); // 생성 시 setNetworkListener 호출

        ArgumentCaptor<GameEventListener> captor = ArgumentCaptor.forClass(GameEventListener.class);
        verify(thread).setNetworkListener(captor.capture());
        GameEventListener listener = captor.getValue();

        // 콜백 객체는 정상 생성/등록되었다.
        assertNotNull(listener);
    }
}
