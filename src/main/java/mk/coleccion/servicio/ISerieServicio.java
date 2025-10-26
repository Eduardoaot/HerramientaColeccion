package mk.coleccion.servicio;

import mk.coleccion.dto.SerieDetallesDTO;

import java.util.List;

public interface ISerieServicio {

    public List<SerieDetallesDTO> buscarSeriesConDescripcion(String query);
}
