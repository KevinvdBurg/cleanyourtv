package com.gathin.tvhome;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Home's background as GeometricGlow draws it, with java.awt in place of android.graphics, plus a mock of
 * the clock, hero name and dock so a composition can be judged in place without a TV. It compiles
 * AppShapes and AmbientScene from the app unchanged (run.sh), writes contact sheets and checks every
 * composition in place. Exits 1 when a check fails.
 */
final class ScenePreview {
    /** Package, label and a colour close to what Home samples from the app's banner. */
    static final Object[][] APPS = {
            {"com.google.android.youtube.tv", "YouTube", 0xF0384F},
            {"tv.twitch.android.app", "Twitch", 0x9B5CF6},
            {"com.stremio.one", "Stremio", 0x6E6AE8},
            {"com.collegehumor.chdropout", "Dropout", 0xE6B030},
            {"com.formulaone.production", "F1 TV", 0xE8323A},
            {"com.netflix.ninja", "Netflix", 0xD8283A},
            {"com.skyshowtime.skyshowtime.google", "SkyShowtime", 0xA54BD6},
            {"nl.uitzendinggemist", "NPO Start", 0xF07A28},
            {"com.android.vending", "Play Store", 0x2BAE8E},
            {"com.spotify.tv.android", "Spotify", 0x2BBE62},
            {"com.nordvpn.android", "NordVPN", 0x4F6BE8},
            {"com.brouken.player", "Just Player", 0x34B07A},
            {"com.viaplay.android", "Viaplay", 0xA03CB8},
            {"org.videolan.vlc", "VLC", 0xF08A1C},
            {"com.android.tv.settings", "Settings", 0x8A8F98},
            {"com.ziggo.tv", "Ziggo GO", 0xF39A1E},
            {"com.amazon.amazonvideo.livingroom", "Prime Video", 0x1A98FF},
            {"com.disney.disneyplus", "Disney+", 0x2A7FE0},
            {"com.apple.atve.androidtv.appletv", "Apple TV", 0x9AA0A8},
            {"com.wbd.stream", "HBO Max", 0x4F5BEF},
            {"com.crunchyroll.crunchyroid", "Crunchyroll", 0xF47521},
            {"com.mxtech.videoplayer.television", "MX Player", 0x2E8BF0},
            {"com.plexapp.android", "Plex", 0xE5A00D},
            {"org.xbmc.kodi", "Kodi", 0x17B2E7},
            {"com.google.android.youtube.tvmusic", "YouTube Music", 0xF03030},
            {"nl.rtl.videoland.v2", "Videoland", 0xFF3746},
    };
    private static final int S = 2;
    private static Font inter;

    private static final List<String> failures = new ArrayList<>();

    private ScenePreview() { }

    public static void main(String[] args) throws IOException {
        File out = new File(args.length > 0 ? args[0] : "build/shape-preview");
        if (!out.isDirectory() && !out.mkdirs()) throw new IOException("cannot create " + out);
        List<Object[]> apps = new ArrayList<>(List.of(APPS));
        // Spread over a few banner colours, since any app can land on any of them.
        int[] others = {0x3FA7D6, 0x4FB286, 0xD6694F, 0x8F6BD6, 0xD6A33F, 0x3FB8B0, 0xD64F86, 0x6B8FD6, 0x9AB83F};
        for (int i = 0; i < AppShapes.FALLBACKS; i++) {
            apps.add(new Object[]{fallbackPackage(i), "Other app " + (i + 1), others[i % others.length]});
        }
        String[] names = {"night", "day"};
        for (int light = 0; light < 2; light++) {
            List<BufferedImage> tiles = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (Object[] app : apps) {
                BufferedImage image = render((String) app[0], (Integer) app[2], light,
                        AmbientScene.Choice.DEFAULT, (String) app[1], true, true);
                tiles.add(image);
                labels.add((String) app[1]);
                if (light == 1) ImageIO.write(image, "png", new File(out, app[1].toString().replace(' ', '-') + ".png"));
            }
            ImageIO.write(sheet(tiles, labels), "png", new File(out, "sheet-" + names[light] + ".png"));
        }
        for (Object[] app : apps) checkInPlace((String) app[0], (String) app[1], (Integer) app[2]);
        checkHoles();
        System.out.println("Wrote " + out.getPath() + "/sheet-day.png, sheet-night.png and one PNG per app");
        if (failures.isEmpty()) {
            System.out.println("All checks passed for " + apps.size() + " compositions");
            return;
        }
        for (String failure : failures) System.out.println("FAIL " + failure);
        System.exit(1);
    }

