package mk.coleccion.dto;

public class MangaDTO {

    private Integer idManga;
    private Float mangaNum;
    private String direccionImagen;
    private Long estadoManga;

    // Constructor, getters y setters
    public MangaDTO(Integer idManga, Float mangaNum, String direccionImagen, Long estadoManga) {
        this.idManga = idManga;
        this.mangaNum = mangaNum;
        this.direccionImagen = direccionImagen;
        this.estadoManga = estadoManga;
    }
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

    public String getDireccionImagen() {
        return direccionImagen;
    }

    public void setDireccionImagen(String direccionImagen) {
        this.direccionImagen = direccionImagen;
    }

    public Long getestadoManga() {
        return estadoManga;
    }

    public void setEstadoManga(Long estadoManga) {
        this.estadoManga = estadoManga;
    }
}
