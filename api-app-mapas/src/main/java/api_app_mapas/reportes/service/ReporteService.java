package api_app_mapas.reportes.service;

import api_app_mapas.reportes.dto.*;
import api_app_mapas.reportes.model.*;
import api_app_mapas.reportes.repository.*;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.lugares.repository.LugarRepository;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.notificaciones.model.Notificacion;
import api_app_mapas.notificaciones.repository.NotificacionRepository;
import api_app_mapas.perfil.service.CloudinaryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ReporteService {
    private final ReporteSeguridadRepository reportes;
    private final ComentarioReporteRepository comentarios;
    private final VotoReporteRepository votos;
    private final ReporteContenidoRepository contenido;
    private final SesionService sesiones;
    private final LugarRepository lugares;
    private final ResenaLugarRepository resenas;
    private final NotificacionRepository notificaciones;
    private final CloudinaryService imagenes;
    public ReporteService(ReporteSeguridadRepository reportes, ComentarioReporteRepository comentarios,
                           VotoReporteRepository votos, ReporteContenidoRepository contenido, SesionService sesiones,
                           LugarRepository lugares, ResenaLugarRepository resenas,
                           NotificacionRepository notificaciones, CloudinaryService imagenes) {
        this.reportes=reportes; this.comentarios=comentarios; this.votos=votos; this.contenido=contenido;
        this.sesiones=sesiones; this.lugares=lugares; this.resenas=resenas; this.notificaciones=notificaciones;
        this.imagenes=imagenes;
    }
    public ReporteSeguridad obtener(Long id) { return reportes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Reporte no encontrado")); }
    public Usuario usuario(String auth) { return sesiones.obtenerUsuario(auth); }
    @Transactional public ReporteResponse crear(String auth, CrearReporteRequest request) {
        ReporteSeguridad r=new ReporteSeguridad(); r.setUsuario(sesiones.obtenerUsuario(auth));
        r.setTipoEvento(request.tipoEvento().trim()); r.setNivelPeligro(request.nivelPeligro());
        r.setDescripcion(request.descripcion().trim()); r.setLatitud(request.latitud()); r.setLongitud(request.longitud());
        r.setRadioMetros(request.radioMetros()); r.setAnonimo(request.anonimo()); r.setEstadoValidacion("PENDIENTE");
        r.setConfirmaciones(0); r.setRefutaciones(0);
        r.setFechaExpiracion(LocalDateTime.now().plusHours(request.duracionHoras()));
        r.setDistrito(limpiar(request.distrito()));
        return ReporteResponse.desde(reportes.save(r), true);
    }
    @Transactional public ReporteResponse crearConImagen(String auth, CrearReporteRequest request,
                                                           MultipartFile imagen) throws IOException {
        validarImagen(imagen, true);
        CloudinaryService.UploadResult subida=imagenes.subirImagenReporte(imagen.getBytes());
        try {
            ReporteSeguridad r=new ReporteSeguridad(); r.setUsuario(sesiones.obtenerUsuario(auth));
            r.setTipoEvento(request.tipoEvento().trim()); r.setNivelPeligro(request.nivelPeligro());
            r.setDescripcion(request.descripcion().trim()); r.setLatitud(request.latitud()); r.setLongitud(request.longitud());
            r.setRadioMetros(request.radioMetros()); r.setAnonimo(request.anonimo()); r.setEstadoValidacion("PENDIENTE");
            r.setConfirmaciones(0); r.setRefutaciones(0); r.setFechaExpiracion(LocalDateTime.now().plusHours(request.duracionHoras()));
            r.setDistrito(limpiar(request.distrito())); r.setImagenUrl(subida.url()); r.setImagenPublicId(subida.publicId());
            return ReporteResponse.desde(reportes.saveAndFlush(r),true);
        } catch(RuntimeException error) { imagenes.eliminarImagen(subida.publicId()); throw error; }
    }
    @Transactional public ComentarioReporteResponse comentar(String auth, Long id, TextoRequest request) {
        ComentarioReporte c=new ComentarioReporte(); c.setReporte(obtener(id)); c.setUsuario(sesiones.obtenerUsuario(auth));
        c.setComentario(request.texto().trim()); return ComentarioReporteResponse.desde(comentarios.save(c));
    }
    @Transactional public ComentarioReporteResponse comentarConImagen(String auth,Long id,String texto,
                                                                        MultipartFile imagen) throws IOException {
        if(texto==null||texto.isBlank()||texto.trim().length()>1000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Escribe un comentario válido");
        validarImagen(imagen,false);
        CloudinaryService.UploadResult subida=imagen==null||imagen.isEmpty()?null:
                imagenes.subirImagenComentarioReporte(imagen.getBytes());
        try {
            ComentarioReporte c=new ComentarioReporte();c.setReporte(obtener(id));c.setUsuario(sesiones.obtenerUsuario(auth));
            c.setComentario(texto.trim());if(subida!=null){c.setImagenUrl(subida.url());c.setImagenPublicId(subida.publicId());}
            return ComentarioReporteResponse.desde(comentarios.saveAndFlush(c));
        } catch(RuntimeException error){if(subida!=null)imagenes.eliminarImagen(subida.publicId());throw error;}
    }
    @Transactional public ReporteResponse votar(String auth, Long id, VotoRequest request) {
        ReporteSeguridad r=obtener(id); Usuario u=sesiones.obtenerUsuario(auth);
        if(r.getUsuario()!=null && r.getUsuario().getIdUsuario().equals(u.getIdUsuario()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"No puedes validar tu propio reporte");
        if(r.getFechaExpiracion().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"El reporte ya caducó");
        String estadoAnterior=r.getEstadoValidacion();
        VotoReporteId key=new VotoReporteId(id,u.getIdUsuario());
        VotoReporte voto=votos.findById(key).orElseGet(VotoReporte::new); voto.setReporte(r); voto.setUsuario(u); voto.setVeracidad(request.veracidad()); votos.save(voto);
        int verdaderos=(int)votos.countByReporte_IdReporteAndVeracidad(id,"VERDADERO");
        int falsos=(int)votos.countByReporte_IdReporteAndVeracidad(id,"FALSO");
        r.setConfirmaciones(verdaderos); r.setRefutaciones(falsos);
        r.setEstadoValidacion(verdaderos>=3 && verdaderos>falsos ? "CONFIRMADO" : falsos>=3 && falsos>verdaderos ? "EN_REVISION" : "PENDIENTE");
        ReporteSeguridad guardado=reportes.save(r);
        if(!estadoAnterior.equals(guardado.getEstadoValidacion())) notificarCambio(guardado,u);
        return ReporteResponse.desde(guardado, false);
    }
    @Transactional public void reportar(String auth, Long id, TextoRequest request) {
        ReporteSeguridad r=obtener(id); ReporteContenido rc=new ReporteContenido(); rc.setUsuario(sesiones.obtenerUsuario(auth));
        rc.setTipoContenido("REPORTE"); rc.setIdContenido(id); rc.setMotivo(request.texto().trim()); rc.setEstado("PENDIENTE"); contenido.save(rc);
        r.setEstadoValidacion("EN_REVISION"); reportes.save(r);
    }
    public List<ComentarioReporteResponse> comentarios(Long id) { obtener(id); return comentarios.findByReporte_IdReporteOrderByFechaCreacionAsc(id).stream().map(ComentarioReporteResponse::desde).toList(); }
    public ComentariosPageResponse comentariosPagina(Long id,int pagina,int limite) {
        obtener(id);if(pagina<0||limite<1||limite>5)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Paginación no válida");
        var page=comentarios.findByReporte_IdReporte(id,PageRequest.of(pagina,limite,
                Sort.by(Sort.Direction.DESC,"fechaCreacion").and(Sort.by(Sort.Direction.DESC,"idComentario"))));
        return new ComentariosPageResponse(page.getTotalElements(),pagina,page.getTotalPages(),
                page.getContent().stream().map(ComentarioReporteResponse::desde).toList());
    }

    public boolean esPropio(String auth, ReporteSeguridad reporte) {
        if(auth==null || auth.isBlank() || reporte.getUsuario()==null) return false;
        try { return reporte.getUsuario().getIdUsuario().equals(sesiones.obtenerUsuario(auth).getIdUsuario()); }
        catch(ResponseStatusException ignored) { return false; }
    }

    @Transactional public void eliminarPropio(String auth, Long id) {
        ReporteSeguridad reporte=obtener(id); Usuario usuario=sesiones.obtenerUsuario(auth);
        if(reporte.getUsuario()==null || !reporte.getUsuario().getIdUsuario().equals(usuario.getIdUsuario()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Solo puedes eliminar tus propios reportes");
        comentarios.findByReporte_IdReporteOrderByFechaCreacionAsc(id).forEach(c->imagenes.eliminarImagen(c.getImagenPublicId()));
        String imagen=reporte.getImagenPublicId();reportes.delete(reporte);reportes.flush();imagenes.eliminarImagen(imagen);
    }

    private void validarImagen(MultipartFile imagen,boolean requerida){
        if(requerida&&(imagen==null||imagen.isEmpty()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La foto del reporte es obligatoria");
        if(imagen!=null&&!imagen.isEmpty()&&(imagen.getSize()>5L*1024*1024||imagen.getContentType()==null||
                !java.util.Set.of("image/jpeg","image/png","image/webp").contains(imagen.getContentType())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Usa una imagen JPG, PNG o WebP de hasta 5 MB");
    }
    private String limpiar(String value){return value==null||value.isBlank()?null:value.trim();}

    @Transactional public ReporteContenidoResponse reportarContenido(String auth, CrearReporteContenidoRequest request) {
        Usuario usuario=sesiones.obtenerUsuario(auth);
        String tipo=request.tipoContenido();
        boolean existe="LUGAR".equals(tipo) ? lugares.existsById(request.idContenido())
                : "RESENA".equals(tipo) ? resenas.existsById(request.idContenido())
                : reportes.existsById(request.idContenido());
        if(!existe) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"El contenido ya no existe");
        if(contenido.existsByUsuario_IdUsuarioAndTipoContenidoAndIdContenidoAndEstado(
                usuario.getIdUsuario(),tipo,request.idContenido(),"PENDIENTE"))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya enviaste este contenido a revisión");
        ReporteContenido value=new ReporteContenido(); value.setUsuario(usuario); value.setTipoContenido(tipo);
        value.setIdContenido(request.idContenido()); value.setMotivo(request.motivo().trim()); value.setEstado("PENDIENTE");
        return ReporteContenidoResponse.desde(contenido.save(value));
    }

    private void notificarCambio(ReporteSeguridad reporte, Usuario voter) {
        Usuario owner=reporte.getUsuario();
        if(owner==null || owner.getIdUsuario().equals(voter.getIdUsuario())) return;
        String status=reporte.getEstadoValidacion();
        if(!"CONFIRMADO".equals(status) && !"EN_REVISION".equals(status)) return;
        String type="REPORTE_"+status;
        if(notificaciones.existsByUsuario_IdUsuarioAndTipoAndIdReferencia(owner.getIdUsuario(),type,reporte.getIdReporte())) return;
        Notificacion n=new Notificacion(); n.setUsuario(owner); n.setTipo(type); n.setIdReferencia(reporte.getIdReporte());
        n.setTitulo("CONFIRMADO".equals(status)?"Reporte confirmado":"Reporte en revisión");
        n.setMensaje("CONFIRMADO".equals(status)?"La comunidad confirmó tu reporte de seguridad.":"La comunidad refutó tu reporte y será revisado.");
        n.setLeida(false); notificaciones.save(n);
    }
}
