package api_app_mapas.ubicacion;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Unifica las variantes que pueden devolver los geocodificadores del teléfono. */
public final class DistrictNormalizer {
    private static final Map<String, String> KNOWN_DISTRICTS = new LinkedHashMap<>();

    static {
        KNOWN_DISTRICTS.put("san martin de porres", "San Martín de Porres");
        KNOWN_DISTRICTS.put("san juan de lurigancho", "San Juan de Lurigancho");
        KNOWN_DISTRICTS.put("san juan de miraflores", "San Juan de Miraflores");
        KNOWN_DISTRICTS.put("villa maria del triunfo", "Villa María del Triunfo");
        KNOWN_DISTRICTS.put("villa el salvador", "Villa El Salvador");
        KNOWN_DISTRICTS.put("puente piedra", "Puente Piedra");
        KNOWN_DISTRICTS.put("los olivos", "Los Olivos");
        KNOWN_DISTRICTS.put("independencia", "Independencia");
        KNOWN_DISTRICTS.put("carabayllo", "Carabayllo");
        KNOWN_DISTRICTS.put("comas", "Comas");
        KNOWN_DISTRICTS.put("rimac", "Rímac");
        KNOWN_DISTRICTS.put("lima cercado", "Cercado de Lima");
        KNOWN_DISTRICTS.put("cercado de lima", "Cercado de Lima");
    }

    private DistrictNormalizer() { }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        String cleaned = value.trim()
                .replaceFirst("(?i)^distrito\\s+de\\s+", "")
                .replaceFirst("(?i)^distrito\\s+", "")
                .replaceFirst("(?i),?\\s*(provincia|region|región|departamento)\\s+de\\s+lima.*$", "")
                .replaceFirst("(?i),\\s*lima.*$", "")
                .trim();
        String comparable = withoutAccents(cleaned).toLowerCase(Locale.ROOT);
        if (comparable.equals("smp")) return "San Martín de Porres";
        for (Map.Entry<String, String> district : KNOWN_DISTRICTS.entrySet()) {
            if (comparable.equals(district.getKey()) || comparable.contains(district.getKey())) {
                return district.getValue();
            }
        }
        return cleaned.isBlank() ? null : cleaned;
    }

    private static String withoutAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
