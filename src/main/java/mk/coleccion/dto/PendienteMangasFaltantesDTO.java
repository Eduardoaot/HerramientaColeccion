package mk.coleccion.dto;

import java.util.List;

public class PendienteMangasFaltantesDTO {
    private String result;
    private List<MangaFaltanteDTO> list;

    // Constructor
    public PendienteMangasFaltantesDTO(String result, List<MangaFaltanteDTO> list) {
        this.result = result;
        this.list = list;
    }

    // Getters y Setters
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<MangaFaltanteDTO> getList() {
        return list;
    }

    public void setList(List<MangaFaltanteDTO> list) {
        this.list = list;
    }

    // Clase interna MangaFaltanteDTO
    public static class MangaFaltanteDTO {
        private Integer idManga;
        private Float mangaNum;
        private String mangaImg;
        private Float precio;
        private String nombreSerie;  // Agregado el nombre de la serie

        // Constructor
        public MangaFaltanteDTO(Integer idManga, Float mangaNum, String mangaImg, Float precio, String nombreSerie) {
            this.idManga = idManga;
            this.mangaNum = mangaNum;
            this.mangaImg = mangaImg;
            this.precio = precio;
            this.nombreSerie = nombreSerie;  // Asignado en el constructor
        }

        // Getters y Setters
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
}
