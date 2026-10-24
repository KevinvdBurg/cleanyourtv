package com.gathin.tvhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One flat composition per app, built from a few primitives that follow the app's own mark: YouTube's
 * screen and play button, Spotify's arcs, the VLC cone. Pure Java, so the Android drawable and the JVM
 * preview draw exactly the same outlines.
 *
 * Unit space: the composition sits around (0, 0), y points down, and stays inside ±REACH_X by ±REACH_Y.
 * AmbientScene places that box right of the clock and above the hero name.
 */
final class AppShapes {
    static final float REACH_X = 1.7f, REACH_Y = 1.2f;
    static final int MOVE = 0, LINE = 1, CUBIC = 2, CLOSE = 3;
    /** Tones, from the colour of the backdrop up to the highlight. */
    static final int ECHO = 0, MAIN = 1, ACCENT = 2;
    /** How many plain compositions apps without a profile are spread over. */
    static final int FALLBACKS = 9;

    /** One filled shape: a tone and its outline commands, filled even-odd so inner outlines become holes. */
    static final class Part {
        final int tone;
        final float[] path;

        Part(int tone, float[] path) {
            this.tone = tone;
            this.path = path;
        }
    }

    private AppShapes() { }

    static List<Part> forPackage(String pkg) {
        Sketch s = new Sketch();
        switch (pkg == null ? "" : pkg) {
            case "com.google.android.youtube.tv": youtube(s); break;
            case "tv.twitch.android.app": twitch(s); break;
            case "com.stremio.one": stremio(s); break;
            case "com.collegehumor.chdropout": dropout(s); break;
            case "com.formulaone.production": f1(s); break;
            case "com.netflix.ninja": netflix(s); break;
            case "com.skyshowtime.skyshowtime.google": skyShowtime(s); break;
            case "nl.uitzendinggemist": npoStart(s); break;
            case "com.android.vending": playStore(s); break;
            case "com.spotify.tv.android": spotify(s); break;
            case "com.nordvpn.android": nordVpn(s); break;
            case "com.brouken.player": justPlayer(s); break;
            case "com.viaplay.android": viaplay(s); break;
            case "org.videolan.vlc": vlc(s); break;
            case "com.android.tv.settings": settings(s); break;
            case "com.ziggo.tv": ziggo(s); break;
            case "com.amazon.amazonvideo.livingroom": primeVideo(s); break;
            case "com.disney.disneyplus": disneyPlus(s); break;
            case "com.apple.atve.androidtv.appletv": case "com.apple.atve.sony.appletv": appleTv(s); break;
            case "com.wbd.stream": case "com.wbd.hbomax": hboMax(s); break;
            case "com.crunchyroll.crunchyroid": crunchyroll(s); break;
            case "com.mxtech.videoplayer.television": case "com.mxtech.videoplayer.ad": mxPlayer(s); break;
            case "com.plexapp.android": plex(s); break;
            case "org.xbmc.kodi": kodi(s); break;
            case "com.google.android.youtube.tvmusic": youtubeMusic(s); break;
            case "nl.rtl.videoland.v2": videoland(s); break;
            default: fallback(s, pkg == null ? 0 : Math.floorMod(pkg.hashCode(), FALLBACKS)); break;
        }
        return Collections.unmodifiableList(s.parts);
    }

    /** The screen and its play button, with the progress bar underneath. */
    private static void youtube(Sketch s) {
        s.add(MAIN, o().rect(-0.15f, -0.1f, 2.4f, 1.68f, 0.48f, 0));
        s.add(ACCENT, o().poly(0.1f, -0.45f, -0.52f, 0.38f, -0.1f, -0.45f, 0.32f));
        s.add(ECHO, o().rect(-0.15f, 1.0f, 2.4f, 0.12f, 0.06f, 0));
        s.add(ACCENT, o().rect(-0.8f, 1.0f, 1.1f, 0.12f, 0.06f, 0));
        s.add(ACCENT, o().circle(-0.25f, 1.0f, 0.13f));
    }

    /** The glitch bubble with its two eyes, answered by a smaller bubble. */
    private static void twitch(Sketch s) {
        float[] bubble = {6, 0, 1.714f, 4.286f, 1.714f, 19.714f, 6.857f, 19.714f, 6.857f, 24,
                11.143f, 19.714f, 14.571f, 19.714f, 22.286f, 12, 22.286f, 0};
        s.add(ECHO, o().poly(0.03f, icon(bubble, 0.55f, 0.95f, 0.55f)));
        s.add(MAIN, o().poly(0.05f, icon(bubble, 1.0f, -0.25f, 0)));
        s.add(ACCENT, o().rect(-0.22f, -0.38f, 0.15f, 0.43f, 0.03f, 0));
        s.add(ACCENT, o().rect(0.17f, -0.38f, 0.15f, 0.43f, 0.03f, 0));
    }

