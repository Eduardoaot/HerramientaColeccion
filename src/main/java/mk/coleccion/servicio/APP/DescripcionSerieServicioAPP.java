package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.DescripcionSerie;

import mk.coleccion.repositorio.APP.DescripcionSerieRepositorioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DescripcionSerieServicioAPP implements IDescripcionSerieServicioAPP {

    @Autowired
    private DescripcionSerieRepositorioAPP descripcionSerieRepositorio;

    @Override
    public DescripcionSerie buscarDescripcionSeriePorId(Integer idDescripcionSerie) {
        return descripcionSerieRepositorio.findById(idDescripcionSerie).orElse(null);
    }

    @Override
    public DescripcionSerie guardarDescripcionSerie(DescripcionSerie descripcionSerie) {
        return descripcionSerieRepositorio.save(descripcionSerie);
    }

    @Override
    public void eliminarDescripcionSerie(DescripcionSerie descripcionSerie) {
        descripcionSerieRepositorio.delete(descripcionSerie);
    }
}
