package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;

public class VerificarAccesoActivity extends AppCompatActivity {
    private String desafio,correo;
    private TextInputEditText code;
    private TextView status;
    private MaterialButton confirm,resend;
    private boolean busy;
    private long resendAt;
    private CountDownTimer timer;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);setContentView(R.layout.activity_verificar_acceso);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.verificationRoot),(v,insets)->{
            var bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            var ime=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left,bars.top,bars.right,Math.max(bars.bottom,ime.bottom));return insets;
        });
        desafio=getIntent().getStringExtra("desafioId");correo=getIntent().getStringExtra("correo");
        if(desafio==null || desafio.isEmpty()){back();return;}
        ((MaterialToolbar)findViewById(R.id.verificationToolbar)).setNavigationOnClickListener(v->back());
        boolean registration="CORREO".equals(getIntent().getStringExtra("paso"));
        ((TextView)findViewById(R.id.verificationTitle)).setText(registration?"Confirma tu correo":"Un paso más para entrar");
        ((TextView)findViewById(R.id.verificationDescription)).setText(
                (registration?"Verifica que este correo te pertenece: ":"Enviamos un código para confirmar tu inicio de sesión a: ") + correo);
        code=findViewById(R.id.verificationCode);status=findViewById(R.id.verificationStatus);
        confirm=findViewById(R.id.verificationConfirm);resend=findViewById(R.id.verificationResend);
        confirm.setOnClickListener(v->submit(false));resend.setOnClickListener(v->submit(true));
        findViewById(R.id.verificationBack).setOnClickListener(v->back());
        code.setOnEditorActionListener((v,action,event)->{if(action==android.view.inputmethod.EditorInfo.IME_ACTION_DONE){submit(false);return true;}return false;});
        resendAt=state==null?SystemClock.elapsedRealtime()+60000:state.getLong("resendAt");
        countdown();
    }
    private void submit(boolean sending) {
        if(busy)return;
        String value=code.getText()==null?"":code.getText().toString();
        TextInputLayout layout=findViewById(R.id.verificationCodeLayout);layout.setError(null);
        if(!sending&&!value.matches("[0-9]{6}")){layout.setError("Ingresa los seis dígitos del correo");return;}
        try {
            JSONObject body=new JSONObject();body.put("desafioId",desafio);
            if(!sending)body.put("codigo",value);
            busy=true;confirm.setEnabled(false);resend.setEnabled(false);
            status.setText(sending?"Enviando otro código…":"Verificando…");
            ApiClient.postJson(ApiConfig.AUTH_BASE_URL+"/verificacion/"+(sending?"reenviar":"confirmar"),body,new ApiClient.ApiCallback(){
                public void onSuccess(String response){runOnUiThread(()->{
                    if(isFinishing()||isDestroyed())return;
                    busy=false;confirm.setEnabled(true);
                    try{
                        JSONObject json=new JSONObject(response);
                        if(sending){code.setText("");resendAt=SystemClock.elapsedRealtime()+60000;countdown();status.setText("Nuevo código enviado. El anterior ya no es válido.");return;}
                        String token=json.optString("tokenSesion","");
                        if(token.isEmpty()||"null".equals(token))throw new IllegalStateException();
                        new SessionManager(VerificarAccesoActivity.this).saveSession(json.getInt("idUsuario"),
                                json.optString("nombreCompleto"),correo,token);
                        startActivity(new Intent(VerificarAccesoActivity.this,MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }catch(Exception error){status.setText("No se recibió una sesión válida. Vuelve a iniciar sesión.");countdown();}
                });}
                public void onError(String error){runOnUiThread(()->{if(isFinishing()||isDestroyed())return;
                    busy=false;confirm.setEnabled(true);status.setText(error);countdown();});}
            });
        }catch(Exception error){busy=false;confirm.setEnabled(true);status.setText("No se pudo preparar la solicitud.");}
    }
    private void countdown(){
        if(timer!=null)timer.cancel();
        long remaining=Math.max(0,resendAt-SystemClock.elapsedRealtime());
        if(remaining==0){resend.setEnabled(!busy);resend.setText("Reenviar código");return;}
        resend.setEnabled(false);
        timer=new CountDownTimer(remaining,1000){
            public void onTick(long time){resend.setText("Reenviar en "+((time+999)/1000)+" s");}
            public void onFinish(){resend.setText("Reenviar código");resend.setEnabled(!busy);}
        }.start();
    }
    private void back(){startActivity(new Intent(this,login.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));finish();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putLong("resendAt",resendAt);}
    @Override protected void onDestroy(){if(timer!=null)timer.cancel();super.onDestroy();}
}
