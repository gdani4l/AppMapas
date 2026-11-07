package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private EditText etCorreoRecuperacion;
    private MaterialButton btnEnviarCodigo;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recuperar_contrasena);
        UiInsets.apply(this);

        executorService = Executors.newSingleThreadExecutor();

        etCorreoRecuperacion = findViewById(R.id.etCorreoRecuperacion);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);
        TextView tvVolverLogin = findViewById(R.id.tvVolverLogin);

        btnEnviarCodigo.setOnClickListener(v -> solicitarCodigo());

        tvVolverLogin.setOnClickListener(v -> finish());
    }

    private void solicitarCodigo() {
        String correo = etCorreoRecuperacion.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            etCorreoRecuperacion.setError("Ingresa tu correo");
            etCorreoRecuperacion.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreoRecuperacion.setError("Ingresa un correo válido");
            etCorreoRecuperacion.requestFocus();
            return;
        }

        btnEnviarCodigo.setEnabled(false);
        btnEnviarCodigo.setText("ENVIANDO...");

        executorService.execute(() -> {
            HttpURLConnection conexion = null;

            try {
                JSONObject datos = new JSONObject();
                datos.put("correo", correo);

                URL url = new URL(
                        ApiConfig.AUTH_BASE_URL + "/recuperacion/solicitar"
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

                int codigo = conexion.getResponseCode();

                String respuesta = leerRespuesta(
                        codigo >= 200 && codigo < 300
                                ? conexion.getInputStream()
                                : conexion.getErrorStream()
                );

                if (codigo >= 200 && codigo < 300) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(
                                RecuperarContrasenaActivity.this,
                                RestablecerContrasenaActivity.class
                        );

                        intent.putExtra("CORREO_RECUPERACION", correo);
                        startActivity(intent);
                    });
                } else {
                    String mensaje = obtenerMensaje(
                            respuesta,
                            "No se pudo enviar el código."
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
        btnEnviarCodigo.setEnabled(true);
        btnEnviarCodigo.setText("ENVIAR CÓDIGO");

        NotificacionDialog.mostrar(
                this,
                "No se pudo enviar",
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
