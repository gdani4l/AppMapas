package com.example.appmovil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReviewEditorActivity extends AppCompatActivity {
    private long placeId;
    private boolean editing;
    private boolean removeImage;
    private byte[] imageBytes;
    private String mimeType;
    private RatingBar rating;
    private TextInputEditText comment;
    private ImageView preview;

    static Intent intent(Activity activity, long placeId, ReviewRepository.Review review) {
        Intent intent = new Intent(activity, ReviewEditorActivity.class).putExtra("placeId", placeId);
        if (review != null) {
            intent.putExtra("editing", true).putExtra("stars", review.stars)
                    .putExtra("comment", review.comment).putExtra("imageUrl", review.imageUrl);
        }
        return intent;
    }

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> { if (uri != null) readImage(uri); });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_review_editor);
        UiInsets.apply(this);
        placeId = getIntent().getLongExtra("placeId", 0);
        editing = getIntent().getBooleanExtra("editing", false);
        if (placeId <= 0) { finish(); return; }
        ((MaterialToolbar) findViewById(R.id.reviewEditorToolbar)).setNavigationOnClickListener(v -> finish());
        rating = findViewById(R.id.reviewRatingBar);
        comment = findViewById(R.id.reviewCommentInput);
        preview = findViewById(R.id.reviewSelectedImage);
        rating.setRating(getIntent().getIntExtra("stars", 0));
        rating.setOnRatingBarChangeListener((bar, stars, fromUser) ->
                ((TextView) findViewById(R.id.reviewStarsLabel)).setText((int) stars + " de 5 estrellas"));
        ((TextView) findViewById(R.id.reviewStarsLabel)).setText((int) rating.getRating() + " de 5 estrellas");
        comment.setText(getIntent().getStringExtra("comment"));
        String oldImageUrl = getIntent().getStringExtra("imageUrl");
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            preview.setVisibility(View.VISIBLE);
            findViewById(R.id.btnRemoveReviewImage).setVisibility(View.VISIBLE);
            RemoteImageLoader.load(preview, oldImageUrl, R.drawable.bg_input);
        }
        ((TextView) findViewById(R.id.btnSaveReview)).setText(
                editing ? "Guardar cambios" : "Publicar reseña");
        findViewById(R.id.btnZeroStars).setOnClickListener(v -> rating.setRating(0));
        findViewById(R.id.btnSelectReviewImage).setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btnRemoveReviewImage).setOnClickListener(v -> {
            imageBytes = null;
            removeImage = true;
            preview.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
        });
        findViewById(R.id.btnSaveReview).setOnClickListener(v -> save());
    }

    private void readImage(Uri uri) {
        String type = getContentResolver().getType(uri);
        if (type == null || !(type.equals("image/jpeg") || type.equals("image/png") ||
                type.equals("image/webp"))) {
            showError("Elige una imagen JPG, PNG o WebP.");
            return;
        }
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalStateException();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                if (output.size() > 5L * 1024 * 1024) {
                    showError("La imagen no debe superar 5 MB.");
                    return;
                }
            }
            imageBytes = output.toByteArray();
            mimeType = type;
            removeImage = false;
            preview.setTag(null);
            preview.setImageURI(uri);
            preview.setVisibility(View.VISIBLE);
            findViewById(R.id.btnRemoveReviewImage).setVisibility(View.VISIBLE);
        } catch (Exception error) { showError("No se pudo leer la imagen."); }
    }

    private void save() {
        String text = comment.getText() == null ? "" : comment.getText().toString().trim();
        if (text.length() > 2000) { showError("El comentario es demasiado largo."); return; }
        findViewById(R.id.btnSaveReview).setEnabled(false);
        ReviewRepository.save(this, placeId, editing, (int) rating.getRating(), text,
                imageBytes, mimeType, removeImage, new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> { setResult(RESULT_OK); finish(); });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> {
                            findViewById(R.id.btnSaveReview).setEnabled(true);
                            showError(error);
                        });
                    }
                });
    }

    private void showError(String message) {
        Snackbar.make(findViewById(R.id.btnSaveReview), message, Snackbar.LENGTH_LONG).show();
    }
}
