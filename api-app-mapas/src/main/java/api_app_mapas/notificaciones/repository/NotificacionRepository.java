package api_app_mapas.notificaciones.repository;

import api_app_mapas.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(Integer userId);
    boolean existsByUsuario_IdUsuarioAndTipoAndIdReferencia(Integer userId, String tipo, Long referenceId);
}
