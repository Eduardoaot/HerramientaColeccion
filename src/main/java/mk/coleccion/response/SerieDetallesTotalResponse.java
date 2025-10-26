package mk.coleccion.response;

import mk.coleccion.dto.SerieDetallesTotalDTO;

public class SerieDetallesTotalResponse {
    private String response;
    private SerieDetallesTotalDTO result;

    // Constructor
    public SerieDetallesTotalResponse(String response, SerieDetallesTotalDTO result) {
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

    public SerieDetallesTotalDTO getResult() {
        return result;
    }

    public void setResult(SerieDetallesTotalDTO result) {
        this.result = result;
    }
}
