package com.example.appmovil;

public final class ApiConfig {

    private ApiConfig() {
    }
    //vps: http://198.44.26.62:8080
    //pc-daniel: http://192.168.18.249:8080
    public static final String ip = "http://192.168.18.241:8080";
    public static final String AUTH_BASE_URL =
            ip+"/api/auth";
    public static final String PROFILE_BASE_URL =
            ip+"/api/perfil";
    public static final String PLACES_BASE_URL =
            ip+"/api/lugares";
    public static final String REPORTS_BASE_URL = ip + "/api/reportes";
    public static final String NOTIFICATIONS_BASE_URL = ip + "/api/notificaciones";
    public static final String ADMIN_BASE_URL = ip + "/api/admin";
    public static final String MAP_EVENTS_URL = ip + "/api/mapa/eventos";
}
