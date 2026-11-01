package api_app_mapas.lugares.repository;

import api_app_mapas.lugares.model.CategoriaLugar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaLugarRepository extends JpaRepository<CategoriaLugar, String> {
    List<CategoriaLugar> findByActivaTrueOrderByOrdenAscNombreAsc();
    boolean existsByCodigoAndActivaTrue(String codigo);
}
