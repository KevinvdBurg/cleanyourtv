package com.gathin.tvhome;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class AppShapesTest {
    /** The apps with their own composition; launcher/preview/ScenePreview.java draws the same list. */
    static final List<String> PROFILES = Arrays.asList("com.google.android.youtube.tv", "tv.twitch.android.app",
            "com.stremio.one", "com.collegehumor.chdropout", "com.formulaone.production", "com.netflix.ninja",
            "com.skyshowtime.skyshowtime.google", "nl.uitzendinggemist", "com.android.vending",
            "com.spotify.tv.android", "com.nordvpn.android", "com.brouken.player", "com.viaplay.android",
            "org.videolan.vlc", "com.android.tv.settings", "com.ziggo.tv", "com.amazon.amazonvideo.livingroom",
            "com.disney.disneyplus", "com.apple.atve.androidtv.appletv", "com.wbd.stream",
            "com.crunchyroll.crunchyroid", "com.mxtech.videoplayer.television", "com.plexapp.android",
            "org.xbmc.kodi", "com.google.android.youtube.tvmusic", "nl.rtl.videoland.v2");

    private static List<String> packages() {
        List<String> out = new ArrayList<>(PROFILES);
        for (int i = 0; i < AppShapes.FALLBACKS; i++) out.add(fallbackPackage(i));
        return out;
    }

    /** A made-up package that lands on the given fallback composition. */
    static String fallbackPackage(int which) {
        for (int i = 0; ; i++) {
            String pkg = "com.example.app" + i;
            if (Math.floorMod(pkg.hashCode(), AppShapes.FALLBACKS) == which) return pkg;
        }
    }

    @Test public void outlinesAreWellFormed() {
        for (String pkg : packages()) {
            for (AppShapes.Part part : AppShapes.forPackage(pkg)) {
                float[] d = part.path;
                assertTrue(pkg, d.length > 0 && d[0] == AppShapes.MOVE);
                boolean open = false;
                for (int j = 0; j < d.length; ) {
                    int command = (int) d[j];
                    int size = command == AppShapes.CUBIC ? 7 : command == AppShapes.CLOSE ? 1 : 3;
                    if (command == AppShapes.MOVE) {
                        assertFalse(pkg + ": subpath not closed", open);
                        open = true;
                    } else {
                        assertTrue(pkg + ": drawing before moveTo", open);
                    }
                    if (command == AppShapes.CLOSE) open = false;
                    for (int k = 1; k < size; k++) assertTrue(pkg, Float.isFinite(d[j + k]));
                    j += size;
                }
                assertFalse(pkg + ": last subpath not closed", open);
                assertTrue(pkg, part.tone >= AppShapes.ECHO && part.tone <= AppShapes.ACCENT);
            }
        }
    }

    @Test public void compositionsStayInTheirBox() {
        for (String pkg : packages()) {
            for (AppShapes.Part part : AppShapes.forPackage(pkg)) {
                // Every point, control points included: the curves never leave their control polygon.
                float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
                float[] d = part.path;
                for (int j = 0; j < d.length; ) {
                    int size = d[j] == AppShapes.CUBIC ? 7 : d[j] == AppShapes.CLOSE ? 1 : 3;
                    for (int k = 1; k < size; k += 2) {
                        minX = Math.min(minX, d[j + k]);
                        maxX = Math.max(maxX, d[j + k]);
                        minY = Math.min(minY, d[j + k + 1]);
                        maxY = Math.max(maxY, d[j + k + 1]);
                    }
                    j += size;
                }
                String b = pkg + " x " + minX + ".." + maxX + " y " + minY + ".." + maxY;
                assertTrue(b, minX >= -AppShapes.REACH_X && maxX <= AppShapes.REACH_X);
                assertTrue(b, minY >= -AppShapes.REACH_Y && maxY <= AppShapes.REACH_Y);
                assertTrue(pkg + ": empty part", maxX - minX > 0.05f && maxY - minY > 0.05f);
            }
        }
    }

    @Test public void everyCompositionHasAMainShapeAndContrast() {
        for (String pkg : packages()) {
            Set<Integer> tones = new HashSet<>();
            for (AppShapes.Part part : AppShapes.forPackage(pkg)) tones.add(part.tone);
            assertTrue(pkg, tones.contains(AppShapes.MAIN));
            assertTrue(pkg, tones.size() >= 2);
            assertTrue(pkg + ": keep it to a few shapes", AppShapes.forPackage(pkg).size() <= 8);
        }
    }

    @Test public void everyAppHasItsOwnComposition() {
        Set<String> seen = new HashSet<>();
        for (String pkg : packages()) assertTrue(pkg, seen.add(signature(pkg)));
    }

    @Test public void otherBuildsOfAnAppShareItsComposition() {
        assertEquals(signature("com.apple.atve.androidtv.appletv"), signature("com.apple.atve.sony.appletv"));
        assertEquals(signature("com.wbd.stream"), signature("com.wbd.hbomax"));
        assertEquals(signature("com.mxtech.videoplayer.television"), signature("com.mxtech.videoplayer.ad"));
    }

    @Test public void unknownAppsGetAStablePlainComposition() {
        assertEquals(signature("com.example.future"), signature("com.example.future"));
        assertEquals(signature(fallbackPackage(1)), signature("com.example.other" + otherWith(1)));
        assertFalse(AppShapes.forPackage(null).isEmpty());
    }

    private static int otherWith(int which) {
        for (int i = 0; ; i++) if (Math.floorMod(("com.example.other" + i).hashCode(), AppShapes.FALLBACKS) == which) return i;
    }

    private static String signature(String pkg) {
        StringBuilder out = new StringBuilder();
        for (AppShapes.Part part : AppShapes.forPackage(pkg)) {
            out.append(part.tone).append(':');
            for (float v : part.path) out.append(Math.round(v * 100)).append(',');
            out.append('|');
        }
        return out.toString();
    }
}
