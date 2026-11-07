package com.example.appmovil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import android.text.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import org.json.*;

public class GestionCuentasActivity extends AppCompatActivity {
    private EditText search;
    private Spinner state,role,verified;
    private TextView status,pageLabel;
    private LinearLayout items;
    private View progress,previous,next;
    private int page,version;
    private boolean mutation;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable pending=()->load();

    @Override protected void onCreate(Bundle saved){
        super.onCreate(saved);setContentView(R.layout.activity_gestion_cuentas);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.accountsRoot),(v,insets)->{
            var bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            var ime=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,ime.bottom));return insets;
        });
        MaterialToolbar toolbar=findViewById(R.id.accountsToolbar);
        toolbar.setNavigationOnClickListener(v->finish());toolbar.inflateMenu(R.menu.menu_feed);
        toolbar.setOnMenuItemClickListener(item->{load();return true;});
        search=findViewById(R.id.accountsSearch);state=findViewById(R.id.accountsState);
        role=findViewById(R.id.accountsRole);verified=findViewById(R.id.accountsVerified);
        status=findViewById(R.id.accountsStatus);pageLabel=findViewById(R.id.accountsPage);
        items=findViewById(R.id.accountsItems);progress=findViewById(R.id.accountsProgress);
        previous=findViewById(R.id.accountsPrevious);next=findViewById(R.id.accountsNext);
        setup(state,new String[]{"Todos los estados","Activas","Suspendidas"});
        setup(role,new String[]{"Todos los roles","Administradores","Usuarios"});
        setup(verified,new String[]{"Todos los correos","Correo verificado","Verificación pendiente"});
        if(saved!=null){
            search.setText(saved.getString("q",""));state.setSelection(saved.getInt("state"));
            role.setSelection(saved.getInt("role"));verified.setSelection(saved.getInt("verified"));
        }
        search.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int count,int after){}
            public void onTextChanged(CharSequence s,int st,int before,int count){schedule();}
            public void afterTextChanged(Editable e){}
        });
        previous.setOnClickListener(v->{if(page>0){page--;load();}});
        next.setOnClickListener(v->{page++;load();});
        schedule();
    }
    private void setup(Spinner spinner,String[] options){
        spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> p){}
            public void onItemSelected(AdapterView<?> p,View v,int position,long id){schedule();}
        });
    }
    private void schedule(){page=0;++version;handler.removeCallbacks(pending);handler.postDelayed(pending,400);}
    private void load(){
        handler.removeCallbacks(pending);final int request=++version;
        previous.setEnabled(false);next.setEnabled(false);progress.setVisibility(View.VISIBLE);
        String[] states={"TODOS","ACTIVA","SUSPENDIDA"},roles={"TODOS","ADMIN","USUARIO"},checks={"TODOS","SI","NO"};
        String query=android.net.Uri.encode(search.getText().toString().trim());
        String url=ApiConfig.ADMIN_BASE_URL+"/cuentas?buscar="+query+"&estado="+states[state.getSelectedItemPosition()]
                +"&rol="+roles[role.getSelectedItemPosition()]+"&verificado="+checks[verified.getSelectedItemPosition()]+"&pagina="+page;
        ApiClient.getAuthorized(url,new SessionManager(this).getToken(),new ApiClient.ApiCallback(){
            public void onSuccess(String response){runOnUiThread(()->{
                if(isDestroyed()||isFinishing()||request!=version)return;
                progress.setVisibility(View.GONE);
                try{
                    JSONObject data=new JSONObject(response);JSONArray rows=data.getJSONArray("cuentas");
                    long total=data.getLong("total");int pages=(int)Math.max(1,(total+24)/25);
                    if(page>=pages){page=pages-1;load();return;}
                    items.removeAllViews();
                    for(int i=0;i<rows.length();i++)add(rows.getJSONObject(i));
                    status.setText(total==0?"No hay cuentas con estos filtros. Prueba otro nombre o estado.":total+" cuentas encontradas");
                    pageLabel.setText((page+1)+" / "+pages);
                    previous.setEnabled(page>0);next.setEnabled(page+1<pages);
                    ((androidx.core.widget.NestedScrollView)findViewById(R.id.accountsScroll)).scrollTo(0,0);
                }catch(Exception error){status.setText("No se pudieron mostrar las cuentas. Pulsa Actualizar.");}
            });}
            public void onError(String error){runOnUiThread(()->{
                if(isDestroyed()||request!=version)return;progress.setVisibility(View.GONE);status.setText(error);
                Snackbar.make(findViewById(R.id.accountsRoot),error,Snackbar.LENGTH_LONG).setAction("Reintentar",v->load()).show();
            });}
        });
    }
    private boolean flag(JSONObject data,String key){return data.optBoolean(key)||data.optInt(key)==1;}
    private void add(JSONObject user){
        View card=getLayoutInflater().inflate(R.layout.item_cuenta,items,false);
        ((TextView)card.findViewById(R.id.accountName)).setText(user.optString("nombre"));
        ((TextView)card.findViewById(R.id.accountEmail)).setText(user.optString("correo"));
        boolean active=flag(user,"activa"),checked=flag(user,"correo_verificado");
        ((TextView)card.findViewById(R.id.accountState)).setText(
                ("ADMIN".equals(user.optString("rol"))?"Administrador":"Usuario")+" · "+(active?"Activa":"Suspendida")+"\n"+
                        (checked?"Correo verificado":"Correo pendiente de verificación"));
        ((TextView)card.findViewById(R.id.accountDate)).setText("Registro: "+user.optString("fecha_registro").replace('T',' '));
        Button manage=card.findViewById(R.id.accountManage);
        boolean self=user.optInt("id_usuario")==new SessionManager(this).getUserId();
        manage.setEnabled(!self);if(self)manage.setText("Tu cuenta");
        manage.setOnClickListener(v->actions(user));items.addView(card);
    }
    private void actions(JSONObject user){
        if(mutation)return;
        boolean active=flag(user,"activa"),admin="ADMIN".equals(user.optString("rol"));
        String[] labels={active?"Suspender cuenta":"Reactivar cuenta",admin?"Cambiar a usuario":"Asignar administrador","Cerrar todas sus sesiones"};
        String[] codes={active?"SUSPENDER":"ACTIVAR",admin?"USUARIO":"ADMIN","CERRAR_SESIONES"};
        new MaterialAlertDialogBuilder(this).setTitle(user.optString("nombre")).setItems(labels,(d,which)->
                new MaterialAlertDialogBuilder(this).setTitle(labels[which])
                    .setMessage("Cuenta: "+user.optString("correo")+"\nSe cerrarán sus sesiones actuales. "+
                            ("ADMIN".equals(codes[which])?"Esta persona podrá administrar cuentas y contenido.":""))
                    .setNegativeButton("Cancelar",null).setPositiveButton("Confirmar",(dialog,w)->change(user.optInt("id_usuario"),codes[which])).show()
        ).show();
    }
    private void change(int id,String action){
        if(mutation)return;mutation=true;
        try{
            JSONObject body=new JSONObject().put("accion",action);
            ApiClient.putAuthorized(ApiConfig.ADMIN_BASE_URL+"/cuentas/"+id,new SessionManager(this).getToken(),body,new ApiClient.ApiCallback(){
                public void onSuccess(String response){runOnUiThread(()->{mutation=false;if(isDestroyed())return;load();Toast.makeText(GestionCuentasActivity.this,"Cuenta actualizada",Toast.LENGTH_SHORT).show();});}
                public void onError(String error){runOnUiThread(()->{mutation=false;if(!isDestroyed())new MaterialAlertDialogBuilder(GestionCuentasActivity.this).setMessage(error).setPositiveButton("Entendido",null).show();});}
            });
        }catch(Exception e){mutation=false;}
    }
    @Override protected void onSaveInstanceState(Bundle out){
        super.onSaveInstanceState(out);out.putString("q",search.getText().toString());out.putInt("state",state.getSelectedItemPosition());
        out.putInt("role",role.getSelectedItemPosition());out.putInt("verified",verified.getSelectedItemPosition());
    }
    @Override protected void onDestroy(){handler.removeCallbacks(pending);super.onDestroy();}
}
