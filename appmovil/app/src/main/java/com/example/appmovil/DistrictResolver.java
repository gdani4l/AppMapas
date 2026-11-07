package com.example.appmovil;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

final class DistrictResolver {
    private DistrictResolver() { }

    static String resolve(Context context, double latitude, double longitude) {
        try {
            if (!Geocoder.isPresent()) return "";
            List<Address> results = new Geocoder(context, Locale.getDefault())
                    .getFromLocation(latitude, longitude, 1);
            if (results == null || results.isEmpty()) return "";
            Address address = results.get(0);
            String[] candidates = {address.getSubLocality(), address.getLocality(),
                    address.getSubAdminArea()};
            for (String candidate : candidates) {
                String normalized = normalize(candidate);
                if (isKnownDistrict(normalized)) return normalized;
            }
            return normalize(firstPresent(candidates));
        } catch (Exception ignored) {
            return "";
        }
    }

    static String normalize(String value) {
        if (value == null || value.isBlank()) return "";
        String cleaned = value.trim()
                .replaceFirst("(?i)^distrito\\s+de\\s+", "")
                .replaceFirst("(?i)^distrito\\s+", "")
                .replaceFirst("(?i),\\s*lima.*$", "")
                .trim();
        String comparable = Normalizer.normalize(cleaned, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        if (comparable.equals("smp") || comparable.contains("san martin de porres")) return "San Martín de Porres";
        if (comparable.contains("san juan de lurigancho")) return "San Juan de Lurigancho";
        if (comparable.contains("san juan de miraflores")) return "San Juan de Miraflores";
        if (comparable.contains("villa maria del triunfo")) return "Villa María del Triunfo";
        if (comparable.contains("villa el salvador")) return "Villa El Salvador";
        if (comparable.contains("puente piedra")) return "Puente Piedra";
        if (comparable.contains("los olivos")) return "Los Olivos";
        if (comparable.contains("independencia")) return "Independencia";
        if (comparable.contains("carabayllo")) return "Carabayllo";
        if (comparable.contains("comas")) return "Comas";
        if (comparable.contains("rimac")) return "Rímac";
        return cleaned;
    }

    private static String firstPresent(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return "";
    }

    private static boolean isKnownDistrict(String value) {
        return value.equals("Comas") || value.equals("Los Olivos")
                || value.equals("Independencia") || value.equals("Carabayllo")
                || value.equals("Puente Piedra") || value.equals("San Martín de Porres")
                || value.equals("San Juan de Lurigancho") || value.equals("San Juan de Miraflores")
                || value.equals("Villa María del Triunfo") || value.equals("Villa El Salvador")
                || value.equals("Rímac");
    }
}
