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
public class DescripcionSerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idDescripcionSerie;

    @Lob
    @Column(name = "description_serie", columnDefinition = "TEXT")
    String descriptionSerie;

    @OneToMany(mappedBy = "descripcionSerie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Serie> series;
}
