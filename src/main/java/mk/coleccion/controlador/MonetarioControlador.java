package mk.coleccion.controlador;

import lombok.RequiredArgsConstructor;
import mk.coleccion.dto.CrearMonetarioAhorrosRequestDTO;
import mk.coleccion.dto.EstadisticasResponseDTO;
import mk.coleccion.servicio.MonetarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/monetario")
@RequiredArgsConstructor
public class MonetarioControlador {

    @Autowired
    private MonetarioServicio monetarioServicio;


    @PostMapping("/guardar")
    public ResponseEntity<String> guardarMonetario(@RequestBody CrearMonetarioAhorrosRequestDTO requestDTO) {
        try {
            monetarioServicio.guardarMonetarioAhorro(requestDTO);
            return ResponseEntity.ok("Datos de ahorro guardados exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar los datos de ahorro: " + e.getMessage());
        }
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<EstadisticasResponseDTO> obtenerEstadisticas(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(monetarioServicio.obtenerEstadisticasUsuario(idUsuario));
    }
}

