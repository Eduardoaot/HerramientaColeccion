package mk.coleccion.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.File;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PresupuestosManga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idPresupuestoManga;

    @ManyToOne
    @JoinColumn(name = "idPresupuesto", nullable = false)
    Presupuestos presupuestos;

    @ManyToOne
    @JoinColumn(name = "idManga", nullable = false)
    Manga manga;
}


