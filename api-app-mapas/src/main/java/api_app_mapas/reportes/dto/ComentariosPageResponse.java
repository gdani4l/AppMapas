package api_app_mapas.reportes.dto;

import java.util.List;

public record ComentariosPageResponse(long total, int pagina, int totalPaginas,
                                      List<ComentarioReporteResponse> comentarios) { }
