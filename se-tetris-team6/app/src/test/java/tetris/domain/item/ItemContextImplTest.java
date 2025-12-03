package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tetris.domain.Board;
import tetris.domain.GameModel;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;

/*
 * 테스트 대상: tetris.domain.item.ItemContextImpl
 *
 * 역할 요약:
 * - 아이템 동작이 GameModel에 명령을 위임할 때 사용하는 기본 컨텍스트.
 * - 보드 조작/점수 수정/버프 추가/파티클·사운드 요청을 GameModel로 전달한다.
 *
 * 테스트 전략:
 * - 각 메서드가 GameModel의 대응 메서드를 호출하는지 검증한다.
 * - applyScoreDelta가 ScoreRepository를 통해 점수를 누적 저장하는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemContextImplTest {

    @Mock GameModel model;
    @Mock Board board;
    @Mock ScoreRepository repo;

    private ItemContextImpl ctx;

    @BeforeEach
    void setUp() {
        when(model.getBoard()).thenReturn(board);
        when(model.getScoreRepository()).thenReturn(repo);
        when(repo.load()).thenReturn(Score.of(10, 1, 0));
        ctx = new ItemContextImpl(model);
    }

    @Test
    void getBoard_delegatesToModel() {
        assertSame(board, ctx.getBoard());
    }

    @Test
    void requestClearCells_and_AddBlocks_delegate() {
        int[][] cells = new int[][] { { 1 } };
        ctx.requestClearCells(1, 2, 3, 4);
        ctx.requestAddBlocks(5, 6, cells);

        verify(model).clearBoardRegion(1, 2, 3, 4);
        verify(model).addBoardCells(5, 6, cells);
    }

    @Test
    void applyScoreDelta_loadsAndSavesAccumulatedScore() {
        ctx.applyScoreDelta(5); // 10 + 5
        ArgumentCaptor<Score> captor = ArgumentCaptor.forClass(Score.class);
        verify(repo).save(captor.capture());
        Score saved = captor.getValue();
        // points 15, level/clearedLines 그대로
        org.junit.jupiter.api.Assertions.assertEquals(15, saved.getPoints());
        org.junit.jupiter.api.Assertions.assertEquals(1, saved.getLevel());
        org.junit.jupiter.api.Assertions.assertEquals(0, saved.getClearedLines());
    }

    @Test
    void addGlobalBuff_and_effectRequests_delegate() {
        Map<String, Object> meta = Map.of("key", "v");

        ctx.addGlobalBuff("buff", 10L, meta);
        ctx.spawnParticles(1, 2, "p");
        ctx.playSfx("s");

        verify(model).addGlobalBuff("buff", 10L, Map.of("key", "v"));
        verify(model).spawnParticles(1, 2, "p");
        verify(model).playSfx("s");
    }
}
