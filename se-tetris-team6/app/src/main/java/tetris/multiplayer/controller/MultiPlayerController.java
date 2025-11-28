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
 * 두 개의 싱글용 {@link GameModel}을 멀티플레이 규칙과 연결하는 어댑터.
 * - 입력 처리는 외부에서 {@link #withPlayer(int, Consumer)}로 전달받는다.
 * - 블록 락/스폰 직전 이벤트는 GameModel 훅을 통해 자동으로 들어온다.
 */
public final class MultiPlayerController {

    private final MultiPlayerGame game;

    public MultiPlayerController(MultiPlayerGame game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    /**
     * 특정 플레이어의 GameModel을 외부에서 직접 조작할 수 있도록 감싸는 헬퍼.
     * 예: 입력 처리 로직이 블록 이동 메서드를 호출할 때 사용.
     */
    public void withPlayer(int playerId, Consumer<GameModel> action) {
        if (action == null) {
            return;
        }
        action.accept(game.modelOf(playerId));
    }

    /**
     * GameModel에서 전달해 준 블록 락 이벤트를 VersusRules에 위임한다.
     */
    public void onPieceLocked(int playerId,
                              LockedPieceSnapshot snapshot,
                              int[] clearedYs) {
        if (snapshot == null || clearedYs == null || clearedYs.length == 0) {
            return;
        }
        GameModel model = game.modelOf(playerId);
        int boardWidth = determineBoardWidth(model);
        game.onPieceLocked(playerId, snapshot, clearedYs, boardWidth);
    }

    public void tick() {
        game.modelOf(1).update();
        game.modelOf(2).update();
    }

    /**
     * 스폰 직전 호출되어 대기 중인 공격 줄을 보드에 실제로 주입한다.
     * 주입 이후 스폰 불가 상태라면 해당 플레이어 패배로 처리한다.
     */
    public void injectAttackBeforeNextSpawn(int playerId) {
        List<AttackLine> lines = game.takeAttackLinesForNextSpawn(playerId);
        if (lines.isEmpty()) {
            return;
        }
        GameModel model = game.modelOf(playerId);
        applyAttackLines(model, lines);
        if (!canSpawnNextPiece(model)) {
            game.markLoser(playerId);
        }
    }

    public int getPendingLines(int playerId) {
        return game.getPendingLines(playerId);
    }

    /**
     * 특정 플레이어가 받을 공격 줄의 실제 패턴을 반환한다.
     */
    public java.util.List<tetris.multiplayer.model.AttackLine> getPendingAttackLines(int playerId) {
        return game.getPendingAttackLines(playerId);
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

        // 기존 격자를 위로 밀어 올려 공격 줄이 들어갈 공간 확보.
        for (int y = 0; y < height - attackCount; y++) {
            System.arraycopy(current[y + attackCount], 0, next[y], 0, width);
        }
        int baseRow = height - attackCount;
        // 새로 추가되는 공격 줄은 공격자가 남긴 구멍(holes)을 그대로 복사한다.
        for (int index = 0; index < attackCount; index++) {
            AttackLine attack = lines.get(index);
            boolean[] holes = attack.copyHoles();
            int rowIndex = baseRow + index;
            for (int x = 0; x < width; x++) {
                next[rowIndex][x] = holes[x] ? 0 : 8; // 8 is an arbitrary grey; adjust if needed.
            }
        }

        // 계산된 격자를 실제 보드에 반영한다.
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

    /**
     * 공격 줄 주입 이후 다음 미노가 정상적으로 스폰 가능한지 미리 확인한다.
     * - GameModel에 활성 블록이 있으면 현재 턴은 스폰을 하지 않으므로 true 유지
     * - next 미노가 없으면 기본값으로 true
     * - 실제 스폰 위치(Board.W/2-1, 0)에서 충돌이 발생하면 false로 처리해 패배 판정
     */
    private static boolean canSpawnNextPiece(GameModel model) {
        if (model == null) {
            return true;
        }
        // 활성 블록이 이미 존재하면 이번 사이클에서 스폰이 일어나지 않는다.
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
}
