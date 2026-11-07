package com.example.appmovil;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ReportCommentActivity extends AppCompatActivity {
    private long reportId;private byte[] image;private String mime;
    static Intent intent(Context context,long reportId){return new Intent(context,ReportCommentActivity.class).putExtra("reportId",reportId);}
    private final ActivityResultLauncher<String> picker=registerForActivityResult(new ActivityResultContracts.GetContent(),uri->{if(uri!=null)read(uri);});
    @Override protected void onCreate(Bundle state){super.onCreate(state);setContentView(R.layout.activity_report_comment);UiInsets.apply(this);reportId=getIntent().getLongExtra("reportId",0);if(reportId<=0){finish();return;}((com.google.android.material.appbar.MaterialToolbar)findViewById(R.id.btnCloseReportComment)).setNavigationOnClickListener(v->finish());findViewById(R.id.btnSelectReportCommentImage).setOnClickListener(v->picker.launch("image/*"));findViewById(R.id.btnPublishReportComment).setOnClickListener(v->save());}
    private void read(Uri uri){String type=getContentResolver().getType(uri);if(type==null||!(type.equals("image/jpeg")||type.equals("image/png")||type.equals("image/webp"))){show("Usa una imagen JPG, PNG o WebP");return;}try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){out.write(buffer,0,n);if(out.size()>5L*1024*1024){show("La imagen no debe superar 5 MB");return;}}image=out.toByteArray();mime=type;ImageView preview=findViewById(R.id.reportCommentImage);preview.setImageURI(uri);preview.setVisibility(android.view.View.VISIBLE);}catch(Exception e){show("No se pudo leer la imagen");}}
    private void save(){String text=((android.widget.EditText)findViewById(R.id.reportCommentText)).getText().toString().trim();if(text.isEmpty()){show("Escribe un comentario");return;}findViewById(R.id.btnPublishReportComment).setEnabled(false);ReportRepository.commentWithImage(this,reportId,text,image,mime,new ApiClient.ApiCallback(){public void onSuccess(String r){runOnUiThread(()->finish());}public void onError(String e){runOnUiThread(()->{findViewById(R.id.btnPublishReportComment).setEnabled(true);show(e);});}});}
    private void show(String value){Snackbar.make(findViewById(R.id.btnPublishReportComment),value,Snackbar.LENGTH_LONG).show();}
}
