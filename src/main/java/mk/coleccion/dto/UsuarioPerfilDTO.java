package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioPerfilDTO {
    private String email;
    private String name;
    private String user;
}
