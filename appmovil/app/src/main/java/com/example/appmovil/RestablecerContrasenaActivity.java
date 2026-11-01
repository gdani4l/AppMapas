package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

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

public class RestablecerContrasenaActivity extends AppCompatActivity {

    private String correo;
    private EditText etCodigo, etNuevaPassword, etConfirmarPassword;
    private MaterialButton btnRestablecer;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restablecer_contrasena);

        correo = getIntent().getStringExtra("CORREO_RECUPERACION");
        executorService = Executors.newSingleThreadExecutor();

        TextView tvCorreoDestino = findViewById(R.id.tvCorreoDestino);
        etCodigo = findViewById(R.id.etCodigo);
        etNuevaPassword = findViewById(R.id.etNuevaPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        btnRestablecer = findViewById(R.id.btnRestablecer);

        tvCorreoDestino.setText("Código enviado a: " + correo);

        btnRestablecer.setOnClickListener(v -> restablecerContrasena());
    }

    private void restablecerContrasena() {
        String codigo = etCodigo.getText().toString().trim();
        String nuevaPassword = etNuevaPassword.getText().toString();
        String confirmarPassword = etConfirmarPassword.getText().toString();

        if (codigo.length() != 6) {
            etCodigo.setError("Ingresa el código de 6 dígitos");
            etCodigo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nuevaPassword) || nuevaPassword.length() < 6) {
            etNuevaPassword.setError("Debe tener al menos 6 caracteres");
            etNuevaPassword.requestFocus();
            return;
        }

        if (!nuevaPassword.equals(confirmarPassword)) {
            etConfirmarPassword.setError("Las contraseñas no coinciden");
            etConfirmarPassword.requestFocus();
            return;
        }

        btnRestablecer.setEnabled(false);
        btnRestablecer.setText("ACTUALIZANDO...");

        executorService.execute(() -> {
            HttpURLConnection conexion = null;

            try {
                JSONObject datos = new JSONObject();
                datos.put("correo", correo);
                datos.put("codigo", codigo);
                datos.put("nuevaPassword", nuevaPassword);
                datos.put("confirmarPassword", confirmarPassword);

                URL url = new URL(
                        ApiConfig.AUTH_BASE_URL + "/recuperacion/restablecer"
                );

                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);
                conexion.setDoOutput(true);
                conexion.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(datos.toString().getBytes(StandardCharsets.UTF_8));
                }

                int codigoRespuesta = conexion.getResponseCode();

                String respuesta = leerRespuesta(
                        codigoRespuesta >= 200 && codigoRespuesta < 300
                                ? conexion.getInputStream()
                                : conexion.getErrorStream()
                );

                if (codigoRespuesta >= 200 && codigoRespuesta < 300) {
                    runOnUiThread(() -> NotificacionDialog.mostrar(
                            this,
                            "Contraseña actualizada",
                            "Tu contraseña fue restablecida correctamente. Ya puedes iniciar sesión.",
                            true,
                            this::volverAlLogin
                    ));
                } else {
                    String mensaje = obtenerMensaje(
                            respuesta,
                            "El código no es válido o ya expiró."
                    );

                    runOnUiThread(() -> mostrarError(mensaje));
                }

            } catch (Exception e) {
                runOnUiThread(() -> mostrarError(
                        "No se pudo conectar con el servidor."
                ));
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        });
    }

    private void volverAlLogin() {
        Intent intent = new Intent(this, login.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
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

    private String obtenerMensaje(String respuesta, String porDefecto) {
        try {
            JSONObject json = new JSONObject(respuesta);

            return json.optString(
                    "mensaje",
                    json.optString(
                            "message",
                            json.optString("detail", porDefecto)
                    )
            );
        } catch (Exception e) {
            return porDefecto;
        }
    }

    private void mostrarError(String mensaje) {
        btnRestablecer.setEnabled(true);
        btnRestablecer.setText("RESTABLECER CONTRASEÑA");

        NotificacionDialog.mostrar(
                this,
                "No se pudo restablecer",
                mensaje,
                false,
                null
        );
    }

    @Override
    protected void onDestroy() {
        if (executorService != null) {
            executorService.shutdownNow();
        }

        super.onDestroy();
    }
}