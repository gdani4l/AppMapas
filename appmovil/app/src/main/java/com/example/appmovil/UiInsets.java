package com.example.appmovil;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/** Aplica zonas seguras y mantiene visible el campo de texto que recibe el foco. */
final class UiInsets {
    private UiInsets() { }

    static void apply(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;
        apply(activity, content.getChildAt(0));
    }

    static void apply(Activity activity, View root) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final int initialLeft = root.getPaddingLeft();
        final int initialTop = root.getPaddingTop();
        final int initialRight = root.getPaddingRight();
        final int initialBottom = root.getPaddingBottom();
        if (root instanceof ViewGroup) ((ViewGroup) root).setClipToPadding(false);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(initialLeft + bars.left, initialTop + bars.top,
                    initialRight + bars.right, initialBottom + Math.max(bars.bottom, ime.bottom));
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
        root.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus == null || !newFocus.onCheckIsTextEditor()) return;
            newFocus.postDelayed(() -> {
                if (!newFocus.isAttachedToWindow()) return;
                Rect area = new Rect(0, 0, newFocus.getWidth(), newFocus.getHeight());
                newFocus.requestRectangleOnScreen(area, false);
            }, 250L);
        });
    }
}
