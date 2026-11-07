package api_app_mapas.lugares.repository;

import api_app_mapas.lugares.model.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface LugarRepository extends JpaRepository<Lugar, Long> {
    List<Lugar> findByCreador_IdUsuarioOrderByFechaCreacionDesc(Integer userId);
    @Query("select l from Lugar l where l.latitud between :sur and :norte " +
            "and l.longitud between :oeste and :este " +
            "and (:tipo is null or l.tipo = :tipo) order by l.idLugar desc")
    List<Lugar> buscarEnVista(@Param("sur") BigDecimal sur, @Param("norte") BigDecimal norte,
                             @Param("oeste") BigDecimal oeste, @Param("este") BigDecimal este,
                             @Param("tipo") String tipo, org.springframework.data.domain.Pageable pagina);

    @Query("select l from Lugar l where l.latitud between :sur and :norte " +
            "and l.longitud between :oeste and :este " +
            "and l.tipo in :tipos order by l.idLugar desc")
    List<Lugar> buscarEnVistaPorTipos(@Param("sur") BigDecimal sur, @Param("norte") BigDecimal norte,
                                     @Param("oeste") BigDecimal oeste, @Param("este") BigDecimal este,
                                     @Param("tipos") List<String> tipos,
                                     org.springframework.data.domain.Pageable pagina);

    @Query("select l from Lugar l where lower(l.nombre) like lower(concat('%', :texto, '%')) " +
            "or lower(l.direccion) like lower(concat('%', :texto, '%')) order by l.nombre")
    List<Lugar> buscarPorTexto(@Param("texto") String texto,
                              org.springframework.data.domain.Pageable pagina);

    @Query("select l from Lugar l where lower(l.nombre) like lower(concat('%', :texto, '%')) " +
            "or lower(l.direccion) like lower(concat('%', :texto, '%')) " +
            "order by ((l.latitud - :latitud) * (l.latitud - :latitud) + " +
            "(l.longitud - :longitud) * (l.longitud - :longitud))")
    List<Lugar> buscarPorTextoCercano(@Param("texto") String texto,
                                     @Param("latitud") BigDecimal latitud,
                                     @Param("longitud") BigDecimal longitud,
                                     org.springframework.data.domain.Pageable pagina);
}
