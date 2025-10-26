package mk.coleccion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MangaPresupuestoDTO {
    private Integer idManga;
    private Float mangaNum;
    private String direccionMangaImg;
    private Float precio;
    private String serieNom;
}
