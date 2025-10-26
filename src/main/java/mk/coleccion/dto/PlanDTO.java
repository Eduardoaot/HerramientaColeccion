package mk.coleccion.dto;

public class PlanDTO {
    private Integer idManga;
    private Float mangaNum;
    private String mangaImg;
    private Float precio;
    private String nombreSerie;

    // Constructor
    public PlanDTO(Integer idManga, Float mangaNum, String mangaImg, Float precio, String nombreSerie) {
        this.idManga = idManga;
        this.mangaNum = mangaNum;
        this.mangaImg = mangaImg;
        this.precio = precio;
        this.nombreSerie = nombreSerie;
    }

    // Getters and Setters
    public Integer getIdManga() {
        return idManga;
    }

    public void setIdManga(Integer idManga) {
        this.idManga = idManga;
    }

    public Float getMangaNum() {
        return mangaNum;
    }

    public void setMangaNum(Float mangaNum) {
        this.mangaNum = mangaNum;
    }

    public String getMangaImg() {
        return mangaImg;
    }

    public void setMangaImg(String mangaImg) {
        this.mangaImg = mangaImg;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public String getNombreSerie() {
        return nombreSerie;
    }

    public void setNombreSerie(String nombreSerie) {
        this.nombreSerie = nombreSerie;
    }
}

