package api_app_mapas.lugares.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record LugarRequest(
        @NotBlank @Size(max = 20) String tipo,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 1000) String descripcion,
        @Size(max = 255) String direccion,
        @Size(max = 100) String distrito,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitud,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitud
) {}
