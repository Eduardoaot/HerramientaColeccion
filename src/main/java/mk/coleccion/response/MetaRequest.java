package mk.coleccion.response;

public class MetaRequest {
    private int idUsuario;
    private String meta;

    // Constructor
    public MetaRequest(int idUsuario, String meta) {
        this.idUsuario = idUsuario;
        this.meta = meta;
    }

    // Getters y setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}

