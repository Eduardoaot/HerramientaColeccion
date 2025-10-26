package mk.coleccion.dto;

import java.sql.Blob;
import java.util.Date;

public class ColeccionMangaDetalleDTO {
    private Integer idUsuario;
    private Integer idManga;
    private Date mangaDate;
    private String mangaImg;
    private Float mangaNum;
    private String estado;
    private Float precio;
    private String serieNom;

    // Constructor
    public ColeccionMangaDetalleDTO(Integer idUsuario, Integer idManga, Date mangaDate, String mangaImg, Float mangaNum, String descripcion, Float precio, String serieNom) {
        this.idUsuario = idUsuario;
        this.idManga = idManga;
        this.mangaDate = mangaDate;
        this.mangaImg = mangaImg;
        this.mangaNum = mangaNum;
        this.estado = descripcion;
        this.precio = precio;
        this.serieNom = serieNom;
    }

    // Getters y setters
    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdManga() {
        return idManga;
    }

    public void setIdManga(Integer idManga) {
        this.idManga = idManga;
    }

    public Date getMangaDate() {
        return mangaDate;
    }

    public void setMangaDate(Date mangaDate) {
        this.mangaDate = mangaDate;
    }

    public String getMangaImg() {
        return mangaImg;
    }

    public void setMangaImg(String mangaImg) {
        this.mangaImg = mangaImg;
    }

    public Float getMangaNum() {
        return mangaNum;
    }

    public void setMangaNum(Float mangaNum) {
        this.mangaNum = mangaNum;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public String getSerieNom() {
        return serieNom;
    }

    public void setSerieNom(String serieNom) {
        this.serieNom = serieNom;
    }
}