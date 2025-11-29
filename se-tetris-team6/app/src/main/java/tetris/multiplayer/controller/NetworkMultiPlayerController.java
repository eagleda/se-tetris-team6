package tetris.multiplayer.controller;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import tetris.domain.Board;
import tetris.domain.BlockKind;
import tetris.domain.GameModel;
import tetris.domain.model.Block;
import tetris.multiplayer.model.AttackLine;
import tetris.multiplayer.model.LockedPieceSnapshot;
import tetris.multiplayer.model.MultiPlayerGame;

/**
 * 네트워크 멀티플레이어용 컨트롤러.
 * 원격 플레이어와의 동기화 및 네트워크 통신을 처리.
 */
public final class NetworkMultiPlayerController {

    private final MultiPlayerGame game;
    private final int localPlayerId;
    private NetworkEventHandler networkHandler;
    // optional transport attached to this controller for direct sending
    private tetris.network.client.GameClient transportClient;
    private tetris.network.server.GameServer transportServer;

    public NetworkMultiPlayerController(MultiPlayerGame game, int localPlayerId) {
        this.game = Objects.requireNonNull(game, "game");
        this.localPlayerId = localPlayerId;
    }

    public void setNetworkHandler(NetworkEventHandler handler) {
        this.networkHandler = handler;
    }

    /**
     * Attach network transport objects so this controller can directly send messages.
     * Passing a non-null client will also register the incoming listener automatically.
     */
    public void attachTransport(tetris.network.client.GameClient client, tetris.network.server.GameServer server) {
        this.transportClient = client;
        this.transportServer = server;
        if (client != null) attachClient(client);
    }

    /**
     * 로컬 플레이어의 GameModel만 조작 가능
     */
    public void withLocalPlayer(Consumer<GameModel> action) {
        if (action == null) {
            return;
        }
        action.accept(game.modelOf(localPlayerId));
    }

    /**
     * 로컬 플레이어의 블록 락 이벤트 처리 및 네트워크 전송
     */
    public void onLocalPieceLocked(LockedPieceSnapshot snapshot, int[] clearedYs) {
        if (snapshot == null || clearedYs == null || clearedYs.length == 0) {
            return;
        }
        GameModel model = game.modelOf(localPlayerId);
        int boardWidth = determineBoardWidth(model);
        game.onPieceLocked(localPlayerId, snapshot, clearedYs, boardWidth);
        
        // 네트워크로 이벤트 전송 (transport가 있으면 직접 전송)
        sendPieceLockedEvent(snapshot, clearedYs);
        // 라인이 클리어되었으면 즉시 게임 상태 동기화 (공격 대기열 포함)
        if (clearedYs.length > 0) {
            sendGameState(model);
        }
    }

    /**
     * 원격 플레이어의 블록 락 이벤트 수신 처리
     */
    public void onRemotePieceLocked(LockedPieceSnapshot snapshot, int[] clearedYs) {
        if (snapshot == null || clearedYs == null || clearedYs.length == 0) {
            return;
        }
        int remotePlayerId = getRemotePlayerId();
        GameModel model = game.modelOf(remotePlayerId);
        int boardWidth = determineBoardWidth(model);
        game.onPieceLocked(remotePlayerId, snapshot, clearedYs, boardWidth);
    }

    /**
     * 서버 권한 방식: 서버(P1)만 게임 로직 실행
     * - 서버: 두 플레이어 모두 update하고 두 플레이어의 스냅샷 전송
     * - 클라이언트: update 안 함, 키 입력만 서버에 전송
     */
    public void tick() {
        if (localPlayerId == 1) {
            GameModel p1 = game.modelOf(1);
            GameModel p2 = game.modelOf(2);
            if (p1 != null) p1.update();
            if (p2 != null) p2.update();
            if (networkHandler != null) {
                if (p1 != null && p1.getCurrentState() == tetris.domain.model.GameState.PLAYING) {
                    networkHandler.sendGameState(p1);
                }
                if (p2 != null && p2.getCurrentState() == tetris.domain.model.GameState.PLAYING) {
                    networkHandler.sendGameState(p2);
                }
            }
        } else {
            // 클라이언트: 아무것도 하지 않음
            // 서버로부터 받은 스냅샷으로만 화면 업데이트
        }
    }

