package mk.coleccion.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EstadoLectura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idEstadoLectura;

    String ReadingStatus;

    @OneToMany(mappedBy = "estadoLectura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColeccionManga> coleccionMangas;
}
