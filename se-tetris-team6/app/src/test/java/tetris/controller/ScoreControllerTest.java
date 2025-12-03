package tetris.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tetris.domain.score.Score;
import tetris.domain.score.ScoreRepository;
import tetris.domain.score.ScoreRuleEngine;
import tetris.view.ScoreView;

import static org.mockito.Mockito.*;

/*
 * 테스트 대상: tetris.controller.ScoreController
 *
 * 역할 요약:
 * - 점수 모델(ScoreRuleEngine/ScoreRepository)과 뷰(ScoreView)를 연결해 점수 변화를 렌더링한다.
 *
 * 테스트 전략:
 * - 사용 라이브러리:
 *   - JUnit 5 (junit-jupiter)
 *   - Mockito로 ScoreRepository, ScoreRuleEngine, ScoreView를 mock 하고, 기대 호출을 검증한다.
 *
 * - 설계 가정:
 *   - 생성 시 초기 점수를 로드해 렌더링하고, 룰 엔진 리스너를 등록한다.
 *   - resetScore()는 ruleEngine.resetScore()에 위임한다.
 *
 * - 테스트 방식:
 *   - given: mock 협력자 준비 후 컨트롤러 생성
 *   - when : 리스너 콜백 또는 resetScore 호출
 *   - then : view.renderScore, ruleEngine.resetScore 호출 여부 검증
 */
@ExtendWith(MockitoExtension.class)
class ScoreControllerTest {

    @Mock ScoreRepository repository;
    @Mock ScoreRuleEngine ruleEngine;
    @Mock ScoreView view;
    @Mock Score score;

    ScoreController controller;

    @BeforeEach
    void setUp() {
        when(repository.load()).thenReturn(score);
        controller = new ScoreController(repository, ruleEngine, view);
    }

    @Test
    void constructor_loadsAndRendersInitialScore() {
        verify(repository).load();
        verify(view).renderScore(score);
    }

    @Test
    void ruleEngineListener_rendersUpdatedScore() {
        ArgumentCaptor<java.util.function.Consumer<Score>> captor = ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(ruleEngine).addListener(captor.capture());

        Score newScore = mock(Score.class);
        captor.getValue().accept(newScore);

        verify(view).renderScore(newScore);
    }

    @Test
    void resetScore_delegatesToRuleEngine() {
        controller.resetScore();

        verify(ruleEngine).resetScore();
    }
}
