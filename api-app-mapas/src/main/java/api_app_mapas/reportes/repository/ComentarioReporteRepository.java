package api_app_mapas.reportes.repository;

import api_app_mapas.reportes.model.ComentarioReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComentarioReporteRepository extends JpaRepository<ComentarioReporte, Long> {
    List<ComentarioReporte> findByReporte_IdReporteOrderByFechaCreacionAsc(Long idReporte);
    Page<ComentarioReporte> findByReporte_IdReporte(Long idReporte, Pageable pageable);
}
