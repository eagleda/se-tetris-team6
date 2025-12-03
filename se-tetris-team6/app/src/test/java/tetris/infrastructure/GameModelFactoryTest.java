package tetris.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import tetris.domain.GameModel;
import tetris.domain.RandomBlockGenerator;

/*
 * 테스트 대상: tetris.infrastructure.GameModelFactory
 *
 * 역할 요약:
 * - GameModel과 그 의존성을 조립해 반환하는 컴포지션 루트 역할의 팩토리.
 * - 기본 생성(createDefault)과 시드 기반 생성(createWithSeed)을 제공한다.
 *
 * 테스트 전략:
 * - null이 아닌 GameModel을 반환하는지 확인.
 * - createWithSeed가 같은 시드로 동일한 블록 시퀀스를 제공하는지 간단히 검증(peekNext 비교).
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - createDefault_returnsGameModel_notNull
 * - createWithSeed_sameSeed_producesDeterministicNextBlock
 */
class GameModelFactoryTest {

    @Test
    void createDefault_returnsGameModel_notNull() {
        GameModel model = GameModelFactory.createDefault();
        assertNotNull(model);
        assertNotNull(model.getBoard());
        assertNotNull(model.getScoreEngine());
    }

    @Test
    void createWithSeed_sameSeed_producesDeterministicNextBlock() {
        GameModel a = GameModelFactory.createWithSeed(123L);
        GameModel b = GameModelFactory.createWithSeed(123L);

        assertTrue(a.getBlockGenerator() instanceof RandomBlockGenerator);
        assertTrue(b.getBlockGenerator() instanceof RandomBlockGenerator);

        // peekNext 결과가 동일해야 함
        assertEquals(a.getBlockGenerator().peekNext(), b.getBlockGenerator().peekNext());
    }
}
