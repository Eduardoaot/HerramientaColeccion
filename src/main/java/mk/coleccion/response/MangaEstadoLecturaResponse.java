package mk.coleccion.response;

public class MangaEstadoLecturaResponse {
    private String message;
    private Integer estadoLectura;

    public MangaEstadoLecturaResponse(String message, Integer estadoLectura) {
        this.message = message;
        this.estadoLectura = estadoLectura;
    }

    // Getters y setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getEstadoLectura() {
        return estadoLectura;
    }

    public void setEstadoLectura(Integer estadoLectura) {
        this.estadoLectura = estadoLectura;
    }
}


