package mk.coleccion.servicio;

import mk.coleccion.dto.PresupuestoMangaDetalleDTO;
import mk.coleccion.modelo.PresupuestosManga;
import mk.coleccion.repositorio.PresupuestoMangaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoMangaServicio implements IPresupuestoMangaServicio{

    @Autowired
    private PresupuestoMangaRepositorio presupuestoMangaRepositorio;

    public List<PresupuestoMangaDetalleDTO> obtenerDetallesPresupuestoManga(Integer idUsuario, Integer idPresupuesto) {
        List<Object[]> results = presupuestoMangaRepositorio.findDetallesPresupuestoManga(idUsuario, idPresupuesto);
        return results.stream()
                .map(row -> new PresupuestoMangaDetalleDTO(
                        (Integer) row[0], // idPresupuesto
                        (String) row[1],  // nombrePresupuesto
                        (String) row[2],   // mangaNum
                        (String) row[3],  // serieNom
                        (Float) row[4]   // precio
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void guardarPresupuestoManga(PresupuestosManga presupuestosManga) {
        this.presupuestoMangaRepositorio.save(presupuestosManga);
    }

    @Override
    public void eliminarPresupuestoMangaPorId(Integer idPresupuestoManga) {
        this.presupuestoMangaRepositorio.deleteById(idPresupuestoManga);
    }
}