    /** The diamond with its play button, inside a thin diamond frame. */
    private static void stremio(Sketch s) {
        s.add(ECHO, o().rect(0, 0, 1.62f, 1.62f, 0.2f, 45).rect(0, 0, 1.46f, 1.46f, 0.16f, 45));
        s.add(MAIN, o().rect(0, 0, 1.2f, 1.2f, 0.2f, 45));
        s.add(ACCENT, o().poly(0.05f, -0.2f, -0.32f, 0.33f, 0, -0.2f, 0.32f));
    }

    /**
     * The :D mark in the app icon's proportions: a half-round bowl, a counter as open as the stem is wide,
     * colon squares as wide as the stem.
     */
    private static void dropout(Sketch s) {
        s.add(MAIN, o().moveTo(-0.49f, -0.95f).lineTo(0.34f, -0.95f).arcTo(0.34f, 0, 0.95f, -90, 180)
                .lineTo(-0.49f, 0.95f).close()
                .moveTo(0.06f, -0.5f).lineTo(0.23f, -0.5f).arcTo(0.23f, 0, 0.5f, -90, 180)
                .lineTo(0.06f, 0.5f).close());
        s.add(ACCENT, o().rect(-1.03f, -0.44f, 0.5f, 0.49f, 0.02f, 0));
        s.add(ACCENT, o().rect(-1.03f, 0.45f, 0.5f, 0.49f, 0.02f, 0));
    }

    /** The slanted F and 1, trailing speed lines. */
    private static void f1(Sketch s) {
        s.add(ECHO, o().slant(-1.55f, 0.66f, 0.45f, 0.76f, 0.8f));
        s.add(ECHO, o().slant(-1.15f, 0.9f, 0.05f, 1.0f, 0.8f));
        s.add(MAIN, o().slant(-1.15f, -0.6f, 0.95f, -0.18f, 0.8f));
        s.add(MAIN, o().slant(-1.3f, 0.0f, 0.15f, 0.42f, 0.8f));
        s.add(ACCENT, o().slant(1.12f, -0.6f, 1.52f, 0.42f, 0.8f));
    }

    /** The N, its diagonal in front, reaching past a wide screen. */
    private static void netflix(Sketch s) {
        s.add(ECHO, o().rect(0, 0, 2.9f, 1.4f, 0.18f, 0));
        s.add(MAIN, o().rect(-0.55f, 0, 0.42f, 2.2f, 0.03f, 0));
        s.add(MAIN, o().rect(0.55f, 0, 0.42f, 2.2f, 0.03f, 0));
        s.add(ACCENT, o().poly(0.02f, -0.76f, -1.1f, -0.34f, -1.1f, 0.76f, 1.1f, 0.34f, 1.1f));
    }

    /** A sun on the horizon under a spotlight arc, with a star above. */
    private static void skyShowtime(Sketch s) {
        s.add(ECHO, o().band(0, 0.95f, 1.4f, 0.16f, 180, 180, false));
        s.add(MAIN, o().moveTo(-1.0f, 0.95f).arcTo(0, 0.95f, 1.0f, 180, 180).close());
        s.add(ACCENT, o().star(0, -0.78f, 0.36f, 0.1f, 4, 0.02f));
    }

    /** Stacked episode tiles, the play button breaking out of the front one. */
    private static void npoStart(Sketch s) {
        s.add(ECHO, o().rect(-0.42f, -0.3f, 1.8f, 1.36f, 0.3f, 0));
        s.add(MAIN, o().rect(0.05f, 0.18f, 1.8f, 1.36f, 0.3f, 0));
        s.add(ACCENT, o().poly(0.08f, 0.45f, -0.38f, 1.38f, 0.18f, 0.45f, 0.74f));
    }

    /** The Play triangle in three facets. */
    private static void playStore(Sketch s) {
        float tx = 1.1f, ax = -0.85f, ay = 1.1f, cx = -0.12f;
        s.add(MAIN, o().poly(0.06f, inset(0.07f, ax, -ay, cx, 0, ax, ay)));
        s.add(ACCENT, o().poly(0.06f, inset(0.07f, ax, -ay, tx, 0, cx, 0)));
        s.add(ECHO, o().poly(0.06f, inset(0.07f, cx, 0, tx, 0, ax, ay)));
    }

    /** The disc with its three arcs, inside a ring. */
    private static void spotify(Sketch s) {
        s.add(ECHO, o().circle(0, 0, 1.2f).circle(0, 0, 1.12f));
        s.add(MAIN, o().circle(0, 0, 1.0f));
        float[][] arcs = {{2.48f, 0.2f, 15.5f}, {2.1f, 0.17f, 14.8f}, {1.76f, 0.14f, 13.6f}};
        for (float[] a : arcs) s.add(ECHO, o().band(0, 2.1f, a[0], a[1], -90 - a[2], 2 * a[2], true));
    }

