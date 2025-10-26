package mk.coleccion.response;

import mk.coleccion.dto.SerieDetallesDTO;

import java.util.List;

public class SerieDetallesResponse {
    private String response;
    private List<SerieDetallesDTO> result;

    // Constructor
    public SerieDetallesResponse(String response, List<SerieDetallesDTO> result) {
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

    public List<SerieDetallesDTO> getResult() {
        return result;
    }

    public void setResult(List<SerieDetallesDTO> result) {
        this.result = result;
    }
}


