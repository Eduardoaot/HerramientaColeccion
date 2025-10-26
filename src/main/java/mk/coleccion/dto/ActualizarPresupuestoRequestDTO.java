package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActualizarPresupuestoRequestDTO {
    private Integer idPresupuesto;   // ID del presupuesto a actualizar
    private String nombrePresupuesto;  // Nombre del presupuesto
    private Float descuento;           // Nuevo descuento
    private List<Integer> mangas_bolsa; // Nueva lista de IDs de mangas
}

