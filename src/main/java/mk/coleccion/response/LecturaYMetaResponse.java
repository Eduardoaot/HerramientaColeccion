package mk.coleccion.response;

public class LecturaYMetaResponse {
    private Integer mangasLeidosMes;
    private Integer meta;

    // Constructor
    public LecturaYMetaResponse(Integer mangasLeidosMes, Integer meta) {
        this.mangasLeidosMes = mangasLeidosMes;
        this.meta = meta;
    }

    // Getters y Setters
    public Integer getMangasLeidosMes() {
        return mangasLeidosMes;
    }

    public void setMangasLeidosMes(Integer mangasLeidosMes) {
        this.mangasLeidosMes = mangasLeidosMes;
    }

    public Integer getMeta() {
        return meta;
    }

    public void setMeta(Integer meta) {
        this.meta = meta;
    }
}
