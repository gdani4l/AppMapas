package api_app_mapas.lugares.controller;

import api_app_mapas.lugares.dto.*;
import api_app_mapas.lugares.model.Lugar;
import api_app_mapas.lugares.repository.LugarRepository;
import api_app_mapas.lugares.repository.CategoriaLugarRepository;
import api_app_mapas.sesion.service.SesionService;
import api_app_mapas.lugares.repository.ResenaLugarRepository;
import api_app_mapas.reportes.repository.ReporteSeguridadRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@RestController
@RequestMapping("/api/lugares")
public class LugarController {
    private final LugarRepository lugares;
    private final CategoriaLugarRepository categorias;
    private final SesionService sesiones;
    private final ResenaLugarRepository resenas;
    private final ReporteSeguridadRepository reportes;

    public LugarController(LugarRepository lugares, CategoriaLugarRepository categorias,
                           SesionService sesiones, ResenaLugarRepository resenas,
                           ReporteSeguridadRepository reportes) {
        this.lugares = lugares;
        this.categorias = categorias;
        this.sesiones = sesiones;
        this.resenas = resenas;
        this.reportes = reportes;
    }

    @GetMapping("/categorias")
    public List<CategoriaLugarResponse> categorias() {
        return categorias.findByActivaTrueOrderByOrdenAscNombreAsc().stream()
                .map(CategoriaLugarResponse::desde).toList();
    }

    @GetMapping
    public List<LugarResponse> listar(@RequestParam BigDecimal sur, @RequestParam BigDecimal norte,
                                      @RequestParam BigDecimal oeste, @RequestParam BigDecimal este,
                                      @RequestParam(required = false) String tipo,
                                      @RequestParam(required = false) List<String> tipos) {
        List<String> filtros = tipos == null ? List.of() : tipos.stream()
                .map(String::trim).filter(valor -> !valor.isEmpty()).distinct().toList();
        if (sur.compareTo(norte) > 0 || oeste.compareTo(este) > 0 ||
                sur.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                norte.compareTo(BigDecimal.valueOf(90)) > 0 ||
                oeste.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                este.compareTo(BigDecimal.valueOf(180)) > 0 ||
                (tipo != null && !categorias.existsByCodigoAndActivaTrue(tipo)) ||
                filtros.size() > 20 || filtros.stream().anyMatch(
                        codigo -> !categorias.existsByCodigoAndActivaTrue(codigo))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro o coordenadas no válidos");
        }
        List<Lugar> resultados = !filtros.isEmpty()
                ? lugares.buscarEnVistaPorTipos(sur, norte, oeste, este, filtros, PageRequest.of(0, 200))
                : lugares.buscarEnVista(sur, norte, oeste, este, tipo, PageRequest.of(0, 200));
        return resultados
                .stream().map(LugarResponse::desde).toList();
    }

    @GetMapping("/buscar")
    public List<LugarResponse> buscar(@RequestParam String q,
                                     @RequestParam(required = false) BigDecimal latitud,
                                     @RequestParam(required = false) BigDecimal longitud) {
        String texto = q.trim();
        if (texto.isEmpty() || texto.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe un nombre o dirección");
        }
        if ((latitud == null) != (longitud == null) ||
                (latitud != null && (latitud.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                        latitud.compareTo(BigDecimal.valueOf(90)) > 0 ||
                        longitud.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                        longitud.compareTo(BigDecimal.valueOf(180)) > 0))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordenadas no válidas");
        }
        List<Lugar> resultados = latitud == null
                ? lugares.buscarPorTexto(texto, PageRequest.of(0, 20))
                : lugares.buscarPorTextoCercano(texto, latitud, longitud, PageRequest.of(0, 20));
        return resultados
                .stream().map(LugarResponse::desde).toList();
    }

    @GetMapping("/recomendados")
    public List<RecomendacionLugarResponse> recomendados(
            @RequestParam BigDecimal latitud, @RequestParam BigDecimal longitud,
            @RequestParam(defaultValue = "SEGURIDAD") String ordenarPor) {
        if(latitud.abs().compareTo(BigDecimal.valueOf(90))>0 || longitud.abs().compareTo(BigDecimal.valueOf(180))>0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Coordenadas no válidas");
        List<RecomendacionLugarResponse> result=new ArrayList<>();
        for(Lugar lugar:lugares.findAll()) {
            double rating=java.util.Optional.ofNullable(resenas.promedio(lugar.getIdLugar())).orElse(0.0);
            long count=resenas.countByLugarIdLugar(lugar.getIdLugar());
            BigDecimal margin=BigDecimal.valueOf(0.005);
            long alerts=reportes.contarAlertas(lugar.getLatitud().subtract(margin),lugar.getLatitud().add(margin),
                    lugar.getLongitud().subtract(margin),lugar.getLongitud().add(margin));
            double distance=distanceKm(latitud.doubleValue(),longitud.doubleValue(),
                    lugar.getLatitud().doubleValue(),lugar.getLongitud().doubleValue());
            result.add(new RecomendacionLugarResponse(LugarResponse.desde(lugar),rating,count,alerts,distance));
        }
        Comparator<RecomendacionLugarResponse> comparator;
        if("CALIFICACION".equalsIgnoreCase(ordenarPor)) comparator=Comparator.comparingDouble(RecomendacionLugarResponse::calificacion).reversed();
        else if("CERCANIA".equalsIgnoreCase(ordenarPor)) comparator=Comparator.comparingDouble(RecomendacionLugarResponse::distanciaKm);
        else comparator=Comparator.comparingLong(RecomendacionLugarResponse::alertasCercanas)
                .thenComparing(Comparator.comparingDouble(RecomendacionLugarResponse::calificacion).reversed());
        return result.stream().sorted(comparator).limit(10).toList();
    }

    private double distanceKm(double lat1,double lon1,double lat2,double lon2) {
        double dLat=Math.toRadians(lat2-lat1),dLon=Math.toRadians(lon2-lon1);
        double a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
        return 6371.0*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
    }

    @GetMapping("/{id}")
    public LugarResponse detalle(@PathVariable Long id) {
        return lugares.findById(id).map(LugarResponse::desde).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LugarResponse crear(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody LugarRequest request) {
        if (!categorias.existsByCodigoAndActivaTrue(request.tipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no válida");
        }
        Lugar lugar = new Lugar();
        lugar.setTipo(request.tipo());
        lugar.setNombre(request.nombre().trim());
        lugar.setDescripcion(limpiar(request.descripcion()));
        lugar.setDireccion(limpiar(request.direccion()));
        lugar.setLatitud(request.latitud());
        lugar.setLongitud(request.longitud());
        lugar.setCreador(sesiones.obtenerUsuario(authorization));
        return LugarResponse.desde(lugares.save(lugar));
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
