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
public class Presupuestos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idPresupuesto;

    @ManyToOne
    @JoinColumn(name = "idUsuario", nullable = false)
    Usuario usuario;

    String nameBudget;

    Float discount;

    @OneToMany(mappedBy = "presupuestos", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PresupuestosManga> presupuestosMangas;

    Date DateBudgetCreated;
}


