package mk.coleccion.dto;

public class UsuarioColeccionDTO {
    private Integer totalMangas;
    private Integer totalSeries;
    private Integer seriesPorCompletar;
    private Integer seriesCompletadas;
    private Double porcentajeSeries;

    // Constructor, Getters y Setters
    public UsuarioColeccionDTO(Integer totalMangas, Integer totalSeries, Integer seriesPorCompletar, Integer seriesCompletadas, Double porcentajeSeries) {
        this.totalMangas = totalMangas;
        this.totalSeries = totalSeries;
        this.seriesPorCompletar = seriesPorCompletar;
        this.seriesCompletadas = seriesCompletadas;
        this.porcentajeSeries = porcentajeSeries;
    }

    // Getters y Setters
    public Integer getTotalMangas() { return totalMangas; }
    public void setTotalMangas(Integer totalMangas) { this.totalMangas = totalMangas; }

    public Integer getTotalSeries() { return totalSeries; }
    public void setTotalSeries(Integer totalSeries) { this.totalSeries = totalSeries; }

    public Integer getSeriesPorCompletar() { return seriesPorCompletar; }
    public void setSeriesPorCompletar(Integer seriesPorCompletar) { this.seriesPorCompletar = seriesPorCompletar; }

    public Integer getSeriesCompletadas() { return seriesCompletadas; }
    public void setSeriesCompletadas(Integer seriesCompletadas) { this.seriesCompletadas = seriesCompletadas; }

    public Double getPorcentajeSeries() { return porcentajeSeries; }
    public void setPorcentajeSeries(Double porcentajeSeries) { this.porcentajeSeries = porcentajeSeries; }
}


