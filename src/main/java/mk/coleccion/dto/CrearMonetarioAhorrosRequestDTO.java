package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CrearMonetarioAhorrosRequestDTO {
    private Integer idUsuario;
    private Float totalAhorrado;
    private Integer totalMangas;
}

