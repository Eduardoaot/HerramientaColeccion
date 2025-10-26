package mk.coleccion.controlador;

import mk.coleccion.dto.SerieDetallesTotalDTO;
import mk.coleccion.response.SerieDetallesResponse;
import mk.coleccion.dto.SerieDetallesDTO;
import mk.coleccion.response.SerieDetallesTotalResponse;
import mk.coleccion.servicio.SerieServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SerieControlador {

    @Autowired
    private SerieServicio serieServicio;

    @GetMapping("/buscar")
    public ResponseEntity<SerieDetallesResponse> buscarSeries(@RequestParam String query) {
        // Llamar al servicio para obtener las series
        List<SerieDetallesDTO> series = serieServicio.buscarSeriesConDescripcion(query);

        // Crear la respuesta envolvente
        SerieDetallesResponse response = new SerieDetallesResponse("success", series);

        // Si no hay resultados, devolver "no content" con una lista vacía
        if (series.isEmpty()) {
            return ResponseEntity.noContent().build(); // No content sin cuerpo, solo código 204
        } else {
            return ResponseEntity.ok(response); // Devuelve la respuesta con los resultados
        }
    }

    @GetMapping("/detalles/{id_usuario}/{id_serie}")
    public ResponseEntity<SerieDetallesTotalResponse> obtenerDetallesSerie(
            @PathVariable Integer id_usuario,
            @PathVariable Integer id_serie) {

        // Llamamos al servicio para obtener los detalles de la serie con mangas
        SerieDetallesTotalDTO detalles = serieServicio.obtenerDetallesSerieConMangas(id_serie, id_usuario);

        // Creamos la respuesta envolvente
        SerieDetallesTotalResponse response = new SerieDetallesTotalResponse("success", detalles);

        // Si no hay detalles, devolver "no content"
        if (detalles == null) {
            return ResponseEntity.noContent().build(); // No content sin cuerpo, solo código 204
        } else {
            return ResponseEntity.ok(response); // Devuelve la respuesta con los resultados
        }
    }

}






