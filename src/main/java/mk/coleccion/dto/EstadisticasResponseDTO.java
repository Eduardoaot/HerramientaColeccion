package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EstadisticasResponseDTO {
    private String response;
    private int totalComprados;
    private int totalCompradosAnio;
    private int totalMes;
    private Float valorTotal;
    private Float ahorroTotal;
    private Float gastoTotal;
    private List<MejorAhorroDTO> mejoresAhorros;
}

