package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NuevoLugarActivity extends AppCompatActivity {
    private List<PlaceCategory> categories = PlaceCategory.defaults();
    private String selectedCategory;
    private AutoCompleteTextView type;
    private EditText name;
    private EditText address;
    private EditText description;
    private MaterialButton saveButton;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_lugar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return insets;
        });
        latitude = getIntent().getDoubleExtra("latitude", Double.NaN);
        longitude = getIntent().getDoubleExtra("longitude", Double.NaN);
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle(R.string.add_place_title);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(view -> finish());
        type = findViewById(R.id.etPlaceType);
        name = findViewById(R.id.etPlaceName);
        address = findViewById(R.id.etPlaceAddress);
        description = findViewById(R.id.etPlaceDescription);
        saveButton = findViewById(R.id.btnSavePlace);
        ((TextView) findViewById(R.id.tvPlaceCoordinates)).setText(String.format(
                Locale.US, "%.6f, %.6f", latitude, longitude));
        bindCategories();
        saveButton.setOnClickListener(view -> savePlace());
        ApiClient.getAuthorized(ApiConfig.PLACES_BASE_URL + "/categorias",
                new SessionManager(this).getToken(), new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            try {
                                categories = PlaceCategory.fromJson(response);
                                bindCategories();
                            } catch (Exception error) { showLoadError(); }
                        });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> showLoadError());
                    }
                });
    }

    private void bindCategories() {
        List<String> labels = new ArrayList<>();
        for (PlaceCategory category : categories) labels.add(category.name);
        type.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, labels));
        type.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = categories.get(position).code;
            type.setError(null);
        });
        for (PlaceCategory category : categories) {
            if (category.code.equals(selectedCategory)) {
                type.setText(category.name, false);
                return;
            }
        }
        selectedCategory = null;
        type.setText("", false);
    }

    private void savePlace() {
        if (selectedCategory == null) {
            type.setError("Selecciona una categoría");
            type.requestFocus();
            return;
        }
        String placeName = name.getText().toString().trim();
        if (placeName.isEmpty()) {
            name.setError("Ingresa el nombre");
            name.requestFocus();
            return;
        }
        try {
            JSONObject data = new JSONObject()
                    .put("tipo", selectedCategory)
                    .put("nombre", placeName)
                    .put("direccion", address.getText().toString().trim())
                    .put("descripcion", description.getText().toString().trim())
                    .put("latitud", latitude)
                    .put("longitud", longitude);
            saveButton.setEnabled(false);
            ApiClient.postAuthorized(ApiConfig.PLACES_BASE_URL,
                    new SessionManager(this).getToken(), data, new ApiClient.ApiCallback() {
                        @Override public void onSuccess(String response) {
                            runOnUiThread(() -> {
                                Intent result = new Intent()
                                        .putExtra("latitude", latitude)
                                        .putExtra("longitude", longitude)
                                        .putExtra("categoryCode", selectedCategory);
                                setResult(RESULT_OK, result);
                                finish();
                            });
                        }
                        @Override public void onError(String error) {
                            runOnUiThread(() -> {
                                saveButton.setEnabled(true);
                                Snackbar.make(findViewById(R.id.main), error, Snackbar.LENGTH_LONG).show();
                            });
                        }
                    });
        } catch (Exception error) {
            saveButton.setEnabled(true);
        }
    }

    private void showLoadError() {
        Snackbar.make(findViewById(R.id.main), R.string.categories_load_error, Snackbar.LENGTH_LONG).show();
    }
}