    /** Two peaks inside a disc, snow on the high one. */
    private static void nordVpn(Sketch s) {
        s.add(ECHO, o().circle(0, 0, 1.08f));
        s.add(MAIN, o().poly(0.04f, -0.86f, 0.58f, -0.22f, -0.74f, 0.12f, -0.14f, 0.38f, -0.48f, 0.86f, 0.58f));
        s.add(ACCENT, o().poly(0.02f, -0.22f, -0.74f, -0.03f, -0.36f, -0.4f, -0.36f));
    }

    /** Play inside play, on a disc. */
    private static void justPlayer(Sketch s) {
        s.add(ECHO, o().circle(0, 0, 1.08f));
        s.add(MAIN, o().poly(0.12f, -0.42f, -0.74f, 0.8f, 0, -0.42f, 0.74f));
        s.add(ACCENT, o().poly(0.04f, -0.16f, -0.3f, 0.34f, 0, -0.16f, 0.3f));
    }

    /** A crescent holding a play button. */
    private static void viaplay(Sketch s) {
        s.add(ECHO, o().circle(-0.98f, -0.75f, 0.16f));
        s.add(MAIN, o().crescent(0, 0, 1.08f, 0.5f, -0.3f, 0.82f));
        s.add(ACCENT, o().poly(0.05f, 0.24f, -0.62f, 0.86f, -0.27f, 0.24f, 0.08f));
    }

    /**
     * The player that plays everything: VLC's striped cone works as a funnel, any file goes in at the wide
     * end and it points on as a play button. The small shapes stand for formats in general. No triangle among
     * them: square, circle and triangle together read as PlayStation buttons.
     */
    private static void vlc(Sketch s) {
        s.add(MAIN, o().poly(new float[]{0.03f, 0.18f, 0.03f}, CONE_FOOT, -0.9f, PLAY_TIP, 0, CONE_FOOT, 0.9f));
        for (float[] stripe : new float[][]{{-0.32f, -0.12f}, {0.3f, 0.47f}}) {
            float h0 = playHeight(stripe[0]) + 0.01f, h1 = playHeight(stripe[1]) + 0.01f;
            s.add(ECHO, o().poly(0, stripe[0], -h0, stripe[1], -h1, stripe[1], h1, stripe[0], h0));
        }
        s.add(ACCENT, o().rect(-1.32f, -0.5f, 0.34f, 0.34f, 0.07f, 14));
        s.add(ACCENT, o().circle(-1.44f, 0.06f, 0.18f));
        s.add(ACCENT, o().rect(-1.3f, 0.6f, 0.44f, 0.2f, 0.1f, -12));
    }

    private static final float CONE_FOOT = -0.85f, PLAY_TIP = 1.2f;

    /** Half the height of VLC's cone at x, between its open end and its tip. */
    private static float playHeight(float x) {
        return 0.9f * (PLAY_TIP - x) / (PLAY_TIP - CONE_FOOT);
    }

    /** A gear with a hub, meshing with a small one. */
    private static void settings(Sketch s) {
        s.add(ECHO, o().gear(1.1f, 0.66f, 0.46f, 0.36f, 8).circle(1.1f, 0.66f, 0.16f));
        s.add(MAIN, o().gear(-0.1f, -0.1f, 1.0f, 0.8f, 9).circle(-0.1f, -0.1f, 0.42f));
        s.add(ACCENT, o().circle(-0.1f, -0.1f, 0.2f));
    }

    /**
     * Live TV and on demand, in Ziggo's soft style: a screen framed by one thick rounded stroke with a play
     * button, a round live dot on its corner, and a second channel behind it.
     */
    private static void ziggo(Sketch s) {
        s.add(ECHO, o().rect(-0.32f, -0.2f, 2.1f, 1.44f, 0.42f, 0));
        // The screen's own surface, so the channel behind does not show through the frame.
        s.add(ECHO, o().rect(0, 0.15f, 1.84f, 1.14f, 0.28f, 0));
        s.add(MAIN, o().rect(0, 0.15f, 2.2f, 1.5f, 0.44f, 0).rect(0, 0.15f, 1.8f, 1.1f, 0.26f, 0));
        s.add(ACCENT, o().poly(0.1f, -0.22f, -0.2f, 0.4f, 0.15f, -0.22f, 0.5f));
        s.add(ACCENT, o().circle(0.97f, -0.47f, 0.17f));
    }

