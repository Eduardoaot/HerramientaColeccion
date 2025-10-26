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
public class DescripcionManga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idDescripcionManga;

    @Lob
    @Column(name = "description_Manga", columnDefinition = "TEXT")
    String descriptionManga;

    @OneToMany(mappedBy = "descripcionManga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Manga> mangas;
}
