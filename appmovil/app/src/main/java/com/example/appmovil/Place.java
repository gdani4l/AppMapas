package com.example.appmovil;

import org.json.JSONObject;

final class Place {
    final long id;
    final String type;
    final String categoryName;
    final String name;
    final String description;
    final String address;
    final double latitude;
    final double longitude;

    Place(long id, String type, String categoryName, String name, String description,
          String address, double latitude, double longitude) {
        this.id = id;
        this.type = type;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    static Place fromJson(JSONObject json) throws Exception {
        return new Place(json.getLong("idLugar"), json.getString("tipo"),
                json.optString("nombreCategoria", PlaceCategory.nameFor(json.getString("tipo"))),
                json.getString("nombre"), json.optString("descripcion", ""),
                json.optString("direccion", ""), json.getDouble("latitud"),
                json.getDouble("longitud"));
    }
}
