package com.gathin.tvhome;

/** Placement, colours and solar light of the focused app's shapes, independent of Android drawing and the clock. */
final class AmbientScene {
    static final String LIGHT_KEY = "ambient_light";
    /** GeometricGlow draws in this space and scales it to the screen (960×540 dp on a 1080p TV). */
    static final int W = 960, H = 540;
    /** Where AppShapes' unit box lands: right of the clock, above the hero name and the dock. */
    static final float CENTER_X = 700, CENTER_Y = 185, UNIT = 140;

    private AmbientScene() { }

    /** Pixels per AppShapes unit at a size choice. */
    static float unit(int size) {
        return UNIT * size / 100f;
    }

    static final class Choice {
        static final Choice DEFAULT = new Choice(100, 100);
        final int size, intensity;

        Choice(int size, int intensity) {
            this.size = Math.max(50, Math.min(150, size));
            this.intensity = Math.max(0, Math.min(150, intensity));
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Choice)) return false;
            Choice c = (Choice) other;
            return size == c.size && intensity == c.intensity;
        }

        @Override public int hashCode() { return size * 151 + intensity; }
    }

    /** A radial glow in the 960×540 space, its colour carrying the centre alpha. */
    static final class Glow {
        final float x, y, radius;
        final int color;

        Glow(float x, float y, float radius, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }
    }

    /**
     * Opaque colours for one app at one light level. Shapes are flat: each tone is the app's colour mixed
     * into the backdrop, so overlapping parts never blend into muddy in-between colours.
     */
    static final class Palette {
        final int background;
        /** Tone colours, indexed by AppShapes.ECHO, MAIN and ACCENT; `tops` are their slightly lighter upper edge. */
        final int[] tones = new int[3], tops = new int[3];
        final Glow[] glows;

        Palette(int accent, float light, int intensity) {
            float k = intensity / 100f;
            int opaque = 0xFF000000 | accent;
            int brand = lerp(lerp(opaque, 0xFF6679EF, 0.12f), lerp(opaque, 0xFFFFC995, 0.20f), light);
            int pale = lerp(brand, 0xFFFFFFFF, 0.35f);
            background = lerp(0xFF02040B, 0xFF100C10, light);
            tones[AppShapes.ECHO] = lerp(background, brand, 0.30f * k);
            tones[AppShapes.MAIN] = lerp(background, brand, 0.50f * k);
            tones[AppShapes.ACCENT] = lerp(background, pale, 0.68f * k);
            for (int i = 0; i < 3; i++) tops[i] = lerp(tones[i], 0xFFFFFFFF, 0.06f * k);
            glows = new Glow[]{
                    new Glow(CENTER_X, CENTER_Y, 420, alpha(brand, (75 + light * 37) * k)),
                    new Glow(880, 340, 340, alpha(pale, (20 + light * 25) * k)),
                    new Glow(180, 350, 310, alpha(brand, 22 * k)),
            };
        }
    }

    /** 0 at night, 1 by day, easing through 90 minutes centred on sunrise and sunset. */
    static float daylight(int minute, int sunrise, int sunset) {
        if (sunrise < 0 || sunset > 1440 || sunrise >= sunset) {
            sunrise = 450;
            sunset = 1140;
        }
        float rise = smooth((minute - sunrise + 45) / 90f);
        float set = 1 - smooth((minute - sunset + 45) / 90f);
        return Math.min(rise, set);
    }

    private static float smooth(float value) {
        float t = Math.max(0, Math.min(1, value));
        return t * t * (3 - 2 * t);
    }

    static int lerp(int from, int to, float t) {
        float f = Math.max(0f, Math.min(1f, t));
        int a = Math.round(((from >>> 24) & 0xFF) + (((to >>> 24) & 0xFF) - ((from >>> 24) & 0xFF)) * f);
        int r = Math.round(((from >> 16) & 0xFF) + (((to >> 16) & 0xFF) - ((from >> 16) & 0xFF)) * f);
        int g = Math.round(((from >> 8) & 0xFF) + (((to >> 8) & 0xFF) - ((from >> 8) & 0xFF)) * f);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int alpha(int color, float alpha) {
        return (Math.max(0, Math.min(255, Math.round(alpha))) << 24) | (color & 0x00FFFFFF);
    }
}