    /** Amazon's smile, arrow and all, under a screen with a play button cut into it. */
    private static void primeVideo(Sketch s) {
        s.add(MAIN, o().rect(-0.1f, -0.25f, 2.3f, 1.35f, 0.2f, 0));
        s.add(ECHO, o().poly(0.06f, -0.38f, -0.62f, 0.3f, -0.27f, -0.38f, 0.08f));
        s.add(ACCENT, o().lens(-1.2f, 0.35f, 0.95f, 0.3f, 0.45f, -0.25f));
        s.add(ACCENT, o().poly(0.03f, 0.77f, 0.19f, 1.18f, 0.07f, 1.06f, 0.48f));
    }

    /** The arc of light sweeping over the plus, a glint where it starts and a fainter arc beyond. */
    private static void disneyPlus(Sketch s) {
        s.add(ECHO, o().lens(-1.6f, 0.6f, 1.5f, 0.15f, -1.3f, 1.24f));
        s.add(MAIN, o().lens(-1.45f, 0.75f, 1.35f, 0.4f, -1.25f, 1.05f));
        s.add(ACCENT, o().plus(0.25f, 0.38f, 0.48f, 0.15f));
        s.add(ACCENT, o().star(-1.42f, 0.72f, 0.24f, 0.06f, 4, 0.01f));
    }

    /** A screen in Apple's soft corners with a sheen of glass, the leaf on top turning it into the apple. */
    private static void appleTv(Sketch s) {
        s.add(MAIN, o().rect(-0.1f, 0.1f, 2.5f, 1.5f, 0.42f, 0));
        s.add(ACCENT, o().poly(0.03f, -0.62f, -0.5f, -0.3f, -0.5f, -0.86f, 0.7f, -1.18f, 0.7f));
        s.add(ACCENT, o().poly(0.02f, -0.12f, -0.5f, -0.02f, -0.5f, -0.58f, 0.7f, -0.68f, 0.7f));
        s.add(ACCENT, o().lens(0.15f, -0.7f, 0.52f, -1.07f, 0.13f, 0.13f));
    }

    /** HBO's ring around a dot, broadcast bars running behind it: it's not TV. */
    private static void hboMax(Sketch s) {
        s.add(ECHO, o().rect(-0.4f, -0.48f, 2.5f, 0.18f, 0.09f, 0));
        s.add(ECHO, o().rect(0.1f, 0, 3.1f, 0.18f, 0.09f, 0));
        s.add(ECHO, o().rect(-0.2f, 0.48f, 2.2f, 0.18f, 0.09f, 0));
        s.add(MAIN, o().circle(0, 0, 0.92f).circle(0, 0, 0.62f));
        s.add(ACCENT, o().circle(0, 0, 0.26f));
    }

    /** The open ring with a disc inside it, and the sparkles of an anime title card. */
    private static void crunchyroll(Sketch s) {
        s.add(MAIN, o().crescent(0, 0, 1.0f, 0.3f, -0.2f, 0.68f));
        s.add(ACCENT, o().circle(0.38f, -0.26f, 0.42f));
        s.add(ECHO, o().star(-1.25f, -0.7f, 0.32f, 0.08f, 4, 0.02f));
        s.add(ECHO, o().star(1.3f, 0.75f, 0.2f, 0.05f, 4, 0.01f));
    }

    /**
     * The player people install for what the stock one cannot do: a play button with subtitles under it and the
     * swipe slider its gestures are known for.
     */
    private static void mxPlayer(Sketch s) {
        s.add(MAIN, o().poly(0.14f, -0.95f, -1.0f, 0.35f, -0.25f, -0.95f, 0.5f));
        s.add(ACCENT, o().rect(-0.5f, 0.78f, 1.5f, 0.14f, 0.07f, 0));
        s.add(ACCENT, o().rect(-0.5f, 1.02f, 0.9f, 0.14f, 0.07f, 0));
        s.add(ECHO, o().rect(0.95f, 0.1f, 0.16f, 1.4f, 0.08f, 0));
        s.add(MAIN, o().circle(0.95f, -0.15f, 0.17f));
    }

    /** Your own shelf of films, one picked, and Plex's chevron sending it on to the screen. */
    private static void plex(Sketch s) {
        s.add(ECHO, o().rect(-1.45f, 0.15f, 0.26f, 1.5f, 0.05f, 0));
        s.add(ECHO, o().rect(-1.12f, 0.03f, 0.26f, 1.75f, 0.05f, 0));
        s.add(ACCENT, o().rect(-0.79f, 0.1f, 0.26f, 1.6f, 0.05f, 0));
        s.add(ECHO, o().rect(-0.39f, 0.22f, 0.26f, 1.4f, 0.05f, 14));
        s.add(MAIN, o().poly(0.06f, 0.15f, -1.0f, 0.65f, -1.0f, 1.55f, 0, 0.65f, 1.0f, 0.15f, 1.0f, 1.05f, 0));
    }

