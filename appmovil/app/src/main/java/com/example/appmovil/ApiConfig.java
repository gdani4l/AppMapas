package com.example.appmovil;

public final class ApiConfig {

    private ApiConfig() {
    }
    //vps: http://198.44.26.62:8080
    //pc-daniel: http://192.168.18.249:8080
    public static final String ip = "http://192.168.18.249:8080";
    public static final String AUTH_BASE_URL =
            ip+"/api/auth";
    public static final String PROFILE_BASE_URL =
            ip+"/api/perfil";
    public static final String PLACES_BASE_URL =
            ip+"/api/lugares";
}
