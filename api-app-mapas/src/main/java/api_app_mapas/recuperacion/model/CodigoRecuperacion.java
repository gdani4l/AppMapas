package api_app_mapas.recuperacion.model;

import api_app_mapas.usuario.model.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "codigos_recuperacion")
public class CodigoRecuperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo_recuperacion")
    private Long idCodigoRecuperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "utilizado", nullable = false)
    private boolean utilizado;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public CodigoRecuperacion() {
    }

    public CodigoRecuperacion(Usuario usuario, String codigoHash, LocalDateTime fechaExpiracion) {
        this.usuario = usuario;
        this.codigoHash = codigoHash;
        this.fechaExpiracion = fechaExpiracion;
        this.utilizado = false;
    }

    public Usuario getUsuario() { return usuario; }
    public String getCodigoHash() { return codigoHash; }
    public boolean isUtilizado() { return utilizado; }
    public void setUtilizado(boolean utilizado) { this.utilizado = utilizado; }
}