    /** Kodi's four tiles, one sliding out of its slot like an add-on being plugged in. */
    private static void kodi(Sketch s) {
        float cx = -0.15f, cy = 0.05f, d = 0.55f, side = 0.66f;
        s.add(ECHO, o().rect(cx + d, cy, side, side, 0.12f, 45));
        s.add(MAIN, o().rect(cx, cy - d, side, side, 0.12f, 45));
        s.add(MAIN, o().rect(cx - d, cy, side, side, 0.12f, 45));
        s.add(MAIN, o().rect(cx, cy + d, side, side, 0.12f, 45));
        s.add(ACCENT, o().rect(cx + d + 0.3f, cy, side, side, 0.12f, 45));
    }

    /** The disc with a ring and play button inside, next to an equaliser. */
    private static void youtubeMusic(Sketch s) {
        s.add(MAIN, o().circle(-0.3f, 0, 1.0f));
        s.add(ACCENT, o().circle(-0.3f, 0, 0.6f).circle(-0.3f, 0, 0.47f));
        s.add(ACCENT, o().poly(0.05f, -0.48f, -0.27f, -0.04f, 0, -0.48f, 0.27f));
        s.add(MAIN, o().rect(1.0f, 0, 0.16f, 0.7f, 0.08f, 0));
        s.add(MAIN, o().rect(1.25f, 0, 0.16f, 1.2f, 0.08f, 0));
        s.add(MAIN, o().rect(1.5f, 0, 0.16f, 0.55f, 0.08f, 0));
    }

    /**
     * Videoland started as a chain of Dutch video rental shops before RTL turned the name into its streaming
     * service: a stack of rental tapes, reels showing in the window.
     */
    private static void videoland(Sketch s) {
        s.add(ECHO, o().rect(-0.35f, -0.35f, 2.3f, 1.4f, 0.16f, 0));
        s.add(MAIN, o().rect(0.15f, 0.1f, 2.3f, 1.4f, 0.16f, 0));
        s.add(ECHO, o().rect(0.15f, 0, 1.3f, 0.56f, 0.1f, 0));
        s.add(ACCENT, o().circle(-0.2f, 0, 0.22f).circle(-0.2f, 0, 0.09f));
        s.add(ACCENT, o().circle(0.5f, 0, 0.22f).circle(0.5f, 0, 0.09f));
    }

    /** Apps without a profile: one of the plain compositions, the same one for a package every time. */
    private static void fallback(Sketch s, int which) {
        switch (which) {
            case 0:
                s.add(ECHO, o().rect(0.38f, 0.2f, 1.6f, 1.6f, 0.3f, 0));
                s.add(MAIN, o().circle(-0.25f, -0.1f, 0.85f));
                s.add(ACCENT, o().circle(0.62f, -0.7f, 0.22f));
                break;
            case 1:
                s.add(ECHO, o().band(0, 0.1f, 1.08f, 0.14f, 180, 180, true));
                s.add(MAIN, o().moveTo(-0.8f, 1.05f).lineTo(-0.8f, 0.1f).arcTo(0, 0.1f, 0.8f, 180, 180)
                        .lineTo(0.8f, 1.05f).close());
                s.add(ACCENT, o().circle(0, 0.3f, 0.22f));
                break;
            case 2:
                s.add(ECHO, o().circle(0, 0, 1.12f).circle(0, 0, 0.94f));
                s.add(MAIN, o().poly(0.12f, 0, -0.82f, 0.78f, 0.56f, -0.78f, 0.56f));
                s.add(ACCENT, o().rect(0, 0.18f, 0.34f, 0.34f, 0.05f, 45));
                break;
            case 3:
                // Two half discs, one rising and one setting.
                s.add(ECHO, o().moveTo(-0.5f, -0.1f).arcTo(0.35f, -0.1f, 0.85f, 180, 180).close());
                s.add(MAIN, o().moveTo(-1.2f, 0.1f).arcTo(-0.35f, 0.1f, 0.85f, 180, -180).close());
                s.add(ACCENT, o().circle(0.85f, 0.42f, 0.18f));
                break;
            case 4:
                // A pinwheel of quarter discs around one point, two large and two small.
                s.add(MAIN, o().quarter(-0.05f, 0, 0.95f, 180));
                s.add(ECHO, o().quarter(0.05f, 0, 0.62f, 270));
                s.add(MAIN, o().quarter(0.05f, 0.1f, 0.95f, 0));
                s.add(ACCENT, o().quarter(-0.05f, 0.1f, 0.62f, 90));
                break;
            case 5:
                // Two overlapping discs, the overlap lit.
                s.add(ECHO, o().circle(-0.45f, 0, 0.85f));
                s.add(MAIN, o().circle(0.45f, 0, 0.85f));
                s.add(ACCENT, o().lens(0, -0.721f, 0, 0.721f, 0.4f, 0.4f));
                break;
            case 6:
                // Two tilted capsules and a dot.
                s.add(ECHO, o().rect(-0.35f, -0.25f, 2.2f, 0.5f, 0.25f, -25));
                s.add(MAIN, o().rect(0.05f, 0.3f, 2.4f, 0.62f, 0.31f, -25));
                s.add(ACCENT, o().circle(-1.05f, 0.75f, 0.2f));
                break;
            case 7:
                // A hexagon inside a hexagon frame, with a dot.
                s.add(ECHO, o().regular(0, 0, 1.15f, 6, 0.1f).regular(0, 0, 0.99f, 6, 0.08f));
                s.add(MAIN, o().regular(0, 0, 0.78f, 6, 0.1f));
                s.add(ACCENT, o().circle(0, 0, 0.22f));
                break;
            default:
                // A striped sun going down on the horizon, a star above.
                Outline sun = o().slice(0, -0.05f, 1.0f, -1.05f, 0.05f);
                for (float[] stripe : new float[][]{{0.15f, 0.35f}, {0.45f, 0.6f}, {0.7f, 0.8f}, {0.88f, 0.93f}}) {
                    sun.slice(0, -0.05f, 1.0f, stripe[0], stripe[1]);
                }
                s.add(MAIN, sun);
                s.add(ECHO, o().rect(0, 1.03f, 3.0f, 0.08f, 0.04f, 0));
                s.add(ACCENT, o().circle(-1.25f, -0.55f, 0.13f));
                break;
        }
    }

