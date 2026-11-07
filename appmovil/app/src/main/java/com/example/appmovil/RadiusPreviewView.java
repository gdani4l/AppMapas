package com.example.appmovil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/** Vista previa visual del alcance seleccionado para un reporte de seguridad. */
public class RadiusPreviewView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int radiusMeters = 25;

    public RadiusPreviewView(Context context) { this(context, null); }
    public RadiusPreviewView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); }

    public void setRadiusMeters(int value) {
        radiusMeters = Math.max(10, Math.min(50, value));
        setContentDescription("Vista previa de un radio de " + radiusMeters + " metros");
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float maxRadius = Math.min(getWidth(), getHeight()) * .40f;
        float radius = maxRadius * (.35f + .65f * radiusMeters / 50f);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0x22EF4444);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp(2)); paint.setColor(0xFFEF4444);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.FILL); paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, dp(8), paint);
        paint.setColor(0xFFDC2626); canvas.drawCircle(cx, cy, dp(5), paint);
    }

    private float dp(int value) { return value * getResources().getDisplayMetrics().density; }
}
