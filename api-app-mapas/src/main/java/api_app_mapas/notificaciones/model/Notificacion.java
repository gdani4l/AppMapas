package api_app_mapas.notificaciones.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(nullable = false, length = 30) private String tipo;
    @Column(nullable = false, length = 120) private String titulo;
    @Column(nullable = false, length = 500) private String mensaje;
    @Column(name = "id_referencia") private Long idReferencia;
    @Column(nullable = false) private boolean leida;
    @Column(name = "fecha_creacion", insertable = false, updatable = false) private LocalDateTime fechaCreacion;
    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario value) { usuario = value; }
    public String getTipo() { return tipo; }
    public void setTipo(String value) { tipo = value; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String value) { titulo = value; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String value) { mensaje = value; }
    public Long getIdReferencia() { return idReferencia; }
    public void setIdReferencia(Long value) { idReferencia = value; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean value) { leida = value; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
