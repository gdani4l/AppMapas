package com.example.appmovil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class RemoteImageLoader {
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Handler main = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap value) { return value.getByteCount(); }
    };

    static void load(ImageView target, String url, int placeholder) {
        target.setTag(null);
        target.setImageResource(placeholder);
        if (url == null || !url.startsWith("https://")) return;
        target.setTag(url);
        Bitmap cached = cache.get(url);
        if (cached != null) { target.setImageBitmap(cached); return; }
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                if (connection.getResponseCode() != 200) return;
                Bitmap bitmap;
                try (InputStream input = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(input);
                }
                if (bitmap != null) {
                    cache.put(url, bitmap);
                    main.post(() -> { if (url.equals(target.getTag())) target.setImageBitmap(bitmap); });
                }
            } catch (Exception ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }
}
