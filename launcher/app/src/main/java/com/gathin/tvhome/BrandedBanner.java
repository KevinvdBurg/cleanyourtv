package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

/** Local brand artwork: a gradient, a soft highlight and the app's mark. */
final class BrandedBanner extends Drawable {
    private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint light = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable logo;
    private final int top, bottom;
    private final boolean adaptive;
    private int alpha = 255;

    static Drawable create(Context context, String pkg, Drawable appIcon) {
        int top, bottom, mark = 0;
        switch (pkg) {
            case "com.google.android.youtube.tv":
                top = 0xFFFF6653; bottom = 0xFFD70B32; mark = R.drawable.brand_youtube; break;
            case "tv.twitch.android.app":
                top = 0xFFB479FF; bottom = 0xFF5820D4; mark = R.drawable.brand_twitch; break;
            case "com.stremio.one":
                top = 0xFF698CFF; bottom = 0xFF5325C3; mark = R.drawable.brand_stremio; break;
            case "com.collegehumor.chdropout":
                top = 0xFFFFCE45; bottom = 0xFFC17A0C; mark = R.drawable.brand_dropout; break;
            case "com.formulaone.production":
                top = 0xFFFF644E; bottom = 0xFFAC102A; break;
            case "com.brouken.player":
                top = 0xFF49E3B4; bottom = 0xFF0E8976; break;
            case "com.netflix.ninja":
                top = 0xFFED425A; bottom = 0xFF8E102B; mark = R.drawable.brand_netflix; break;
            case "com.nordvpn.android":
                top = 0xFF65ACFF; bottom = 0xFF2850BD; mark = R.drawable.brand_nordvpn; break;
            case "nl.uitzendinggemist":
                top = 0xFFFFB24C; bottom = 0xFFD45C12; break;
            case "com.android.vending":
                top = 0xFF4FE2BD; bottom = 0xFF15888E; mark = R.drawable.brand_googleplay; break;
            case "com.android.tv.settings":
                top = 0xFF95A3BB; bottom = 0xFF414E68; mark = R.drawable.ic_settings; break;
            case "com.skyshowtime.skyshowtime.google":
                top = 0xFFE277E7; bottom = 0xFF772AB9; mark = R.drawable.brand_skyshowtime; break;
            case "com.spotify.tv.android":
                top = 0xFF4DE78F; bottom = 0xFF0B7F48; mark = R.drawable.brand_spotify; break;
            case "com.viaplay.android":
                top = 0xFFF27ACC; bottom = 0xFF7C2EC4; mark = R.drawable.brand_viaplay; break;
            case "org.videolan.vlc":
                top = 0xFFFFBC53; bottom = 0xFFDA630F; mark = R.drawable.brand_vlcmediaplayer; break;
            default:
                return null;
        }
        return new BrandedBanner(top, bottom, mark == 0 ? appIcon : context.getDrawable(mark));
    }

    private BrandedBanner(int top, int bottom, Drawable logo) {
        this.top = top;
        this.bottom = bottom;
        adaptive = logo instanceof AdaptiveIconDrawable;
        this.logo = (adaptive ? ((AdaptiveIconDrawable) logo).getForeground() : logo).mutate();
    }

    @Override protected void onBoundsChange(Rect bounds) {
        if (bounds.isEmpty()) return;
        background.setShader(new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.bottom,
                top, bottom, Shader.TileMode.CLAMP));
        light.setShader(new RadialGradient(bounds.left + bounds.width() * 0.22f, bounds.top,
                bounds.width() * 0.8f, 0x38FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        int maxHeight = Math.round(bounds.height() * (adaptive ? 0.86f : 0.60f));
        float aspect = logo.getIntrinsicWidth() > 0 && logo.getIntrinsicHeight() > 0
                ? logo.getIntrinsicWidth() / (float) logo.getIntrinsicHeight() : 1f;
        int width = Math.max(1, Math.min(Math.round(bounds.width() * 0.74f), Math.round(maxHeight * aspect)));
        int height = Math.max(1, Math.round(width / aspect));
        int left = bounds.centerX() - width / 2, top = bounds.centerY() - height / 2;
        logo.setBounds(left, top, left + width, top + height);
    }

    @Override public void draw(Canvas canvas) {
        canvas.drawRect(getBounds(), background);
        canvas.drawRect(getBounds(), light);
        logo.draw(canvas);
    }

    @Override public void setAlpha(int alpha) {
        this.alpha = alpha;
        background.setAlpha(alpha);
        light.setAlpha(alpha);
        logo.setAlpha(alpha);
        invalidateSelf();
    }

    @Override public void setColorFilter(ColorFilter filter) {
        background.setColorFilter(filter);
        light.setColorFilter(filter);
        logo.setColorFilter(filter);
        invalidateSelf();
    }

    @Override public int getOpacity() { return alpha == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return 360; }
    @Override public int getIntrinsicHeight() { return 202; }
}
