package mk.coleccion.response;

import mk.coleccion.dto.MangaDetallesDTO;

import java.util.List;

public class MangaDetallesResponse {
    private String response;
    private List<MangaDetallesDTO> result;

    // Constructor
    public MangaDetallesResponse(String response, List<MangaDetallesDTO> result) {
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

    public List<MangaDetallesDTO> getResult() {
        return result;
    }

    public void setResult(List<MangaDetallesDTO> result) {
        this.result = result;
    }
}
