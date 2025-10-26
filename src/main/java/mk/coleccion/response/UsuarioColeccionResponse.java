package mk.coleccion.response;

import mk.coleccion.dto.UsuarioColeccionDTO;

public class UsuarioColeccionResponse {
    private String response;
    private UsuarioColeccionDTO result;

    // Constructor
    public UsuarioColeccionResponse(String response, UsuarioColeccionDTO result) {
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

    public UsuarioColeccionDTO getResult() {
        return result;
    }

    public void setResult(UsuarioColeccionDTO result) {
        this.result = result;
    }
}
