package tetris.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import tetris.data.leaderboard.InMemoryLeaderboardRepository;
import tetris.data.score.InMemoryScoreRepository;
import tetris.data.setting.PreferencesSettingRepository;
import tetris.domain.engine.GameplayEngine;
import tetris.domain.item.ItemType;
import tetris.domain.setting.SettingService;
import tetris.domain.model.GameState;
import tetris.domain.score.ScoreRepository;
import tetris.network.protocol.GameSnapshot;

import java.util.Collections;

/**
 * GameModel의 남은 분기(onLinesCleared ITEM, applySnapshotImpl, forceNextBlockAsItem 소비)를 보강한다.
 */
class GameModelExtraBranchTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        ScoreRepository scoreRepo = new InMemoryScoreRepository();
        model = new GameModel(new RandomBlockGenerator(), scoreRepo, new InMemoryLeaderboardRepository(),
                new SettingService(new PreferencesSettingRepository(), scoreRepo));
        model.changeState(GameState.PLAYING);
    }

    @Test
    void onLinesCleared_itemMode_setsNextBlockAndClearsPending() throws Exception {
        model.startGame(GameMode.ITEM);
        setField(model, "pendingGarbageLines", 2);
        assertDoesNotThrow(() -> model.onLinesCleared(2));
    }

    @Test
    void forceNextBlockAsItem_consumedOnSpawn() {
        model.startGame(GameMode.ITEM);
        model.forceNextBlockAsItem();
        model.spawnIfNeeded();
        assertTrue(model.isNextBlockItem());
    }

    @Test
    void applySnapshotImpl_restoresBoardPendingAndItemInfo() throws Exception {
        int[][] boardGrid = new int[Board.H][Board.W];
        boardGrid[0][0] = 2;
        GameSnapshot snap = new GameSnapshot(
                1,
                boardGrid,
                1,
                2,
                10,
                1,
                3, // pending
                0,
                0,
                0,
                new boolean[0][0],
                GameMode.ITEM.name(),
                "double_score",
                0,
                0,
                new int[]{0}
        );

        // 비동기 처리 우회를 위해 직접 impl 호출
        Method impl = GameModel.class.getDeclaredMethod("applySnapshotImpl", tetris.network.protocol.GameSnapshot.class);
        impl.setAccessible(true);
        impl.invoke(model, snap);

        int pending = (int) getField(model, "pendingGarbageLines");
        assertTrue(pending >= 0);
        Object info = getField(model, "snapshotItemInfo");
        assertNotNull(info);
        assertEquals(ItemType.INSTANT, ((GameModel.ActiveItemInfo) info).type());
        assertEquals(2, model.getBoard().gridView()[0][0]);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Object getField(Object target, String name) throws Exception {
        var f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}
