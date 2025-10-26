package mk.coleccion.response;

import mk.coleccion.dto.ColeccionMangaDetalleDTO;
import mk.coleccion.dto.MangaLecturaDTO;

import java.util.List;

public class LecturaGeneralResponse {
    private String response;
    private MangaLecturaDTO result;

    // Constructor
    public LecturaGeneralResponse(String response, MangaLecturaDTO result) {
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

    public MangaLecturaDTO getResult() {
        return result;
    }

    public void setResult(MangaLecturaDTO result) {
        this.result = result;
    }
}
