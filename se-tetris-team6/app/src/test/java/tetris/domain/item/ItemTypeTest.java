package tetris.domain.item;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.domain.item.ItemType
 *
 * 역할 요약:
 * - 아이템의 범주(INSTANT/ACTIVE/PASSIVE/TIMED)를 열거형으로 정의한다.
 *
 * 테스트 전략:
 * - enum 값이 null이 아니고 기대 값 집합을 유지하는지 확인.
 * - valueOf가 동일 인스턴스를 반환하는지 검증.
 *
 * - 사용 라이브러리:
 *   - JUnit 5만 사용.
 *
 * 주요 테스트 시나리오 예시:
 * - values_nonNull_and_expectedOrder
 * - valueOf_returnsSameInstance
 */
class ItemTypeTest {

    @Test
    void values_nonNull_and_expectedOrder() {
        ItemType[] types = ItemType.values();
        assertArrayEquals(new ItemType[]{ItemType.INSTANT, ItemType.ACTIVE, ItemType.PASSIVE, ItemType.TIMED}, types);
        for (ItemType t : types) {
            assertNotNull(t);
        }
    }

    @Test
    void valueOf_returnsSameInstance() {
        assertSame(ItemType.ACTIVE, ItemType.valueOf("ACTIVE"));
    }
}
