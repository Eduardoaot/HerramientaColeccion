package mk.coleccion.response;

import mk.coleccion.dto.ColeccionMangaDetalleDTO;

import java.util.List;

public class MangaResponse {
    private String response;
    private List<ColeccionMangaDetalleDTO> result;

    // Constructor
    public MangaResponse(String response, List<ColeccionMangaDetalleDTO> result) {
        this.response = response;
        this.result = result;
    }

    // Getters y Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<ColeccionMangaDetalleDTO> getResult() {
        return result;
    }

    public void setResult(List<ColeccionMangaDetalleDTO> result) {
        this.result = result;
    }
}
