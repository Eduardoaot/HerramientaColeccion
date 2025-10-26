package mk.coleccion.response;

public class UsuarioResponse {
    private String message;
    private Integer userId;

    public UsuarioResponse(String message, Integer userId) {
        this.message = message;
        this.userId = userId;
    }

    // Getters y setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}