    /**
     * 서버: 입력 처리 후 즉시 스냅샷 전송
     * 클라이언트: 입력은 이미 서버로 전송됨, 추가 동작 없음
     */
    public void onLocalInput() {
        if (localPlayerId == 1 && networkHandler != null) {
            GameModel p1 = game.modelOf(1);
            GameModel p2 = game.modelOf(2);
            if (p1 != null && p1.getCurrentState() == tetris.domain.model.GameState.PLAYING) networkHandler.sendGameState(p1);
            if (p2 != null && p2.getCurrentState() == tetris.domain.model.GameState.PLAYING) networkHandler.sendGameState(p2);
        }
        // 클라이언트는 아무것도 하지 않음 (키 입력은 이미 전송됨)
    }

    /**
     * 원격 플레이어 상태 업데이트 수신
     */
    public void updateRemotePlayerState(GameModel remoteState) {
        // 원격 플레이어 상태를 로컬 게임에 반영
        // 실제 구현에서는 상태 동기화 로직 필요
    }

    public void injectAttackBeforeNextSpawn(int playerId) {
        List<AttackLine> lines = game.takeAttackLinesForNextSpawn(playerId);
        if (lines.isEmpty()) {
            return;
        }
        GameModel model = game.modelOf(playerId);
        applyAttackLines(model, lines);
        
        // 공격 라인이 주입되었으므로 모든 플레이어가 즉시 상태 동기화
        if (playerId == localPlayerId) {
            sendGameState(model);
        }
        
        if (!canSpawnNextPiece(model)) {
            game.markLoser(playerId);
            
            // 게임 종료 이벤트 네트워크 전송
            if (playerId == localPlayerId) {
                sendGameOverEvent();
            }
        }
    }

    public int getPendingLines(int playerId) {
        return game.getPendingLines(playerId);
    }

    public List<AttackLine> getPendingAttackLines(int playerId) {
        return game.getPendingAttackLines(playerId);
    }

    private int getRemotePlayerId() {
        return localPlayerId == 1 ? 2 : 1;
    }

    private static int determineBoardWidth(GameModel model) {
        int[][] snapshot = model.getBoard().gridView();
        return snapshot.length == 0 ? Board.W : snapshot[0].length;
    }

