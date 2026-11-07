package com.example.appmovil;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Indicadores de comunidad construidos con datos agregados del servidor. */
public class BusinessIntelligenceActivity extends AppCompatActivity {
    private LinearLayout content;
    private boolean loading;
    private JSONObject currentData;
    private final ActivityResultLauncher<String> pdfLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
                if (uri == null || currentData == null) return;
                JSONObject snapshot = currentData;
                new Thread(() -> {
                    try {
                        DashboardPdfExporter.exportCommunity(this, uri, snapshot);
                        runOnUiThread(() -> Snackbar.make(content,
                                "PDF guardado correctamente", Snackbar.LENGTH_LONG).show());
                    } catch (Exception error) {
                        runOnUiThread(() -> Snackbar.make(content,
                                "No se pudo generar el PDF", Snackbar.LENGTH_LONG).show());
                    }
                }).start();
            });

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(getColor(R.color.app_background));
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom); return insets;
        });
        MaterialToolbar toolbar = new MaterialToolbar(this);
        toolbar.setTitle("Análisis de comunidad");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.inflateMenu(R.menu.menu_dashboard);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.actionRefresh) { load(); return true; }
            if (item.getItemId() == R.id.actionExportPdf) { exportPdf(); return true; }
            return false;
        });
        root.addView(toolbar);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16),dp(16),dp(16),dp(24));
        scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        load();
    }

    private void load() {
        if (loading) return;
        loading=true;
        content.removeAllViews();
        text(content,"Cargando indicadores…",16,true);
        ApiClient.getAuthorized(ApiConfig.ADMIN_BASE_URL+"/bi",new SessionManager(this).getToken(),new ApiClient.ApiCallback(){
            public void onSuccess(String response){runOnUiThread(()->{
                if(isDestroyed() || isFinishing())return;
                loading=false;
                try{render(new JSONObject(response));}catch(Exception e){error("No se pudieron interpretar los indicadores.");}
            });}
            public void onError(String message){runOnUiThread(()->{loading=false;if(!isDestroyed())error(message);});}
        });
    }
    private void error(String message){content.removeAllViews();text(content,message+"\nPulsa Actualizar para reintentar.",16,false);}

    private void render(JSONObject data) throws Exception {
        currentData = data;
        content.removeAllViews();
        text(content,"Participación, seguridad y movilidad",22,true);
        text(content,"Totales históricos, salvo las altas de los últimos 30 días. Los recorridos de prueba se integran en las métricas generales.",14,false);
        section("Usuarios y crecimiento");
        chart("Nuevos usuarios · últimos 30 días",data.getJSONArray("registros"), DashboardChartView.Mode.LINE);
        chart("Usuarios por distrito declarado",data.getJSONArray("distritos"), DashboardChartView.Mode.HORIZONTAL_BAR);
        section("Seguridad de la comunidad");
        chart("Reportes de seguridad por distrito",data.getJSONArray("reportesDistritos"), DashboardChartView.Mode.HORIZONTAL_BAR);
        chart("Incidentes por tipo",data.getJSONArray("incidentes"), DashboardChartView.Mode.HORIZONTAL_BAR);
        chart("Incidente predominante en cada distrito",data.getJSONArray("incidentesDistrito"), DashboardChartView.Mode.HORIZONTAL_BAR);
        chart("Estado de validación de los reportes",data.getJSONArray("validacion"), DashboardChartView.Mode.DONUT);
        chart("Horarios con más reportes",data.getJSONArray("reportesHoras"), DashboardChartView.Mode.LINE);
        chart("Días con más reportes",data.getJSONArray("reportesDias"), DashboardChartView.Mode.BAR);
        chart("Alertas recibidas por distrito",data.getJSONArray("zonasAlertas"), DashboardChartView.Mode.HORIZONTAL_BAR);
        chart("Reportes en zonas reincidentes por distrito",data.getJSONArray("zonasReincidentes"), DashboardChartView.Mode.HORIZONTAL_BAR);
        section("Participación y contenido");
        chart("Distribución de valoraciones",data.getJSONArray("valoraciones"), DashboardChartView.Mode.BAR);
        chart("Distritos con mayor actividad de usuarios",data.getJSONArray("actividadDistritos"), DashboardChartView.Mode.HORIZONTAL_BAR);
    }

    private void exportPdf() {
        if (currentData == null) {
            Snackbar.make(content, "Espera a que terminen de cargar los indicadores",
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(new java.util.Date());
        pdfLauncher.launch("analisis-comunidad-" + date + ".pdf");
    }

    private void chart(String title,JSONArray rows, DashboardChartView.Mode mode) throws Exception {
        LinearLayout card=card();text(card,title,18,true);
        if(rows.length()==0){text(card,"Todavía no hay datos para este indicador.",14,false);return;}
        List<String> labels = new ArrayList<>();
        long[] values = new long[rows.length()];
        for(int i=0;i<rows.length();i++){
            JSONObject row=rows.getJSONObject(i);
            labels.add(row.optString("etiqueta").replace('_',' '));
            values[i]=row.optLong("valor");
        }
        DashboardChartView chart = new DashboardChartView(this);
        chart.setMode(mode);
        chart.setData(labels, values);
        card.addView(chart,new LinearLayout.LayoutParams(-1,-2));
    }
    private LinearLayout card(){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(12),dp(16),dp(16));card.setBackgroundResource(R.drawable.bg_dashboard_card);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,-2);params.topMargin=dp(16);
        content.addView(card,params);return card;
    }
    private void section(String title){
        TextView heading=new TextView(this);heading.setText(title);heading.setTextSize(19);
        heading.setTextColor(getColor(R.color.app_primary_dark));heading.setTypeface(null,android.graphics.Typeface.BOLD);
        heading.setPadding(dp(4),dp(26),dp(4),0);content.addView(heading);
    }
    private void text(LinearLayout parent,String value,int size,boolean bold){
        TextView text=new TextView(this);text.setText(value);text.setTextSize(size);
        text.setTextColor(getColor(bold?R.color.app_text_primary:R.color.app_text_secondary));
        if(bold)text.setTypeface(null,android.graphics.Typeface.BOLD);
        text.setPadding(0,dp(8),0,dp(8));parent.addView(text);
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
}
