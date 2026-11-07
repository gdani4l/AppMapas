package com.example.appmovil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Mantiene una conexión ligera para recibir altas y eliminaciones del mapa. */
final class MapEventStream {
    interface Listener { void onMapUpdated(String contentType); }
    private final String endpoint;
    private volatile boolean running;
    private volatile HttpURLConnection connection;
    private Thread worker;

    MapEventStream(String endpoint) { this.endpoint = endpoint; }

    synchronized void start(String token, Listener listener) {
        if (running || token == null || token.isBlank()) return;
        running = true;
        worker = new Thread(() -> listen(token, listener), "map-events");
        worker.start();
    }

    synchronized void stop() {
        running = false;
        HttpURLConnection active = connection;
        if (active != null) active.disconnect();
        if (worker != null) worker.interrupt();
        worker = null;
    }

    private void listen(String token, Listener listener) {
        while (running) {
            try {
                HttpURLConnection current = (HttpURLConnection) new URL(endpoint).openConnection();
                connection = current;
                current.setRequestMethod("GET");
                current.setRequestProperty("Authorization", "Bearer " + token);
                current.setRequestProperty("Accept", "text/event-stream");
                current.setRequestProperty("Cache-Control", "no-cache");
                current.setConnectTimeout(10000);
                current.setReadTimeout(0);
                if (current.getResponseCode() != HttpURLConnection.HTTP_OK) throw new IllegalStateException();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        current.getInputStream(), StandardCharsets.UTF_8))) {
                    String event = "";
                    String data = "";
                    String line;
                    while (running && (line = reader.readLine()) != null) {
                        if (line.startsWith("event:")) event = line.substring(6).trim();
                        else if (line.startsWith("data:")) data = line.substring(5).trim();
                        else if (line.isEmpty()) {
                            if ("mapa-actualizado".equals(event)) {
                                int separator = data.indexOf('|');
                                listener.onMapUpdated(separator < 0 ? data : data.substring(0, separator));
                            }
                            event = "";
                            data = "";
                        }
                    }
                }
            } catch (Exception ignored) {
                // La conexión se reabre automáticamente al cambiar de red o reiniciar la API.
            } finally {
                HttpURLConnection active = connection;
                connection = null;
                if (active != null) active.disconnect();
            }
            if (running) {
                try { Thread.sleep(2500L); }
                catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
