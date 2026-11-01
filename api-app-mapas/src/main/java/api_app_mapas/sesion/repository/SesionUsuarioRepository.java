package api_app_mapas.sesion.repository;

import api_app_mapas.sesion.model.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    Optional<SesionUsuario> findByTokenHashAndRevocadaFalse(String tokenHash);
}
