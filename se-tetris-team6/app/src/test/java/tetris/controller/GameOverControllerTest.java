package tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.domain.GameMode;
import tetris.domain.GameModel;
import tetris.domain.leaderboard.LeaderboardEntry;
import tetris.domain.leaderboard.LeaderboardRepository;
import tetris.domain.leaderboard.LeaderboardResult;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.view.GameComponent.GameOverPanel;
import tetris.view.TetrisFrame;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.controller.GameOverController
 *
 * 역할 요약:
 * - 게임 오버 시 리더보드를 불러와 패널에 렌더링하고, 이름 저장/스킵/메뉴 복귀 입력을 처리한다.
 *
 * 테스트 전략:
 * - 사용 라이브러리:
 *   - JUnit 5 (junit-jupiter)
 *   - Mockito로 ScoreRepository, LeaderboardRepository, GameOverPanel, TetrisFrame 등을 mock 하고,
 *     컨트롤러가 이들에게 기대한 메서드를 호출하는지 검증한다.
 *
 * - 설계 가정:
 *   - 컨트롤러는 생성자로 협력자를 주입받는다.
 *   - 패널 리스너는 생성자에서 등록되므로, 캡처 후 직접 호출해 흐름을 검증한다.
 *
 * - 테스트 방식:
 *   - given: mock 협력자 준비 후 컨트롤러 생성
 *   - when : show(), onSave/onSkip/onBackToMenu 리스너 호출
 *   - then : 리더보드 로드/저장, 패널 표시/숨김, 프레임 전환 호출을 verify한다.
 */
@ExtendWith(MockitoExtension.class)
class GameOverControllerTest {

    @Mock ScoreRepository scoreRepository;
    @Mock LeaderboardRepository leaderboardRepository;
    @Mock GameOverPanel panel;
    @Mock TetrisFrame frame;
    @Mock Score score;
    @Mock LeaderboardResult result;

    GameOverController controller;

    @BeforeEach
    void setup() {
        controller = new GameOverController(scoreRepository, leaderboardRepository, panel, frame);
    }

    @Test
    void show_loadsLeaderboardAndShowsPanel() {
        GameModel model = mock(GameModel.class);
        when(frame.getGameModel()).thenReturn(model);
        when(model.getLastMode()).thenReturn(GameMode.ITEM);
        List<LeaderboardEntry> entries = List.of(mock(LeaderboardEntry.class));
        when(leaderboardRepository.loadTop(10, GameMode.ITEM)).thenReturn(entries);

        controller.show(score, true);

        verify(leaderboardRepository).loadTop(10, GameMode.ITEM);
        verify(panel).renderLeaderboard(GameMode.ITEM, entries);
        verify(panel).show(score, true);
    }

    @Test
    void onSave_savesEntryAndShowsScoreboard() {
        GameModel model = mock(GameModel.class);
        when(frame.getGameModel()).thenReturn(model);
        when(model.getLastMode()).thenReturn(GameMode.ITEM);
        when(scoreRepository.load()).thenReturn(score);
        when(score.getPoints()).thenReturn(123);
        when(leaderboardRepository.saveAndHighlight(any())).thenReturn(result);

        ArgumentCaptor<GameOverPanel.Listener> captor = ArgumentCaptor.forClass(GameOverPanel.Listener.class);
        verify(panel).setListener(captor.capture());
        GameOverPanel.Listener listener = captor.getValue();

        listener.onSave("AAA");

        verify(scoreRepository).load();
        verify(leaderboardRepository).saveAndHighlight(any());
        verify(frame).setPendingLeaderboard(eq(GameMode.ITEM), eq(result));
        verify(panel).hidePanel();
        verify(frame).showScoreboardPanel();
    }

    @Test
    void onSave_blankName_skipsSave() {
        ArgumentCaptor<GameOverPanel.Listener> captor = ArgumentCaptor.forClass(GameOverPanel.Listener.class);
        verify(panel).setListener(captor.capture());
        GameOverPanel.Listener listener = captor.getValue();

        listener.onSave("   ");

        verify(scoreRepository, never()).save(any());
        verify(leaderboardRepository, never()).saveAndHighlight(any());
        verify(panel).hidePanel();
        verify(frame).showScoreboardPanel();
    }

    @Test
    void onBackToMenu_hidesPanelAndShowsMain() {
        ArgumentCaptor<GameOverPanel.Listener> captor = ArgumentCaptor.forClass(GameOverPanel.Listener.class);
        verify(panel).setListener(captor.capture());
        GameOverPanel.Listener listener = captor.getValue();

        listener.onBackToMenu();

        verify(panel).hidePanel();
        verify(frame).showMainPanel();
    }
}
