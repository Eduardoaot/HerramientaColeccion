package mk.coleccion.controlador;

import mk.coleccion.dto.ActualizarPresupuestoRequestDTO;
import mk.coleccion.dto.CrearPresupuestoRequestDTO;
import mk.coleccion.dto.PresupuestoResponseDTO;
import mk.coleccion.servicio.PresupuestoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoControlador {

    @Autowired
    private PresupuestoServicio presupuestoServicio;

    @PostMapping("/crear")
    public ResponseEntity<String> crearPresupuesto(@RequestBody CrearPresupuestoRequestDTO request) {
        presupuestoServicio.crearPresupuestoConMangas(request);
        return ResponseEntity.ok("Presupuesto y mangas guardados correctamente.");
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<Map<String, Object>> obtenerPresupuestos(@PathVariable Integer idUsuario) {
        List<PresupuestoResponseDTO> presupuestos = presupuestoServicio.obtenerPresupuestosPorUsuario(idUsuario);
        Map<String, Object> response = new HashMap<>();
        response.put("response", "successful");
        response.put("result", presupuestos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detalle/{idPresupuesto}")
    public ResponseEntity<Map<String, Object>> obtenerPresupuestoPorId(@PathVariable Integer idPresupuesto) {
        PresupuestoResponseDTO presupuesto = presupuestoServicio.obtenerPresupuestoPorId(idPresupuesto);

        if (presupuesto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("response", "Presupuesto no encontrado"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("response", "successful");
        response.put("result", presupuesto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/eliminar/{idPresupuesto}")
    public ResponseEntity<Map<String, String>> eliminarPresupuesto(@PathVariable Integer idPresupuesto) {
        try {
            // Eliminar el presupuesto y los registros relacionados en presupuestos_manga
            presupuestoServicio.eliminarPresupuesto(idPresupuesto);
            Map<String, String> response = new HashMap<>();
            response.put("response", "Presupuesto y registros relacionados eliminados correctamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("response", "Error al eliminar el presupuesto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/actualizar")
    public ResponseEntity<String> actualizarPresupuesto(@RequestBody ActualizarPresupuestoRequestDTO actualizarPresupuestoRequestDTO) {
        try {
            presupuestoServicio.actualizarPresupuesto(actualizarPresupuestoRequestDTO);
            return ResponseEntity.ok("Presupuesto actualizado con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el presupuesto: " + e.getMessage());
        }
    }
}


