package api_app_mapas.lugares.repository;

import api_app_mapas.lugares.model.ResenaLugar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

public interface ResenaLugarRepository extends JpaRepository<ResenaLugar, Long> {
    @Query(value = "select r from ResenaLugar r join fetch r.usuario where r.lugar.idLugar = :lugarId",
           countQuery = "select count(r) from ResenaLugar r where r.lugar.idLugar = :lugarId")
    Page<ResenaLugar> listar(Long lugarId, Pageable pageable);

    Optional<ResenaLugar> findByLugarIdLugarAndUsuarioIdUsuario(Long lugarId, Integer usuarioId);

    long countByLugarIdLugar(Long lugarId);

    @Query("select avg(r.estrellas) from ResenaLugar r where r.lugar.idLugar = :lugarId")
    Double promedio(Long lugarId);

    @Query("select r.imagenUrl from ResenaLugar r where r.lugar.idLugar = :lugarId "
            + "and r.imagenUrl is not null order by r.fechaCreacion desc")
    List<String> imagenes(Long lugarId, Pageable pageable);

    @Query("select r from ResenaLugar r join fetch r.usuario where r.lugar.idLugar = :lugarId "
            + "and r.imagenUrl is not null order by r.fechaCreacion desc, r.id desc")
    List<ResenaLugar> fotosConAutor(Long lugarId, Pageable pageable);
}
