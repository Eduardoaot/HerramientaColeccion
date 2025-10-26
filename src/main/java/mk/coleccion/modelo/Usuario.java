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
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer idUsuario;

    String userName;
    String userPassword;
    String userEmail;
    String fullName;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Presupuestos> presupuestos;

    @OneToMany(mappedBy = "usuario4", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MonetarioAhorros> monetarioAhorros;

    @OneToMany(mappedBy = "usuario2", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColeccionManga> coleccionMangas;

    String userMeta;
}
