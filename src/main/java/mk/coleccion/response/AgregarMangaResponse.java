package mk.coleccion.response;

public class AgregarMangaResponse {
    private String message;
    private Integer idManga;

    public AgregarMangaResponse(String message, Integer idManga) {
        this.message = message;
        this.idManga = idManga;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getIdManga() {
        return idManga;
    }

    public void setIdManga(Integer idManga) {
        this.idManga = idManga;
    }
}
