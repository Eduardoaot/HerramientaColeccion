package mk.coleccion.dto;

public class SerieDetallesDTO {
    private Integer idSerie;
    private String nombre;
    private Integer totalTomos;
    private String imagenPrimerTomo;  // Campo para la imagen del primer tomo

    public SerieDetallesDTO(Integer idSerie, String nombre, Integer totalTomos, String imagenPrimerTomo) {
        this.idSerie = idSerie;
        this.nombre = nombre;
        this.totalTomos = totalTomos;
        this.imagenPrimerTomo = imagenPrimerTomo;  // Asignación en el constructor
    }

    // Getters y setters
    public Integer getIdSerie() {
        return idSerie;
    }

    public void setIdSerie(Integer idSerie) {
        this.idSerie = idSerie;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getTotalTomos() {
        return totalTomos;
    }

    public void setTotalTomos(Integer totalTomos) {
        this.totalTomos = totalTomos;
    }

    public String getImagenPrimerTomo() {
        return imagenPrimerTomo;
    }

    public void setImagenPrimerTomo(String imagenPrimerTomo) {
        this.imagenPrimerTomo = imagenPrimerTomo;
    }
}