    /** Simple Icons coordinates (a 24 by 24 grid) moved into unit space. */
    private static float[] icon(float[] grid, float scale, float x, float y) {
        float[] out = new float[grid.length];
        for (int i = 0; i < grid.length; i += 2) {
            out[i] = x + (grid[i] - 12) / 12 * scale;
            out[i + 1] = y + (grid[i + 1] - 12) / 12 * scale;
        }
        return out;
    }

    /** A triangle pulled towards its centre, leaving a gap between neighbouring facets. */
    private static float[] inset(float by, float... p) {
        float cx = (p[0] + p[2] + p[4]) / 3, cy = (p[1] + p[3] + p[5]) / 3;
        float[] out = new float[6];
        for (int i = 0; i < 6; i += 2) {
            float dx = p[i] - cx, dy = p[i + 1] - cy, d = (float) Math.hypot(dx, dy);
            out[i] = p[i] - dx / d * by;
            out[i + 1] = p[i + 1] - dy / d * by;
        }
        return out;
    }

    private static Outline o() { return new Outline(); }

    private static final class Sketch {
        final List<Part> parts = new ArrayList<>();

        void add(int tone, Outline outline) { parts.add(new Part(tone, outline.toArray())); }
    }

    /** Builds outline commands: lines and cubic curves only, so both renderers replay them one to one. */
    static final class Outline {
        /** Control distance for a cubic that follows a quarter circle. */
        private static final float K = 0.5523f;
        private float[] d = new float[64];
        private int n;

        private Outline put(float... values) {
            if (n + values.length > d.length) d = java.util.Arrays.copyOf(d, Math.max(d.length * 2, n + values.length));
            System.arraycopy(values, 0, d, n, values.length);
            n += values.length;
            return this;
        }

        float[] toArray() { return java.util.Arrays.copyOf(d, n); }

        Outline moveTo(float x, float y) { return put(MOVE, x, y); }
        Outline lineTo(float x, float y) { return put(LINE, x, y); }
        Outline close() { return put(CLOSE); }

        Outline cubicTo(float x1, float y1, float x2, float y2, float x, float y) {
            return put(CUBIC, x1, y1, x2, y2, x, y);
        }

        /** Continues along a circle from the current point, which must lie on it at startDeg. */
        Outline arcTo(float cx, float cy, float r, float startDeg, float sweepDeg) {
            int segments = Math.max(1, (int) Math.ceil(Math.abs(sweepDeg) / 90 - 1e-4));
            double step = Math.toRadians(sweepDeg / segments), a = Math.toRadians(startDeg);
            float k = (float) (4.0 / 3 * Math.tan(step / 4)) * r;
            for (int i = 0; i < segments; i++) {
                double b = a + step;
                float x0 = cx + r * (float) Math.cos(a), y0 = cy + r * (float) Math.sin(a);
                float x1 = cx + r * (float) Math.cos(b), y1 = cy + r * (float) Math.sin(b);
                cubicTo(x0 - k * (float) Math.sin(a), y0 + k * (float) Math.cos(a),
                        x1 + k * (float) Math.sin(b), y1 - k * (float) Math.cos(b), x1, y1);
                a = b;
            }
            return this;
        }

        Outline circle(float cx, float cy, float r) {
            return moveTo(cx + r, cy).arcTo(cx, cy, r, 0, 360).close();
        }

