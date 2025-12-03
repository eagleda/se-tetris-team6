package tetris.view.palette;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Color;

import org.junit.jupiter.api.Test;

/*
 * 테스트 대상: tetris.view.palette.ColorPaletteProvider
 *
 * 역할 요약:
 * - 일반 팔레트와 색각 보정 팔레트를 제공하는 정적 유틸리티.
 *
 * 테스트 전략:
 * - 팔레트 길이와 일부 색상이 기대한 값인지 확인.
 * - colorBlindMode true/false에서 팔레트가 달라지는지 검증.
 */
class ColorPaletteProviderTest {

    @Test
    void paletteReturnsExpectedColors() {
        Color[] standard = ColorPaletteProvider.palette(false);
        Color[] colorBlind = ColorPaletteProvider.palette(true);

        assertEquals(8, standard.length);
        assertEquals(8, colorBlind.length);

        assertEquals(new Color(0, 240, 240), standard[1]); // I 블록 기본 팔레트
        assertNotEquals(standard[1], colorBlind[1]); // 색각 보정 시 다른 색상
    }
}
