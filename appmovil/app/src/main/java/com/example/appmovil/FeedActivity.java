package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;

public class FeedActivity extends AppCompatActivity {
    static final String EXTRA_MODE = "mode";
    static final String MODE_REPORTS = "reports", MODE_NOTIFICATIONS = "notifications",
            MODE_RECOMMENDATIONS = "recommendations", MODE_HISTORY = "history";

    private String mode, activityFilter = "LUGAR", activityDetailFilter = "TODOS",
            recommendationOrder = "SEGURIDAD";
    private int reportDateFilter;
    private LinearLayout items;
    private View progress;
    private TextView empty, filterSummary;
    private JSONArray cachedItems;
    private int requestVersion;
    private boolean loading;
    private String loadingOrder;
    private double latitude = -12.0464, longitude = -77.0428;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_feed);
        UiInsets.apply(this);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        latitude = getIntent().getDoubleExtra("latitude", latitude);
        longitude = getIntent().getDoubleExtra("longitude", longitude);
        MaterialToolbar toolbar = findViewById(R.id.feedToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(title());
        toolbar.inflateMenu(R.menu.menu_feed);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != R.id.actionRefresh) return false;
            load(recommendationOrder);
            return true;
        });
        items = findViewById(R.id.feedItems);
        progress = findViewById(R.id.feedProgress);
        empty = findViewById(R.id.feedEmpty);
        filterSummary = findViewById(R.id.feedFilterSummary);
        configureControls();
    }

    private void configureControls() {
        Spinner sort = findViewById(R.id.feedSort);
        View filters = findViewById(R.id.activityFilterPanel);
        if (MODE_RECOMMENDATIONS.equals(mode)) {
            sort.setVisibility(View.VISIBLE);
            String[] options = {"Más seguros", "Mejor calificados", "Más cercanos"};
            sort.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, options));
            sort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
                @Override public void onItemSelected(android.widget.AdapterView<?> parent,
                                                     View view, int position, long id) {
                    recommendationOrder = position == 1 ? "CALIFICACION"
                            : position == 2 ? "CERCANIA" : "SEGURIDAD";
                    load(recommendationOrder);
                }
            });
            return;
        }
        if (MODE_HISTORY.equals(mode)) {
            filters.setVisibility(View.VISIBLE);
            ChipGroup group = findViewById(R.id.activityFilterGroup);
            group.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int id = checkedIds.get(0);
                activityFilter = id == R.id.filterPlaces ? "LUGAR"
                        : id == R.id.filterReviews ? "RESENA"
                        : "REPORTE";
                configureActivityDetailFilter();
                renderCached();
            });
            configureActivityDetailFilter();
        }
        if(MODE_REPORTS.equals(mode)){
            Spinner date=findViewById(R.id.reportDateFilter);date.setVisibility(View.VISIBLE);
            date.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Todas las fechas","Hoy","Últimos 7 días","Últimos 30 días"}));
            date.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){reportDateFilter=position;renderCached();}});
        }
        load("");
    }

    private String title() {
        if (MODE_NOTIFICATIONS.equals(mode)) return "Notificaciones";
        if (MODE_RECOMMENDATIONS.equals(mode)) return "Lugares recomendados";
        if (MODE_HISTORY.equals(mode)) return "Mi actividad";
        return "Mis reportes";
    }

    private void load(String order) {
        if (loading && order.equals(loadingOrder)) return;
        loading = true;
        loadingOrder = order;
        final int version = ++requestVersion;
        progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);
        String endpoint;
        if (MODE_NOTIFICATIONS.equals(mode)) endpoint = ApiConfig.NOTIFICATIONS_BASE_URL;
        else if (MODE_RECOMMENDATIONS.equals(mode)) endpoint = ApiConfig.PLACES_BASE_URL
                + "/recomendados?latitud=" + latitude + "&longitud=" + longitude
                + "&ordenarPor=" + order;
        else endpoint = ApiConfig.PROFILE_BASE_URL + "/actividad";
        ApiClient.getAuthorized(endpoint, new SessionManager(this).getToken(),
                new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed() || version != requestVersion) return;
                            loading = false;
                            try {
                                cachedItems = new JSONArray(response);
                                renderCached();
                            } catch (Exception error) {
                                progress.setVisibility(View.GONE);
                                show("No se pudo mostrar la información");
                            }
                        });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed() || version != requestVersion) return;
                            loading = false;
                            progress.setVisibility(View.GONE);
                            if (cachedItems == null) {
                                empty.setText("No pudimos cargar la información. Comprueba tu conexión y pulsa Actualizar.");
                                empty.setVisibility(View.VISIBLE);
                            }
                            Snackbar.make(findViewById(R.id.feedRoot), error, Snackbar.LENGTH_LONG)
                                    .setAction("Reintentar", v -> load(order)).show();
                        });
                    }
                });
    }

    private void renderCached() {
        if (cachedItems == null) return;
        progress.setVisibility(View.GONE);
        items.removeAllViews();
        int shown = 0;
        try {
            for (int i = 0; i < cachedItems.length(); i++) {
                JSONObject json = cachedItems.getJSONObject(i);
                if (!accept(json)) continue;
                addItem(json);
                shown++;
                if (MODE_RECOMMENDATIONS.equals(mode) && shown >= 10) break;
            }
        } catch (Exception error) {
            show("No se pudo mostrar la información");
        }
        if (MODE_HISTORY.equals(mode)) filterSummary.setText(shown
                + (shown == 1 ? " actividad encontrada" : " actividades encontradas"));
        empty.setText(emptyText());
        empty.setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
    }

    private boolean accept(JSONObject json) {
        String type = json.optString("tipo");
        if (MODE_REPORTS.equals(mode)) return "REPORTE".equals(type)&&isActive(json)&&matchesReportDate(json);
        if (!MODE_HISTORY.equals(mode)) return true;
        if (!"TODOS".equals(activityFilter) && !activityFilter.equals(type)) return false;
        if ("TODOS".equals(activityDetailFilter)) return true;
        if ("LUGAR".equals(type)) return activityDetailFilter.equals(json.optString("categoria"));
        if ("RESENA".equals(type)) return json.optInt("valor",-1)==Integer.parseInt(activityDetailFilter);
        if ("REPORTE".equals(type)) return activityDetailFilter.equals(json.optString("estado"));
        return true;
    }

    private boolean isActive(JSONObject json){try{return java.time.LocalDateTime.parse(json.optString("fechaExpiracion")).isAfter(java.time.LocalDateTime.now());}catch(Exception e){return false;}}
    private boolean matchesReportDate(JSONObject json){if(reportDateFilter==0)return true;try{java.time.LocalDateTime date=java.time.LocalDateTime.parse(json.optString("fecha"));java.time.LocalDateTime limit=reportDateFilter==1?java.time.LocalDate.now().atStartOfDay():java.time.LocalDateTime.now().minusDays(reportDateFilter==2?7:30);return !date.isBefore(limit);}catch(Exception e){return false;}}

    private String emptyText() {
        if (MODE_NOTIFICATIONS.equals(mode)) return "Todavía no tienes notificaciones.";
        if (MODE_RECOMMENDATIONS.equals(mode)) return "Todavía no hay lugares para recomendar.";
        if (MODE_REPORTS.equals(mode)) return "Todavía no publicaste reportes.";
        return "TODOS".equals(activityFilter) ? "Todavía no hay actividad en tu perfil."
                : "No hay actividad de este tipo.";
    }

    private void addItem(JSONObject json) {
        View card = getLayoutInflater().inflate(R.layout.view_feed_item, items, false);
        TextView badge = card.findViewById(R.id.feedItemBadge);
        TextView title = card.findViewById(R.id.feedItemTitle);
        TextView detail = card.findViewById(R.id.feedItemDetail);
        TextView meta = card.findViewById(R.id.feedItemMeta);
        if (MODE_RECOMMENDATIONS.equals(mode)) {
            JSONObject place = json.optJSONObject("lugar");
            if (place == null) return;
            badge.setText(place.optString("nombreCategoria", place.optString("tipo")));
            title.setText(place.optString("nombre"));
            detail.setText(place.optString("direccion", "Dirección no indicada"));
            meta.setText(String.format(Locale.getDefault(),
                    "★ %.1f · %d reseñas · %d alertas · %.1f km",
                    json.optDouble("calificacion"), json.optLong("resenas"),
                    json.optLong("alertasCercanas"), json.optDouble("distanciaKm")));
        } else if (MODE_NOTIFICATIONS.equals(mode)) {
            badge.setText(json.optBoolean("leida") ? "LEÍDA" : "NUEVA");
            title.setText(json.optString("titulo"));
            detail.setText(json.optString("mensaje"));
            meta.setText(date(json.optString("fechaCreacion")));
            long id = json.optLong("id");
            card.setOnClickListener(v -> {
                if (json.optBoolean("leida") || Boolean.TRUE.equals(card.getTag())) return;
                card.setTag(true);
                markRead(id, badge, json, card);
            });
        } else {
            String type = json.optString("tipo");
            badge.setText("RESENA".equals(type) ? "RESEÑA" : type);
            title.setText(json.optString("titulo"));
            detail.setText(json.optString("detalle"));
            meta.setText(date(json.optString("fecha")));
            if (MODE_REPORTS.equals(mode)) addDeleteReportButton(card, json.optLong("id"));
            else if (MODE_HISTORY.equals(mode)) addOpenActivityButton(card,json);
        }
        items.addView(card);
    }

    private void configureActivityDetailFilter() {
        Spinner spinner=findViewById(R.id.activityDetailFilter);
        String[] labels;String[] values;
        if("LUGAR".equals(activityFilter)){
            labels=new String[]{"Todas las categorías","Hoteles","Restaurantes","Parques","Farmacias","Hospitales","Comisarías"};
            values=new String[]{"TODOS","HOTEL","RESTAURANTE","PARQUE","FARMACIA","HOSPITAL","COMISARIA"};
        }else if("RESENA".equals(activityFilter)){
            labels=new String[]{"Todas las valoraciones","5 estrellas","4 estrellas","3 estrellas","2 estrellas","1 estrella","0 estrellas"};
            values=new String[]{"TODOS","5","4","3","2","1","0"};
        }else if("REPORTE".equals(activityFilter)){
            labels=new String[]{"Todos los estados","Pendientes","Confirmados","En revisión","Refutados"};
            values=new String[]{"TODOS","PENDIENTE","CONFIRMADO","EN_REVISION","REFUTADO"};
        }else{
            activityDetailFilter="TODOS";spinner.setVisibility(View.GONE);return;
        }
        spinner.setVisibility(View.VISIBLE);
        spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels));
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                activityDetailFilter=values[position];renderCached();
            }
        });
    }

    private void addOpenActivityButton(View card,JSONObject json){
        LinearLayout actions=card.findViewById(R.id.feedItemActions);actions.setVisibility(View.VISIBLE);
        com.google.android.material.button.MaterialButton button=new com.google.android.material.button.MaterialButton(this);
        button.setText("VER ACTIVIDAD");
        button.setOnClickListener(v->openActivityItem(json));
        actions.addView(button,new LinearLayout.LayoutParams(-1,dp(48)));
    }

    private void openActivityItem(JSONObject item){
        String type=item.optString("tipo");long reference=item.optLong("idReferencia",item.optLong("id"));
        String endpoint="REPORTE".equals(type)?ApiConfig.REPORTS_BASE_URL+"/"+reference:
                ApiConfig.PLACES_BASE_URL+"/"+reference;
        ApiClient.getAuthorized(endpoint,new SessionManager(this).getToken(),new ApiClient.ApiCallback(){
            public void onSuccess(String response){runOnUiThread(()->{
                Intent intent=new Intent(FeedActivity.this,MainActivity.class)
                        .putExtra(MainActivity.EXTRA_MAP_FOCUS_TYPE,"REPORTE".equals(type)?"REPORTE":"LUGAR")
                        .putExtra(MainActivity.EXTRA_MAP_FOCUS_JSON,response)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            });}
            public void onError(String error){runOnUiThread(()->show(error));}
        });
    }

    private void addDeleteReportButton(View card, long id) {
        android.widget.LinearLayout container = card.findViewById(R.id.feedItemActions);
        container.setVisibility(View.VISIBLE);
        com.google.android.material.button.MaterialButton button =
                new com.google.android.material.button.MaterialButton(this);
        button.setText("ELIMINAR MI REPORTE");
        button.setTextColor(0xFFDC2626);
        button.setOnClickListener(v -> new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar reporte")
                .setMessage("Se eliminarán también sus comentarios y votos.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> ReportRepository.deleteOwn(
                        this, id, new ApiClient.ApiCallback() {
                            @Override public void onSuccess(String response) {
                                runOnUiThread(() -> { show("Reporte eliminado"); load(""); });
                            }
                            @Override public void onError(String error) {
                                runOnUiThread(() -> show(error));
                            }
                        })).show());
        container.addView(button, new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
    }

    private void markRead(long id, TextView badge, JSONObject notification, View card) {
        ApiClient.putAuthorized(ApiConfig.NOTIFICATIONS_BASE_URL + "/" + id + "/leida",
                new SessionManager(this).getToken(), new JSONObject(),
                new ApiClient.ApiCallback() {
                    @Override public void onSuccess(String response) {
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            try { notification.put("leida", true); } catch (Exception ignored) { }
                            card.setTag(false);
                            badge.setText("LEÍDA");
                        });
                    }
                    @Override public void onError(String error) {
                        runOnUiThread(() -> { card.setTag(false); if (!isDestroyed()) show(error); });
                    }
                });
    }

    private void show(String message) {
        Snackbar.make(findViewById(R.id.feedRoot), message, Snackbar.LENGTH_LONG).show();
    }

    private String date(String value) {
        return value.length() >= 16 ? value.substring(0, 16).replace('T', ' ') : value;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
