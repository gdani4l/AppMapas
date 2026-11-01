package api_app_mapas.recuperacion.repository;

import api_app_mapas.recuperacion.model.CodigoRecuperacion;
import api_app_mapas.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Long> {
    Optional<CodigoRecuperacion> findFirstByUsuarioAndUtilizadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
            Usuario usuario, LocalDateTime fechaActual);

    @Modifying
    @Query("""
            update CodigoRecuperacion codigo
            set codigo.utilizado = true
            where codigo.usuario = :usuario and codigo.utilizado = false
            """)
    void invalidarCodigosActivos(@Param("usuario") Usuario usuario);
}
