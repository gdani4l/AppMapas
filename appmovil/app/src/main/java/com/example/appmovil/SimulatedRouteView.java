package com.example.appmovil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

/** Captura el trazo sólo mientras está activo el modo de simulación. */
final class SimulatedRouteView extends View {
    interface Listener { void onPoint(float x, float y, boolean first); }
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private Listener listener;
    private boolean enabled;

    SimulatedRouteView(Context context) {
        super(context);
        float density = getResources().getDisplayMetrics().density;
        line.setColor(Color.rgb(13, 148, 136));
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(5 * density);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        dot.setColor(Color.rgb(2, 132, 199));
        setVisibility(GONE);
    }

    void start(Listener listener) {
        this.listener = listener;
        enabled = true;
        path.reset();
        setVisibility(VISIBLE);
        invalidate();
    }

    void stop() {
        enabled = false;
        listener = null;
        setVisibility(GONE);
        path.reset();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!enabled) return false;
        float x = event.getX(), y = event.getY();
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            path.moveTo(x, y);
            listener.onPoint(x, y, true);
            invalidate();
            return true;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                float hx = event.getHistoricalX(i), hy = event.getHistoricalY(i);
                path.lineTo(hx, hy);
                listener.onPoint(hx, hy, false);
            }
            path.lineTo(x, y);
            listener.onPoint(x, y, false);
            invalidate();
            return true;
        }
        return event.getActionMasked() == MotionEvent.ACTION_UP;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, line);
    }
}
