package api_app_mapas.lugares.dto;

import java.util.List;

public record ResenasPageResponse(long total, double promedio, List<ResenaResponse> resenas) { }