    private static void applyAttackLines(GameModel model, List<AttackLine> lines) {
        Board board = model.getBoard();
        int[][] current = board.gridView();
        int height = current.length;
        if (height == 0) {
            return;
        }
        int width = current[0].length;
        int attackCount = Math.min(lines.size(), height);
        int[][] next = new int[height][width];

        for (int y = 0; y < height - attackCount; y++) {
            System.arraycopy(current[y + attackCount], 0, next[y], 0, width);
        }
        int baseRow = height - attackCount;
        for (int index = 0; index < attackCount; index++) {
            AttackLine attack = lines.get(index);
            boolean[] holes = attack.copyHoles();
            int rowIndex = baseRow + index;
            for (int x = 0; x < width; x++) {
                next[rowIndex][x] = holes[x] ? 0 : 8;
            }
        }

        board.clear();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = next[y][x];
                if (value > 0) {
                    board.setCell(x, y, value);
                }
            }
        }
    }

    private static boolean canSpawnNextPiece(GameModel model) {
        if (model == null) {
            return true;
        }
        if (model.getActiveBlock() != null) {
            return true;
        }
        BlockKind nextKind = model.getNextBlockKind();
        if (nextKind == null) {
            return true;
        }
        Board board = model.getBoard();
        Block candidate = Block.spawn(nextKind, Board.W / 2 - 1, 0);
        return board.canSpawn(candidate.getShape(), candidate.getX(), candidate.getY());
    }

    /**
     * 네트워크 이벤트 처리를 위한 인터페이스
     */
    public interface NetworkEventHandler {
        void sendPieceLockedEvent(LockedPieceSnapshot snapshot, int[] clearedYs);
        void sendGameState(GameModel gameState);
        void sendGameOverEvent();
    }

    /**
     * Apply a remote player's direct input to the corresponding GameModel.
     * This is used by the networking layer when a PLAYER_INPUT message arrives.
     */
    public void applyRemotePlayerInput(int playerId, tetris.network.protocol.PlayerInput input) {
        if (input == null) return;
        GameModel model = game.modelOf(playerId);
        if (model == null) return;
        switch (input.inputType()) {
            case MOVE_LEFT -> model.moveBlockLeft();
            case MOVE_RIGHT -> model.moveBlockRight();
            case SOFT_DROP -> model.moveBlockDown();
            case ROTATE -> model.rotateBlockClockwise();
            case ROTATE_CCW -> model.rotateBlockCounterClockwise();
            case HARD_DROP -> model.hardDropBlock();
            case HOLD -> model.holdCurrentBlock();
            default -> {}
        }
    }

    /**
     * Apply attack lines received from the network to the given player's model.
     * The GameModel currently exposes `applyAttackLines` that accepts network protocol lines.
     */
    public void applyRemoteAttackLines(int playerId, tetris.network.protocol.AttackLine[] lines) {
        if (lines == null || lines.length == 0) return;
        GameModel model = game.modelOf(playerId);
        if (model == null) return;
        model.applyAttackLines(lines);
    }

    /**
     * Apply an authoritative snapshot received from the network to a remote player's model.
     */
    /**
     * Apply an authoritative snapshot received from the network to the correct player's model.
     * Previously this always mapped every snapshot to getRemotePlayerId(), causing the client
     * to overwrite only the host board and never update its own board. We now respect the
     * snapshot's embedded playerId so both P1 and P2 snapshots update their respective models.
     */
    public void applyRemoteSnapshot(tetris.network.protocol.GameSnapshot snapshot) {
        if (snapshot == null) return;
        int targetPlayerId = snapshot.playerId();
        GameModel model = game.modelOf(targetPlayerId);
        if (model == null) return;
        model.applySnapshot(snapshot);
    }

    /* ----------------- Transport (sending) helpers ----------------- */
    public void sendPlayerInput(tetris.network.protocol.PlayerInput input) {
        if (input == null) return;
        try {
            if (transportClient != null) {
                transportClient.sendPlayerInput(input);
                return;
            }
            if (transportServer != null) {
                transportServer.sendHostMessage(new tetris.network.protocol.GameMessage(
                    tetris.network.protocol.MessageType.PLAYER_INPUT,
                    "Player-1",
                    input
                ));
            }
        } catch (Exception e) {
            System.err.println("Failed to send player input: " + e.getMessage());
        }
    }

    public void sendPieceLockedEvent(LockedPieceSnapshot snapshot, int[] clearedYs) {
        try {
            tetris.network.protocol.AttackLine[] attackLines = null;
            if (clearedYs != null && clearedYs.length > 0) {
                attackLines = new tetris.network.protocol.AttackLine[clearedYs.length];
                for (int i = 0; i < clearedYs.length; i++) attackLines[i] = new tetris.network.protocol.AttackLine(1);
            }

            if (attackLines != null && attackLines.length > 0) {
                tetris.network.protocol.GameMessage message = new tetris.network.protocol.GameMessage(
                    tetris.network.protocol.MessageType.ATTACK_LINES,
                    transportClient != null ? "CLIENT" : "SERVER",
                    attackLines
                );
                if (transportClient != null) transportClient.sendMessage(message);
                else if (transportServer != null) transportServer.sendHostMessage(message);
            }
        } catch (Exception e) {
            System.err.println("Failed to send piece locked event: " + e.getMessage());
        }
    }

    public void sendGameState(GameModel model) {
        if (model == null) return;
        try {
            int pid = localPlayerId;
            tetris.network.protocol.GameSnapshot snapshot = model.toSnapshot(pid);
            if (transportClient != null) {
                transportClient.sendGameStateSnapshot(snapshot);
            } else if (transportServer != null) {
                transportServer.broadcastGameStateSnapshot(snapshot);
            }
        } catch (Exception e) {
            System.err.println("Failed to send game state: " + e.getMessage());
        }
    }

    public void sendGameOverEvent() {
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            // prefer explicit winner from game if available
            Integer winner = game.getWinnerId();
            if (winner != null) data.put("winnerId", winner);
            tetris.network.protocol.GameMessage message = new tetris.network.protocol.GameMessage(
                tetris.network.protocol.MessageType.GAME_END,
                transportClient != null ? "CLIENT" : "SERVER",
                data
            );
            if (transportClient != null) transportClient.sendMessage(message);
            else if (transportServer != null) transportServer.sendHostMessage(message);
        } catch (Exception e) {
            System.err.println("Failed to send GAME_END message: " + e.getMessage());
        }
    }

    /**
     * Attach a network client so that incoming network messages are
     * forwarded into this controller (snapshots, inputs, attack lines, game end).
     */
    public void attachClient(tetris.network.client.GameClient client) {
        if (client == null) return;
        client.setGameStateListener(new tetris.network.client.GameStateListener() {
            @Override
            public void onOpponentBoardUpdate(tetris.network.protocol.GameMessage message) {
                // no-op; we use snapshots and higher-level messages
            }

            @Override
            public void onGameStateSnapshot(tetris.network.protocol.GameSnapshot snapshot) {
                if (snapshot == null) return;
                // Apply snapshot to the actual player indicated inside the snapshot (authoritative id)
                Runnable apply = () -> applyRemoteSnapshot(snapshot);
                if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                    javax.swing.SwingUtilities.invokeLater(apply);
                } else {
                    apply.run();
                }
            }

            @Override
            public void onGameStateChange(tetris.network.protocol.GameMessage message) {
                if (message == null) return;
                switch (message.getType()) {
                    case PLAYER_INPUT: {
                        Object payload = message.getPayload();
                        if (payload instanceof tetris.network.protocol.PlayerInput pi) {
                            applyRemotePlayerInput(getRemotePlayerId(), pi);
                        }
                        break;
                    }
                    case ATTACK_LINES: {
                        Object payload = message.getPayload();
                        if (payload instanceof tetris.network.protocol.AttackLine[] lines) {
                            applyRemoteAttackLines(getRemotePlayerId(), lines);
                        }
                        break;
                    }
                    case GAME_END: {
                        Runnable handle = () -> {
                            Object payloadObj = message.getPayload();
                            Integer winnerId = null;
                            if (payloadObj instanceof java.util.Map) {
                                Object winnerIdObj = ((java.util.Map<?, ?>) payloadObj).get("winnerId");
                                if (winnerIdObj instanceof Number) winnerId = ((Number) winnerIdObj).intValue();
                            }
                            if (winnerId != null) {
                                int loserId = (winnerId == 1) ? 2 : 1;
                                game.markLoser(loserId);
                            } else {
                                game.markLoser(getRemotePlayerId());
                            }

                            // Ensure both models transition to GAME_OVER so UI can react
                            GameModel localModel = game.modelOf(localPlayerId);
                            GameModel opponentModel = game.modelOf(getRemotePlayerId());
                            if (localModel != null && opponentModel != null) {
                                localModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                                opponentModel.changeState(tetris.domain.model.GameState.GAME_OVER);
                            }
                        };
                        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
                            javax.swing.SwingUtilities.invokeLater(handle);
                        } else {
                            handle.run();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        });
    }
}