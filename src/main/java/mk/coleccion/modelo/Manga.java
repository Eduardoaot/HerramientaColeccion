package mk.coleccion.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Manga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idManga;

    @ManyToOne
    @JoinColumn(name = "idSerie", nullable = false)
    Serie serie;

    Float volumeNumber;

    @ManyToOne
    @JoinColumn(name = "idDesc", nullable = false)
    DescripcionManga descripcionManga;

    @Column(name = "manga_price", nullable = false)
    Float mangaPrice;

    @ManyToOne
    @JoinColumn(name = "idMangaImagen", nullable = false)
    MangaImagen mangaImagen;

    Date mangaDate;

    @OneToMany(mappedBy = "manga", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PresupuestosManga> presupuestosMangas;

    @OneToMany(mappedBy = "manga2", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColeccionManga> coleccionMangas;
}