        /** A polygon whose corners are rounded by up to `round`, never more than half the shorter edge. */
        Outline poly(float round, float... p) {
            float[] radii = new float[p.length / 2];
            java.util.Arrays.fill(radii, round);
            return poly(radii, p);
        }

        /** A polygon with its own rounding at each corner. */
        Outline poly(float[] radii, float... p) {
            int count = p.length / 2;
            for (int i = 0; i < count; i++) {
                float round = radii[i];
                int prev = (i + count - 1) % count, next = (i + 1) % count;
                float vx = p[2 * i], vy = p[2 * i + 1];
                float px = p[2 * prev] - vx, py = p[2 * prev + 1] - vy;
                float qx = p[2 * next] - vx, qy = p[2 * next + 1] - vy;
                float lp = (float) Math.hypot(px, py), lq = (float) Math.hypot(qx, qy);
                // A repeated point (a cone's tip, say) has no edge to round along.
                float r = lp > 1e-4f && lq > 1e-4f ? Math.min(round, Math.min(lp, lq) / 2) : 0;
                float ax = r > 0 ? vx + px / lp * r : vx, ay = r > 0 ? vy + py / lp * r : vy;
                float bx = r > 0 ? vx + qx / lq * r : vx, by = r > 0 ? vy + qy / lq * r : vy;
                if (i == 0) moveTo(ax, ay); else lineTo(ax, ay);
                if (r > 0) cubicTo(ax + (vx - ax) * K, ay + (vy - ay) * K, bx + (vx - bx) * K, by + (vy - by) * K, bx, by);
            }
            return close();
        }

        Outline rect(float cx, float cy, float w, float h, float round, float deg) {
            double a = Math.toRadians(deg);
            float c = (float) Math.cos(a), s = (float) Math.sin(a);
            float[] corners = {-w / 2, -h / 2, w / 2, -h / 2, w / 2, h / 2, -w / 2, h / 2};
            float[] p = new float[8];
            for (int i = 0; i < 8; i += 2) {
                p[i] = cx + corners[i] * c - corners[i + 1] * s;
                p[i + 1] = cy + corners[i] * s + corners[i + 1] * c;
            }
            return poly(round, p);
        }

        /** A bar from x0 to x1 on its top edge, leaning right by `lean` per unit of height. */
        Outline slant(float x0, float y0, float x1, float y1, float lean) {
            float shift = lean * (y1 - y0);
            return poly(0.02f, x0, y0, x1, y0, x1 - shift, y1, x0 - shift, y1);
        }

        /** A thick arc of radius r, `width` wide, with round or square ends. */
        Outline band(float cx, float cy, float r, float width, float startDeg, float sweepDeg, boolean roundEnds) {
            float outer = r + width / 2, inner = r - width / 2, end = startDeg + sweepDeg;
            float turn = Math.signum(sweepDeg) * 180;
            moveTo(px(cx, outer, startDeg), py(cy, outer, startDeg)).arcTo(cx, cy, outer, startDeg, sweepDeg);
            if (roundEnds) arcTo(px(cx, r, end), py(cy, r, end), width / 2, end, turn);
            else lineTo(px(cx, inner, end), py(cy, inner, end));
            arcTo(cx, cy, inner, end, -sweepDeg);
            if (roundEnds) arcTo(px(cx, r, startDeg), py(cy, r, startDeg), width / 2, startDeg + 180, turn);
            return close();
        }

        /**
         * The area between two arcs joining (x0, y0) and (x1, y1). Each arc bulges by its amount to the left of
         * its own direction (down for an arc going right), so equal bulges make a leaf and a smaller return
         * bulge on the same side makes a sweep that tapers to both ends.
         */
        Outline lens(float x0, float y0, float x1, float y1, float out, float back) {
            return moveTo(x0, y0).bulge(x0, y0, x1, y1, out).bulge(x1, y1, x0, y0, back).close();
        }

        private Outline bulge(float x0, float y0, float x1, float y1, float by) {
            float chord = (float) Math.hypot(x1 - x0, y1 - y0);
            if (Math.abs(by) < 1e-4f) return lineTo(x1, y1);
            float nx = -(y1 - y0) / chord, ny = (x1 - x0) / chord;
            float r = (chord * chord / 4 + by * by) / (2 * Math.abs(by));
            float mx = (x0 + x1) / 2 + nx * by, my = (y0 + y1) / 2 + ny * by;
            float cx = mx - nx * Math.signum(by) * r, cy = my - ny * Math.signum(by) * r;
            float a0 = deg(x0 - cx, y0 - cy);
            return arcTo(cx, cy, r, a0, away(a0, deg(x1 - cx, y1 - cy), deg(cx - mx, cy - my)));
        }

