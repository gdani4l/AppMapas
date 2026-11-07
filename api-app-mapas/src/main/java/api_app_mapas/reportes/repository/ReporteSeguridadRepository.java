package api_app_mapas.reportes.repository;

import api_app_mapas.reportes.model.ReporteSeguridad;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ReporteSeguridadRepository extends JpaRepository<ReporteSeguridad, Long> {
    @Query("select r from ReporteSeguridad r left join fetch r.usuario where r.latitud between :sur and :norte and r.longitud between :oeste and :este and r.fechaExpiracion > CURRENT_TIMESTAMP order by r.fechaCreacion desc")
    List<ReporteSeguridad> buscarEnVista(@Param("sur") BigDecimal sur, @Param("norte") BigDecimal norte,
                                         @Param("oeste") BigDecimal oeste, @Param("este") BigDecimal este,
                                         Pageable pageable);
    @Query("select r from ReporteSeguridad r left join fetch r.usuario where r.usuario.idUsuario = :idUsuario order by r.fechaCreacion desc")
    List<ReporteSeguridad> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(@Param("idUsuario") Integer idUsuario);
    @Query("select r from ReporteSeguridad r left join fetch r.usuario where r.usuario.idUsuario = :idUsuario and r.fechaExpiracion > CURRENT_TIMESTAMP order by r.fechaCreacion desc")
    List<ReporteSeguridad> listarActivosDelUsuario(@Param("idUsuario") Integer idUsuario);
    @Query("select count(r) from ReporteSeguridad r where r.latitud between :sur and :norte " +
            "and r.longitud between :oeste and :este and r.estadoValidacion <> 'REFUTADO' and r.fechaExpiracion > CURRENT_TIMESTAMP")
    long contarAlertas(@Param("sur") BigDecimal sur, @Param("norte") BigDecimal norte,
                       @Param("oeste") BigDecimal oeste, @Param("este") BigDecimal este);
    @Query("select r from ReporteSeguridad r left join fetch r.usuario order by r.fechaCreacion desc")
    List<ReporteSeguridad> listarTodos(Pageable pageable);
    @Query("select r from ReporteSeguridad r left join fetch r.usuario where r.fechaExpiracion > CURRENT_TIMESTAMP order by r.fechaCreacion desc")
    List<ReporteSeguridad> listarActivos(Pageable pageable);
    @Query("select r from ReporteSeguridad r left join fetch r.usuario where r.fechaExpiracion <= CURRENT_TIMESTAMP order by r.fechaExpiracion desc")
    List<ReporteSeguridad> listarCaducados(Pageable pageable);
}
