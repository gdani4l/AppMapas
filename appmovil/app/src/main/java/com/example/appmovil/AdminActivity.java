package com.example.appmovil;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.graphics.drawable.GradientDrawable;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.chip.ChipGroup;
import androidx.core.widget.NestedScrollView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    public void openAccounts(View view) {
        startActivity(new Intent(this, GestionCuentasActivity.class));
    }
    public void openBusinessIntelligence(View view) {
        startActivity(new Intent(this, BusinessIntelligenceActivity.class));
    }
    private LinearLayout items,places,safetyReports,expiredReports; private TextView empty;
    private JSONObject summaryData, usageData;
    private JSONArray pendingData,activeData,expiredData,placesData;
    private String pendingQuery="",activeQuery="",expiredQuery="",placesQuery="";
    private int pendingFilter,activeFilter,expiredFilter,placesFilter;
    private int currentReportSection;
    private final ActivityResultLauncher<String> pdfLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
                if (uri == null || summaryData == null || usageData == null) return;
                JSONObject summary = summaryData;
                JSONObject usage = usageData;
                new Thread(() -> {
                    try {
                        DashboardPdfExporter.exportSummary(this, uri, summary, usage);
                        runOnUiThread(() -> show("PDF guardado correctamente"));
                    } catch (Exception error) {
                        runOnUiThread(() -> show("No se pudo generar el PDF"));
                    }
                }).start();
            });
    @Override protected void onCreate(Bundle state){super.onCreate(state);setContentView(R.layout.activity_admin);UiInsets.apply(this);findViewById(R.id.adminToolbar).setOnClickListener(v->finish());
        com.google.android.material.appbar.MaterialToolbar toolbar=findViewById(R.id.adminToolbar);toolbar.setNavigationOnClickListener(v->finish());toolbar.inflateMenu(R.menu.menu_dashboard);toolbar.setOnMenuItemClickListener(item->{if(item.getItemId()==R.id.actionRefresh){load();return true;}if(item.getItemId()==R.id.actionExportPdf){exportPdf();return true;}return false;});items=findViewById(R.id.adminItems);places=findViewById(R.id.adminPlaces);safetyReports=findViewById(R.id.adminSafetyReports);expiredReports=findViewById(R.id.adminExpiredReports);empty=findViewById(R.id.adminEmpty);findViewById(R.id.adminHeatmap).setOnClickListener(v->startActivity(new Intent(this,HeatmapActivity.class)));configureShortcuts();load();}
    private void configureShortcuts(){
        NestedScrollView scroll=findViewById(R.id.adminScroll);ChipGroup group=findViewById(R.id.adminShortcutGroup);
        com.google.android.material.button.MaterialButtonToggleGroup reportTabs=findViewById(R.id.adminReportTabs);
        reportTabs.addOnButtonCheckedListener((tabs,id,isChecked)->{if(!isChecked)return;
            showReportSection(id==R.id.adminReportTabPending?0:id==R.id.adminReportTabActive?1:2);});
        configureListFilters((LinearLayout)findViewById(R.id.adminPendingPanel),2,
                new String[]{"Todos los contenidos","Lugares","Reseñas","Reportes"},(query,filter)->{
                    pendingQuery=query;pendingFilter=filter;if(pendingData!=null)renderReports(pendingData.toString());});
        configureListFilters((LinearLayout)findViewById(R.id.adminReportsPanel),2,reportFilterLabels(),(query,filter)->{
            activeQuery=query;activeFilter=filter;if(activeData!=null)renderManaged("reportes",safetyReports,activeData.toString());});
        configureListFilters((LinearLayout)findViewById(R.id.adminExpiredPanel),2,reportFilterLabels(),(query,filter)->{
            expiredQuery=query;expiredFilter=filter;if(expiredData!=null)renderManaged("reportes",expiredReports,expiredData.toString());});
        configureListFilters((LinearLayout)findViewById(R.id.adminPlacesPanel),2,
                new String[]{"Todas las categorías","Hoteles","Restaurantes","Parques","Farmacias","Hospitales","Comisarías"},(query,filter)->{
                    placesQuery=query;placesFilter=filter;if(placesData!=null)renderManaged("lugares",places,placesData.toString());});
        group.setOnCheckedStateChangeListener((g,ids)->{
            if(ids.isEmpty())return;int selected=ids.get(0);
            findViewById(R.id.adminSummaryPanel).setVisibility(selected==R.id.adminShortcutSummary?View.VISIBLE:View.GONE);
            boolean reportsSelected=selected==R.id.adminShortcutReports;
            findViewById(R.id.adminReportTabs).setVisibility(reportsSelected?View.VISIBLE:View.GONE);
            findViewById(R.id.adminPendingPanel).setVisibility(reportsSelected&&currentReportSection==0?View.VISIBLE:View.GONE);
            findViewById(R.id.adminReportsPanel).setVisibility(reportsSelected&&currentReportSection==1?View.VISIBLE:View.GONE);
            findViewById(R.id.adminExpiredPanel).setVisibility(reportsSelected&&currentReportSection==2?View.VISIBLE:View.GONE);
            findViewById(R.id.adminPlacesPanel).setVisibility(selected==R.id.adminShortcutPlaces?View.VISIBLE:View.GONE);
            ((com.google.android.material.appbar.MaterialToolbar)findViewById(R.id.adminToolbar))
                    .getMenu().findItem(R.id.actionExportPdf)
                    .setVisible(selected==R.id.adminShortcutSummary);
            scroll.post(()->scroll.scrollTo(0,0));
        });
    }
    private void showReportSection(int section){
        currentReportSection=section;
        findViewById(R.id.adminPendingPanel).setVisibility(section==0?View.VISIBLE:View.GONE);
        findViewById(R.id.adminReportsPanel).setVisibility(section==1?View.VISIBLE:View.GONE);
        findViewById(R.id.adminExpiredPanel).setVisibility(section==2?View.VISIBLE:View.GONE);
        ((NestedScrollView)findViewById(R.id.adminScroll)).scrollTo(0,0);
    }
    private String[] reportFilterLabels(){return new String[]{"Todos los reportes","Peligro alto","Peligro medio",
            "Pendientes","Confirmados","En revisión","Refutados"};}
    private interface FilterListener{void changed(String query,int filter);}
    private void configureListFilters(LinearLayout panel,int index,String[] options,FilterListener listener){
        View filters=getLayoutInflater().inflate(R.layout.view_admin_filters,panel,false);
        android.widget.EditText search=filters.findViewById(R.id.adminFilterSearch);
        android.widget.Spinner spinner=filters.findViewById(R.id.adminFilterSpinner);
        spinner.setAdapter(new android.widget.ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));
        final int[] selection={0};final String[] query={""};
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){query[0]=s.toString().trim();listener.changed(query[0],selection[0]);}
            public void afterTextChanged(android.text.Editable s){}});
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){selection[0]=position;listener.changed(query[0],position);}});
        panel.addView(filters,index);
    }
    private void load(){String token=new SessionManager(this).getToken();ApiClient.getAuthorized(ApiConfig.ADMIN_BASE_URL+"/dashboard",token,new ApiClient.ApiCallback(){public void onSuccess(String response){runOnUiThread(()->renderStats(response));}public void onError(String error){runOnUiThread(()->show(error));}});ApiClient.getAuthorized(ApiConfig.ip+"/api/recorridos/estadisticas",token,new ApiClient.ApiCallback(){public void onSuccess(String response){runOnUiThread(()->renderUsage(response));}public void onError(String error){runOnUiThread(()->show(error));}});ApiClient.getAuthorized(ApiConfig.ADMIN_BASE_URL+"/denuncias",token,new ApiClient.ApiCallback(){public void onSuccess(String response){runOnUiThread(()->renderReports(response));}public void onError(String error){runOnUiThread(()->show(error));}});loadManaged("reportes",safetyReports,false);loadManaged("reportes",expiredReports,true);loadManaged("lugares",places,false);}
    private void renderUsage(String response){
        try{
            JSONObject j=new JSONObject(response); usageData=j;
            LinearLayout container=findViewById(R.id.adminUsageStats); container.removeAllViews();
            long total=j.optLong("puntos");
            JSONArray zones=j.optJSONArray("zonas");
            addMetricRow(container,"Usuarios",j.optLong("usuarios"),0xFFE0F2FE,
                    "Activos 7 días",j.optLong("activos7Dias"),0xFFCCFBF1);
            addMetricRow(container,"Muestras de recorrido",total,0xFFDBEAFE,
                    "Zonas con actividad",zones==null?0:zones.length(),0xFFFEF3C7);
            JSONArray days=j.optJSONArray("ultimosDias");
            if(days!=null&&days.length()>0){
                List<String> labels=new ArrayList<>(); long[] values=new long[days.length()];
                for(int i=0;i<days.length();i++){
                    JSONObject day=days.getJSONObject(i); labels.add(shortDate(day.optString("fecha")));
                    values[i]=day.optLong("usuarios");
                }
                addChartTitle(container,"Usuarios activos por día");
                addChart(container,DashboardChartView.Mode.LINE,labels,values);
            }
            addCaption(container,(zones==null?0:zones.length())+" zonas con actividad registrada");
        }catch(Exception e){show("No se pudieron mostrar los datos de uso");}
    }
    private void renderStats(String response){
        try{
            JSONObject j=new JSONObject(response); summaryData=j;
            LinearLayout container=findViewById(R.id.adminStats); container.removeAllViews();
            long places=j.optLong("lugares"),reviews=j.optLong("resenas");
            long reports=j.optLong("reportesSeguridad"),pending=j.optLong("denunciasPendientes");
            addMetricRow(container,"Lugares",places,0xFFE0F2FE,"Reseñas",reviews,0xFFCCFBF1);
            addMetricRow(container,"Zonas de riesgo",reports,0xFFFFE4E6,"Pendientes",pending,0xFFFEF3C7);
            addChartTitle(container,"Distribución del contenido");
            addChart(container,DashboardChartView.Mode.DONUT,
                    Arrays.asList("Lugares","Reseñas","Zonas","Pendientes"),
                    new long[]{places,reviews,reports,pending});
        }catch(Exception e){show("No se pudo mostrar el resumen");}
    }
    private void renderReports(String response){items.removeAllViews();try{JSONArray a=new JSONArray(response);pendingData=a;int shown=0;for(int i=0;i<a.length();i++){JSONObject value=a.getJSONObject(i);if(!matchesPending(value))continue;addReport(value);shown++;}empty.setText(a.length()==0?"No hay denuncias pendientes.":"No hay resultados con estos filtros.");empty.setVisibility(shown==0?View.VISIBLE:View.GONE);}catch(Exception e){show("No se pudieron mostrar las denuncias");}}
    private boolean matchesPending(JSONObject value){
        String[] types={"","LUGAR","RESENA","REPORTE"};
        if(pendingFilter>0&&!types[pendingFilter].equals(value.optString("tipoContenido")))return false;
        return containsQuery(pendingQuery,value.optString("categoriaContenido"),value.optString("nombreContenido"),
                value.optString("motivo"),value.optString("usuario"));
    }
    private void addReport(JSONObject j){
        View card=getLayoutInflater().inflate(R.layout.view_feed_item,items,false);
        ((TextView)card.findViewById(R.id.feedItemBadge)).setText(j.optString("categoriaContenido",j.optString("tipoContenido")));
        ((TextView)card.findViewById(R.id.feedItemTitle)).setText(j.optString("nombreContenido","Contenido denunciado"));
        ((TextView)card.findViewById(R.id.feedItemDetail)).setText(j.optString("motivo"));
        ((TextView)card.findViewById(R.id.feedItemMeta)).setText("Reportado por "+j.optString("usuario"));
        LinearLayout actions=card.findViewById(R.id.feedItemActions); actions.setVisibility(View.VISIBLE);
        MaterialButton dismiss=actionButton("DESCARTAR",false);
        MaterialButton delete=actionButton("ELIMINAR",true);
        actions.addView(dismiss,actionParams(true)); actions.addView(delete,actionParams(false));
        long id=j.optLong("id");
        dismiss.setOnClickListener(v->moderate(id,false));
        delete.setOnClickListener(v->new MaterialAlertDialogBuilder(this).setTitle("Eliminar contenido")
                .setMessage("Esta acción eliminará el contenido señalado de forma permanente.")
                .setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->moderate(id,true)).show());
        items.addView(card);
    }
    private void moderate(long id,boolean delete){String endpoint=ApiConfig.ADMIN_BASE_URL+"/denuncias/"+id+(delete?"/contenido":"/descartar");ApiClient.ApiCallback callback=new ApiClient.ApiCallback(){public void onSuccess(String r){runOnUiThread(()->{show(delete?"Contenido eliminado":"Denuncia descartada");load();});}public void onError(String e){runOnUiThread(()->show(e));}};if(delete)ApiClient.deleteAuthorized(endpoint,new SessionManager(this).getToken(),callback);else ApiClient.putAuthorized(endpoint,new SessionManager(this).getToken(),new JSONObject(),callback);}
    private void loadManaged(String type,LinearLayout target,boolean expired){String suffix="reportes".equals(type)?"?caducados="+expired:"";ApiClient.getAuthorized(ApiConfig.ADMIN_BASE_URL+"/"+type+suffix,new SessionManager(this).getToken(),new ApiClient.ApiCallback(){public void onSuccess(String response){runOnUiThread(()->renderManaged(type,target,response));}public void onError(String error){runOnUiThread(()->show(error));}});}
    private void renderManaged(String type,LinearLayout target,String response){
        target.removeAllViews();
        try{
            JSONArray array=new JSONArray(response);
            if(target==safetyReports)activeData=array;else if(target==expiredReports)expiredData=array;else placesData=array;
            JSONArray filtered=new JSONArray();
            for(int index=0;index<array.length();index++)if(matchesManaged(type,target,array.getJSONObject(index)))
                filtered.put(array.getJSONObject(index));
            array=filtered;
            if(array.length()==0){
                TextView message=new TextView(this);
                message.setText("No hay resultados con estos filtros.");
                message.setTextColor(getColor(R.color.app_text_secondary));message.setTextSize(15);
                message.setGravity(android.view.Gravity.CENTER);message.setPadding(dp(20),dp(32),dp(20),dp(32));
                message.setBackgroundResource(R.drawable.bg_dashboard_card);target.addView(message);return;
            }
            for(int i=0;i<array.length();i++){
                JSONObject j=array.getJSONObject(i);
                long id=j.optLong("lugares".equals(type)?"idLugar":"id");
                String title="lugares".equals(type)?j.optString("nombre"):j.optString("tipoEvento");
                String detail="lugares".equals(type)?j.optString("direccion",j.optString("tipo")):j.optString("descripcion");
                View card=getLayoutInflater().inflate(R.layout.view_feed_item,target,false);
                ((TextView)card.findViewById(R.id.feedItemBadge)).setText("lugares".equals(type)?j.optString("nombreCategoria"):
                        target==expiredReports?"CADUCADO · "+j.optString("estadoValidacion"):j.optString("estadoValidacion"));
                ((TextView)card.findViewById(R.id.feedItemTitle)).setText(title);
                ((TextView)card.findViewById(R.id.feedItemDetail)).setText(detail);
                String meta=target==expiredReports?"Caducó: "+readableDate(j.optString("fechaExpiracion")):"ID "+id;
                ((TextView)card.findViewById(R.id.feedItemMeta)).setText(meta);
                LinearLayout actions=card.findViewById(R.id.feedItemActions); actions.setVisibility(View.VISIBLE);
                MaterialButton view=actionButton("VER EN MAPA",false);
                MaterialButton delete=actionButton("ELIMINAR",true);
                view.setOnClickListener(v->openOnMap(type,j));
                delete.setOnClickListener(v->new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar "+("lugares".equals(type)?"lugar":"reporte"))
                        .setMessage("Esta acción es permanente.").setNegativeButton("Cancelar",null)
                        .setPositiveButton("Eliminar",(d,w)->deleteManaged(type,id)).show());
                actions.addView(view,actionParams(true)); actions.addView(delete,actionParams(false));
                target.addView(card);
            }
        }catch(Exception e){show("No se pudo cargar "+type);}
    }
    private boolean matchesManaged(String type,LinearLayout target,JSONObject value){
        if("lugares".equals(type)){
            String[] categories={"","HOTEL","RESTAURANTE","PARQUE","FARMACIA","HOSPITAL","COMISARIA"};
            if(placesFilter>0&&!categories[placesFilter].equals(value.optString("tipo")))return false;
            return containsQuery(placesQuery,value.optString("nombre"),value.optString("nombreCategoria"),
                    value.optString("direccion"),value.optString("descripcion"));
        }
        int filter=target==expiredReports?expiredFilter:activeFilter;
        String query=target==expiredReports?expiredQuery:activeQuery;
        if(filter==1&&!"ALTO".equals(value.optString("nivelPeligro")))return false;
        if(filter==2&&!"MEDIO".equals(value.optString("nivelPeligro")))return false;
        String[] states={"","","","PENDIENTE","CONFIRMADO","EN_REVISION","REFUTADO"};
        if(filter>=3&&!states[filter].equals(value.optString("estadoValidacion")))return false;
        return containsQuery(query,value.optString("tipoEvento"),value.optString("descripcion"),
                value.optString("autor"),value.optString("estadoValidacion"));
    }
    private boolean containsQuery(String query,String...values){
        if(query==null||query.trim().isEmpty())return true;String needle=query.toLowerCase(java.util.Locale.ROOT);
        for(String value:values)if(value!=null&&value.toLowerCase(java.util.Locale.ROOT).contains(needle))return true;
        return false;
    }
    private void deleteManaged(String type,long id){ApiClient.deleteAuthorized(ApiConfig.ADMIN_BASE_URL+"/"+type+"/"+id,new SessionManager(this).getToken(),new ApiClient.ApiCallback(){public void onSuccess(String response){runOnUiThread(()->{show("Contenido eliminado");load();});}public void onError(String error){runOnUiThread(()->show(error));}});}
    private String readableDate(String value){return value==null||value.isEmpty()?"fecha no disponible":
            (value.length()>=16?value.substring(0,16).replace('T',' '):value);}
    private MaterialButton actionButton(String label,boolean danger){
        MaterialButton button=new MaterialButton(this,null,com.google.android.material.R.attr.materialButtonOutlinedStyle);
        int color=danger?0xFFDC2626:getColor(R.color.app_primary_dark);
        button.setText(label); button.setTextSize(12); button.setSingleLine(true);
        button.setTextColor(color); button.setStrokeColor(android.content.res.ColorStateList.valueOf(color));
        button.setStrokeWidth(dp(1)); button.setCornerRadius(dp(12)); button.setMinHeight(dp(48));
        button.setInsetTop(0); button.setInsetBottom(0); return button;
    }
    private LinearLayout.LayoutParams actionParams(boolean first){
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(48),1);
        params.setMargins(first?0:dp(5),0,first?dp(5):0,0); return params;
    }
    private void openOnMap(String type,JSONObject item){
        Intent intent=new Intent(this,MainActivity.class)
                .putExtra(MainActivity.EXTRA_MAP_FOCUS_TYPE,"lugares".equals(type)?"LUGAR":"REPORTE")
                .putExtra(MainActivity.EXTRA_MAP_FOCUS_JSON,item.toString())
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    private void addMetricRow(LinearLayout parent,String firstLabel,long firstValue,int firstColor,
                              String secondLabel,long secondValue,int secondColor){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(metricCard(firstLabel,firstValue,firstColor),weightedCardParams(true));
        row.addView(metricCard(secondLabel,secondValue,secondColor),weightedCardParams(false));
        parent.addView(row,new LinearLayout.LayoutParams(-1,-2));
    }
    private View metricCard(String label,long value,int color){
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14),dp(12),dp(14),dp(12));
        GradientDrawable background=new GradientDrawable(); background.setColor(color); background.setCornerRadius(dp(14));
        card.setBackground(background);
        TextView number=new TextView(this); number.setText(java.text.NumberFormat.getIntegerInstance(new java.util.Locale("es","PE")).format(value));
        number.setTextSize(24); number.setTextColor(getColor(R.color.app_text_primary)); number.setTypeface(null,android.graphics.Typeface.BOLD);
        TextView name=new TextView(this); name.setText(label); name.setTextSize(13); name.setTextColor(getColor(R.color.app_text_secondary));
        card.addView(number); card.addView(name); return card;
    }
    private LinearLayout.LayoutParams weightedCardParams(boolean first){
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);
        params.setMargins(first?0:dp(6),dp(5),first?dp(6):0,dp(5)); return params;
    }
    private void addChartTitle(LinearLayout parent,String value){
        TextView title=new TextView(this); title.setText(value); title.setTextSize(16);
        title.setTextColor(getColor(R.color.app_text_primary)); title.setTypeface(null,android.graphics.Typeface.BOLD);
        title.setPadding(0,dp(18),0,dp(4)); parent.addView(title);
    }
    private void addCaption(LinearLayout parent,String value){
        TextView caption=new TextView(this); caption.setText(value); caption.setTextSize(13);
        caption.setTextColor(getColor(R.color.app_text_secondary)); caption.setPadding(0,dp(8),0,0); parent.addView(caption);
    }
    private void addChart(LinearLayout parent,DashboardChartView.Mode mode,List<String> labels,long[] values){
        DashboardChartView chart=new DashboardChartView(this); chart.setMode(mode); chart.setData(labels,values);
        parent.addView(chart,new LinearLayout.LayoutParams(-1,-2));
    }
    private String shortDate(String value){
        if(value==null)return ""; int split=value.lastIndexOf('-'); return split>=0?value.substring(split+1):value;
    }
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private void show(String message){Snackbar.make(findViewById(R.id.adminRoot),message,Snackbar.LENGTH_LONG).show();}
    private void exportPdf(){if(summaryData==null||usageData==null){show("Espera a que terminen de cargar los indicadores");return;}String date=new java.text.SimpleDateFormat("yyyy-MM-dd",java.util.Locale.US).format(new java.util.Date());pdfLauncher.launch("resumen-plataforma-"+date+".pdf");}
}
