package api_app_mapas.lugares.dto;

public record RecomendacionLugarResponse(LugarResponse lugar, double calificacion,
                                         long resenas, long alertasCercanas,
                                         double distanciaKm) { }
