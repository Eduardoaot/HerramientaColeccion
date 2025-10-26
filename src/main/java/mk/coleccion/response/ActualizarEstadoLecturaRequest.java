package mk.coleccion.response;

public class ActualizarEstadoLecturaRequest {
    private Integer idManga;
    private Integer idUsuario;
    private Integer estadoLectura;

    // Getters y setters
    public Integer getIdManga() {
        return idManga;
    }

    public void setIdManga(Integer idManga) {
        this.idManga = idManga;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getEstadoLectura() {
        return estadoLectura;
    }

    public void setEstadoLectura(Integer estadoLectura) {
        this.estadoLectura = estadoLectura;
    }
}

