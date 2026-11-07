package com.example.appmovil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.google.android.material.appbar.MaterialToolbar;

public class NuevoReporteActivity extends AppCompatActivity {
    private double latitude, longitude;
    private Spinner eventType, danger, duration;
    private MaterialButtonToggleGroup radiusGroup;
    private EditText description;
    private CheckBox anonymous;
    private byte[] imageBytes; private String imageMime;
    private volatile String district="";
    private volatile boolean districtResolving=true;
    private final ActivityResultLauncher<String> pickImage=registerForActivityResult(new ActivityResultContracts.GetContent(),uri->{if(uri!=null)readImage(uri);});
    @Override protected void onCreate(Bundle state){super.onCreate(state);setContentView(R.layout.activity_nuevo_reporte);UiInsets.apply(this);
        latitude=getIntent().getDoubleExtra("latitude",0);longitude=getIntent().getDoubleExtra("longitude",0);
        MaterialToolbar toolbar=findViewById(R.id.topAppBar);toolbar.setTitle("Reportar zona de riesgo");toolbar.setSubtitle("Describe lo ocurrido y el alcance aproximado");toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);toolbar.setNavigationOnClickListener(v->finish());
        eventType=findViewById(R.id.spReportEvent); danger=findViewById(R.id.spReportDanger);duration=findViewById(R.id.spReportDuration);
        radiusGroup=findViewById(R.id.reportRadiusGroup);
        description=findViewById(R.id.etReportDescription); anonymous=findViewById(R.id.cbAnonymousReport);
        eventType.setAdapter(adapter(new String[]{"Asalto o robo","Accidente de tránsito","Acoso","Vandalismo","Otro riesgo"}));
        danger.setAdapter(adapter(new String[]{"Peligro medio","Peligro alto"}));
        duration.setAdapter(adapter(new String[]{"1 hora","6 horas","12 horas","24 horas","48 horas","7 días"}));duration.setSelection(3);
        radiusGroup.check(R.id.btnRadius25);
        radiusGroup.addOnButtonCheckedListener((group,checkedId,isChecked)->{if(isChecked)updateRadiusHelp(checkedId);});
        updateRadiusHelp(R.id.btnRadius25);
        ((TextView)findViewById(R.id.tvReportCoordinates)).setText(String.format(java.util.Locale.ROOT,"%.6f, %.6f",latitude,longitude));
        loadDistrict();
        findViewById(R.id.btnSelectReportImage).setOnClickListener(v->pickImage.launch("image/*"));
        findViewById(R.id.btnSaveReport).setOnClickListener(v->save());
    }
    private ArrayAdapter<String> adapter(String[] values){ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values);return a;}
    private void save(){String text=description.getText().toString().trim();if(text.isEmpty()){description.setError("Describe lo ocurrido");return;}
        if(districtResolving){Snackbar.make(findViewById(R.id.btnSaveReport),"Espera mientras identificamos el distrito",Snackbar.LENGTH_SHORT).show();return;}
        if(imageBytes==null){Snackbar.make(findViewById(R.id.btnSaveReport),"Debes añadir una imagen del incidente",Snackbar.LENGTH_LONG).show();return;}
        View button=findViewById(R.id.btnSaveReport);button.setEnabled(false);
        ApiClient.uploadSafetyReport(ApiConfig.REPORTS_BASE_URL+"/con-imagen",new SessionManager(this).getToken(),eventType.getSelectedItem().toString(),danger.getSelectedItemPosition()==0?"MEDIO":"ALTO",text,latitude,longitude,selectedRadius(),selectedDuration(),district,anonymous.isChecked(),imageBytes,imageMime,new ApiClient.ApiCallback(){public void onSuccess(String response){runOnUiThread(()->{setResult(Activity.RESULT_OK);finish();});}public void onError(String error){runOnUiThread(()->{button.setEnabled(true);Snackbar.make(button,error,Snackbar.LENGTH_LONG).show();});}});
    }
    private int selectedDuration(){int[] values={1,6,12,24,48,168};return values[duration.getSelectedItemPosition()];}
    private void readImage(Uri uri){String type=getContentResolver().getType(uri);if(type==null||!(type.equals("image/jpeg")||type.equals("image/png")||type.equals("image/webp"))){show("Usa una imagen JPG, PNG o WebP");return;}try(InputStream input=getContentResolver().openInputStream(uri);ByteArrayOutputStream output=new ByteArrayOutputStream()){if(input==null)throw new IllegalStateException();byte[] buffer=new byte[8192];int count;while((count=input.read(buffer))!=-1){output.write(buffer,0,count);if(output.size()>5L*1024*1024){show("La imagen no debe superar 5 MB");return;}}imageBytes=output.toByteArray();imageMime=type;ImageView preview=findViewById(R.id.reportImagePreview);preview.setImageURI(uri);preview.setVisibility(View.VISIBLE);((TextView)findViewById(R.id.btnSelectReportImage)).setText("CAMBIAR IMAGEN");}catch(Exception e){show("No se pudo leer la imagen");}}
    private void loadDistrict(){new Thread(()->{String value=DistrictResolver.resolve(this,latitude,longitude);district=value;districtResolving=false;runOnUiThread(()->((TextView)findViewById(R.id.tvReportDistrict)).setText(value.isBlank()?"Distrito no identificado":value));},"report-district").start();}
    private void show(String value){Snackbar.make(findViewById(R.id.btnSaveReport),value,Snackbar.LENGTH_LONG).show();}
    private int selectedRadius(){
        int checked=radiusGroup.getCheckedButtonId();
        if(checked==R.id.btnRadius10)return 10;
        if(checked==R.id.btnRadius50)return 50;
        return 25;
    }
    private void updateRadiusHelp(int checkedId){
        int meters=checkedId==R.id.btnRadius10?10:checkedId==R.id.btnRadius50?50:25;
        String detail=meters==10?"Punto muy concreto, como una esquina o una entrada."
                :meters==25?"Alcance recomendado para una cuadra corta."
                :"Cobertura máxima para una zona cercana.";
        ((TextView)findViewById(R.id.tvRadiusHelp)).setText(meters+" metros · "+detail);
        ((RadiusPreviewView)findViewById(R.id.radiusPreview)).setRadiusMeters(meters);
    }
}
