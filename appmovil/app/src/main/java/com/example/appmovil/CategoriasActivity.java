package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CategoriasActivity extends AppCompatActivity {
    private ChipGroup chipGroup;
    private final Set<String> selectedCodes = new LinkedHashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_categorias);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle(R.string.categories_title);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(view -> finish());
        chipGroup = findViewById(R.id.categoryChipGroup);
        ArrayList<String> initial = getIntent().getStringArrayListExtra("categoryCodes");
        if (initial != null) selectedCodes.addAll(initial);
        else {
            String oldCode = getIntent().getStringExtra("categoryCode");
            if (oldCode != null && !oldCode.isEmpty()) selectedCodes.add(oldCode);
        }
        renderCategories(PlaceCategory.defaults(), false);
        findViewById(R.id.btnApplyFilter).setOnClickListener(view -> {
            Intent result = new Intent()
                    .putStringArrayListExtra("categoryCodes", new ArrayList<>(selectedCodes));
            setResult(RESULT_OK, result);
            finish();
        });

        ApiClient.getAuthorized(ApiConfig.PLACES_BASE_URL + "/categorias",
                new SessionManager(this).getToken(), new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            try { renderCategories(PlaceCategory.fromJson(response), true); }
                            catch (Exception error) { showLoadError(); }
                        });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> showLoadError());
                    }
                });
    }

    private void renderCategories(List<PlaceCategory> categories, boolean authoritative) {
        chipGroup.removeAllViews();
        addChip("", getString(R.string.all_places));
        for (PlaceCategory category : categories) addChip(category.code, category.name);
        Set<String> availableCodes = new LinkedHashSet<>();
        for (PlaceCategory category : categories) availableCodes.add(category.code);
        if (authoritative) selectedCodes.retainAll(availableCodes);
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            String code = (String) chip.getTag();
            chip.setChecked(code.isEmpty() ? selectedCodes.isEmpty() : selectedCodes.contains(code));
        }
    }

    private void addChip(String code, String name) {
        Chip chip = new Chip(this);
        chip.setId(android.view.View.generateViewId());
        chip.setCheckable(true);
        chip.setText(name);
        chip.setTag(code);
        chip.setOnClickListener(view -> {
            if (code.isEmpty()) {
                selectedCodes.clear();
                for (int i = 1; i < chipGroup.getChildCount(); i++) {
                    ((Chip) chipGroup.getChildAt(i)).setChecked(false);
                }
            }
            else if (chip.isChecked()) selectedCodes.add(code);
            else selectedCodes.remove(code);
            ((Chip) chipGroup.getChildAt(0)).setChecked(selectedCodes.isEmpty());
        });
        chipGroup.addView(chip);
    }

    private void showLoadError() {
        Snackbar.make(findViewById(R.id.main), R.string.categories_load_error, Snackbar.LENGTH_LONG).show();
    }
}
