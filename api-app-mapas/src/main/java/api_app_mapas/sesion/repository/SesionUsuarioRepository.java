package api_app_mapas.sesion.repository;

import api_app_mapas.sesion.model.SesionUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<SesionUsuario> findByTokenHashAndRevocadaFalse(String tokenHash);
}
