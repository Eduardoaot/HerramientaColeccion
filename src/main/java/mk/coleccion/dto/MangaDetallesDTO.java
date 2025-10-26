package mk.coleccion.dto;

public class MangaDetallesDTO {
    private String tituloSerie;
    private Float numeroManga;
    private String estadoLectura;
    private String descripcion;
    private String nombreAutor;
    private String imagenManga;
    private Float precioManga;
    private Long estadoAgregado;

    public MangaDetallesDTO(String tituloSerie, Float numeroManga, String estadoLectura,
                            String descripcion, String nombreAutor, String imagenManga, Float precioManga, Long estadoAgregado) {
        this.tituloSerie = tituloSerie;
        this.numeroManga = numeroManga;
        this.estadoLectura = estadoLectura;
        this.descripcion = descripcion;
        this.nombreAutor = nombreAutor;
        this.imagenManga = imagenManga;
        this.precioManga = precioManga;
        this.estadoAgregado = estadoAgregado;
    }

    // Getters y setters
    public String getTituloSerie() { return tituloSerie; }
    public void setTituloSerie(String tituloSerie) { this.tituloSerie = tituloSerie; }

    public Float getNumeroManga() { return numeroManga; }
    public void setNumeroManga(Float numeroManga) { this.numeroManga = numeroManga; }

    public String getEstadoLectura() { return estadoLectura; }
    public void setEstadoLectura(String estadoLectura) { this.estadoLectura = estadoLectura; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNombreAutor() { return nombreAutor; }
    public void setNombreAutor(String nombreAutor) { this.nombreAutor = nombreAutor; }

    public String getImagenManga() { return imagenManga; }
    public void setImagenManga(String imagenManga) { this.imagenManga = imagenManga; }

    public Float getPrecioManga() { return precioManga; }
    public void setPrecioManga(Float precioManga) { this.precioManga = precioManga; }

    public Long getEstadoAgregado() { return estadoAgregado; }
    public void setEstadoAgregado(Long estadoAgregado) { this.estadoAgregado = estadoAgregado; }
}
