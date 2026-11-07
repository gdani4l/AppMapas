package api_app_mapas.perfil.dto;

import java.time.LocalDateTime;

public record ActividadResponse(String tipo, Long id, String titulo, String detalle,
                                LocalDateTime fecha, String categoria, String estado,
                                Integer valor, Long idReferencia, LocalDateTime fechaExpiracion) { }
