package mk.coleccion.dto;

import java.util.List;

public class SerieDetallesTotalDTO {

    private Long totalMangasEnColeccion; // Total mangas en la colección
    private Long totalMangasSinLeer; // Total mangas sin leer
    private String nombreSerie; // Nombre de la serie
    private Integer totalMangaSerie; // Total mangas de la serie
    private List<MangaDTO> listaMangas; // Lista de mangas (DTO para detalles de cada manga)
    private String descripcionSerie; // Descripción de la serie
    private String autorSerie; // Autor de la serie
    private Float porcentajeCompletado; // Porcentaje completado
    private Float porcentajePorLeer; // Porcentaje por leer

    // Getters y setters
    public Long getTotalMangasEnColeccion() {
        return totalMangasEnColeccion;
    }

    public void setTotalMangasEnColeccion(Long totalMangasEnColeccion) {
        this.totalMangasEnColeccion = totalMangasEnColeccion;
    }

    public Long getTotalMangasSinLeer() {
        return totalMangasSinLeer;
    }

    public void setTotalMangasSinLeer(Long totalMangasSinLeer) {
        this.totalMangasSinLeer = totalMangasSinLeer;
    }

    public String getNombreSerie() {
        return nombreSerie;
    }

    public void setNombreSerie(String nombreSerie) {
        this.nombreSerie = nombreSerie;
    }

    public Integer getTotalMangaSerie() {
        return totalMangaSerie;
    }

    public void setTotalMangaSerie(Integer totalMangaSerie) {
        this.totalMangaSerie = totalMangaSerie;
    }

    public List<MangaDTO> getListaMangas() {
        return listaMangas;
    }

    public void setListaMangas(List<MangaDTO> listaMangas) {
        this.listaMangas = listaMangas;
    }

    public String getDescripcionSerie() {
        return descripcionSerie;
    }

    public void setDescripcionSerie(String descripcionSerie) {
        this.descripcionSerie = descripcionSerie;
    }

    public String getAutorSerie() {
        return autorSerie;
    }

    public void setAutorSerie(String autorSerie) {
        this.autorSerie = autorSerie;
    }

    public Float getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    public void setPorcentajeCompletado(Float porcentajeCompletado) {
        this.porcentajeCompletado = porcentajeCompletado;
    }

    public Float getPorcentajePorLeer() {
        return porcentajePorLeer;
    }

    public void setPorcentajePorLeer(Float porcentajePorLeer) {
        this.porcentajePorLeer = porcentajePorLeer;
    }
}
