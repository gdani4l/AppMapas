package com.example.appmovil;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
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

public class registro extends AppCompatActivity {

    private EditText etFirstName;
    private EditText etSecondName;
    private EditText etPaternalLastName;
    private EditText etMaternalLastName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnRegister;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        UiInsets.apply(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        executorService = Executors.newSingleThreadExecutor();

        etFirstName = findViewById(R.id.etFirstName);
        etSecondName = findViewById(R.id.etSecondName);
        etPaternalLastName = findViewById(R.id.etPaternalLastName);
        etMaternalLastName = findViewById(R.id.etMaternalLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        CheckBox cbShowPassword = findViewById(R.id.cbShowPassword);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mostrarPassword(etPassword, isChecked);
            mostrarPassword(etConfirmPassword, isChecked);
        });

        btnRegister.setOnClickListener(v -> registrarCuenta());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, login.class));
            finish();
        });
    }

    private void registrarCuenta() {
        String primerNombre = etFirstName.getText().toString().trim();
        String segundoNombre = etSecondName.getText().toString().trim();
        String apellidoPaterno = etPaternalLastName.getText().toString().trim();
        String apellidoMaterno = etMaternalLastName.getText().toString().trim();
        String correo = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmarPassword = etConfirmPassword.getText().toString();

        if (!validarDatos(
                primerNombre,
                apellidoPaterno,
                correo,
                password,
                confirmarPassword
        )) {
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("CREANDO...");

        executorService.execute(() -> {
            HttpURLConnection conexion = null;

            try {
                JSONObject datos = new JSONObject();
                datos.put("primerNombre", primerNombre);
                datos.put("segundoNombre", segundoNombre);
                datos.put("apellidoPaterno", apellidoPaterno);
                datos.put("apellidoMaterno", apellidoMaterno);
                datos.put("correo", correo);
                datos.put("password", password);
                datos.put("confirmarPassword", confirmarPassword);

                URL url = new URL(ApiConfig.AUTH_BASE_URL + "/registro");
                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(30000);
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
                    String desafioId = json.optString("desafioId", "");
                    if (desafioId.isEmpty() || "null".equals(desafioId)) {
                        runOnUiThread(() -> mostrarError("No se recibió la solicitud de verificación."));
                        return;
                    }
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        startActivity(new Intent(this, VerificarAccesoActivity.class)
                                .putExtra("desafioId", desafioId).putExtra("correo", correo)
                                .putExtra("paso", "CORREO"));
                        finish();
                    });
                } else {
                    String mensaje = obtenerMensajeError(
                            respuesta,
                            "No se pudo crear la cuenta."
                    );
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

    private boolean validarDatos(
            String primerNombre,
            String apellidoPaterno,
            String correo,
            String password,
            String confirmarPassword
    ) {
        if (TextUtils.isEmpty(primerNombre)) {
            etFirstName.setError("Ingresa tu primer nombre");
            etFirstName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(apellidoPaterno)) {
            etPaternalLastName.setError("Ingresa tu apellido paterno");
            etPaternalLastName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(correo)
                || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmail.setError("Ingresa un correo válido");
            etEmail.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("La contraseña debe tener al menos 8 caracteres");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmarPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        btnRegister.setEnabled(true);
        btnRegister.setText("¡CREAR CUENTA!");
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
