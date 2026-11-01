package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class login extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        executorService = Executors.newSingleThreadExecutor();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        CheckBox cbShowPassword = findViewById(R.id.cbShowPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) ->
                mostrarPassword(etPassword, isChecked)
        );

        btnLogin.setOnClickListener(v -> iniciarSesion());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, registro.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(login.this, RecuperarContrasenaActivity.class))
        );
    }

    private void iniciarSesion() {
        String correo = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (!validarDatos(correo, password)) {
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("INGRESANDO...");

        executorService.execute(() -> {
            HttpURLConnection conexion = null;

            try {
                JSONObject datos = new JSONObject();
                datos.put("correo", correo);
                datos.put("password", password);

                URL url = new URL(ApiConfig.AUTH_BASE_URL + "/login");
                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conexion.setRequestProperty("Accept", "application/json");

                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(datos.toString().getBytes(StandardCharsets.UTF_8));
                }

                int codigo = conexion.getResponseCode();
                String respuesta = leerRespuesta(
                        codigo >= 200 && codigo < 300
                                ? conexion.getInputStream()
                                : conexion.getErrorStream()
                );

                if (codigo >= 200 && codigo < 300) {
                    JSONObject json = new JSONObject(respuesta);
                    String nombreCompleto = json.optString("nombreCompleto", "");
                    int idUsuario = json.optInt("idUsuario", 0);
                    String tokenSesion = json.optString("tokenSesion", "");

                    if (idUsuario <= 0 || tokenSesion.isEmpty()) {
                        runOnUiThread(() -> mostrarError(
                                "El servidor no devolvió una sesión válida. Reinicia el backend."
                        ));
                    } else {
                        runOnUiThread(() -> abrirPantallaPrincipal(
                                idUsuario,
                                nombreCompleto,
                                correo,
                                tokenSesion
                        ));
                    }
                } else {
                    String mensaje = obtenerMensajeError(respuesta, "Correo o contraseña incorrectos.");
                    runOnUiThread(() -> mostrarError(mensaje));
                }
            } catch (Exception e) {
                runOnUiThread(() -> mostrarError(
                        "No se pudo conectar con el servidor. Verifica que Spring Boot esté iniciado."
                ));
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        });
    }

    private boolean validarDatos(String correo, String password) {
        if (TextUtils.isEmpty(correo)) {
            etEmail.setError("Ingresa tu correo");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmail.setError("Ingresa un correo válido");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingresa tu contraseña");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void abrirPantallaPrincipal(
            int idUsuario,
            String nombreCompleto,
            String correo,
            String tokenSesion
    ) {
        new SessionManager(this).saveSession(
                idUsuario,
                nombreCompleto,
                correo,
                tokenSesion
        );
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("NOMBRE_USUARIO", nombreCompleto);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
        btnLogin.setText("INGRESAR");
    }

    private void mostrarPassword(EditText campo, boolean mostrar) {
        campo.setTransformationMethod(
                mostrar
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance()
        );
        campo.setSelection(campo.length());
    }

    private String leerRespuesta(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();

        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)
        )) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                resultado.append(linea);
            }
        }

        return resultado.toString();
    }

    private String obtenerMensajeError(String respuesta, String mensajePorDefecto) {
        try {
            JSONObject json = new JSONObject(respuesta);
            return json.optString(
                    "detail",
                    json.optString("message", mensajePorDefecto)
            );
        } catch (Exception e) {
            return mensajePorDefecto;
        }
    }

    @Override
    protected void onDestroy() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }
}
