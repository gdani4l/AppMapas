package com.example.appmovil;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.json.JSONObject;
import java.util.List;
import org.json.JSONArray;
import android.widget.ImageView;

final class ReportDetailSheet {
    static void show(Activity activity,SafetyReport report,Runnable refreshMap){
        BottomSheetDialog dialog=new BottomSheetDialog(activity);View content=activity.getLayoutInflater().inflate(R.layout.sheet_report_detail,null);
        bind(activity,content,report);dialog.setContentView(content);dialog.show();
        if(dialog.getWindow()!=null)dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View bottom=dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if(bottom!=null)bottom.setBackgroundColor(Color.TRANSPARENT);
        content.findViewById(R.id.btnCloseReportDetail).setOnClickListener(v->dialog.dismiss());
        content.findViewById(R.id.btnReportTrue).setOnClickListener(v->vote(activity,content,report,"VERDADERO",refreshMap));
        content.findViewById(R.id.btnReportFalse).setOnClickListener(v->vote(activity,content,report,"FALSO",refreshMap));
        TextView action=content.findViewById(R.id.btnFlagSafetyReport);
        if(report.own){content.findViewById(R.id.btnReportTrue).setVisibility(View.GONE);content.findViewById(R.id.btnReportFalse).setVisibility(View.GONE);action.setText("ELIMINAR MI REPORTE");action.setOnClickListener(v->confirmDelete(activity,content,dialog,report,false,refreshMap));}
        else if("ADMIN".equalsIgnoreCase(new SessionManager(activity).getRole())){action.setText("ELIMINAR REPORTE");action.setOnClickListener(v->confirmDelete(activity,content,dialog,report,true,refreshMap));}
        else action.setOnClickListener(v->flag(activity,content,report));
        EditText commentInput=content.findViewById(R.id.etReportComment);
        if(bottom!=null)commentInput.setOnFocusChangeListener((v,focused)->{if(focused)
            com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottom)
                    .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);});
        int[] page={0};
        content.findViewById(R.id.btnSendReportComment).setOnClickListener(v->{String text=commentInput.getText().toString().trim();if(text.isEmpty())return;ReportRepository.comment(activity,report.id,text,new ReportRepository.Callback<>(){public void success(JSONObject value){activity.runOnUiThread(()->{commentInput.setText("");page[0]=0;loadComments(activity,content,report.id,page);});}public void error(String message){activity.runOnUiThread(()->Snackbar.make(content,message,Snackbar.LENGTH_LONG).show());}});});
        content.findViewById(R.id.btnReportCommentWithImage).setOnClickListener(v->{dialog.dismiss();activity.startActivity(ReportCommentActivity.intent(activity,report.id));});
        content.findViewById(R.id.btnPreviousReportComments).setOnClickListener(v->{if(page[0]>0){page[0]--;loadComments(activity,content,report.id,page);}});
        content.findViewById(R.id.btnNextReportComments).setOnClickListener(v->{page[0]++;loadComments(activity,content,report.id,page);});
        loadComments(activity,content,report.id,page);
    }
    private static void bind(Activity a,View v,SafetyReport r){((TextView)v.findViewById(R.id.tvReportDetailType)).setText(r.eventType);((TextView)v.findViewById(R.id.tvReportDetailDanger)).setText(r.dangerLevel.equals("ALTO")?"Zona de peligro alto":"Zona de peligro medio");((TextView)v.findViewById(R.id.tvReportDetailDescription)).setText(r.description);String district=r.district.isBlank()?"":(" · "+r.district);((TextView)v.findViewById(R.id.tvReportDetailMeta)).setText(r.author+district+" · "+age(r.createdAt)+" · radio "+r.radiusMeters+" m · caduca "+expiry(r.expiresAt));((TextView)v.findViewById(R.id.tvReportValidation)).setText(status(r.status)+" · "+r.confirmations+" confirman · "+r.refutations+" refutan");ImageView image=v.findViewById(R.id.reportDetailImage);if(!r.imageUrl.isBlank()){image.setVisibility(View.VISIBLE);RemoteImageLoader.load(image,r.imageUrl,R.drawable.bg_input);image.setOnClickListener(x->a.startActivity(PhotoViewerActivity.intent(a,java.util.Collections.singletonList(r.imageUrl),0,"Imagen del reporte")));}}
    private static void loadComments(Activity a,View content,long id,int[] page){ReportRepository.comments(id,page[0],new ReportRepository.Callback<>(){public void success(JSONObject result){a.runOnUiThread(()->{LinearLayout list=content.findViewById(R.id.reportComments);list.removeAllViews();JSONArray values=result.optJSONArray("comentarios");if(values==null)values=new JSONArray();for(int i=0;i<values.length();i++){JSONObject comment=values.optJSONObject(i);if(comment!=null)list.addView(ReportCommentCardBinder.create(a,list,comment));}boolean empty=values.length()==0;content.findViewById(R.id.tvNoReportComments).setVisibility(empty?View.VISIBLE:View.GONE);int totalPages=result.optInt("totalPaginas");View pager=content.findViewById(R.id.reportCommentPager);pager.setVisibility(totalPages>1?View.VISIBLE:View.GONE);((TextView)content.findViewById(R.id.tvReportCommentPage)).setText((page[0]+1)+" / "+Math.max(1,totalPages));content.findViewById(R.id.btnPreviousReportComments).setEnabled(page[0]>0);content.findViewById(R.id.btnNextReportComments).setEnabled(page[0]+1<totalPages);});}public void error(String m){}});}
    private static void vote(Activity a,View v,SafetyReport r,String value,Runnable refresh){ReportRepository.vote(a,r.id,value,new ReportRepository.Callback<>(){public void success(JSONObject json){a.runOnUiThread(()->{Snackbar.make(v,"Gracias por validar el reporte",Snackbar.LENGTH_SHORT).show();refresh.run();});}public void error(String m){a.runOnUiThread(()->Snackbar.make(v,m,Snackbar.LENGTH_LONG).show());}});}
    private static void flag(Activity a,View v,SafetyReport r){EditText input=new EditText(a);input.setHint("Explica por qué el reporte es falso o inapropiado");new MaterialAlertDialogBuilder(a).setTitle("Enviar a revisión").setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Enviar",(d,w)->ReportRepository.flag(a,r.id,input.getText().toString().trim().isEmpty()?"Información posiblemente falsa":input.getText().toString().trim(),new ReportRepository.Callback<>(){public void success(JSONObject x){a.runOnUiThread(()->Snackbar.make(v,"Enviado a revisión",Snackbar.LENGTH_LONG).show());}public void error(String m){a.runOnUiThread(()->Snackbar.make(v,m,Snackbar.LENGTH_LONG).show());}})).show();}
    private static void confirmDelete(Activity a,View v,BottomSheetDialog dialog,SafetyReport r,boolean admin,Runnable refresh){new MaterialAlertDialogBuilder(a).setTitle("Eliminar reporte").setMessage("El reporte, sus comentarios y votos se eliminarán permanentemente.").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->{ApiClient.ApiCallback callback=new ApiClient.ApiCallback(){public void onSuccess(String x){a.runOnUiThread(()->{dialog.dismiss();refresh.run();Snackbar.make(a.findViewById(android.R.id.content),"Reporte eliminado",Snackbar.LENGTH_LONG).show();});}public void onError(String m){a.runOnUiThread(()->Snackbar.make(v,m,Snackbar.LENGTH_LONG).show());}};if(admin)ReportRepository.deleteAsAdmin(a,r.id,callback);else ReportRepository.deleteOwn(a,r.id,callback);}).show();}
    private static String status(String value) {
        if ("CONFIRMADO".equals(value)) return "Confirmado por la comunidad";
        if ("EN_REVISION".equals(value)) return "En revisión";
        if ("REFUTADO".equals(value)) return "Refutado";
        return "Pendiente de validar";
    }
    private static String age(String value){try{java.time.LocalDateTime t=java.time.LocalDateTime.parse(value);long min=java.time.Duration.between(t,java.time.LocalDateTime.now()).toMinutes();if(min<60)return"hace "+Math.max(1,min)+" min";if(min<1440)return"hace "+(min/60)+" h";return"hace "+(min/1440)+" d";}catch(Exception e){return"reciente";}}
    private static String expiry(String value){try{long min=java.time.Duration.between(java.time.LocalDateTime.now(),java.time.LocalDateTime.parse(value)).toMinutes();if(min<=0)return"expirado";if(min<60)return"en "+min+" min";return"en "+(min/60)+" h";}catch(Exception e){return"en 24 h";}}
}
