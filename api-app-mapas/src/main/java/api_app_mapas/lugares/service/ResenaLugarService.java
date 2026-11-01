package api_app_mapas.lugares.service;

import api_app_mapas.lugares.dto.*;
import api_app_mapas.lugares.model.ResenaLugar;
import api_app_mapas.lugares.repository.LugarRepository;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.perfil.service.CloudinaryService;
import api_app_mapas.usuario.model.Usuario;
import api_app_mapas.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.List;

@Service
public class ResenaLugarService {
    private final ResenaLugarRepository resenas;
    private final LugarRepository lugares;
    private final CloudinaryService imagenes;
    private final UsuarioRepository usuarios;

    public ResenaLugarService(ResenaLugarRepository resenas, LugarRepository lugares,
                             CloudinaryService imagenes, UsuarioRepository usuarios) {
        this.resenas = resenas;
        this.lugares = lugares;
        this.imagenes = imagenes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public ResenasPageResponse listar(Long lugarId, int pagina, int limite) {
        verificarLugar(lugarId);
        if (pagina < 0 || limite < 1 || limite > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paginación no válida");
        }
        var page = resenas.listar(lugarId, PageRequest.of(pagina, limite,
                Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by(Sort.Direction.DESC, "id"))));
        long total = resenas.countByLugarIdLugar(lugarId);
        Double media = resenas.promedio(lugarId);
        double promedio = media == null ? 0 : media;
        return new ResenasPageResponse(total, promedio,
                page.getContent().stream().map(ResenaResponse::desde).toList());
    }

    @Transactional(readOnly = true)
    public ResenaResponse mia(Long lugarId, Usuario usuario) {
        verificarLugar(lugarId);
        return resenas.findByLugarIdLugarAndUsuarioIdUsuario(lugarId, usuario.getIdUsuario())
                .map(ResenaResponse::desde).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<String> imagenes(Long lugarId) {
        verificarLugar(lugarId);
        return resenas.imagenes(lugarId, PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    public List<FotoLugarResponse> fotos(Long lugarId) {
        verificarLugar(lugarId);
        return resenas.fotosConAutor(lugarId, PageRequest.of(0, 50)).stream()
                .map(FotoLugarResponse::desde).toList();
    }

    @Transactional
    public ResenaResponse guardar(Long lugarId, Usuario usuario, Integer estrellas,
                                  String comentario, MultipartFile imagen, boolean editar,
                                  boolean quitarImagen) throws IOException {
        validar(estrellas, comentario, imagen);
        // El usuario obtenido de la sesión en el controlador puede ser un proxy ya desacoplado.
        Usuario autor = usuarios.findById(usuario.getIdUsuario()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        ResenaLugar resena;
        if (editar) {
            resena = resenas.findByLugarIdLugarAndUsuarioIdUsuario(lugarId, autor.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Aún no tienes una reseña en este lugar"));
        } else {
            if (resenas.findByLugarIdLugarAndUsuarioIdUsuario(lugarId, autor.getIdUsuario()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Ya reseñaste este lugar; puedes editar tu reseña");
            }
            resena = new ResenaLugar();
            resena.setLugar(lugares.findById(lugarId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado")));
        }
        resena.setUsuario(autor);
        String anterior = resena.getImagenPublicId();
        CloudinaryService.UploadResult subida = imagen != null && !imagen.isEmpty()
                ? imagenes.subirImagenResena(imagen.getBytes()) : null;
        resena.setEstrellas(estrellas);
        resena.setComentario(comentario == null || comentario.isBlank() ? null : comentario.trim());
        if (subida != null) {
            resena.setImagenUrl(subida.url());
            resena.setImagenPublicId(subida.publicId());
        } else if (quitarImagen) {
            resena.setImagenUrl(null);
            resena.setImagenPublicId(null);
        }
        try {
            ResenaResponse response = ResenaResponse.desde(resenas.saveAndFlush(resena));
            if ((subida != null || quitarImagen) && anterior != null) imagenes.eliminarImagen(anterior);
            return response;
        } catch (RuntimeException error) {
            if (subida != null) imagenes.eliminarImagen(subida.publicId());
            throw error;
        }
    }

    @Transactional
    public void eliminar(Long lugarId, Usuario usuario) {
        ResenaLugar resena = resenas.findByLugarIdLugarAndUsuarioIdUsuario(lugarId, usuario.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró tu reseña"));
        String publicId = resena.getImagenPublicId();
        resenas.delete(resena);
        resenas.flush();
        if (publicId != null) imagenes.eliminarImagen(publicId);
    }

    private void verificarLugar(Long lugarId) {
        if (!lugares.existsById(lugarId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado");
        }
    }

    private void validar(Integer estrellas, String comentario, MultipartFile imagen) {
        if (estrellas == null || estrellas < 0 || estrellas > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La calificación debe estar entre 0 y 5");
        }
        if (comentario != null && comentario.trim().length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El comentario supera 2000 caracteres");
        }
        if (imagen != null && !imagen.isEmpty()) {
            if (imagen.getSize() > 5L * 1024 * 1024 || imagen.getContentType() == null ||
                    !java.util.Set.of("image/jpeg", "image/png", "image/webp").contains(imagen.getContentType())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Usa una imagen JPG, PNG o WebP de hasta 5 MB");
            }
        }
    }
}
