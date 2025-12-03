package tetris;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tetris.domain.GameModel;
import tetris.infrastructure.GameModelFactory;

/*
 * 테스트 대상: tetris.App (엔트리 포인트) / GameModelFactory.createDefault()
 *
 * 역할 요약:
 * - 기본 구성의 GameModel을 생성하고 UI(TetrisFrame)를 띄운다. 테스트에서는 UI를 띄우지 않고
 *   팩토리로 생성된 GameModel이 정상적으로 초기화되는지만 검증한다.
 *
 * 테스트 전략:
 * - createDefault()가 null이 아닌 GameModel을 반환하는지 확인.
 * - 점수 저장소/룰 엔진 등 핵심 구성 요소가 null 없이 생성되는지 간단 검증.
 */
class AppTest {

    @Test
    void createDefault_returnsInitializedGameModel() {
        GameModel model = GameModelFactory.createDefault();
        assertNotNull(model);
        assertNotNull(model.getScoreRepository());
        assertNotNull(model.getScoreEngine());
        assertNotNull(model.getBlockGenerator());
    }
}
