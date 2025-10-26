package mk.coleccion.controlador;

import mk.coleccion.dto.MangasResponseDTO;
import mk.coleccion.dto.PendienteMangasFaltantesDTO;
import mk.coleccion.dto.PlanDTO;
import mk.coleccion.servicio.PendienteServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mangas-pendientes")
public class PendientesControlador {

    @Autowired
    private PendienteServicio pendienteServicio;

    @GetMapping("/faltantes/{idUsuario}")
    public ResponseEntity<PendienteMangasFaltantesDTO> obtenerMangasPendientes(@PathVariable Integer idUsuario) {
        // Obtener los mangas pendientes usando el servicio
        PendienteMangasFaltantesDTO mangasPendientes = pendienteServicio.obtenerMangasFaltantes(idUsuario);

        // Retornar la respuesta con el estado "success"
        return ResponseEntity.ok(mangasPendientes);
    }

    @PostMapping("/buscar")
    public ResponseEntity<MangasResponseDTO> buscarMangasPost(@RequestBody Map<String, List<Integer>> body) {
        List<Integer> listaIds = body.get("listaIds");
        List<PlanDTO> resultado = pendienteServicio.obtenerMangasPorIds(listaIds);

        // Crear el objeto DTO con el formato deseado
        MangasResponseDTO responseDTO = new MangasResponseDTO("success", resultado);

        return ResponseEntity.ok(responseDTO);
    }
}

