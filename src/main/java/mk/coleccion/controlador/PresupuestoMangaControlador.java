package mk.coleccion.controlador;

import mk.coleccion.dto.PresupuestoMangaDetalleDTO;
import mk.coleccion.modelo.PresupuestosManga;
import mk.coleccion.servicio.PresupuestoMangaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presupuestos-manga")
public class PresupuestoMangaControlador {

    private static final Logger logger = LoggerFactory.getLogger(PresupuestoMangaControlador.class);

    @Autowired
    private PresupuestoMangaServicio presupuestoMangaServicio;

    @GetMapping("/detalles/{idUsuario}/{idPresupuesto}")
    public List<PresupuestoMangaDetalleDTO> obtenerDetallesPresupuestoManga(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idPresupuesto) {
        return presupuestoMangaServicio.obtenerDetallesPresupuestoManga(idUsuario, idPresupuesto);
    }

    @PostMapping("/guardar")
    public void guardarPresupuestoManga(@RequestBody PresupuestosManga presupuestosManga) {
        presupuestoMangaServicio.guardarPresupuestoManga(presupuestosManga);
    }

    // Endpoint para eliminar un PresupuestosManga por ID
    @DeleteMapping("/eliminar/{idPresupuestoManga}")
    public void eliminarPresupuestoMangaPorId(@PathVariable Integer idPresupuestoManga) {
        presupuestoMangaServicio.eliminarPresupuestoMangaPorId(idPresupuestoManga);
    }
}