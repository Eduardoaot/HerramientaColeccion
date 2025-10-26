package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.DescripcionSerie;

public interface IDescripcionSerieServicioAPP {

    DescripcionSerie buscarDescripcionSeriePorId(Integer idDescripcionSerie);
    DescripcionSerie guardarDescripcionSerie(DescripcionSerie descripcionSerie);
    void eliminarDescripcionSerie(DescripcionSerie descripcionSerie);
}
