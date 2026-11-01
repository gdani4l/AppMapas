package com.example.appmovil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    public interface ApiCallback {
        void onSuccess(String respuesta);
        void onError(String error);
    }

    public static void postJson(String endpoint, JSONObject datos, ApiCallback callback) {
        new Thread(() -> {
            HttpURLConnection conexion = null;

            try {
                URL url = new URL(endpoint);
                conexion = (HttpURLConnection) url.openConnection();

                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conexion.setRequestProperty("Accept", "application/json");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);
                conexion.setDoOutput(true);

                OutputStream outputStream = conexion.getOutputStream();
                outputStream.write(datos.toString().getBytes("UTF-8"));
                outputStream.close();

                int codigoRespuesta = conexion.getResponseCode();

                InputStream inputStream = codigoRespuesta >= 200 && codigoRespuesta < 300
                        ? conexion.getInputStream()
                        : conexion.getErrorStream();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8")
                );

                StringBuilder respuesta = new StringBuilder();
                String linea;

                while ((linea = reader.readLine()) != null) {
                    respuesta.append(linea);
                }

                reader.close();

                if (codigoRespuesta >= 200 && codigoRespuesta < 300) {
                    callback.onSuccess(respuesta.toString());
                } else {
                    callback.onError(obtenerMensaje(respuesta.toString()));
                }

            } catch (Exception e) {
                callback.onError("No se pudo conectar con el servidor.");
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        }).start();
    }

    public static void getAuthorized(
            String endpoint,
            String token,
            ApiCallback callback
    ) {
        requestAuthorized("GET", endpoint, token, null, callback);
    }

    public static void putAuthorized(
            String endpoint,
            String token,
            JSONObject datos,
            ApiCallback callback
    ) {
        requestAuthorized("PUT", endpoint, token, datos, callback);
    }

    public static void postAuthorized(
            String endpoint,
            String token,
            JSONObject datos,
            ApiCallback callback
    ) {
        requestAuthorized("POST", endpoint, token, datos, callback);
    }

    public static void deleteAuthorized(String endpoint, String token, ApiCallback callback) {
        requestAuthorized("DELETE", endpoint, token, null, callback);
    }

    public static void uploadReview(String method, String endpoint, String token, int stars,
                                    String comment, byte[] imageBytes, String mimeType,
                                    boolean removeImage, ApiCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            String boundary = "AppMapasReview" + System.currentTimeMillis();
            try {
                connection = (HttpURLConnection) new URL(endpoint).openConnection();
                connection.setRequestMethod(method);
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(45000);
                connection.setDoOutput(true);
                try (BufferedOutputStream output = new BufferedOutputStream(connection.getOutputStream())) {
                    writeFormField(output, boundary, "estrellas", Integer.toString(stars));
                    writeFormField(output, boundary, "comentario", comment);
                    if (removeImage) writeFormField(output, boundary, "quitarImagen", "true");
                    if (imageBytes != null) {
                        output.write(("--" + boundary + "\r\nContent-Disposition: form-data; "
                                + "name=\"imagen\"; filename=\"resena.jpg\"\r\nContent-Type: "
                                + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                        output.write(imageBytes);
                        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
                    }
                    output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                }
                entregarRespuesta(connection, callback);
            } catch (Exception error) {
                callback.onError("No se pudo guardar la reseña. Revisa tu conexión.");
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private static void writeFormField(BufferedOutputStream output, String boundary,
                                       String name, String value) throws Exception {
        output.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\""
                + name + "\"\r\n\r\n" + (value == null ? "" : value)
                + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private static void requestAuthorized(
            String method,
            String endpoint,
            String token,
            JSONObject datos,
            ApiCallback callback
    ) {
        new Thread(() -> {
            HttpURLConnection conexion = null;
            try {
                conexion = (HttpURLConnection) new URL(endpoint).openConnection();
                conexion.setRequestMethod(method);
                conexion.setRequestProperty("Accept", "application/json");
                conexion.setRequestProperty("Authorization", "Bearer " + token);
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(15000);

                if (datos != null) {
                    conexion.setDoOutput(true);
                    conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    try (OutputStream output = conexion.getOutputStream()) {
                        output.write(datos.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                entregarRespuesta(conexion, callback);
            } catch (Exception e) {
                callback.onError("No se pudo conectar con el servidor.");
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        }).start();
    }

    public static void uploadProfileImage(
            String endpoint,
            String token,
            byte[] imageBytes,
            String mimeType,
            ApiCallback callback
    ) {
        new Thread(() -> {
            HttpURLConnection conexion = null;
            String boundary = "AppMapasBoundary" + System.currentTimeMillis();
            try {
                conexion = (HttpURLConnection) new URL(endpoint).openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Authorization", "Bearer " + token);
                conexion.setRequestProperty("Accept", "application/json");
                conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conexion.setConnectTimeout(15000);
                conexion.setReadTimeout(30000);
                conexion.setDoOutput(true);

                try (BufferedOutputStream output = new BufferedOutputStream(conexion.getOutputStream())) {
                    String header = "--" + boundary + "\r\n"
                            + "Content-Disposition: form-data; name=\"imagen\"; filename=\"perfil.jpg\"\r\n"
                            + "Content-Type: " + mimeType + "\r\n\r\n";
                    output.write(header.getBytes(StandardCharsets.UTF_8));
                    output.write(imageBytes);
                    output.write(("\r\n--" + boundary + "--\r\n")
                            .getBytes(StandardCharsets.UTF_8));
                }

                entregarRespuesta(conexion, callback);
            } catch (Exception e) {
                callback.onError("No se pudo subir la imagen.");
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
        }).start();
    }

    private static void entregarRespuesta(
            HttpURLConnection conexion,
            ApiCallback callback
    ) throws Exception {
        int codigo = conexion.getResponseCode();
        InputStream input = codigo >= 200 && codigo < 300
                ? conexion.getInputStream()
                : conexion.getErrorStream();
        String respuesta = leer(input);
        if (codigo >= 200 && codigo < 300) {
            callback.onSuccess(respuesta);
        } else {
            callback.onError(obtenerMensaje(respuesta));
        }
    }

    private static String leer(InputStream input) throws Exception {
        if (input == null) {
            return "";
        }
        StringBuilder respuesta = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8)
        )) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                respuesta.append(linea);
            }
        }
        return respuesta.toString();
    }

    public static String obtenerMensaje(String respuesta) {
        try {
            JSONObject json = new JSONObject(respuesta);

            if (json.has("mensaje")) {
                return json.getString("mensaje");
            }

            if (json.has("error")) {
                return json.getString("error");
            }

            if (json.has("detail")) {
                return json.getString("detail");
            }

            if (json.has("message")) {
                return json.getString("message");
            }

        } catch (Exception ignored) {
        }

        return respuesta.isEmpty()
                ? "Ocurrió un error. Inténtalo nuevamente."
                : respuesta;
    }
}
