package api_app_mapas.lugares.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias_lugar")
public class CategoriaLugar {
    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean activa;

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getOrden() { return orden; }
    public boolean isActiva() { return activa; }
}
