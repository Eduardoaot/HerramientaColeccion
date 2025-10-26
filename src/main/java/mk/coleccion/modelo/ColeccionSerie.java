package mk.coleccion.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ColeccionSerie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idColeccionSerie;

    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    Usuario usuario3;

    @ManyToOne
    @JoinColumn(name = "idSerie", nullable = false)
    Serie Serie2;

    @ManyToOne
    @JoinColumn(name = "idEstadoSerie", nullable = false)
    EstadoSerie estadoSerie;

    Date serieAddedDate;

}
