package com.example.appmovil;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "sesion_app_mapas";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_PROFILE_IMAGE = "profile_image";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(int userId, String userName, String email, String token) {
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getInt(KEY_USER_ID, 0) != userId) editor.remove(KEY_PROFILE_IMAGE);
        editor
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_EMAIL, email)
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public int getUserId() { return preferences.getInt(KEY_USER_ID, 0); }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, "");
    }

    public String getProfileImage() {
        return preferences.getString(KEY_PROFILE_IMAGE, "");
    }

    public void updateProfile(String userName, String email, String profileImage) {
        SharedPreferences.Editor editor = preferences.edit()
                .putString(KEY_USER_NAME, userName)
                .putString(KEY_EMAIL, email);
        if (profileImage != null) {
            editor.putString(KEY_PROFILE_IMAGE, profileImage);
        }
        editor.apply();
    }
    public void setRole(String role) { preferences.edit().putString(KEY_ROLE,role == null ? "USUARIO" : role).apply(); }
    public String getRole() { return preferences.getString(KEY_ROLE,"USUARIO"); }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