        /** A plus reaching `reach` from its centre, its arms `half` wide on each side. */
        Outline plus(float cx, float cy, float reach, float half) {
            return poly(0.04f, cx - half, cy - reach, cx + half, cy - reach, cx + half, cy - half, cx + reach, cy - half,
                    cx + reach, cy + half, cx + half, cy + half, cx + half, cy + reach, cx - half, cy + reach,
                    cx - half, cy + half, cx - reach, cy + half, cx - reach, cy - half, cx - half, cy - half);
        }

        /** The part of one disc outside another that overlaps it. */
        Outline crescent(float cx, float cy, float r, float cutX, float cutY, float cutR) {
            float dx = cutX - cx, dy = cutY - cy, dist = (float) Math.hypot(dx, dy);
            float along = (r * r - cutR * cutR + dist * dist) / (2 * dist);
            float half = (float) Math.sqrt(r * r - along * along);
            float bx = cx + dx / dist * along, by = cy + dy / dist * along;
            float ix = bx - dy / dist * half, iy = by + dx / dist * half;
            float jx = bx + dy / dist * half, jy = by - dx / dist * half;
            float a1 = deg(ix - cx, iy - cy), a2 = deg(jx - cx, jy - cy);
            float sweep = away(a1, a2, deg(dx, dy));
            float b1 = deg(jx - cutX, jy - cutY), b2 = deg(ix - cutX, iy - cutY);
            // Back along the cut, through the side facing the disc's centre.
            float back = away(b1, b2, deg(-dx, -dy));
            back = back > 0 ? back - 360 : back + 360;
            return moveTo(ix, iy).arcTo(cx, cy, r, a1, sweep).arcTo(cutX, cutY, cutR, b1, back).close();
        }

        /** A quarter disc with its corner at (x, y), its arc starting at startDeg and turning 90 degrees. */
        Outline quarter(float x, float y, float r, float startDeg) {
            return moveTo(x, y).lineTo(px(x, r, startDeg), py(y, r, startDeg)).arcTo(x, y, r, startDeg, 90).close();
        }

        /** The part of a disc between the horizontal lines at y0 and y1 (y0 above y1). */
        Outline slice(float cx, float cy, float r, float y0, float y1) {
            float a0 = (float) Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, (y0 - cy) / r))));
            float a1 = (float) Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, (y1 - cy) / r))));
            return moveTo(px(cx, r, a0), py(cy, r, a0)).arcTo(cx, cy, r, a0, a1 - a0)
                    .lineTo(px(cx, r, 180 - a1), py(cy, r, 180 - a1)).arcTo(cx, cy, r, 180 - a1, a1 - a0).close();
        }

        /** A regular polygon with a corner pointing up. */
        Outline regular(float cx, float cy, float r, int sides, float round) {
            float[] p = new float[sides * 2];
            for (int i = 0; i < sides; i++) {
                p[2 * i] = px(cx, r, -90 + i * 360f / sides);
                p[2 * i + 1] = py(cy, r, -90 + i * 360f / sides);
            }
            return poly(round, p);
        }

        Outline star(float cx, float cy, float outer, float inner, int points, float round) {
            float[] p = new float[points * 4];
            for (int i = 0; i < points * 2; i++) {
                float a = -90 + i * 180f / points, r = i % 2 == 0 ? outer : inner;
                p[2 * i] = px(cx, r, a);
                p[2 * i + 1] = py(cy, r, a);
            }
            return poly(round, p);
        }

        Outline gear(float cx, float cy, float outer, float root, int teeth) {
            float[] p = new float[teeth * 8];
            float pitch = 360f / teeth;
            for (int i = 0; i < teeth; i++) {
                float a = -90 + i * pitch;
                float[] angles = {a - pitch * 0.32f, a - pitch * 0.17f, a + pitch * 0.17f, a + pitch * 0.32f};
                float[] radii = {root, outer, outer, root};
                for (int j = 0; j < 4; j++) {
                    p[8 * i + 2 * j] = px(cx, radii[j], angles[j]);
                    p[8 * i + 2 * j + 1] = py(cy, radii[j], angles[j]);
                }
            }
            return poly(0.03f, p);
        }

        private static float px(float cx, float r, float deg) { return cx + r * (float) Math.cos(Math.toRadians(deg)); }
        private static float py(float cy, float r, float deg) { return cy + r * (float) Math.sin(Math.toRadians(deg)); }
        private static float deg(float x, float y) { return (float) Math.toDegrees(Math.atan2(y, x)); }

        /** The sweep from `from` to `to` that does not pass through `avoid`. */
        private static float away(float from, float to, float avoid) {
            float sweep = Math.floorMod(Math.round((to - from) * 1000), 360_000) / 1000f;
            float toAvoid = Math.floorMod(Math.round((avoid - from) * 1000), 360_000) / 1000f;
            return toAvoid < sweep ? sweep - 360 : sweep;
        }
    }
}
