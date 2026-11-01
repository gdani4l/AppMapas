package com.example.appmovil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.CircleLayer;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.maplibre.android.style.layers.PropertyFactory.*;
import static org.maplibre.android.style.expressions.Expression.get;

/** Los puntos se renderizan dentro de MapLibre y quedan sincronizados con cada fotograma del mapa. */
final class MapMarkerRenderer {
    // Cambia este valor para reducir o ampliar TODOS los PNG de los lugares.
    static final int PLACE_ICON_SIZE_DP = 24;
    // Los iconos aparecen al acercarse al nivel donde se leen las avenidas.
    static final float PLACE_MIN_ZOOM = 14.5f;
    // Los nombres requieren acercarse un poco más que los iconos.
    static final float PLACE_LABEL_MIN_ZOOM = 16f;
    static final String PLACE_LAYER = "app-places-layer";
    private static final String PLACE_LABEL_LAYER = "app-places-label-layer";
    private static final String PLACE_SOURCE = "app-places-source";
    private static final String LOCATION_SOURCE = "app-location-source";
    private static final String SEARCH_SOURCE = "app-search-source";
    private final Context context;
    private final MapLibreMap map;

    MapMarkerRenderer(Context context, MapLibreMap map, Style style) {
        this.context = context;
        this.map = map;
        int pixels = Math.round(PLACE_ICON_SIZE_DP * context.getResources().getDisplayMetrics().density);
        for (String type : new String[]{"HOTEL", "RESTAURANTE", "PARQUE", "FARMACIA", "HOSPITAL", "COMISARIA"}) {
            int id = context.getResources().getIdentifier("marker_" + type.toLowerCase(Locale.ROOT),
                    "drawable", context.getPackageName());
            Bitmap original = id == 0 ? fallback(type, pixels) : BitmapFactory.decodeResource(context.getResources(), id);
            style.addImage("app-icon-" + type, Bitmap.createScaledBitmap(original, pixels, pixels, true));
        }
        style.addImage("app-icon-OTRO", fallback("OTRO", pixels));
        style.addSource(new GeoJsonSource(PLACE_SOURCE, empty()));
        SymbolLayer placeLayer = new SymbolLayer(PLACE_LAYER, PLACE_SOURCE).withProperties(
                iconImage(get("icon")), iconSize(1f), iconAnchor("bottom"),
                iconAllowOverlap(true), iconIgnorePlacement(true));
        placeLayer.setMinZoom(PLACE_MIN_ZOOM);
        style.addLayer(placeLayer);
        SymbolLayer labelLayer = new SymbolLayer(PLACE_LABEL_LAYER, PLACE_SOURCE).withProperties(
                textField(get("name")), textSize(Math.max(11f, PLACE_ICON_SIZE_DP * 0.5f)),
                textAnchor("top"), textOffset(new Float[]{0f, 0.3f}),
                textMaxWidth(9f), textColor(Color.rgb(15, 23, 42)),
                textHaloColor(Color.WHITE), textHaloWidth(1.5f),
                textAllowOverlap(true), textIgnorePlacement(true));
        labelLayer.setMinZoom(PLACE_LABEL_MIN_ZOOM);
        style.addLayer(labelLayer);

        style.addSource(new GeoJsonSource(LOCATION_SOURCE, empty()));
        style.addLayer(new CircleLayer("app-location-halo", LOCATION_SOURCE).withProperties(
                circleRadius(13f), circleColor(Color.WHITE), circleOpacity(0.92f)));
        style.addLayer(new CircleLayer("app-location-dot", LOCATION_SOURCE).withProperties(
                circleRadius(8f), circleColor(Color.rgb(2, 132, 199)),
                circleStrokeColor(Color.WHITE), circleStrokeWidth(2f)));

        style.addSource(new GeoJsonSource(SEARCH_SOURCE, empty()));
        style.addLayer(new CircleLayer("app-search-point", SEARCH_SOURCE).withProperties(
                circleRadius(9f), circleColor(Color.rgb(13, 148, 136)),
                circleStrokeColor(Color.WHITE), circleStrokeWidth(3f)));
    }

    void setPlaces(List<Place> places) {
        List<Feature> features = new ArrayList<>();
        for (Place place : places) {
            Feature feature = Feature.fromGeometry(Point.fromLngLat(place.longitude, place.latitude));
            feature.addNumberProperty("idLugar", place.id);
            feature.addStringProperty("icon", "app-icon-" + iconType(place.type));
            feature.addStringProperty("name", place.name);
            features.add(feature);
        }
        source(PLACE_SOURCE).setGeoJson(FeatureCollection.fromFeatures(features));
    }

    void setLocation(LatLng position) { setPoint(LOCATION_SOURCE, position); }
    void setSearchPoint(LatLng position) { setPoint(SEARCH_SOURCE, position); }

    Long placeAt(LatLng point) {
        PointF screen = map.getProjection().toScreenLocation(point);
        List<Feature> features = map.queryRenderedFeatures(screen, PLACE_LAYER, PLACE_LABEL_LAYER);
        if (features.isEmpty() || !features.get(0).hasProperty("idLugar")) return null;
        return features.get(0).getNumberProperty("idLugar").longValue();
    }

    private void setPoint(String sourceId, LatLng point) {
        source(sourceId).setGeoJson(point == null ? empty() : FeatureCollection.fromFeature(
                Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude()))));
    }

    private GeoJsonSource source(String id) { return (GeoJsonSource) map.getStyle().getSource(id); }
    private FeatureCollection empty() { return FeatureCollection.fromFeatures(new Feature[]{}); }

    private String iconType(String type) {
        switch (type) {
            case "HOTEL": case "RESTAURANTE": case "PARQUE": case "FARMACIA":
            case "HOSPITAL": case "COMISARIA": return type;
            default: return "OTRO";
        }
    }

    private Bitmap fallback(String type, int pixels) {
        Bitmap bitmap = Bitmap.createBitmap(pixels, pixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(2, 132, 199));
        canvas.drawCircle(pixels / 2f, pixels / 2f, pixels * 0.48f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(pixels * 0.5f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(type.substring(0, 1), pixels / 2f, pixels * 0.68f, paint);
        return bitmap;
    }
}
