package com.example.appmovil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import java.util.Locale;
import java.util.List;

final class PlaceDetailSheet {
    private final Activity activity;
    private final Place place;
    private final BottomSheetDialog dialog;
    private final View content;
    private boolean refreshOnResume;

    private PlaceDetailSheet(Activity activity, Place place) {
        this.activity = activity;
        this.place = place;
        dialog = new BottomSheetDialog(activity);
        content = activity.getLayoutInflater().inflate(R.layout.sheet_place_reviews, null);
        ((TextView) content.findViewById(R.id.tvPlaceDetailName)).setText(place.name);
        ((TextView) content.findViewById(R.id.tvPlaceDetailType)).setText(place.categoryName);
        ((TextView) content.findViewById(R.id.tvPlaceDetailAddress)).setText(
                place.address.isEmpty() ? "Dirección no indicada" : place.address);
        ((TextView) content.findViewById(R.id.tvPlaceDetailDescription)).setText(
                place.description.isEmpty() ? "Sin descripción todavía." : place.description);
        content.findViewById(R.id.btnClosePlaceDetail).setOnClickListener(view -> dialog.dismiss());
        TextView placeAction=content.findViewById(R.id.btnReportPlace);
        if("ADMIN".equalsIgnoreCase(new SessionManager(activity).getRole())) {
            placeAction.setText("Eliminar lugar");
            placeAction.setOnClickListener(view -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
                    .setTitle("Eliminar lugar").setMessage("También se eliminarán todas sus reseñas e imágenes asociadas.")
                    .setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->deleteAsAdmin()).show());
        } else placeAction.setOnClickListener(view -> ContentReportDialog.show(activity, content, "LUGAR", place.id));
        content.findViewById(R.id.btnAllReviews).setOnClickListener(view -> {
            refreshOnResume = true;
            activity.startActivity(new Intent(activity, ReviewsActivity.class)
                    .putExtra("placeId", place.id).putExtra("placeName", place.name));
        });
        content.findViewById(R.id.btnWriteReview).setOnClickListener(view -> openEditor());
        dialog.setContentView(content);
        dialog.show();
        View bottom = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottom != null) bottom.setBackgroundColor(Color.TRANSPARENT);
        refresh();
    }

    static PlaceDetailSheet show(Activity activity, Place place) {
        return new PlaceDetailSheet(activity, place);
    }

    boolean isShowing() { return dialog.isShowing(); }

    void refreshAfterChangesOnResume() {
        if (!refreshOnResume) return;
        refreshOnResume = false;
        refresh();
    }

    void refresh() {
        ReviewRepository.photos(activity, place.id, new ReviewRepository.Callback<List<ReviewRepository.Photo>>() {
            @Override public void success(List<ReviewRepository.Photo> photos) {
                activity.runOnUiThread(() -> {
                    if (!dialog.isShowing()) return;
                    LinearLayout gallery = content.findViewById(R.id.placeGalleryItems);
                    gallery.removeAllViews();
                    for (int i = 0; i < photos.size(); i++) {
                        int selected = i;
                        ReviewRepository.Photo photo = photos.get(i);
                        View tile = activity.getLayoutInflater().inflate(R.layout.view_place_photo,
                                gallery, false);
                        ImageView image = tile.findViewById(R.id.placePhotoThumb);
                        RemoteImageLoader.load(image, photo.imageUrl, R.drawable.bg_input);
                        ImageView avatar = tile.findViewById(R.id.placePhotoAuthorAvatar);
                        avatar.setClipToOutline(true);
                        RemoteImageLoader.load(avatar, photo.avatarUrl, R.drawable.ic_person_circle);
                        ((TextView) tile.findViewById(R.id.placePhotoAuthorName)).setText(photo.author);
                        tile.setOnClickListener(view -> activity.startActivity(
                                PhotoViewerActivity.intentWithAuthors(activity, photos, selected, place.name)));
                        gallery.addView(tile);
                    }
                    int visibility = photos.isEmpty() ? View.GONE : View.VISIBLE;
                    content.findViewById(R.id.tvPlacePhotosTitle).setVisibility(visibility);
                    content.findViewById(R.id.placeGalleryScroll).setVisibility(visibility);
                });
            }
            @Override public void error(String message) {
                activity.runOnUiThread(() -> {
                    if (dialog.isShowing()) Snackbar.make(content, message, Snackbar.LENGTH_LONG).show();
                });
            }
        });
        ReviewRepository.list(activity, place.id, 0, 3, new ReviewRepository.Callback<ReviewRepository.Page>() {
            @Override public void success(ReviewRepository.Page page) {
                activity.runOnUiThread(() -> {
                    if (!dialog.isShowing()) return;
                    ((TextView) content.findViewById(R.id.tvPlaceRating)).setText(page.total == 0
                            ? "Sin calificaciones todavía"
                            : String.format(Locale.getDefault(), "★ %.1f/5 · %d reseña%s",
                                    page.average, page.total, page.total == 1 ? "" : "s"));
                    LinearLayout list = content.findViewById(R.id.reviewPreviewList);
                    list.removeAllViews();
                    for (ReviewRepository.Review review : page.reviews) {
                        list.addView(ReviewCardBinder.create(activity, list, place.id, review,
                                PlaceDetailSheet.this::refresh, () -> refreshOnResume = true));
                    }
                    content.findViewById(R.id.tvReviewsEmpty).setVisibility(
                            page.total == 0 ? View.VISIBLE : View.GONE);
                    content.findViewById(R.id.btnAllReviews).setVisibility(
                            page.total > 3 ? View.VISIBLE : View.GONE);
                });
            }
            @Override public void error(String message) {
                activity.runOnUiThread(() -> {
                    if (dialog.isShowing()) Snackbar.make(content, message, Snackbar.LENGTH_LONG).show();
                });
            }
        });
        if (!new SessionManager(activity).getToken().isEmpty()) {
            ReviewRepository.mine(activity, place.id, new ReviewRepository.Callback<ReviewRepository.Review>() {
                @Override public void success(ReviewRepository.Review review) {
                    activity.runOnUiThread(() -> ((TextView) content.findViewById(R.id.btnWriteReview))
                            .setText(review == null ? "Escribir reseña" : "Editar mi reseña"));
                }
                @Override public void error(String message) { }
            });
        }
    }

    private void openEditor() {
        if (new SessionManager(activity).getToken().isEmpty()) {
            Snackbar.make(content, "Inicia sesión para reseñar un lugar", Snackbar.LENGTH_LONG).show();
            return;
        }
        ReviewRepository.mine(activity, place.id, new ReviewRepository.Callback<ReviewRepository.Review>() {
            @Override public void success(ReviewRepository.Review review) {
                activity.runOnUiThread(() -> {
                    refreshOnResume = true;
                    activity.startActivity(ReviewEditorActivity.intent(activity, place.id, review));
                });
            }
            @Override public void error(String message) {
                activity.runOnUiThread(() -> Snackbar.make(content, message, Snackbar.LENGTH_LONG).show());
            }
        });
    }

    private void deleteAsAdmin() {
        ApiClient.deleteAuthorized(ApiConfig.ADMIN_BASE_URL+"/lugares/"+place.id,
                new SessionManager(activity).getToken(),new ApiClient.ApiCallback(){
                    public void onSuccess(String response){activity.runOnUiThread(()->{dialog.dismiss();if(activity instanceof MainActivity)((MainActivity)activity).refreshMapContent();Snackbar.make(activity.findViewById(android.R.id.content),"Lugar eliminado",Snackbar.LENGTH_LONG).show();});}
                    public void onError(String error){activity.runOnUiThread(()->Snackbar.make(content,error,Snackbar.LENGTH_LONG).show());}
                });
    }
}
