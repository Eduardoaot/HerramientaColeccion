package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.Serie;
import java.util.List;

public interface ISerieServicioAPP {
    List<Serie> listarSeries();
    Serie buscarSeriePorId(Integer idSerie);
    Serie guardarSerie(Serie serie);
    void eliminarSerie(Serie serie);
}
