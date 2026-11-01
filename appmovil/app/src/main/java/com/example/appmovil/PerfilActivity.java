package com.example.appmovil;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerfilActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private SessionManager sessionManager;
    private ImageView profileImage;
    private EditText firstName;
    private EditText secondName;
    private EditText lastName;
    private EditText secondLastName;
    private EditText email;
    private EditText birthDate;
    private EditText district;
    private EditText phone;
    private EditText address;
    private MaterialButton saveButton;
    private MaterialButton changeImageButton;
    private CircularProgressIndicator progress;
    private boolean profileLoaded;
    private int imageRequestVersion;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            this::uploadSelectedImage
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        sessionManager = new SessionManager(this);
        if (sessionManager.getToken().isEmpty()) {
            volverAlLogin();
            return;
        }

        bindViews();
        configureToolbar();
        birthDate.setOnClickListener(view -> showDatePicker());
        changeImageButton.setOnClickListener(view -> imagePicker.launch("image/*"));
        saveButton.setOnClickListener(view -> saveProfile());
        loadProfile();
    }

    private void bindViews() {
        profileImage = findViewById(R.id.ivProfileImage);
        firstName = findViewById(R.id.etFirstName);
        secondName = findViewById(R.id.etSecondName);
        lastName = findViewById(R.id.etLastName);
        secondLastName = findViewById(R.id.etSecondLastName);
        email = findViewById(R.id.etProfileEmail);
        birthDate = findViewById(R.id.etBirthDate);
        district = findViewById(R.id.etDistrict);
        phone = findViewById(R.id.etPhone);
        address = findViewById(R.id.etAddress);
        saveButton = findViewById(R.id.btnSaveProfile);
        changeImageButton = findViewById(R.id.btnChangeImage);
        progress = findViewById(R.id.profileProgress);
    }

    private void configureToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle(R.string.profile_title);
        toolbar.setSubtitle(R.string.profile_subtitle);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    private void loadProfile() {
        setLoading(true);
        ApiClient.getAuthorized(
                ApiConfig.PROFILE_BASE_URL,
                sessionManager.getToken(),
                new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(String respuesta) {
                        runOnUiThread(() -> {
                            try {
                                showProfile(new JSONObject(respuesta));
                            } catch (Exception e) {
                                showError("No se pudo interpretar el perfil.");
                            }
                            setLoading(false);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            showError(error);
                        });
                    }
                }
        );
    }

    private void showProfile(JSONObject json) {
        profileLoaded = true;
        firstName.setText(value(json, "primerNombre"));
        secondName.setText(value(json, "segundoNombre"));
        lastName.setText(value(json, "apellidoPaterno"));
        secondLastName.setText(value(json, "apellidoMaterno"));
        email.setText(value(json, "correo"));
        birthDate.setText(value(json, "fechaNacimiento"));
        district.setText(value(json, "distrito"));
        phone.setText(value(json, "telefono"));
        address.setText(value(json, "direccion"));

        String imageUrl = value(json, "imagenPerfilUrl");
        sessionManager.updateProfile(buildFullName(), email.getText().toString(), imageUrl);
        loadProfileImage(imageUrl);
    }

    private void saveProfile() {
        String first = text(firstName);
        String paternal = text(lastName);
        String emailValue = text(email);

        if (first.isEmpty()) {
            firstName.setError("Ingresa tu primer nombre");
            firstName.requestFocus();
            return;
        }
        if (paternal.isEmpty()) {
            lastName.setError("Ingresa tu apellido paterno");
            lastName.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            email.setError("Ingresa un correo válido");
            email.requestFocus();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("primerNombre", first);
            putOptional(body, "segundoNombre", text(secondName));
            body.put("apellidoPaterno", paternal);
            putOptional(body, "apellidoMaterno", text(secondLastName));
            body.put("correo", emailValue);
            putOptional(body, "fechaNacimiento", text(birthDate));
            putOptional(body, "distrito", text(district));
            putOptional(body, "telefono", text(phone));
            putOptional(body, "direccion", text(address));

            setLoading(true);
            ApiClient.putAuthorized(
                    ApiConfig.PROFILE_BASE_URL,
                    sessionManager.getToken(),
                    body,
                    new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(String respuesta) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                try {
                                    JSONObject updated = new JSONObject(respuesta);
                                    showProfile(updated);
                                    Toast.makeText(
                                            PerfilActivity.this,
                                            R.string.profile_saved,
                                            Toast.LENGTH_LONG
                                    ).show();
                                } catch (Exception e) {
                                    showError("El perfil se guardó, pero no se pudo actualizar la pantalla.");
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                showError(error);
                            });
                        }
                    }
            );
        } catch (Exception e) {
            showError("No se pudieron preparar los datos del perfil.");
        }
    }

    private void uploadSelectedImage(Uri uri) {
        if (uri == null) {
            return;
        }

        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                showError(getString(R.string.select_valid_image));
                return;
            }
            byte[] bytes = readImageBytes(input);
            if (bytes == null || bytes.length == 0) {
                showError(getString(R.string.select_valid_image));
                return;
            }

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                showError(getString(R.string.select_valid_image));
                return;
            }

            setLoading(true);
            ApiClient.uploadProfileImage(
                    ApiConfig.PROFILE_BASE_URL + "/imagen",
                    sessionManager.getToken(),
                    bytes,
                    mimeType,
                    new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(String respuesta) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                try {
                                    JSONObject json = new JSONObject(respuesta);
                                    String imageUrl = json.optString("imagenPerfilUrl", "");
                                    if (imageUrl.isEmpty() || "null".equals(imageUrl)) {
                                        throw new IllegalStateException("Respuesta sin URL de imagen");
                                    }
                                    sessionManager.updateProfile(
                                            buildFullName(),
                                            text(email),
                                            imageUrl
                                    );
                                    profileImage.setPadding(0, 0, 0, 0);
                                    profileImage.setImageURI(uri);
                                    loadProfileImage(imageUrl);
                                    Toast.makeText(
                                            PerfilActivity.this,
                                            json.optString("mensaje", "Imagen actualizada"),
                                            Toast.LENGTH_LONG
                                    ).show();
                                } catch (Exception e) {
                                    showError("La imagen se subió, pero no se pudo mostrar.");
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                setLoading(false);
                                showError(error);
                            });
                        }
                    }
            );
        } catch (Exception e) {
            showError(getString(R.string.select_valid_image));
        }
    }

    private byte[] readImageBytes(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            total += read;
            if (total > MAX_IMAGE_BYTES) {
                return null;
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private void loadProfileImage(String imageUrl) {
        final int requestVersion = ++imageRequestVersion;
        if (TextUtils.isEmpty(imageUrl)) {
            profileImage.setImageResource(R.drawable.ic_person_circle);
            profileImage.setPadding(20, 20, 20, 20);
            return;
        }

        imageExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept", "image/jpeg,image/png,image/webp");
                if (connection.getResponseCode() != 200) {
                    throw new java.io.IOException("No se pudo descargar la imagen");
                }
                Bitmap bitmap;
                try (InputStream input = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(input);
                }
                if (bitmap == null) {
                    throw new java.io.IOException("Formato de imagen no compatible");
                }
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing() || requestVersion != imageRequestVersion) return;
                    profileImage.setPadding(0, 0, 0, 0);
                    profileImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    profileImage.setImageBitmap(bitmap);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isDestroyed() || isFinishing() || requestVersion != imageRequestVersion) return;
                    showError("La foto está guardada, pero no se pudo descargar. Revisa tu conexión y vuelve a abrir el perfil.");
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> birthDate.setText(String.format(
                        Locale.US,
                        "%04d-%02d-%02d",
                        year,
                        month + 1,
                        day
                )),
                calendar.get(Calendar.YEAR) - 18,
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private String buildFullName() {
        StringBuilder name = new StringBuilder(text(firstName));
        if (!text(secondName).isEmpty()) {
            name.append(' ').append(text(secondName));
        }
        if (!text(lastName).isEmpty()) {
            name.append(' ').append(text(lastName));
        }
        return name.toString().trim();
    }

    private String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private String value(JSONObject json, String key) {
        return json.isNull(key) ? "" : json.optString(key, "");
    }

    private void putOptional(JSONObject json, String key, String value) throws Exception {
        json.put(key, value.isEmpty() ? JSONObject.NULL : value);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading && profileLoaded);
        changeImageButton.setEnabled(!loading && profileLoaded);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void volverAlLogin() {
        sessionManager.clear();
        Intent intent = new Intent(this, login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdownNow();
        super.onDestroy();
    }
}