    /** Clear of the clock, the hero name and the dock at every size up to the default, and visible against the glow. */
    private static void checkInPlace(String pkg, String label, int accent) {
        for (int size : new int[]{50, 100}) {
            AmbientScene.Choice choice = new AmbientScene.Choice(size, 150);
            BufferedImage with = render(pkg, accent, 1, choice, "", true, false);
            BufferedImage without = render(pkg, accent, 1, choice, "", false, false);
            check(difference(with, without, 0, 0, 440, 200) == 0, label + " at " + size + "% reaches the clock");
            check(difference(with, without, 0, 362, 960, 540) == 0, label + " at " + size + "% reaches the dock");
            check(difference(with, without, 852, 20, 914, 78) == 0, label + " at " + size + "% runs into the Settings button");
        }
        for (int light = 0; light < 2; light++) {
            BufferedImage with = render(pkg, accent, light, AmbientScene.Choice.DEFAULT, "", true, false);
            BufferedImage without = render(pkg, accent, light, AmbientScene.Choice.DEFAULT, "", false, false);
            float mean = difference(with, without, 460, 0, 960, 360) / (500f * 360);
            check(mean > 4, label + " too faint against the glow (" + mean + ")");
        }
        for (AppShapes.Part part : AppShapes.forPackage(pkg)) {
            Rectangle2D b = path(part.path).getBounds2D();
            check(b.getMinX() >= -AppShapes.REACH_X && b.getMaxX() <= AppShapes.REACH_X
                    && b.getMinY() >= -AppShapes.REACH_Y && b.getMaxY() <= AppShapes.REACH_Y, label + " leaves its box: " + b);
        }
    }

    /** Dropout's D has a counter, Viaplay's crescent is cut by a second disc, the Settings gear has a bore. */
    private static void checkHoles() {
        Path2D d = shape("com.collegehumor.chdropout", AppShapes.MAIN);
        check(!d.contains(0.3, 0) && d.contains(0.85, 0), "Dropout's D lost its counter");
        Path2D crescent = shape("com.viaplay.android", AppShapes.MAIN);
        check(crescent.contains(-0.8, 0.3) && !crescent.contains(0.5, -0.3) && !crescent.contains(1.2, 0),
                "Viaplay's crescent is not cut");
        Path2D gear = shape("com.android.tv.settings", AppShapes.MAIN);
        check(!gear.contains(-0.1, -0.1) && gear.contains(-0.1, -0.7), "the Settings gear has no bore");
        Path2D band = path(new AppShapes.Outline().band(0, 0, 1, 0.2f, 180, 180, true).toArray());
        check(band.contains(0, -1) && band.contains(-1, 0.05) && !band.contains(0, 0) && !band.contains(0, 1),
                "a round-ended band does not follow its arc");
    }

    private static Path2D shape(String pkg, int tone) {
        for (AppShapes.Part part : AppShapes.forPackage(pkg)) if (part.tone == tone) return path(part.path);
        throw new IllegalStateException(pkg + " has no tone " + tone);
    }

    private static void check(boolean ok, String failure) {
        if (!ok) failures.add(failure);
    }

    /** A made-up package that lands on the given fallback composition. */
    private static String fallbackPackage(int which) {
        for (int i = 0; ; i++) {
            String pkg = "com.example.app" + i;
            if (Math.floorMod(pkg.hashCode(), AppShapes.FALLBACKS) == which) return pkg;
        }
    }

    /** Sum of channel differences inside a rectangle given in the 960×540 space. */
    private static long difference(BufferedImage a, BufferedImage b, int left, int top, int right, int bottom) {
        long sum = 0;
        for (int y = top * S; y < bottom * S; y += S) {
            for (int x = left * S; x < right * S; x += S) {
                int p = a.getRGB(x, y), q = b.getRGB(x, y);
                sum += Math.abs(((p >> 16) & 0xFF) - ((q >> 16) & 0xFF))
                        + Math.abs(((p >> 8) & 0xFF) - ((q >> 8) & 0xFF)) + Math.abs((p & 0xFF) - (q & 0xFF));
            }
        }
        return sum;
    }

