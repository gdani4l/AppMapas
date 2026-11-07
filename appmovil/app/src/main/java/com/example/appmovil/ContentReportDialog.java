package com.example.appmovil;

import android.app.Activity;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;

final class ContentReportDialog {
    static void show(Activity activity, android.view.View anchor, String type, long contentId) {
        if (new SessionManager(activity).getToken().isEmpty()) {
            Snackbar.make(anchor,"Inicia sesión para enviar el contenido a revisión",Snackbar.LENGTH_LONG).show();
            return;
        }
        EditText reason=new EditText(activity);
        reason.setHint("Indica qué información es falsa, ofensiva o spam");
        reason.setMinLines(3); reason.setMaxLines(6); reason.setPadding(32,20,32,20);
        AlertDialog dialog=new MaterialAlertDialogBuilder(activity).setTitle("Reportar contenido")
                .setMessage("Un administrador revisará el contenido antes de tomar una decisión.")
                .setView(reason).setNegativeButton("Cancelar",null)
                .setPositiveButton("ENVIAR",(dialogInterface,which)->send(activity,anchor,type,contentId,reason.getText().toString().trim()))
                .create();
        dialog.setOnShowListener(ignored->{if(dialog.getWindow()!=null)
            dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);});
        dialog.show();
    }
    private static void send(Activity activity,android.view.View anchor,String type,long contentId,String reason) {
        if(reason.isEmpty()) { Snackbar.make(anchor,"Escribe el motivo del reporte",Snackbar.LENGTH_LONG).show(); return; }
        try {
            JSONObject body=new JSONObject().put("tipoContenido",type).put("idContenido",contentId).put("motivo",reason);
            ApiClient.postAuthorized(ApiConfig.REPORTS_BASE_URL+"/contenido",new SessionManager(activity).getToken(),body,new ApiClient.ApiCallback(){
                public void onSuccess(String response){activity.runOnUiThread(()->Snackbar.make(anchor,"Contenido enviado a revisión",Snackbar.LENGTH_LONG).show());}
                public void onError(String error){activity.runOnUiThread(()->Snackbar.make(anchor,error,Snackbar.LENGTH_LONG).show());}
            });
        } catch(Exception error) { Snackbar.make(anchor,"No se pudo preparar el reporte",Snackbar.LENGTH_LONG).show(); }
    }
}
