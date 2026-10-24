package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * The focused app's flat shapes (AppShapes) over a soft glow in its colour. The glow is a small cached
 * texture, the shapes a handful of vector paths; both change only on focus or solar-light changes.
 */
final class GeometricGlow extends Drawable {
    private static final int W = AmbientScene.W, H = AmbientScene.H;
    private final Context context;
    private final BitmapDrawable lighting;
    private final BitmapDrawable scrim;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint composite = new Paint();
    private final Paint[] tones = {new Paint(Paint.ANTI_ALIAS_FLAG), new Paint(Paint.ANTI_ALIAS_FLAG),
            new Paint(Paint.ANTI_ALIAS_FLAG)};
    private final double latitude, longitude;
    /** Outlines in AppShapes' unit space, and the same placed in the 960×540 space at the chosen size. */
    private Path[] unitPaths = new Path[0], paths = new Path[0];
    private int[] pathTones = new int[0];
    private String app;
    private int color;
    private float lastLight = -1;
    private AmbientScene.Choice choice = AmbientScene.Choice.DEFAULT;

    GeometricGlow(Context context) {
        this.context = context;
        lighting = texture(context);
        scrim = texture(context);
        Canvas canvas = textureCanvas(scrim);
        paint.setShader(new LinearGradient(0, 320, 0, H,
                new int[]{0x00000000, 0xC0000000, 0xF5000000},
                new float[]{0, 0.65f, 1}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 320, W, H, paint);
        paint.setShader(new LinearGradient(0, 0, 460, 0,
                new int[]{0xAA000000, 0x00000000}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, 460, H, paint);
        double lat = Double.NaN, lon = Double.NaN;
        try {
            lat = Double.parseDouble(context.getString(R.string.latitude));
            lon = Double.parseDouble(context.getString(R.string.longitude));
        } catch (NumberFormatException ignored) { }
        latitude = lat;
        longitude = lon;
    }

    private static BitmapDrawable texture(Context context) {
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(),
                Bitmap.createBitmap(W / 2, H / 2, Bitmap.Config.ARGB_8888));
        drawable.setGravity(Gravity.FILL);
        drawable.setFilterBitmap(true);
        return drawable;
    }

    private static Canvas textureCanvas(BitmapDrawable drawable) {
        Canvas c = new Canvas(drawable.getBitmap());
        c.scale(0.5f, 0.5f);
        return c;
    }

    @Override protected void onBoundsChange(Rect bounds) {
        lighting.setBounds(bounds);
        scrim.setBounds(bounds);
    }

    @Override public void draw(Canvas target) {
        Rect bounds = getBounds();
        int save = composite.getAlpha() == 255 && composite.getColorFilter() == null ? target.save()
                : target.saveLayer(bounds.left, bounds.top, bounds.right, bounds.bottom, composite);
        lighting.draw(target);
        int shapes = target.save();
        target.translate(bounds.left, bounds.top);
        target.scale(bounds.width() / (float) W, bounds.height() / (float) H);
        for (int i = 0; i < paths.length; i++) target.drawPath(paths[i], tones[pathTones[i]]);
        target.restoreToCount(shapes);
        scrim.draw(target);
        target.restoreToCount(save);
    }

    @Override public void setAlpha(int alpha) {
        composite.setAlpha(alpha);
        invalidateSelf();
    }

    @Override public void setColorFilter(ColorFilter filter) {
        composite.setColorFilter(filter);
        invalidateSelf();
    }

    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    boolean isApp(String pkg) {
        return java.util.Objects.equals(app, pkg);
    }

    AmbientScene.Choice choiceFor(String pkg) {
        android.content.SharedPreferences p = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
        String key = "shape." + pkg + ".";
        return new AmbientScene.Choice(p.getInt(key + "size", 100), p.getInt(key + "intensity", 100));
    }

    void saveChoice(String pkg, AmbientScene.Choice value) {
        String key = "shape." + pkg + ".";
        context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE).edit()
                .putInt(key + "size", value.size).putInt(key + "intensity", value.intensity).apply();
    }

    void preview(AmbientScene.Choice value) {
        choice = value;
        render(lightNow());
    }

    void setApp(String pkg, int accent) {
        float light = lightNow();
        AmbientScene.Choice saved = choiceFor(pkg);
        if (isApp(pkg) && color == accent && light == lastLight && saved.equals(choice)) return;
        if (!isApp(pkg)) shapes(AppShapes.forPackage(pkg));
        app = pkg;
        color = accent;
        choice = saved;
        render(light);
    }

    void update() {
        if (app == null) return;
        float light = lightNow();
        if (light != lastLight) render(light);
    }

    private void shapes(List<AppShapes.Part> parts) {
        unitPaths = new Path[parts.size()];
        pathTones = new int[parts.size()];
        for (int i = 0; i < unitPaths.length; i++) {
            AppShapes.Part part = parts.get(i);
            pathTones[i] = part.tone;
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            float[] d = part.path;
            for (int j = 0; j < d.length; ) {
                switch ((int) d[j]) {
                    case AppShapes.MOVE: path.moveTo(d[j + 1], d[j + 2]); j += 3; break;
                    case AppShapes.LINE: path.lineTo(d[j + 1], d[j + 2]); j += 3; break;
                    case AppShapes.CUBIC: path.cubicTo(d[j + 1], d[j + 2], d[j + 3], d[j + 4], d[j + 5], d[j + 6]); j += 7; break;
                    default: path.close(); j += 1; break;
                }
            }
            unitPaths[i] = path;
        }
    }

    private float lightNow() {
        String mode = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE)
                .getString(AmbientScene.LIGHT_KEY, "auto");
        if ("day".equals(mode)) return 1;
        if ("night".equals(mode)) return 0;
        LocalDateTime now = LocalDateTime.now();
        int[] sun = Double.isFinite(latitude) && Double.isFinite(longitude)
                ? Sun.riseAndSet(now.toLocalDate(), latitude, longitude, ZoneId.systemDefault()) : null;
        return AmbientScene.daylight(now.getHour() * 60 + now.getMinute(),
                sun == null ? 450 : sun[0], sun == null ? 1140 : sun[1]);
    }

    private void render(float light) {
        lastLight = light;
        AmbientScene.Palette palette = new AmbientScene.Palette(color, light, choice.intensity);
        Canvas canvas = textureCanvas(lighting);
        canvas.drawColor(palette.background);
        for (AmbientScene.Glow glow : palette.glows) {
            paint.setShader(new RadialGradient(glow.x, glow.y, glow.radius,
                    glow.color, glow.color & 0x00FFFFFF, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, W, H, paint);
        }
        paint.setShader(null);
        // Placed at full size rather than scaled while drawing, so curves are flattened at screen resolution.
        float unit = AmbientScene.unit(choice.size);
        Matrix place = new Matrix();
        place.setScale(unit, unit);
        place.postTranslate(AmbientScene.CENTER_X, AmbientScene.CENTER_Y);
        paths = new Path[unitPaths.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path();
            unitPaths[i].transform(place, paths[i]);
            paths[i].setFillType(Path.FillType.EVEN_ODD);
        }
        float top = AmbientScene.CENTER_Y - AppShapes.REACH_Y * unit, bottom = AmbientScene.CENTER_Y + AppShapes.REACH_Y * unit;
        for (int i = 0; i < tones.length; i++) {
            tones[i].setShader(new LinearGradient(0, top, 0, bottom,
                    palette.tops[i], palette.tones[i], Shader.TileMode.CLAMP));
        }
        invalidateSelf();
    }
}
