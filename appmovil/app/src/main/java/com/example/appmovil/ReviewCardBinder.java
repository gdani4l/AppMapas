package com.example.appmovil;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.util.Collections;

final class ReviewCardBinder {
    static View create(Activity activity, ViewGroup parent, long placeId,
                       ReviewRepository.Review review, Runnable refresh, Runnable beforeEditor) {
        View card = activity.getLayoutInflater().inflate(R.layout.view_review_card, parent, false);
        ((TextView) card.findViewById(R.id.reviewAuthor)).setText(review.author);
        ((TextView) card.findViewById(R.id.reviewDate)).setText(
                review.date.length() >= 10 ? review.date.substring(0, 10) : "");
        ((TextView) card.findViewById(R.id.reviewStars)).setText("★ " + review.stars + "/5");
        TextView comment = card.findViewById(R.id.reviewComment);
        if (review.comment.isEmpty()) comment.setVisibility(View.GONE);
        else comment.setText(review.comment);
        ImageView avatar = card.findViewById(R.id.reviewAvatar);
        avatar.setClipToOutline(true);
        RemoteImageLoader.load(avatar, review.avatarUrl, R.drawable.ic_person_circle);
        if (!review.imageUrl.isEmpty()) {
            ImageView image = card.findViewById(R.id.reviewImage);
            image.setVisibility(View.VISIBLE);
            RemoteImageLoader.load(image, review.imageUrl, R.drawable.bg_input);
            image.setOnClickListener(view -> activity.startActivity(PhotoViewerActivity.intent(
                    activity, Collections.singletonList(review.imageUrl), 0, review.author)));
        }
        card.findViewById(R.id.btnLikeReview).setOnClickListener(view ->
                Snackbar.make(card, "Me gusta estará disponible próximamente", Snackbar.LENGTH_SHORT).show());
        card.findViewById(R.id.btnReportReview).setOnClickListener(view ->
                ContentReportDialog.show(activity, card, "RESENA", review.id));
        if (new SessionManager(activity).getUserId() == review.userId) {
            card.findViewById(R.id.reviewOwnerActions).setVisibility(View.VISIBLE);
            card.findViewById(R.id.btnEditReview).setOnClickListener(view -> {
                beforeEditor.run();
                activity.startActivity(ReviewEditorActivity.intent(activity, placeId, review));
            });
            card.findViewById(R.id.btnDeleteReview).setOnClickListener(view ->
                    new MaterialAlertDialogBuilder(activity)
                            .setTitle("Eliminar reseña")
                            .setMessage("¿Quieres eliminar tu reseña y su imagen?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar", (dialog, which) ->
                                    ReviewRepository.delete(activity, placeId, new ApiClient.ApiCallback() {
                                        @Override public void onSuccess(String response) {
                                            activity.runOnUiThread(refresh);
                                        }
                                        @Override public void onError(String error) {
                                            activity.runOnUiThread(() -> Snackbar.make(parent,
                                                    error, Snackbar.LENGTH_LONG).show());
                                        }
                                    })).show());
        }
        return card;
    }
}
