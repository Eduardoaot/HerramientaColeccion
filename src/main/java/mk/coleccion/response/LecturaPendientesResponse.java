package mk.coleccion.response;

import mk.coleccion.dto.MangaPendienteDTO;

import java.util.List;

public class LecturaPendientesResponse {
    private String response;
    private List<MangaPendienteDTO> detalles;

    // Constructor
    public LecturaPendientesResponse(String response, List<MangaPendienteDTO> detalles) {
        this.response = response;
        this.detalles = detalles;
    }

    // Getters y Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<MangaPendienteDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<MangaPendienteDTO> detalles) {
        this.detalles = detalles;
    }
}
