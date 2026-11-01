package com.example.appmovil;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class PlaceCategory {
    final String code;
    final String name;

    PlaceCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }

    static List<PlaceCategory> defaults() {
        List<PlaceCategory> result = new ArrayList<>();
        result.add(new PlaceCategory("HOTEL", "Hoteles"));
        result.add(new PlaceCategory("RESTAURANTE", "Restaurantes"));
        result.add(new PlaceCategory("PARQUE", "Parques"));
        result.add(new PlaceCategory("FARMACIA", "Farmacias"));
        result.add(new PlaceCategory("HOSPITAL", "Hospitales"));
        result.add(new PlaceCategory("COMISARIA", "Comisarías"));
        return result;
    }

    static List<PlaceCategory> fromJson(String response) throws Exception {
        JSONArray json = new JSONArray(response);
        List<PlaceCategory> result = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject item = json.getJSONObject(i);
            result.add(new PlaceCategory(item.getString("codigo"), item.getString("nombre")));
        }
        return result;
    }

    static String nameFor(String code) {
        for (PlaceCategory category : defaults()) {
            if (category.code.equals(code)) return category.name;
        }
        return code;
    }
}
