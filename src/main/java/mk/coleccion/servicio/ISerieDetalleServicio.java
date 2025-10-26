package mk.coleccion.servicio;

import mk.coleccion.dto.SerieDetallesTotalDTO;

public interface ISerieDetalleServicio {

    SerieDetallesTotalDTO obtenerDetallesSerieConMangas(Integer idSerie, Integer idUsuario);
}
