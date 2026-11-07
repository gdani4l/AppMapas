package api_app_mapas.reportes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CrearReporteRequest(
        @NotBlank @Size(max=40) String tipoEvento,
        @NotBlank @Pattern(regexp="MEDIO|ALTO") String nivelPeligro,
        @NotBlank @Size(max=2000) String descripcion,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitud,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitud,
        @NotNull @Min(10) @Max(50) Integer radioMetros,
        @NotNull @Min(1) @Max(168) Integer duracionHoras,
        @Size(max=100) String distrito,
        boolean anonimo) {}
