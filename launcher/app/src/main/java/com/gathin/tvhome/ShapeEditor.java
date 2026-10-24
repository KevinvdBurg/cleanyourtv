package com.gathin.tvhome;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/** Previews one app's geometry without writing choices until Save. */
final class ShapeEditor extends Dialog {
    private final GeometricGlow glow;
    private final AmbientScene.Choice original;
    private AmbientScene.Choice current;
    private boolean saved;
    private final TextView size, intensity;

    ShapeEditor(Activity activity, GeometricGlow glow, String pkg, String label, Runnable closed) {
        super(activity);
        this.glow = glow;
        original = current = glow.choiceFor(pkg);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_shape_editor);
        ((TextView) findViewById(R.id.shapeAppName)).setText(label);
        size = findViewById(R.id.shapeSize);
        intensity = findViewById(R.id.shapeIntensity);
        adjustable(size, true);
        adjustable(intensity, false);
        findViewById(R.id.shapeReset).setOnClickListener(v -> {
            current = AmbientScene.Choice.DEFAULT;
            refresh();
        });
        findViewById(R.id.shapeSave).setOnClickListener(v -> {
            glow.saveChoice(pkg, current);
            saved = true;
            dismiss();
        });
        findViewById(R.id.shapeCancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.shapeAppInfo).setOnClickListener(v -> {
            dismiss();
            try {
                activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + pkg)));
            } catch (android.content.ActivityNotFoundException ignored) { }
        });
        setOnDismissListener(dialog -> {
            glow.preview(saved ? current : original);
            closed.run();
        });
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = Math.round(48 * activity.getResources().getDisplayMetrics().density);
        window.setAttributes(lp);
        refresh();
        size.requestFocus();
    }

    private void adjustable(TextView row, boolean isSize) {
        row.setOnClickListener(v -> adjust(isSize, 1));
        row.setOnKeyListener((v, key, event) -> {
            if (key != KeyEvent.KEYCODE_DPAD_LEFT && key != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) adjust(isSize, key == KeyEvent.KEYCODE_DPAD_LEFT ? -1 : 1);
            return true;
        });
    }

    private void adjust(boolean isSize, int direction) {
        current = new AmbientScene.Choice(current.size + (isSize ? direction * 10 : 0),
                current.intensity + (isSize ? 0 : direction * 10));
        refresh();
    }

    private void refresh() {
        size.setText(getContext().getString(R.string.shape_size_value, current.size));
        intensity.setText(getContext().getString(R.string.shape_intensity_value, current.intensity));
        glow.preview(current);
    }
}
