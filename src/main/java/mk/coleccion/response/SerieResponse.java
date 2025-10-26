package mk.coleccion.response;

import mk.coleccion.dto.SerieInfoDTO;

import java.util.List;

public class SerieResponse {
    private String response;
    private List<SerieInfoDTO> result;

    // Constructor
    public SerieResponse(String response, List<SerieInfoDTO> result) {
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

    public List<SerieInfoDTO> getResult() {
        return result;
    }

    public void setResult(List<SerieInfoDTO> result) {
        this.result = result;
    }
}


