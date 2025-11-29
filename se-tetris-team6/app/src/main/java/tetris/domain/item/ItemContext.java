package tetris.domain.item;

import java.util.Map;

import tetris.domain.Board;

/**
 * 아이템이 게임 모델에 요청을 전달할 때 사용하는 경량 컨텍스트.
 * 실질적인 적용 여부는 GameModel에서 검증한다.
 */
public interface ItemContext {

    Board getBoard();

    void requestClearCells(int x, int y, int width, int height);

    void requestAddBlocks(int x, int y, int[][] cells);

    void applyScoreDelta(int points);

    void addGlobalBuff(String buffId, long durationTicks, Map<String, Object> meta);

    long currentTick();

    void spawnParticles(int x, int y, String type);

    void playSfx(String id);
}
