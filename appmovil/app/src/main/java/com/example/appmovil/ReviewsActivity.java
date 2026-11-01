package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;

public class ReviewsActivity extends AppCompatActivity {
    private long placeId;
    private int page;
    private long total;
    private boolean loading;
    private boolean refreshOnResume;
    private LinearLayout list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_reviews);
        placeId = getIntent().getLongExtra("placeId", 0);
        if (placeId <= 0) { finish(); return; }
        ((MaterialToolbar) findViewById(R.id.reviewsToolbar)).setNavigationOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.reviewsPlaceName)).setText(getIntent().getStringExtra("placeName"));
        list = findViewById(R.id.reviewsList);
        findViewById(R.id.btnMoreReviews).setOnClickListener(v -> loadNext());
        findViewById(R.id.btnReviewFromList).setOnClickListener(v ->
                ReviewRepository.mine(this, placeId, new ReviewRepository.Callback<ReviewRepository.Review>() {
                    @Override public void success(ReviewRepository.Review review) {
                        runOnUiThread(() -> {
                            refreshOnResume = true;
                            startActivity(ReviewEditorActivity.intent(
                                    ReviewsActivity.this, placeId, review));
                        });
                    }
                    @Override public void error(String message) {
                        runOnUiThread(() -> Snackbar.make(list, message, Snackbar.LENGTH_LONG).show());
                    }
                }));
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        if (list != null && refreshOnResume) {
            refreshOnResume = false;
            refresh();
        }
    }

    private void refresh() {
        page = 0;
        total = 0;
        list.removeAllViews();
        loadNext();
    }

    private void loadNext() {
        if (loading) return;
        loading = true;
        ReviewRepository.list(this, placeId, page, 10,
                new ReviewRepository.Callback<ReviewRepository.Page>() {
                    @Override public void success(ReviewRepository.Page result) {
                        runOnUiThread(() -> {
                            loading = false;
                            total = result.total;
                            ((TextView) findViewById(R.id.reviewsSummary)).setText(total == 0
                                    ? "Sin calificaciones todavía"
                                    : String.format(Locale.getDefault(), "★ %.1f/5 · %d reseñas",
                                            result.average, total));
                            for (ReviewRepository.Review review : result.reviews) {
                                list.addView(ReviewCardBinder.create(ReviewsActivity.this, list,
                                        placeId, review, ReviewsActivity.this::refresh,
                                        () -> refreshOnResume = true));
                            }
                            page++;
                            findViewById(R.id.reviewsEmpty).setVisibility(total == 0 ? View.VISIBLE : View.GONE);
                            findViewById(R.id.btnMoreReviews).setVisibility(
                                    list.getChildCount() < total ? View.VISIBLE : View.GONE);
                        });
                    }
                    @Override public void error(String message) {
                        runOnUiThread(() -> {
                            loading = false;
                            Snackbar.make(list, message, Snackbar.LENGTH_LONG).show();
                        });
                    }
                });
    }
}
