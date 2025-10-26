package mk.coleccion.controlador;

import mk.coleccion.dto.MangaDetallesDTO;
import mk.coleccion.dto.MangaLecturaDTO;
import mk.coleccion.dto.MangaPendienteDTO;
import mk.coleccion.response.*;
import mk.coleccion.servicio.MangaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manga")
public class MangaControlador {

    @Autowired
    private MangaServicio mangaServicio;

    // Método GET para obtener detalles del manga
    @GetMapping("/{idManga}/{idUsuario}/detalles")
    public MangaDetallesResponse obtenerDetallesManga(@PathVariable Integer idManga,
                                                      @PathVariable Integer idUsuario) {
        List<MangaDetallesDTO> detalles = mangaServicio.obtenerDetallesManga(idManga, idUsuario);
        String response = detalles.isEmpty() ? "No encontrado" : "Éxito";
        return new MangaDetallesResponse(response, detalles);
    }

    // Método POST para actualizar el estado de lectura
    @PostMapping("/actualizarEstadoLectura")
    public ResponseEntity<MangaEstadoLecturaResponse> actualizarEstadoLectura(@RequestBody ActualizarEstadoLecturaRequest request) {
        MangaEstadoLecturaResponse response = mangaServicio.actualizarEstadoLectura(request.getIdManga(), request.getIdUsuario(), request.getEstadoLectura());
        return ResponseEntity.ok(response);
    }

    @GetMapping("lectura/{id_usuario}")
    public LecturaGeneralResponse obtenerDetallesLectura(@PathVariable Integer id_usuario) throws InterruptedException {
        // Obtener los detalles de la lectura
        MangaLecturaDTO detalles = mangaServicio.obtenerDetallesLectura(id_usuario);

        // Verificar si los detalles no están vacíos
        String response = (detalles.getListaMangasCompradosData().isEmpty() && detalles.getListaMangasLeidosData().isEmpty() && detalles.getListaMangasMeses().isEmpty())
                ? "No encontrado"
                : "Éxito";

        // Devolver la respuesta general con los detalles
        return new LecturaGeneralResponse(response, detalles);
    }

    @GetMapping("/pendientes/{idUsuario}")
    public LecturaPendientesResponse obtenerMangasPendientes(@PathVariable Integer idUsuario) {
        // Obtener las mangas pendientes desde el servicio
        List<MangaPendienteDTO> mangasPendientes = mangaServicio.obtenerMangasPendientes(idUsuario);

        // Crear la respuesta personalizada
        String response = "Mangas pendientes obtenidos con éxito";
        LecturaPendientesResponse lecturaPendientesResponse = new LecturaPendientesResponse(response, mangasPendientes);

        // Devolver la respuesta
        return new LecturaPendientesResponse(response, mangasPendientes);
    }

}



