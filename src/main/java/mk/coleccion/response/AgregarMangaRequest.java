package mk.coleccion.response;

public class AgregarMangaRequest {
    private Integer idManga;
    private Integer idUsuario;
    private Float precio;

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

    public Float getPrecio() {  // Se corrigió este getter
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }
}
