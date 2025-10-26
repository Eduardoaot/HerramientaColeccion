package mk.coleccion.dto;

public class SerieInfoDTO {

    private Integer id_serie;
    private String serieNom;
    private double seriePorcentaje;
    private String serieEstado;
    private String serieImagen;

    // Constructor
    public SerieInfoDTO(Integer id_serie, String serieNom, double seriePorcentaje, String serieEstado, String serieImagen) {
        this.id_serie = id_serie;
        this.serieNom = serieNom;
        this.seriePorcentaje = seriePorcentaje;
        this.serieEstado = serieEstado;
        this.serieImagen = serieImagen;
    }

    // Getters y setters
    public Integer getSerieId() { return id_serie; }
    public void setSerieId(Integer id_serie) { this.id_serie = id_serie; }

    public String getSerieNom() { return serieNom; }
    public void setSerieNom(String serieNom) { this.serieNom = serieNom; }

    public double getSeriePorcentaje() { return seriePorcentaje; }
    public void setSeriePorcentaje(double seriePorcentaje) { this.seriePorcentaje = seriePorcentaje; }

    public String getSerieEstado() { return serieEstado; }
    public void setSerieEstado(String serieEstado) { this.serieEstado = serieEstado; }

    public String getSerieImagen() { return serieImagen; }
    public void setSerieImagen(String serieImagen) { this.serieImagen = serieImagen; }
}

