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
public class Serie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idSerie;

    String SerieName;

    @ManyToOne
    @JoinColumn(name = "idDescripcionSerie", nullable = false)
    private DescripcionSerie descripcionSerie;


    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Manga> mangas;

    String authorName;
    Integer SerieTotals;

}
