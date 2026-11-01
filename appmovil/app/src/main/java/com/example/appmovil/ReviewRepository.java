package com.example.appmovil;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class ReviewRepository {
    interface Callback<T> { void success(T value); void error(String message); }

    static final class Review {
        final long id;
        final int userId;
        final String author;
        final String avatarUrl;
        final int stars;
        final String comment;
        final String imageUrl;
        final String date;

        Review(JSONObject json) {
            id = json.optLong("idResena");
            userId = json.optInt("idUsuario");
            author = json.optString("nombreUsuario", "Usuario");
            avatarUrl = nullableString(json, "imagenPerfilUrl");
            stars = json.optInt("estrellas");
            comment = nullableString(json, "comentario");
            imageUrl = nullableString(json, "imagenUrl");
            date = json.optString("fechaCreacion", "");
        }
    }

    private static String nullableString(JSONObject json, String key) {
        return json.isNull(key) ? "" : json.optString(key, "");
    }

    static final class Page {
        final long total;
        final double average;
        final List<Review> reviews;
        Page(JSONObject json) throws Exception {
            total = json.getLong("total");
            average = json.optDouble("promedio");
            reviews = new ArrayList<>();
            JSONArray array = json.getJSONArray("resenas");
            for (int i = 0; i < array.length(); i++) reviews.add(new Review(array.getJSONObject(i)));
        }
    }

    static final class Photo {
        final String imageUrl;
        final String author;
        final String avatarUrl;

        Photo(JSONObject json) {
            imageUrl = nullableString(json, "imagenUrl");
            author = json.optString("nombreUsuario", "Usuario");
            avatarUrl = nullableString(json, "imagenPerfilUrl");
        }
    }

    static void list(Context context, long placeId, int page, int limit, Callback<Page> callback) {
        String endpoint = ApiConfig.PLACES_BASE_URL + "/" + placeId
                + "/resenas?pagina=" + page + "&limite=" + limit;
        ApiClient.getAuthorized(endpoint, new SessionManager(context).getToken(),
                new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        try { callback.success(new Page(new JSONObject(response))); }
                        catch (Exception error) { callback.error("Respuesta de reseñas no válida"); }
                    }
                    @Override public void onError(String error) { callback.error(error); }
                });
    }

    static void photos(Context context, long placeId, Callback<List<Photo>> callback) {
        ApiClient.getAuthorized(ApiConfig.PLACES_BASE_URL + "/" + placeId + "/resenas/fotos",
                new SessionManager(context).getToken(), new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        try {
                            JSONArray array = new JSONArray(response);
                            List<Photo> photos = new ArrayList<>();
                            for (int i = 0; i < array.length(); i++) {
                                Photo photo = new Photo(array.getJSONObject(i));
                                if (photo.imageUrl.startsWith("https://")) photos.add(photo);
                            }
                            callback.success(photos);
                        } catch (Exception error) { callback.error("No se pudieron leer las fotos"); }
                    }
                    @Override public void onError(String error) { callback.error(error); }
                });
    }

    static void save(Context context, long placeId, boolean editing, int stars, String comment,
                     byte[] image, String mimeType, boolean removeImage,
                     ApiClient.ApiCallback callback) {
        ApiClient.uploadReview(editing ? "PUT" : "POST",
                ApiConfig.PLACES_BASE_URL + "/" + placeId + "/resenas",
                new SessionManager(context).getToken(), stars, comment, image, mimeType,
                removeImage, callback);
    }

    static void mine(Context context, long placeId, Callback<Review> callback) {
        ApiClient.getAuthorized(ApiConfig.PLACES_BASE_URL + "/" + placeId + "/resenas/mia",
                new SessionManager(context).getToken(), new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        try {
                            callback.success(response.isBlank() ? null
                                    : new Review(new JSONObject(response)));
                        } catch (Exception error) { callback.error("No se pudo leer tu reseña"); }
                    }
                    @Override public void onError(String error) { callback.error(error); }
                });
    }

    static void delete(Context context, long placeId, ApiClient.ApiCallback callback) {
        ApiClient.deleteAuthorized(ApiConfig.PLACES_BASE_URL + "/" + placeId + "/resenas",
                new SessionManager(context).getToken(), callback);
    }
}
