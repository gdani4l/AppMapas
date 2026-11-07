package api_app_mapas.reportes.repository;

import api_app_mapas.reportes.model.ReporteContenido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteContenidoRepository extends JpaRepository<ReporteContenido, Long> {
    boolean existsByUsuario_IdUsuarioAndTipoContenidoAndIdContenidoAndEstado(
            Integer usuarioId, String tipoContenido, Long idContenido, String estado);
    @org.springframework.data.jpa.repository.Query("select r from ReporteContenido r join fetch r.usuario where r.estado = :estado order by r.fechaCreacion asc")
    java.util.List<ReporteContenido> findByEstadoOrderByFechaCreacionAsc(String estado);
}
