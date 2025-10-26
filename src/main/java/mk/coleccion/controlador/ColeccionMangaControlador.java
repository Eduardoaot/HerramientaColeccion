package mk.coleccion.controlador;

import mk.coleccion.dto.*;
import mk.coleccion.repositorio.ColeccionMangaRepositorio;
import mk.coleccion.repositorio.MangaRepositorio;
import mk.coleccion.repositorio.MonetarioAhorrosRepositorio;
import mk.coleccion.response.*;
import mk.coleccion.servicio.ColeccionMangaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coleccion-manga")
public class    ColeccionMangaControlador {

    @Autowired
    private ColeccionMangaServicio coleccionMangaServicio;

    @GetMapping("/detalles/{idUsuario}")
    public ResponseEntity<MangaResponse> obtenerDetallesColeccionManga(@PathVariable Integer idUsuario) {
        List<ColeccionMangaDetalleDTO> detalles = coleccionMangaServicio.obtenerDetallesColeccionManga(idUsuario);

        // Crear el objeto de respuesta
        MangaResponse response = new MangaResponse("success", detalles);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/series/{idUsuario}")
    public ResponseEntity<SerieResponse> obtenerSeriesDeColeccion(@PathVariable Integer idUsuario) {
        List<SerieInfoDTO> series = coleccionMangaServicio.obtenerSeriesDeColeccion(idUsuario);

        // Crear el objeto de respuesta
        SerieResponse response = new SerieResponse("success", series);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario-detalles/{idUsuario}")
    public ResponseEntity<UsuarioColeccionResponse> obtenerDetallesColeccionDelUsuario(@PathVariable Integer idUsuario) {
        UsuarioColeccionDTO detallesUsuario = coleccionMangaServicio.obtenerDetallesColeccionDelUsuario(idUsuario);

        // Crear el objeto de respuesta
        UsuarioColeccionResponse response = new UsuarioColeccionResponse("success", detallesUsuario);

        return ResponseEntity.ok(response);
    }

        @PostMapping("/eliminar-manga")
    public ResponseEntity<EliminarMangaResponse> eliminarManga(@RequestBody EliminarMangaRequest request) {
        try {
            coleccionMangaServicio.eliminarMangaYSerieSiEsNecesario(request.getIdManga(), request.getIdUsuario());
            return new ResponseEntity<>(new EliminarMangaResponse("Manga eliminado correctamente"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new EliminarMangaResponse("Error al eliminar el manga"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/agregar-manga")
    public ResponseEntity<AgregarMangaResponse> agregarManga(@RequestBody AgregarMangaRequest request) {
        coleccionMangaServicio.agregarMangaYSerieSiEsNecesario(request.getIdManga(), request.getIdUsuario(), request.getPrecio());
        return ResponseEntity.ok(new AgregarMangaResponse("Manga agregado correctamente", request.getIdManga()));
    }

}