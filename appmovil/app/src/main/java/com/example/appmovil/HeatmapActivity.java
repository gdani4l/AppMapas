package com.example.appmovil;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import org.json.JSONArray;
import org.json.JSONObject;
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;
import java.util.ArrayList;
import java.util.List;
import static org.maplibre.android.style.expressions.Expression.get;
import static org.maplibre.android.style.layers.PropertyFactory.*;

public class HeatmapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MapLibreMap map;
    private JSONArray zones;
    private TextView legend;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        MapLibre.getInstance(this);
        setContentView(R.layout.activity_heatmap);
        UiInsets.apply(this);
        MaterialToolbar toolbar = findViewById(R.id.heatToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_feed);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != R.id.actionRefresh) return false;
            load();
            return true;
        });
        legend = findViewById(R.id.heatLegend);
        mapView = findViewById(R.id.heatMap);
        mapView.onCreate(state);
        mapView.getMapAsync(this);
        load();
    }

    private void load() {
        ApiClient.getAuthorized(ApiConfig.ip + "/api/recorridos/estadisticas",
                new SessionManager(this).getToken(), new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            try {
                                zones = new JSONObject(response).getJSONArray("zonas");
                                legend.setText(zones.length() + " zonas · Mayor tamaño y color rojo: más visitas registradas.");
                                drawZones();
                            } catch (Exception error) {
                                legend.setText("No se pudieron mostrar las zonas.");
                            }
                        });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> legend.setText(error));
                    }
                });
    }

    @Override public void onMapReady(@NonNull MapLibreMap map) {
        this.map = map;
        map.getUiSettings().setLogoEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-12.0464, -77.0428), 11));
        map.setStyle("https://tiles.openfreemap.org/styles/liberty", style -> {
            style.addSource(new GeoJsonSource("visited-zones", FeatureCollection.fromFeatures(new Feature[]{})));
            style.addLayer(new CircleLayer("visited-zone-circles", "visited-zones").withProperties(
                    circleRadius(get("radius")), circleColor(get("color")),
                    circleOpacity(0.58f), circleStrokeColor(0xFF9A3412), circleStrokeWidth(1f)));
            drawZones();
        });
        map.addOnMapClickListener(point -> {
            android.graphics.PointF screen = map.getProjection().toScreenLocation(point);
            List<Feature> hits = map.queryRenderedFeatures(screen, "visited-zone-circles");
            if (hits.isEmpty()) return false;
            legend.setText(hits.get(0).getNumberProperty("visitas") + " visitas registradas en esta zona");
            return true;
        });
    }

    private void drawZones() {
        if (zones == null || map == null || map.getStyle() == null) return;
        GeoJsonSource source = map.getStyle().getSourceAs("visited-zones");
        if (source == null) return;
        List<Feature> features = new ArrayList<>();
        int maximum = 1;
        for (int i = 0; i < zones.length(); i++) maximum = Math.max(maximum,
                zones.optJSONObject(i).optInt("visitas"));
        for (int i = 0; i < zones.length(); i++) {
            JSONObject zone = zones.optJSONObject(i);
            if (zone == null) continue;
            int visits = zone.optInt("visitas");
            Feature feature = Feature.fromGeometry(Point.fromLngLat(
                    zone.optDouble("longitud"), zone.optDouble("latitud")));
            feature.addNumberProperty("radius", 12 + 28f * visits / maximum);
            feature.addStringProperty("color", visits * 2 >= maximum ? "#DC2626" : "#F97316");
            feature.addNumberProperty("visitas", visits);
            features.add(feature);
        }
        source.setGeoJson(FeatureCollection.fromFeatures(features));
        if (!features.isEmpty()) {
            JSONObject first = zones.optJSONObject(0);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    first.optDouble("latitud"), first.optDouble("longitud")), 13));
        }
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override protected void onSaveInstanceState(@NonNull Bundle out) { super.onSaveInstanceState(out); mapView.onSaveInstanceState(out); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
}
