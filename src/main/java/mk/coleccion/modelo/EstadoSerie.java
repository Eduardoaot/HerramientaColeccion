package mk.coleccion.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EstadoSerie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idEstadoSerie;

    String SeriesStatus;

    @OneToMany(mappedBy = "estadoSerie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColeccionSerie> coleccionSerie;

}
