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
public class ColeccionManga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idColeccionManga;

    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    Usuario usuario2;

    @ManyToOne
    @JoinColumn(name = "idManga", nullable = false)
    Manga manga2;

    @ManyToOne
    @JoinColumn(name = "idEstadoLectura", nullable = false)
    EstadoLectura estadoLectura;

    Date addedMangaDate;

    Date ReadingDate;

    Float totalVolumenSave;
}
