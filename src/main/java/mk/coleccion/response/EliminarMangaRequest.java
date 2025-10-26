package mk.coleccion.response;


public class EliminarMangaRequest {
    private Integer idManga;
    private Integer idUsuario;

    // Constructor vacío
    public EliminarMangaRequest() {}

    // Constructor con parámetros
    public EliminarMangaRequest(Integer idManga, Integer idUsuario) {
        this.idManga = idManga;
        this.idUsuario = idUsuario;
    }

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
}

