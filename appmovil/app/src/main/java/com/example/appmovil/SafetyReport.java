package com.example.appmovil;

import org.json.JSONObject;

final class SafetyReport {
    final long id;
    final String eventType, dangerLevel, description, author, authorImage, status, createdAt, expiresAt, district, imageUrl;
    final double latitude, longitude;
    final int radiusMeters, confirmations, refutations;
    final boolean anonymous, own;
    SafetyReport(long id, String eventType, String dangerLevel, String description, double latitude,
                 double longitude, int radiusMeters, boolean anonymous, String author,
                 String authorImage, String status, int confirmations, int refutations, String createdAt,
                 String expiresAt, String district, String imageUrl, boolean own) {
        this.id=id; this.eventType=eventType; this.dangerLevel=dangerLevel; this.description=description;
        this.latitude=latitude; this.longitude=longitude; this.radiusMeters=radiusMeters; this.anonymous=anonymous;
        this.author=author; this.authorImage=authorImage; this.status=status; this.confirmations=confirmations;
        this.refutations=refutations; this.createdAt=createdAt; this.expiresAt=expiresAt;
        this.district=district;this.imageUrl=imageUrl;this.own=own;
    }
    static SafetyReport fromJson(JSONObject j) {
        return new SafetyReport(j.optLong("id"),j.optString("tipoEvento"),j.optString("nivelPeligro"),
                j.optString("descripcion"),j.optDouble("latitud"),j.optDouble("longitud"),
                j.optInt("radioMetros"),j.optBoolean("anonimo"),j.optString("autor","Anónimo"),
                j.optString("imagenAutor"),j.optString("estadoValidacion","PENDIENTE"),
                j.optInt("confirmaciones"),j.optInt("refutaciones"),j.optString("fechaCreacion"),
                j.optString("fechaExpiracion"),nullable(j,"distrito"),nullable(j,"imagenUrl"),j.optBoolean("propio"));
    }
    private static String nullable(JSONObject json,String key){return json.isNull(key)?"":json.optString(key,"");}
}
