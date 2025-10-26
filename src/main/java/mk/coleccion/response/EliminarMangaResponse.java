package mk.coleccion.response;

public class EliminarMangaResponse {
    private String message;

    // Constructor
    public EliminarMangaResponse(String message) {
        this.message = message;
    }

    // Getters y setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
