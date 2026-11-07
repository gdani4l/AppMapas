package api_app_mapas.reportes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearReporteContenidoRequest(
        @NotBlank @Pattern(regexp = "LUGAR|RESENA|REPORTE") String tipoContenido,
        @NotNull Long idContenido,
        @NotBlank @Size(max = 500) String motivo) { }
