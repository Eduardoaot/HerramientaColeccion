package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresupuestoResponseDTO {
    private Integer idPresupuesto;
    private String nombrePresupuesto;
    private Integer idUsuario;
    private Float descuento;
    private String fechaPresupuestoCreado;
    private List<MangaPresupuestoDTO> listaMangasPresupuesto;
}

