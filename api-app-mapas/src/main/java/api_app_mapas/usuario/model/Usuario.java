package api_app_mapas.usuario.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Column(name = "correo_verificado", nullable = false)
    private boolean correoVerificado;
    @Column(name = "activa", nullable = false)
    private boolean activa = true;
    public boolean isCorreoVerificado() { return correoVerificado; }
    public void setCorreoVerificado(boolean value) { correoVerificado = value; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean value) { activa = value; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "primer_nombre", nullable = false, length = 50)
    private String primerNombre;

    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", length = 50)
    private String apellidoMaterno;

    @Column(name = "correo", nullable = false, unique = true, length = 254)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate value) { this.fechaNacimiento = value; }

    @Column(name = "distrito", length = 100)
    private String distrito;
    public String getDistrito() { return distrito; }
    public void setDistrito(String value) { this.distrito = value; }

    @Column(name = "telefono", length = 20)
    private String telefono;
    public String getTelefono() { return telefono; }
    public void setTelefono(String value) { this.telefono = value; }

    @Column(name = "direccion", length = 255)
    private String direccion;
    public String getDireccion() { return direccion; }
    public void setDireccion(String value) { this.direccion = value; }

    @Column(name = "imagen_perfil_url", length = 500)
    private String imagenPerfilUrl;
    public String getImagenPerfilUrl() { return imagenPerfilUrl; }
    public void setImagenPerfilUrl(String value) { this.imagenPerfilUrl = value; }

    @Column(name = "imagen_public_id", length = 255)
    private String imagenPublicId;
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String value) { this.imagenPublicId = value; }

    @Column(name = "rol", nullable = false, length = 20)
    private String rol = "USUARIO";
    public String getRol() { return rol; }
    public void setRol(String value) { rol = value; }

    public Usuario() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}
