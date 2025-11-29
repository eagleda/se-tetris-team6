package tetris.domain.item;

import java.util.HashMap;
import java.util.Map;

import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;

/**
 * 아이템이 GameModel에 명령을 전달할 때 사용하는 기본 컨텍스트.
 */
public class ItemContextImpl implements ItemContext {

    private final GameModel model;

    public ItemContextImpl(GameModel model) {
        this.model = model;
    }

    @Override
    public Board getBoard() {
        return model.getBoard();
    }

    @Override
    public void requestClearCells(int x, int y, int width, int height) {
        model.clearBoardRegion(x, y, width, height);
    }

    @Override
    public void requestAddBlocks(int x, int y, int[][] cells) {
        model.addBoardCells(x, y, cells);
    }

    @Override
    public void applyScoreDelta(int points) {
        if (points == 0) {
            return;
        }
        ScoreRepository repo = model.getScoreRepository();
        Score current = repo.load();
        repo.save(current.withAdditionalPoints(points));
    }

    @Override
    public void addGlobalBuff(String buffId, long durationTicks, Map<String, Object> meta) {
        model.addGlobalBuff(buffId, durationTicks, meta == null ? new HashMap<>() : new HashMap<>(meta));
    }

    @Override
    public long currentTick() {
        return model.getCurrentTick();
    }

    @Override
    public void spawnParticles(int x, int y, String type) {
        model.spawnParticles(x, y, type);
    }

    @Override
    public void playSfx(String id) {
        model.playSfx(id);
    }
}
