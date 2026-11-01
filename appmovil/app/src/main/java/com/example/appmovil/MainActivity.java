package com.example.appmovil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.module.http.HttpRequestUtil;
import org.maplibre.android.style.layers.Layer;
import org.maplibre.android.style.layers.Property;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.layers.SymbolLayer;
import android.graphics.Bitmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final LatLng LIMA = new LatLng(-12.0464, -77.0428);
    private static final float DEFAULT_ZOOM = 16f;
    private static final String OSM_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty";
    private static final String OSM_ATTRIBUTION_URL = "https://www.openstreetmap.org/copyright";
    private static final String USER_AGENT = "AppDeMapasUniversitaria/1.0 (com.example.appmovil)";
    private static final long SEARCH_DEBOUNCE_MS = 550L;
    private static final long PLACES_DEBOUNCE_MS = 350L;

    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private MapMarkerRenderer markers;
    private PlaceDetailSheet activeDetailSheet;
    private final Map<Long, Place> visiblePlaces = new HashMap<>();
    private boolean mapStyleReady;
    private FusedLocationProviderClient fusedLocationClient;
    private ExecutorService searchExecutor;
    private ExecutorService profileImageExecutor;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private EditText mapSearch;
    private ImageView toolbarAvatar;
    private LinearLayout searchSuggestions;
    private Runnable pendingSearch;
    private Runnable pendingPlacesLoad;
    private int suggestionVersion;
    private boolean suppressSuggestions;
    private final Map<String, List<SearchResult>> searchCache = new HashMap<>();
    private boolean searchAvenues;
    private final Set<String> selectedTypes = new LinkedHashSet<>();
    private int placesRequestVersion;
    private int profileImageVersion;
    private String avatarRequestedUrl = "";
    private Bitmap avatarBitmap;
    private double loadedLatitude = Double.NaN;
    private double loadedLongitude;
    private double loadedSpan;
    private String loadedFilterKey;
    private long lastPlacesErrorAt;
    private org.maplibre.android.maps.MapLibreMap.OnMapClickListener placePointListener;
    private Snackbar placePointPrompt;

    private final ActivityResultLauncher<Intent> categoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                    updateQuickFilters();
                    return;
                }
                selectedTypes.clear();
                ArrayList<String> codes = result.getData().getStringArrayListExtra("categoryCodes");
                if (codes != null) selectedTypes.addAll(codes);
                updateQuickFilters();
                loadVisiblePlaces(true);
            });

    private final ActivityResultLauncher<Intent> addPlaceLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Intent data = result.getData();
                selectedTypes.clear();
                updateQuickFilters();
                LatLng point = new LatLng(data.getDoubleExtra("latitude", LIMA.getLatitude()),
                        data.getDoubleExtra("longitude", LIMA.getLongitude()));
                mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, DEFAULT_ZOOM));
                loadVisiblePlaces(true);
                Snackbar.make(findViewById(R.id.main), R.string.place_added, Snackbar.LENGTH_LONG).show();
            });

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean fineGranted = Boolean.TRUE.equals(
                                result.get(Manifest.permission.ACCESS_FINE_LOCATION)
                        );
                        boolean coarseGranted = Boolean.TRUE.equals(
                                result.get(Manifest.permission.ACCESS_COARSE_LOCATION)
                        );

                        if (fineGranted || coarseGranted) {
                            enableLocationAndCenterMap();
                        } else {
                            showLimaAndPermissionMessage();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(this);
        configureMapHttpClient();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        searchExecutor = Executors.newSingleThreadExecutor();
        profileImageExecutor = Executors.newSingleThreadExecutor();
        configureToolbar();
        configurePlaceholderActions();
        refreshProfileFromServer();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void configureMapHttpClient() {
        File cacheDirectory = new File(getCacheDir(), "openstreetmap_tiles");
        Cache tileCache = new Cache(cacheDirectory, 50L * 1024L * 1024L);
        OkHttpClient client = new OkHttpClient.Builder()
                .cache(tileCache)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("User-Agent", USER_AGENT)
                            .build();
                    return chain.proceed(request);
                })
                .build();
        HttpRequestUtil.setOkHttpClient(client);
    }

    private void configureToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setTitle(R.string.map_title);
        toolbar.setSubtitle(R.string.map_subtitle);
        toolbar.inflateMenu(R.menu.menu_main);
        FrameLayout avatarAction = new FrameLayout(this);
        avatarAction.setPadding(dp(4), dp(4), dp(4), dp(4));
        toolbarAvatar = new ImageView(this);
        toolbarAvatar.setImageResource(R.drawable.ic_person_circle);
        toolbarAvatar.setContentDescription(getString(R.string.menu_profile));
        toolbarAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        toolbarAvatar.setBackgroundResource(R.drawable.bg_profile_avatar);
        toolbarAvatar.setClipToOutline(true);
        avatarAction.addView(toolbarAvatar,
                new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.CENTER));
        avatarAction.setOnClickListener(view -> startActivity(new Intent(this, PerfilActivity.class)));
        toolbar.getMenu().findItem(R.id.action_profile).setActionView(avatarAction);

        updateToolbarGreeting(toolbar);
        updateProfileAvatar();

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                startActivity(new Intent(this, PerfilActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.action_logout) {
                new SessionManager(this).clear();
                Intent intent = new Intent(this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }

            showComingSoon();
            return true;
        });
    }

    private void updateToolbarGreeting(MaterialToolbar toolbar) {
        String userName = new SessionManager(this).getUserName();
        if (userName.isEmpty()) {
            userName = getIntent().getStringExtra("NOMBRE_USUARIO");
        }
        if (userName != null && !userName.trim().isEmpty()) {
            toolbar.setSubtitle("Hola, " + userName.trim());
        } else {
            toolbar.setSubtitle(R.string.map_subtitle);
        }
    }

    private void refreshProfileFromServer() {
        SessionManager session = new SessionManager(this);
        if (session.getToken().isEmpty()) return;
        ApiClient.getAuthorized(ApiConfig.PROFILE_BASE_URL, session.getToken(),
                new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            if (isFinishing()) return;
                            try {
                                JSONObject json = new JSONObject(response);
                                session.updateProfile(session.getUserName(),
                                        json.optString("correo", ""),
                                        json.optString("imagenPerfilUrl", ""));
                                updateProfileAvatar();
                            } catch (Exception ignored) { }
                        });
                    }
                    @Override public void onError(String error) { }
                });
    }

    private void updateProfileAvatar() {
        ImageView avatar = toolbarAvatar;
        if (avatar == null) return;
        String imageUrl = new SessionManager(this).getProfileImage();
        if (imageUrl == null || imageUrl.isEmpty()) {
            profileImageVersion++;
            avatarRequestedUrl = "";
            avatarBitmap = null;
            avatar.setImageResource(R.drawable.ic_person_circle);
            return;
        }
        if (imageUrl.equals(avatarRequestedUrl)) {
            if (avatarBitmap != null) avatar.setImageBitmap(avatarBitmap);
            return;
        }
        avatarRequestedUrl = imageUrl;
        avatarBitmap = null;
        int requestVersion = ++profileImageVersion;
        profileImageExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                if (connection.getResponseCode() != 200) throw new java.io.IOException("Imagen no disponible");
                Bitmap bitmap;
                try (InputStream input = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(input);
                }
                if (bitmap == null) throw new java.io.IOException("Imagen inválida");
                runOnUiThread(() -> {
                    if (!isFinishing() && requestVersion == profileImageVersion) {
                        avatarBitmap = bitmap;
                        avatar.setImageBitmap(bitmap);
                    }
                });
            } catch (Exception ignored) {
                runOnUiThread(() -> {
                    if (requestVersion == profileImageVersion) avatarRequestedUrl = "";
                });
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private void configurePlaceholderActions() {
        findViewById(R.id.btnReportZone).setOnClickListener(view -> showComingSoon());
        findViewById(R.id.btnAddPlace).setOnClickListener(view -> beginAddPlace());
        findViewById(R.id.chipNearby).setOnClickListener(view -> selectAllTypes());
        findViewById(R.id.chipHotels).setOnClickListener(view -> toggleType("HOTEL"));
        findViewById(R.id.chipRestaurants).setOnClickListener(view -> toggleType("RESTAURANTE"));
        findViewById(R.id.chipMoreCategories).setOnClickListener(view -> {
            Intent intent = new Intent(this, CategoriasActivity.class)
                    .putStringArrayListExtra("categoryCodes", new ArrayList<>(selectedTypes));
            categoryLauncher.launch(intent);
        });
        findViewById(R.id.fabMyLocation).setOnClickListener(view -> requestOrCenterLocation());
        /*findViewById(R.id.tvOsmAttribution).setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(OSM_ATTRIBUTION_URL)))
        );*/

        mapSearch = findViewById(R.id.etMapSearch);
        mapSearch.setHint(R.string.search_places_hint);
        searchSuggestions = findViewById(R.id.searchSuggestions);
        ((com.google.android.material.button.MaterialButtonToggleGroup) findViewById(R.id.mapSearchMode))
                .addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                    if (!isChecked) return;
                    searchAvenues = checkedId == R.id.btnSearchAvenues;
                    suggestionVersion++;
                    hideSuggestions();
                    mapSearch.setHint(searchAvenues ? R.string.search_avenues_hint : R.string.search_places_hint);
                    scheduleSuggestions(mapSearch.getText().toString().trim());
                });
        mapSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleSuggestions(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        mapSearch.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAddress();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        mapLibreMap = map;
        mapLibreMap.getUiSettings().setLogoEnabled(false);
        mapLibreMap.getUiSettings().setAttributionEnabled(false);
        mapLibreMap.setStyle(OSM_STYLE_URI, style -> {
            for (Layer layer : style.getLayers()) {
                if (layer instanceof SymbolLayer) {
                    String id = layer.getId().toLowerCase(Locale.ROOT);
                    if (!id.contains("road") && !id.contains("street") && !id.contains("highway")) {
                        layer.setProperties(PropertyFactory.visibility(Property.NONE));
                    }
                }
            }
            markers = new MapMarkerRenderer(this, mapLibreMap, style);
            mapStyleReady = true;
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LIMA, 11f));
            mapLibreMap.addOnCameraIdleListener(this::schedulePlacesLoad);
            mapLibreMap.addOnMapClickListener(point -> {
                if (placePointListener != null || markers == null) return false;
                Long id = markers.placeAt(point);
                Place place = id == null ? null : visiblePlaces.get(id);
                if (place == null) return false;
                showPlaceDetail(place);
                return true;
            });
            loadVisiblePlaces(false);
            requestOrCenterLocation();
        });
    }

    private void selectAllTypes() {
        selectedTypes.clear();
        updateQuickFilters();
        loadVisiblePlaces(true);
    }

    private void toggleType(String type) {
        if (!selectedTypes.add(type)) selectedTypes.remove(type);
        updateQuickFilters();
        loadVisiblePlaces(true);
    }

    private void updateQuickFilters() {
        ((Chip) findViewById(R.id.chipNearby)).setChecked(selectedTypes.isEmpty());
        ((Chip) findViewById(R.id.chipHotels)).setChecked(selectedTypes.contains("HOTEL"));
        ((Chip) findViewById(R.id.chipRestaurants)).setChecked(selectedTypes.contains("RESTAURANTE"));
        ((Chip) findViewById(R.id.chipMoreCategories)).setChecked(selectedTypes.stream()
                .anyMatch(type -> !type.equals("HOTEL") && !type.equals("RESTAURANTE")));
        ((Chip) findViewById(R.id.chipMoreCategories)).setText(selectedTypes.size() > 1
                ? getString(R.string.more_categories) + " (" + selectedTypes.size() + ")"
                : getString(R.string.more_categories));
    }

    private void schedulePlacesLoad() {
        if (pendingPlacesLoad != null) uiHandler.removeCallbacks(pendingPlacesLoad);
        pendingPlacesLoad = () -> loadVisiblePlaces(false);
        uiHandler.postDelayed(pendingPlacesLoad, PLACES_DEBOUNCE_MS);
    }

    private void loadVisiblePlaces(boolean force) {
        if (!mapStyleReady || mapLibreMap == null) return;
        if (mapLibreMap.getCameraPosition().zoom < MapMarkerRenderer.PLACE_MIN_ZOOM) return;
        LatLng center = mapLibreMap.getCameraPosition().target;
        if (center == null) return;
        double span = Math.max(0.006, Math.min(1.5,
                0.015 * Math.pow(2, 16 - mapLibreMap.getCameraPosition().zoom)));
        List<String> sortedTypes = new ArrayList<>(selectedTypes);
        java.util.Collections.sort(sortedTypes);
        String filterKey = String.join(",", sortedTypes);
        if (!force && filterKey.equals(loadedFilterKey)
                && Math.abs(center.getLatitude() - loadedLatitude) < loadedSpan * 0.5
                && Math.abs(center.getLongitude() - loadedLongitude) < loadedSpan * 0.5
                && span <= loadedSpan) return;
        loadedLatitude = center.getLatitude();
        loadedLongitude = center.getLongitude();
        loadedSpan = span;
        loadedFilterKey = filterKey;
        String endpoint = ApiConfig.PLACES_BASE_URL
                + "?sur=" + (center.getLatitude() - span)
                + "&norte=" + (center.getLatitude() + span)
                + "&oeste=" + (center.getLongitude() - span)
                + "&este=" + (center.getLongitude() + span)
                + (sortedTypes.isEmpty() ? "" : "&tipos=" + filterKey);
        int version = ++placesRequestVersion;
        ApiClient.getAuthorized(endpoint, new SessionManager(this).getToken(), new ApiClient.ApiCallback() {
            @Override public void onSuccess(String response) {
                runOnUiThread(() -> {
                    if (version != placesRequestVersion || isFinishing()) return;
                    try {
                        JSONArray json = new JSONArray(response);
                        List<Place> places = new ArrayList<>();
                        for (int i = 0; i < json.length(); i++) places.add(Place.fromJson(json.getJSONObject(i)));
                        renderPlaces(places);
                    } catch (Exception error) {
                        showPlacesError();
                    }
                });
            }
            @Override public void onError(String error) {
                runOnUiThread(() -> {
                    if (version == placesRequestVersion && !isFinishing()) {
                        loadedLatitude = Double.NaN;
                        showPlacesError();
                    }
                });
            }
        });
    }

    private void renderPlaces(List<Place> places) {
        visiblePlaces.clear();
        for (Place place : places) visiblePlaces.put(place.id, place);
        if (markers != null) markers.setPlaces(places);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showPlacesError() {
        long now = SystemClock.elapsedRealtime();
        if (now - lastPlacesErrorAt < 10000) return;
        lastPlacesErrorAt = now;
        Snackbar.make(findViewById(R.id.main), R.string.places_load_error, Snackbar.LENGTH_LONG).show();
    }

    private void showPlaceDetail(Place place) {
        activeDetailSheet = PlaceDetailSheet.show(this, place);
    }

    private void beginAddPlace() {
        if (new SessionManager(this).getToken().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Inicia sesión para registrar lugares.", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!mapStyleReady || mapLibreMap == null) return;
        if (placePointListener != null) {
            stopPickingPlace();
            return;
        }
        placePointPrompt = Snackbar.make(findViewById(R.id.main), R.string.select_place_point, Snackbar.LENGTH_INDEFINITE)
                .setAction("Cancelar", view -> stopPickingPlace());
        placePointPrompt.show();
        placePointListener = point -> {
            stopPickingPlace();
            addPlaceLauncher.launch(new Intent(this, NuevoLugarActivity.class)
                    .putExtra("latitude", point.getLatitude())
                    .putExtra("longitude", point.getLongitude()));
            return true;
        };
        mapLibreMap.addOnMapClickListener(placePointListener);
    }

    private void stopPickingPlace() {
        if (placePointListener != null) {
            mapLibreMap.removeOnMapClickListener(placePointListener);
            placePointListener = null;
        }
        if (placePointPrompt != null) {
            placePointPrompt.dismiss();
            placePointPrompt = null;
        }
    }

    private void requestOrCenterLocation() {
        if (hasLocationPermission()) {
            enableLocationAndCenterMap();
            return;
        }

        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void enableLocationAndCenterMap() {
        if (!mapStyleReady || mapLibreMap == null || !hasLocationPermission()) {
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            centerMap(location);
                        } else {
                            requestFreshLocation();
                        }
                    })
                    .addOnFailureListener(error -> showLocationUnavailable());
        } catch (SecurityException ignored) {
            showLimaAndPermissionMessage();
        }
    }

    private void requestFreshLocation() {
        if (!hasLocationPermission()) {
            showLimaAndPermissionMessage();
            return;
        }

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            centerMap(location);
                        } else {
                            showLocationUnavailable();
                        }
                    })
                    .addOnFailureListener(error -> showLocationUnavailable());
        } catch (SecurityException ignored) {
            showLimaAndPermissionMessage();
        }
    }

    private void centerMap(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (markers != null) markers.setLocation(currentPosition);
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, DEFAULT_ZOOM));
    }

    private void showLimaAndPermissionMessage() {
        if (mapLibreMap != null && mapStyleReady) {
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LIMA, 11f));
        }

        Snackbar.make(findViewById(R.id.main), R.string.location_permission_required, Snackbar.LENGTH_LONG)
                .setAction("PERMITIR", view -> requestOrCenterLocation())
                .show();
    }

    private void showLocationUnavailable() {
        Snackbar.make(findViewById(R.id.main), R.string.location_unavailable, Snackbar.LENGTH_LONG)
                .show();
    }

    private void showComingSoon() {
        Snackbar.make(findViewById(R.id.main), R.string.feature_coming_soon, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void searchAddress() {
        if (!mapStyleReady || mapLibreMap == null || mapLibreMap.getCameraPosition().target == null) {
            Snackbar.make(findViewById(R.id.main), "Espera a que cargue el mapa.", Snackbar.LENGTH_SHORT).show();
            return;
        }
        String query = mapSearch.getText().toString().trim();
        if (query.isEmpty()) {
            mapSearch.setError(getString(R.string.search_hint));
            return;
        }

        suggestionVersion++;
        if (pendingSearch != null) uiHandler.removeCallbacks(pendingSearch);
        hideSuggestions();
        hideKeyboard();
        if (searchAvenues) {
            searchExternalAddress(query);
            return;
        }
        mapSearch.setEnabled(false);
        String endpoint;
        try {
            LatLng center = mapLibreMap.getCameraPosition().target;
            endpoint = ApiConfig.PLACES_BASE_URL + "/buscar?q="
                    + URLEncoder.encode(query, StandardCharsets.UTF_8.name())
                    + "&latitud=" + center.getLatitude() + "&longitud=" + center.getLongitude();
        } catch (Exception error) {
            mapSearch.setEnabled(true);
            return;
        }
        ApiClient.getAuthorized(endpoint, new SessionManager(this).getToken(), new ApiClient.ApiCallback() {
            @Override public void onSuccess(String response) {
                runOnUiThread(() -> {
                    if (isFinishing()) return;
                    try {
                        JSONArray matches = new JSONArray(response);
                        if (matches.length() > 0) {
                            mapSearch.setEnabled(true);
                            String[] names = new String[matches.length()];
                            Place[] places = new Place[matches.length()];
                            for (int i = 0; i < matches.length(); i++) {
                                places[i] = Place.fromJson(matches.getJSONObject(i));
                                names[i] = places[i].name + " · " + places[i].categoryName;
                            }
                            if (places.length == 1) {
                                showFoundPlace(places[0]);
                            } else {
                                new MaterialAlertDialogBuilder(MainActivity.this)
                                        .setTitle("Lugares encontrados")
                                        .setItems(names, (dialog, which) -> showFoundPlace(places[which]))
                                        .setNegativeButton("Cancelar", null).show();
                            }
                        } else finishSearchWithMessage(R.string.registered_place_not_found);
                    } catch (Exception error) {
                        mapSearch.setEnabled(true);
                        showPlacesError();
                    }
                });
            }
            @Override public void onError(String error) {
                runOnUiThread(() -> {
                    mapSearch.setEnabled(true);
                    showPlacesError();
                });
            }
        });
    }

    private void showFoundPlace(Place place) {
        if (!selectedTypes.isEmpty() && !selectedTypes.contains(place.type)) {
            selectedTypes.clear();
            updateQuickFilters();
            loadVisiblePlaces(true);
        }
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(place.latitude, place.longitude), DEFAULT_ZOOM));
        hideSuggestions();
        hideKeyboard();
        showPlaceDetail(place);
    }

    private void searchExternalAddress(String query) {
        LatLng center = mapLibreMap.getCameraPosition().target;
        searchExecutor.execute(() -> {
            try {
                List<SearchResult> results = requestPhotonSearch(query, center);
                runOnUiThread(() -> {
                    mapSearch.setEnabled(true);
                    if (results.isEmpty()) finishSearchWithMessage(R.string.place_not_found);
                    else showSearchResult(results.get(0));
                });
            } catch (Exception error) {
                runOnUiThread(() -> finishSearchWithMessage(R.string.place_search_error));
            }
        });
    }

    private void scheduleSuggestions(String query) {
        if (suppressSuggestions) return;
        int version = ++suggestionVersion;
        if (pendingSearch != null) uiHandler.removeCallbacks(pendingSearch);
        if (query.length() < 3 || mapLibreMap == null || !mapStyleReady) {
            hideSuggestions();
            return;
        }
        LatLng center = mapLibreMap.getCameraPosition().target;
        if (!searchAvenues) {
            try {
                String endpoint = ApiConfig.PLACES_BASE_URL + "/buscar?q="
                        + URLEncoder.encode(query, StandardCharsets.UTF_8.name())
                        + "&latitud=" + center.getLatitude() + "&longitud=" + center.getLongitude();
                pendingSearch = () -> ApiClient.getAuthorized(endpoint,
                        new SessionManager(this).getToken(), new ApiClient.ApiCallback() {
                            @Override public void onSuccess(String response) {
                                runOnUiThread(() -> {
                                    if (isFinishing() || version != suggestionVersion || !mapSearch.hasFocus()) return;
                                    try {
                                        JSONArray array = new JSONArray(response);
                                        List<SearchResult> results = new ArrayList<>();
                                        for (int i = 0; i < array.length() && results.size() < 5; i++) {
                                            Place place = Place.fromJson(array.getJSONObject(i));
                                            results.add(new SearchResult(place));
                                        }
                                        showSuggestions(results);
                                    } catch (Exception ignored) { hideSuggestions(); }
                                });
                            }
                            @Override public void onError(String error) {
                                runOnUiThread(() -> { if (version == suggestionVersion) hideSuggestions(); });
                            }
                        });
                uiHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
            } catch (Exception ignored) { hideSuggestions(); }
            return;
        }
        pendingSearch = () -> searchExecutor.execute(() -> {
            try {
                List<SearchResult> results = requestPhotonSearch(query, center);
                runOnUiThread(() -> {
                    if (!isFinishing() && version == suggestionVersion
                            && mapSearch.hasFocus()) showSuggestions(results);
                });
            } catch (Exception ignored) {
                runOnUiThread(() -> {
                    if (version == suggestionVersion) hideSuggestions();
                });
            }
        });
        uiHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    private List<SearchResult> requestPhotonSearch(String query, LatLng center) throws Exception {
        String cacheKey = query.toLowerCase(Locale.ROOT) + "@"
                + Math.round(center.getLatitude() * 100) + ":"
                + Math.round(center.getLongitude() * 100);
        List<SearchResult> cached = searchCache.get(cacheKey);
        if (cached != null) return cached;
        HttpURLConnection connection = null;
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            URL url = new URL(
                    "https://photon.komoot.io/api/?q=" + encodedQuery
                            + "&limit=15&countrycode=PE&layer=street&layer=house"
                            + "&lat=" + center.getLatitude() + "&lon=" + center.getLongitude()
                            + "&zoom=14"
            );
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "application/json");

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException("Photon HTTP " + statusCode);
            }

            String response = readResponse(connection.getInputStream());
            JSONArray features = new JSONObject(response).getJSONArray("features");
            List<SearchResult> results = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (int i = 0; i < features.length() && results.size() < 5; i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                String title = properties.optString("name", "").trim();
                if (title.isEmpty()) continue;
                String subtitle = String.join(", ", nonEmpty(
                        properties.optString("district", ""),
                        properties.optString("city", ""),
                        properties.optString("state", "")));
                JSONArray coordinates = feature.getJSONObject("geometry").getJSONArray("coordinates");
                double latitude = coordinates.getDouble(1);
                double longitude = coordinates.getDouble(0);
                float[] distance = new float[1];
                Location.distanceBetween(center.getLatitude(), center.getLongitude(),
                        latitude, longitude, distance);
                String distanceLabel = distance[0] < 1000
                        ? Math.round(distance[0] / 10f) * 10 + " m"
                        : String.format(Locale.ROOT, "%.1f km", distance[0] / 1000f);
                subtitle += " · " + distanceLabel;
                SearchResult result = new SearchResult(latitude, longitude, title, subtitle);
                String locationKey = String.format(Locale.ROOT, "%.5f,%.5f", latitude, longitude);
                if (seen.add(locationKey)) results.add(result);
            }
            if (searchCache.size() >= 60) searchCache.clear();
            searchCache.put(cacheKey, results);
            return results;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private List<String> nonEmpty(String... values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (!value.isEmpty() && !result.contains(value)) result.add(value);
        }
        return result;
    }

    private void showSuggestions(List<SearchResult> results) {
        searchSuggestions.removeAllViews();
        if (results.isEmpty()) {
            hideSuggestions();
            return;
        }
        for (SearchResult result : results) {
            View row = getLayoutInflater().inflate(R.layout.view_search_suggestion,
                    searchSuggestions, false);
            ((TextView) row.findViewById(R.id.tvSuggestionTitle)).setText(result.title);
            ((TextView) row.findViewById(R.id.tvSuggestionSubtitle)).setText(result.subtitle);
            row.setOnClickListener(view -> {
                suggestionVersion++;
                suppressSuggestions = true;
                mapSearch.setText(result.title);
                suppressSuggestions = false;
                hideSuggestions();
                hideKeyboard();
                if (result.place != null) showFoundPlace(result.place);
                else showSearchResult(result);
            });
            searchSuggestions.addView(row);
        }
        findViewById(R.id.searchSuggestionCard).setVisibility(View.VISIBLE);
    }

    private void hideSuggestions() {
        View card = findViewById(R.id.searchSuggestionCard);
        if (card != null) card.setVisibility(View.GONE);
    }

    private String readResponse(InputStream inputStream) throws Exception {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private void showSearchResult(SearchResult result) {
        if (!mapStyleReady || mapLibreMap == null) {
            return;
        }

        LatLng position = new LatLng(result.latitude, result.longitude);
        if (markers != null) markers.setSearchPoint(position);
        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
        Snackbar.make(findViewById(R.id.main), result.title + " · " + result.subtitle,
                Snackbar.LENGTH_LONG).show();
    }

    private void finishSearchWithMessage(int messageResource) {
        mapSearch.setEnabled(true);
        Snackbar.make(findViewById(R.id.main), messageResource, Snackbar.LENGTH_LONG).show();
    }

    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(mapSearch.getWindowToken(), 0);
        mapSearch.clearFocus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        updateToolbarGreeting(findViewById(R.id.topAppBar));
        updateProfileAvatar();
        if (activeDetailSheet != null && activeDetailSheet.isShowing()) {
            activeDetailSheet.refreshAfterChangesOnResume();
        }
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        if (pendingPlacesLoad != null) uiHandler.removeCallbacks(pendingPlacesLoad);
        if (pendingSearch != null) uiHandler.removeCallbacks(pendingSearch);
        if (searchExecutor != null) {
            searchExecutor.shutdownNow();
        }
        if (profileImageExecutor != null) profileImageExecutor.shutdownNow();
        mapView.onDestroy();
        super.onDestroy();
    }

    private static final class SearchResult {
        private final double latitude;
        private final double longitude;
        private final String title;
        private final String subtitle;
        private final Place place;

        private SearchResult(double latitude, double longitude, String title, String subtitle) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.title = title;
            this.subtitle = subtitle;
            this.place = null;
        }

        private SearchResult(Place place) {
            this.latitude = place.latitude;
            this.longitude = place.longitude;
            this.title = place.name;
            this.subtitle = place.categoryName + (place.address.isEmpty() ? "" : " · " + place.address);
            this.place = place;
        }
    }

}
