package com.gathin.tvhome;

import org.junit.Test;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.junit.Assert.*;

public class AmbientSceneTest {
    @Test public void choicesStayInRange() {
        AmbientScene.Choice c = new AmbientScene.Choice(10, 300);
        assertEquals(50, c.size);
        assertEquals(150, c.intensity);
        assertEquals(new AmbientScene.Choice(150, 0), new AmbientScene.Choice(999, -20));
        assertNotEquals(new AmbientScene.Choice(110, 100), AmbientScene.Choice.DEFAULT);
    }

    @Test public void tonesRiseFromTheBackdropToTheAccent() {
        int[] accents = {0xFF0033, 0x9146FF, 0x1DB954, 0xF5C518, 0x8A8F98};
        for (int accent : accents) {
            for (float light : new float[]{0, 0.5f, 1}) {
                AmbientScene.Palette p = new AmbientScene.Palette(accent, light, 100);
                float back = luminance(p.background);
                float echo = luminance(p.tones[AppShapes.ECHO]);
                float main = luminance(p.tones[AppShapes.MAIN]);
                float accentTone = luminance(p.tones[AppShapes.ACCENT]);
                String at = Integer.toHexString(accent) + " at " + light;
                assertTrue(at, echo > back + 0.02f);
                assertTrue(at, main > echo + 0.02f);
                assertTrue(at, accentTone > main + 0.04f);
                for (int tone : p.tones) assertEquals(at, 0xFF, tone >>> 24);
            }
        }
    }

    @Test public void zeroIntensityHidesTheShapes() {
        AmbientScene.Palette p = new AmbientScene.Palette(0xFF0033, 1, 0);
        for (int tone : p.tones) assertEquals(p.background, tone);
        for (AmbientScene.Glow glow : p.glows) assertEquals(0, glow.color >>> 24);
    }

    @Test public void nightIsCoolerThanDay() {
        AmbientScene.Palette night = new AmbientScene.Palette(0x808080, 0, 100);
        AmbientScene.Palette day = new AmbientScene.Palette(0x808080, 1, 100);
        int n = night.tones[AppShapes.MAIN], d = day.tones[AppShapes.MAIN];
        assertTrue((n & 0xFF) - ((n >> 16) & 0xFF) > (d & 0xFF) - ((d >> 16) & 0xFF));
    }

    @Test public void daylightBlendsOnlyAroundSunriseAndSunset() {
        assertEquals(0, AmbientScene.daylight(0, 480, 1080), 0);
        assertEquals(0, AmbientScene.daylight(435, 480, 1080), 0);
        assertEquals(0.5f, AmbientScene.daylight(480, 480, 1080), 0);
        assertEquals(1, AmbientScene.daylight(525, 480, 1080), 0);
        assertEquals(1, AmbientScene.daylight(720, 480, 1080), 0);
        assertEquals(0.5f, AmbientScene.daylight(1080, 480, 1080), 0);
        assertEquals(0, AmbientScene.daylight(1125, 480, 1080), 0);
        for (int minute = 1; minute < 1440; minute++) {
            float change = Math.abs(AmbientScene.daylight(minute, 480, 1080)
                    - AmbientScene.daylight(minute - 1, 480, 1080));
            assertTrue("abrupt change at " + minute, change < 0.02f);
        }
    }

    @Test public void tilburgUsesSeasonAndDaylightSavingTime() {
        ZoneId zone = ZoneId.of("Europe/Amsterdam");
        int[] winter = Sun.riseAndSet(LocalDate.of(2026, 12, 21), 51.56, 5.09, zone);
        int[] summer = Sun.riseAndSet(LocalDate.of(2026, 6, 21), 51.56, 5.09, zone);
        assertTrue(winter[0] > 8 * 60 && winter[1] < 17 * 60);
        assertTrue(summer[0] < 6 * 60 && summer[1] > 21 * 60);
        assertEquals(0, AmbientScene.daylight(19 * 60, winter[0], winter[1]), 0);
        assertEquals(1, AmbientScene.daylight(19 * 60, summer[0], summer[1]), 0);
    }

    @Test public void invalidSunTimesUseTheReferenceDay() {
        assertEquals(AmbientScene.daylight(600, 450, 1140),
                AmbientScene.daylight(600, 1000, 300), 0);
    }

    private static float luminance(int c) {
        return (0.2126f * ((c >> 16) & 0xFF) + 0.7152f * ((c >> 8) & 0xFF) + 0.0722f * (c & 0xFF)) / 255;
    }
}
