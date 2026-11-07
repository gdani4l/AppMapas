package api_app_mapas.reportes.repository;

import api_app_mapas.reportes.model.VotoReporte;
import api_app_mapas.reportes.model.VotoReporteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoReporteRepository extends JpaRepository<VotoReporte, VotoReporteId> {
    long countByReporte_IdReporteAndVeracidad(Long idReporte, String veracidad);
}
