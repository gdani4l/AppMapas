package com.example.appmovil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Gráfico ligero reutilizable para los paneles, sin depender de servicios externos. */
public class DashboardChartView extends View {
    public enum Mode { BAR, HORIZONTAL_BAR, LINE, DONUT }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> labels = new ArrayList<>();
    private long[] values = new long[0];
    private Mode mode = Mode.BAR;
    private final int[] palette;
    private final float density;
    private final float scaledDensity;

    public DashboardChartView(Context context) { this(context, null); }

    public DashboardChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        palette = new int[]{
                context.getColor(R.color.app_primary),
                context.getColor(R.color.app_secondary),
                0xFFF59E0B, 0xFFEF4444, 0xFF8B5CF6, 0xFF14B8A6,
                0xFF6366F1, 0xFFEC4899
        };
        gridPaint.setColor(0xFFE2E8F0);
        gridPaint.setStrokeWidth(dp(1));
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.BAR : mode;
        requestLayout();
        invalidate();
    }

    public void setData(List<String> newLabels, long[] newValues) {
        labels.clear();
        if (newLabels != null) labels.addAll(newLabels);
        values = newValues == null ? new long[0] : newValues.clone();
        setContentDescription(buildDescription());
        requestLayout();
        invalidate();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight;
        if (mode == Mode.HORIZONTAL_BAR) desiredHeight = dp(Math.max(150, 42 * Math.max(1, values.length) + 24));
        else if (mode == Mode.DONUT) desiredHeight = dp(Math.max(190, 38 * Math.max(2, values.length)));
        else desiredHeight = dp(220);
        setMeasuredDimension(resolveSize(dp(280), widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (values.length == 0) {
            paint.setColor(getContext().getColor(R.color.app_text_secondary));
            paint.setTextSize(sp(14));
            canvas.drawText("Todavía no hay datos", dp(12), getHeight() / 2f, paint);
            return;
        }
        if (mode == Mode.HORIZONTAL_BAR) drawHorizontalBars(canvas);
        else if (mode == Mode.LINE) drawLine(canvas);
        else if (mode == Mode.DONUT) drawDonut(canvas);
        else drawBars(canvas);
    }

    private void drawBars(Canvas canvas) {
        float left = dp(34), right = getWidth() - dp(10), top = dp(28), bottom = getHeight() - dp(42);
        long max = maxValue();
        drawGrid(canvas, left, right, top, bottom, max);
        float slot = (right - left) / Math.max(1, values.length);
        float barWidth = Math.min(dp(34), slot * .62f);
        for (int i = 0; i < values.length; i++) {
            float h = (bottom - top) * values[i] / (float) max;
            float center = left + slot * (i + .5f);
            paint.setColor(palette[i % palette.length]);
            canvas.drawRoundRect(center - barWidth / 2, bottom - h, center + barWidth / 2, bottom,
                    dp(5), dp(5), paint);
            drawCentered(canvas, format(values[i]), center, Math.max(top + sp(10), bottom - h - dp(7)), 11,
                    getContext().getColor(R.color.app_text_primary));
            drawCentered(canvas, shortLabel(labelAt(i), 8), center, bottom + dp(19), 10,
                    getContext().getColor(R.color.app_text_secondary));
        }
    }

    private void drawHorizontalBars(Canvas canvas) {
        float labelWidth = Math.min(dp(150), getWidth() * .44f);
        float left = labelWidth, right = getWidth() - dp(42), rowHeight = dp(42);
        long max = maxValue();
        paint.setTextSize(sp(11));
        for (int i = 0; i < values.length; i++) {
            float y = dp(14) + i * rowHeight;
            paint.setColor(getContext().getColor(R.color.app_text_secondary));
            canvas.drawText(shortLabel(labelAt(i), 22), dp(4), y + dp(19), paint);
            paint.setColor(0xFFE2E8F0);
            canvas.drawRoundRect(left, y + dp(7), right, y + dp(25), dp(9), dp(9), paint);
            float width = (right - left) * values[i] / (float) max;
            paint.setColor(palette[i % palette.length]);
            canvas.drawRoundRect(left, y + dp(7), left + Math.max(width, dp(4)), y + dp(25), dp(9), dp(9), paint);
            paint.setColor(getContext().getColor(R.color.app_text_primary));
            paint.setTextSize(sp(11));
            canvas.drawText(format(values[i]), right + dp(7), y + dp(21), paint);
        }
    }

    private void drawLine(Canvas canvas) {
        float left = dp(38), right = getWidth() - dp(14), top = dp(28), bottom = getHeight() - dp(40);
        long max = maxValue();
        drawGrid(canvas, left, right, top, bottom, max);
        Path path = new Path();
        float step = values.length <= 1 ? 0 : (right - left) / (values.length - 1f);
        for (int i = 0; i < values.length; i++) {
            float x = left + step * i;
            float y = bottom - (bottom - top) * values[i] / (float) max;
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(getContext().getColor(R.color.app_primary));
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < values.length; i++) {
            float x = left + step * i;
            float y = bottom - (bottom - top) * values[i] / (float) max;
            paint.setColor(0xFFFFFFFF);
            canvas.drawCircle(x, y, dp(5), paint);
            paint.setColor(getContext().getColor(R.color.app_primary));
            canvas.drawCircle(x, y, dp(3), paint);
            if (shouldShowLabel(i, values.length)) {
                drawCentered(canvas, shortLabel(labelAt(i), 8), x, bottom + dp(20), 10,
                        getContext().getColor(R.color.app_text_secondary));
            }
        }
    }

    private void drawDonut(Canvas canvas) {
        long total = 0;
        for (long value : values) total += Math.max(0, value);
        float centerX = Math.min(getWidth() * .30f, dp(110));
        float centerY = getHeight() / 2f;
        float radius = Math.min(dp(76), Math.min(centerX - dp(8), getHeight() / 2f - dp(14)));
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(22));
        paint.setStrokeCap(Paint.Cap.BUTT);
        float angle = -90;
        if (total == 0) {
            paint.setColor(0xFFE2E8F0); canvas.drawArc(oval, 0, 360, false, paint);
        } else {
            for (int i = 0; i < values.length; i++) {
                float sweep = 360f * Math.max(0, values[i]) / total;
                paint.setColor(palette[i % palette.length]);
                canvas.drawArc(oval, angle, Math.max(1, sweep - 1.5f), false, paint);
                angle += sweep;
            }
        }
        paint.setStyle(Paint.Style.FILL);
        drawCentered(canvas, format(total), centerX, centerY + sp(5), 22,
                getContext().getColor(R.color.app_text_primary));
        float legendX = centerX + radius + dp(28), legendY = dp(18);
        for (int i = 0; i < values.length; i++) {
            paint.setColor(palette[i % palette.length]);
            canvas.drawCircle(legendX, legendY + dp(7), dp(5), paint);
            paint.setColor(getContext().getColor(R.color.app_text_secondary));
            paint.setTextSize(sp(12));
            canvas.drawText(shortLabel(labelAt(i), 18) + "  " + format(values[i]), legendX + dp(12), legendY + dp(11), paint);
            legendY += dp(32);
        }
    }

    private void drawGrid(Canvas canvas, float left, float right, float top, float bottom, long max) {
        paint.setTextSize(sp(10));
        paint.setColor(getContext().getColor(R.color.app_text_secondary));
        for (int i = 0; i <= 4; i++) {
            float y = bottom - (bottom - top) * i / 4f;
            canvas.drawLine(left, y, right, y, gridPaint);
            canvas.drawText(format(Math.round(max * i / 4f)), dp(2), y + sp(3), paint);
        }
    }

    private boolean shouldShowLabel(int index, int count) {
        if (count <= 7) return true;
        int interval = (int) Math.ceil(count / 5d);
        return index == 0 || index == count - 1 || index % interval == 0;
    }

    private String labelAt(int index) { return index < labels.size() ? labels.get(index) : ""; }
    private long maxValue() { long max = 1; for (long value : values) max = Math.max(max, value); return max; }
    private String format(long value) { return NumberFormat.getIntegerInstance(new Locale("es", "PE")).format(value); }
    private String shortLabel(String value, int max) {
        if (value == null) return "";
        String clean = value.replace('_', ' ');
        return clean.length() <= max ? clean : clean.substring(0, Math.max(1, max - 1)) + "…";
    }
    private void drawCentered(Canvas canvas, String text, float x, float y, int sizeSp, int color) {
        paint.setTextSize(sp(sizeSp)); paint.setColor(color); paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, x, y, paint); paint.setTextAlign(Paint.Align.LEFT);
    }
    private String buildDescription() {
        StringBuilder result = new StringBuilder("Gráfico. ");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) result.append(", ");
            result.append(labelAt(i)).append(": ").append(values[i]);
        }
        return result.toString();
    }
    private int dp(int value) { return Math.round(value * density); }
    private float sp(int value) { return value * scaledDensity; }
}
