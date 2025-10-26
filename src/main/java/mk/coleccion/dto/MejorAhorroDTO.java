package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MejorAhorroDTO {
    private int top;
    private int totalMangas;
    private Float totalAhorrado;
    private String fechaAhorro;  // Nuevo campo para la fecha

    // Si deseas, puedes agregar un constructor sin fecha, pero el @AllArgsConstructor ya lo cubre.
}

