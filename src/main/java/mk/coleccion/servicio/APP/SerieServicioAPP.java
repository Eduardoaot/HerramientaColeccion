package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.Serie;
import mk.coleccion.repositorio.APP.SerieRepositorioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SerieServicioAPP implements ISerieServicioAPP {

    @Autowired
    private SerieRepositorioAPP serieRepositorio;

    @Override
    public List<Serie> listarSeries() {
        return serieRepositorio.findAll();
    }

    @Override
    public Serie buscarSeriePorId(Integer idSerie) {
        return serieRepositorio.findById(idSerie).orElse(null);
    }

    @Override
    public Serie guardarSerie(Serie serie) {
        return serieRepositorio.save(serie);
    }

    @Override
    public void eliminarSerie(Serie serie) {
        serieRepositorio.delete(serie);
    }
}
