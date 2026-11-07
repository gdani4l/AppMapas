package api_app_mapas.usuario.repository;

import api_app_mapas.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.idUsuario = :id")
    Optional<Usuario> bloquearPorId(@org.springframework.data.repository.query.Param("id") Integer id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.correo = :correo")
    Optional<Usuario> bloquearPorCorreo(@org.springframework.data.repository.query.Param("correo") String correo);

    boolean existsByCorreo(String correo);

    Optional<Usuario> findByCorreo(String correo);
}
