package mk.coleccion.dto;

public class MangaPendienteDTO {
    private Integer idManga;
    private float mangaNum;
    private String direccionMangaImg;

    // Constructor
    public MangaPendienteDTO(Integer idManga, float mangaNum, String direccionMangaImg) {
        this.idManga = idManga;
        this.mangaNum = mangaNum;
        this.direccionMangaImg = direccionMangaImg;
    }

    // Getters y Setters
    public Integer getIdManga() {
        return idManga;
    }

    public void setIdManga(Integer idManga) {
        this.idManga = idManga;
    }

    public float getMangaNum() {
        return mangaNum;
    }

    public void setMangaNum(float mangaNum) {
        this.mangaNum = mangaNum;
    }

    public String getDireccionMangaImg() {
        return direccionMangaImg;
    }

    public void setDireccionMangaImg(String direccionMangaImg) {
        this.direccionMangaImg = direccionMangaImg;
    }
}
