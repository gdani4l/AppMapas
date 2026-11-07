package api_app_mapas.lugares.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lugares")
public class Lugar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lugar")
    private Long idLugar;

    @Column(nullable = false, length = 20)
    private String tipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo", referencedColumnName = "codigo", insertable = false, updatable = false)
    private CategoriaLugar categoria;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(length = 255)
    private String direccion;

    @Column(length = 100)
    private String distrito;

    @Column(nullable = false, precision = 10, scale = 7)
    private java.math.BigDecimal latitud;

    @Column(nullable = false, precision = 10, scale = 7)
    private java.math.BigDecimal longitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private Usuario creador;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Long getIdLugar() { return idLugar; }
    public String getTipo() { return tipo; }
    public CategoriaLugar getCategoria() { return categoria; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
    public java.math.BigDecimal getLatitud() { return latitud; }
    public void setLatitud(java.math.BigDecimal latitud) { this.latitud = latitud; }
    public java.math.BigDecimal getLongitud() { return longitud; }
    public void setLongitud(java.math.BigDecimal longitud) { this.longitud = longitud; }
    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
