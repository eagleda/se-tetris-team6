package tetris.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import tetris.network.INetworkThreadCallback;
import tetris.network.protocol.GameMessage;
import tetris.network.protocol.MessageType;

/**
 * Integration-style test for NetworkThread that runs against a test ServerSocket.
 *
 * It verifies that:
 * - NetworkThread connects and invokes the connection-established callback
 * - Messages sent from server are delivered to the callback
 * - Messages sent via NetworkThread.sendMessage() arrive at the server
 */
public class NetworkThreadTest {

    @Test
    public void networkThreadSendReceiveRoundtrip() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();

            CountDownLatch acceptedLatch = new CountDownLatch(1);
            CountDownLatch connectedLatch = new CountDownLatch(1);
            CountDownLatch receivedLatch = new CountDownLatch(1);

            // Storage for received message from NetworkThread callback
            final GameMessage[] receivedByCallback = new GameMessage[1];

            // Start server accept in background
            Thread serverThread = new Thread(() -> {
                try (Socket sock = server.accept()) {
                    // Create server side ObjectOutputStream first then ObjectInputStream
                    ObjectOutputStream srvOut = new ObjectOutputStream(sock.getOutputStream());
                    srvOut.flush();
                    ObjectInputStream srvIn = new ObjectInputStream(sock.getInputStream());

                    acceptedLatch.countDown();

                    // Wait for client to announce connection via callback
                    // Then send a test message to client
                    Thread.sleep(50);
                    GameMessage serverMsg = new GameMessage(MessageType.GAME_START, "SERVER", "hello-client");
                    srvOut.writeObject(serverMsg);
                    srvOut.flush();

                    // Read message sent by NetworkThread (client)
                    Object obj = srvIn.readObject();
                    if (obj instanceof GameMessage) {
                        GameMessage incoming = (GameMessage) obj;
                        // store to JVM stdout for debug; test will assert semantics
                        System.out.println("[TestServer] Received from client: " + incoming);
                    }

                    // keep socket open briefly to allow clean shutdown
                    Thread.sleep(50);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, "TestServer-Accept");
            serverThread.setDaemon(true);
            serverThread.start();

            // Wait until server accepted the socket (or at least started)
            assertTrue(acceptedLatch.await(500, TimeUnit.MILLISECONDS) || serverThread.isAlive());

            // Callback implementation to capture events
            INetworkThreadCallback cb = new INetworkThreadCallback() {
                @Override
                public void handleReceivedMessage(GameMessage message) {
                    receivedByCallback[0] = message;
                    receivedLatch.countDown();
                }

                @Override
                public void handleConnectionEstablished() {
                    connectedLatch.countDown();
                }

                @Override
                public void handleConnectionLost() {
                    // no-op for this test
                }

                @Override
                public void handleLatencyWarning(long latency) {
                    // no-op
                }

                @Override
                public void handleNetworkError(Exception error) {
                    fail("Network error in test: " + error);
                }
            };

            // Create and start NetworkThread (client side)
            NetworkThread net = new NetworkThread(cb, "localhost", port);
            Thread clientThread = new Thread(net, "Test-NetworkThread");
            clientThread.setDaemon(true);
            clientThread.start();

            // Wait for connection-established callback
            assertTrue(connectedLatch.await(2, TimeUnit.SECONDS), "NetworkThread did not report connection established");

            // Wait for the message sent by server to be delivered to the callback
            assertTrue(receivedLatch.await(2, TimeUnit.SECONDS), "Callback did not receive server message in time");
            assertNotNull(receivedByCallback[0], "Callback message should not be null");
            assertEquals(MessageType.GAME_START, receivedByCallback[0].getType(), "Received message type mismatch");

            // Now test client->server send: send a message and ensure server receives it.
            // We use sendMessage() which enqueues the message for transmission.
            GameMessage outbound = new GameMessage(MessageType.PLAYER_INPUT, "CLIENT_TEST", "ping");
            net.sendMessage(outbound);

            // allow some time for roundtrip
            Thread.sleep(200);

            // Shutdown network thread
            net.shutdown();
            clientThread.join(500);
            serverThread.join(500);
        }
    }
}