    static BufferedImage render(String pkg, int accent, float light, AmbientScene.Choice choice,
                                String label, boolean shapes, boolean ui) {
        BufferedImage image = new BufferedImage(AmbientScene.W * S, AmbientScene.H * S, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.scale(S, S);
        AmbientScene.Palette palette = new AmbientScene.Palette(accent, light, choice.intensity);
        g.setColor(new Color(palette.background));
        g.fillRect(0, 0, AmbientScene.W, AmbientScene.H);
        for (AmbientScene.Glow glow : palette.glows) {
            g.setPaint(new RadialGradientPaint(glow.x, glow.y, glow.radius, new float[]{0, 1},
                    new Color[]{new Color(glow.color, true), new Color(glow.color & 0x00FFFFFF, true)}));
            g.fillRect(0, 0, AmbientScene.W, AmbientScene.H);
        }
        if (shapes) {
            Graphics2D s = (Graphics2D) g.create();
            float unit = AmbientScene.unit(choice.size);
            s.translate(AmbientScene.CENTER_X, AmbientScene.CENTER_Y);
            s.scale(unit, unit);
            for (AppShapes.Part part : AppShapes.forPackage(pkg)) {
                s.setPaint(new GradientPaint(0, -AppShapes.REACH_Y, new Color(palette.tops[part.tone]),
                        0, AppShapes.REACH_Y, new Color(palette.tones[part.tone])));
                s.fill(path(part.path));
            }
            s.dispose();
        }
        g.setPaint(new LinearGradientPaint(0, 320, 0, AmbientScene.H, new float[]{0, 0.65f, 1},
                new Color[]{new Color(0, true), new Color(0xC0000000, true), new Color(0xF5000000, true)}));
        g.fillRect(0, 320, AmbientScene.W, AmbientScene.H - 320);
        g.setPaint(new GradientPaint(0, 0, new Color(0xAA000000, true), 460, 0, new Color(0, true)));
        g.fillRect(0, 0, 460, AmbientScene.H);
        if (ui) mockHome(g, label, accent);
        g.dispose();
        return image;
    }

    static Path2D.Float path(float[] d) {
        Path2D.Float p = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        for (int j = 0; j < d.length; ) {
            switch ((int) d[j]) {
                case AppShapes.MOVE: p.moveTo(d[j + 1], d[j + 2]); j += 3; break;
                case AppShapes.LINE: p.lineTo(d[j + 1], d[j + 2]); j += 3; break;
                case AppShapes.CUBIC: p.curveTo(d[j + 1], d[j + 2], d[j + 3], d[j + 4], d[j + 5], d[j + 6]); j += 7; break;
                default: p.closePath(); j += 1; break;
            }
        }
        return p;
    }

    /** Clock, date, settings button, hero name and dock, placed as on the Chromecast screenshots. */
    private static void mockHome(Graphics2D g, String label, int accent) {
        g.setColor(Color.WHITE);
        g.setFont(font(Font.PLAIN, 36));
        g.drawString("09:03", 56, 62);
        g.setColor(new Color(0xCCFFFFFF, true));
        g.setFont(font(Font.PLAIN, 12));
        g.drawString("Thursday 22 October", 58, 86);
        g.setColor(new Color(0x40FFFFFF, true));
        g.fillOval(862, 28, 42, 42);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawOval(876, 42, 14, 14);
        g.setFont(font(Font.PLAIN, 32));
        g.drawString(label, 68, 336);
        g.setColor(new Color(0xFF141518));
        g.fill(new RoundRectangle2D.Float(57, 367, 845, 108, 54, 54));
        float[] x = {60, 239, 408, 577, 747};
        for (int i = 0; i < x.length; i++) {
            boolean focused = i == 0;
            int c = focused ? accent : AmbientScene.lerp(0xFF000000 | accent, 0xFF606060, 0.35f + i * 0.1f);
            g.setPaint(new GradientPaint(0, 381, new Color(AmbientScene.lerp(0xFF000000 | c, 0xFFFFFFFF, 0.25f)),
                    0, 461, new Color(0xFF000000 | c)));
            if (focused) g.fill(new RoundRectangle2D.Float(x[i], 372, 161, 90, 18, 18));
            else g.fill(new RoundRectangle2D.Float(x[i], 381, 143, 80, 16, 16));
        }
        g.setColor(new Color(0xE0FFFFFF, true));
        g.setStroke(new BasicStroke(2.5f));
        g.draw(new RoundRectangle2D.Float(60, 372, 161, 90, 18, 18));
    }

    private static Font font(int style, float size) {
        if (inter == null) {
            try {
                inter = Font.createFont(Font.TRUETYPE_FONT, new File(System.getProperty("preview.font", "app/src/main/res/font/inter.ttf")));
            } catch (Exception e) {
                inter = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            }
        }
        return inter.deriveFont(style, size);
    }

    /** Thumbnails in a grid, each labelled, four to a row. */
    static BufferedImage sheet(List<BufferedImage> tiles, List<String> labels) {
        int cols = 4, w = 640, h = 360, gap = 28;
        int rows = (tiles.size() + cols - 1) / cols;
        BufferedImage out = new BufferedImage(cols * w, rows * (h + gap), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setComposite(AlphaComposite.Src);
        for (int i = 0; i < tiles.size(); i++) {
            int x = (i % cols) * w, y = (i / cols) * (h + gap);
            g.drawImage(tiles.get(i), x, y + gap, w, h, null);
            g.setColor(Color.WHITE);
            g.setFont(font(Font.PLAIN, 16));
            g.drawString(labels.get(i), x + 8, y + 20);
        }
        g.dispose();
        return out;
    }
}
