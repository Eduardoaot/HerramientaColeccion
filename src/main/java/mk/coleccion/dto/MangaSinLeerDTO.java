package mk.coleccion.dto;

public class MangaSinLeerDTO {

    private Integer idManga;
    private String serieNom;
    private Float mangaSum;
    private String direccionManga;


    // Constructor con parámetros
    public MangaSinLeerDTO(Integer idManga, String serieNom, Float mangaSum, String direccionManga) {
        this.idManga = idManga;
        this.serieNom = serieNom;
        this.mangaSum = mangaSum;
        this.direccionManga = direccionManga;
    }

    // Getters y Setters
    public Integer getIdManga() {
        return idManga;
    }

    public void setIdManga(Integer idManga) {
        this.idManga = idManga;
    }

    public String getSerieNom() {
        return serieNom;
    }

    public void setSerieNom(String serieNom) {
        this.serieNom = serieNom;
    }

    public Float getMangaSum() {
        return mangaSum;
    }

    public void setMangaSum(Float mangaSum) {
        this.mangaSum = mangaSum;
    }

    public String getDireccionManga() {
        return direccionManga;
    }

    public void setDireccionManga(String direccionManga) {
        this.direccionManga = direccionManga;
    }
}
