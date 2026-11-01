package api_app_mapas.lugares.dto;

import api_app_mapas.lugares.model.Lugar;
import java.math.BigDecimal;

public record LugarResponse(Long idLugar, String tipo, String nombreCategoria, String nombre, String descripcion,
                            String direccion, BigDecimal latitud, BigDecimal longitud) {
    public static LugarResponse desde(Lugar lugar) {
        return new LugarResponse(lugar.getIdLugar(), lugar.getTipo(),
                lugar.getCategoria() == null ? lugar.getTipo() : lugar.getCategoria().getNombre(),
                lugar.getNombre(),
                lugar.getDescripcion(), lugar.getDireccion(), lugar.getLatitud(), lugar.getLongitud());
    }
}
