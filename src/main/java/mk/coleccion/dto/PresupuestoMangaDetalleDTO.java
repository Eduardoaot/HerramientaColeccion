package mk.coleccion.dto;

public class PresupuestoMangaDetalleDTO {
    private Integer idPresupuesto;
    private String nombrePresupuesto;
    private String mangaNum;
    private String serieNom;
    private Float precio;

    // Constructor
    public PresupuestoMangaDetalleDTO(Integer idPresupuesto, String nombrePresupuesto, String mangaNum, String serieNom, Float precio) {
        this.idPresupuesto = idPresupuesto;
        this.nombrePresupuesto = nombrePresupuesto;
        this.mangaNum = mangaNum;
        this.serieNom = serieNom;
        this.precio = precio;
    }

    // Getters y setters
    public Integer getIdPresupuesto() {
        return idPresupuesto;
    }

    public void setIdPresupuesto(Integer idPresupuesto) {
        this.idPresupuesto = idPresupuesto;
    }

    public String getNombrePresupuesto() {
        return nombrePresupuesto;
    }

    public void setNombrePresupuesto(String nombrePresupuesto) {
        this.nombrePresupuesto = nombrePresupuesto;
    }

    public String getMangaNum() {
        return mangaNum;
    }

    public void setMangaNum(String mangaNum) {
        this.mangaNum = mangaNum;
    }

    public String getSerieNom() {
        return serieNom;
    }

    public void setSerieNom(String serieNom) {
        this.serieNom = serieNom;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }
}