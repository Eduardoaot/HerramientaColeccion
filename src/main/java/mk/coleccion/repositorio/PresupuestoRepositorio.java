package mk.coleccion.repositorio;

import mk.coleccion.modelo.Presupuestos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresupuestoRepositorio extends JpaRepository<Presupuestos, Integer> {

    @Query(value = "SELECT p.id_presupuesto, " +
            "p.name_budget, " +
            "p.id_usuario, " +
            "p.discount, " +
            "DATE_FORMAT(p.date_budget_created, '%Y-%m-%d') AS date_budget_created, " +
            "m.id_manga, " +
            "m.volume_number, " +
            "mi.manga_image_name, " +
            "m.manga_price AS precio, " +
            "s.serie_name " +
            "FROM presupuestos p " +
            "LEFT JOIN presupuestos_manga pm ON p.id_presupuesto = pm.id_presupuesto " +
            "LEFT JOIN manga m ON pm.id_manga = m.id_manga " +
            "LEFT JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "LEFT JOIN serie s ON m.id_serie = s.id_serie " +
            "WHERE p.id_usuario = :idUsuario " +
            "ORDER BY p.date_budget_created DESC",
            nativeQuery = true)
    List<Object[]> obtenerPresupuestosPorUsuario(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT " +
            "p.id_presupuesto, " +
            "p.name_budget, " +
            "p.id_usuario, " +
            "p.discount, " +
            "DATE_FORMAT(p.date_budget_created, '%Y-%m-%d') AS date_budget_created, " +
            "m.id_manga, " +
            "m.volume_number, " +
            "mi.manga_image_name, " +
            "m.manga_price AS precio, " +
            "s.serie_name " +
            "FROM presupuestos p " +
            "LEFT JOIN presupuestos_manga pm ON p.id_presupuesto = pm.id_presupuesto " +
            "LEFT JOIN manga m ON pm.id_manga = m.id_manga " +
            "LEFT JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "LEFT JOIN serie s ON m.id_serie = s.id_serie " +
            "WHERE p.id_presupuesto = :idPresupuesto",
            nativeQuery = true)
    List<Object[]> obtenerPresupuestoPorId(@Param("idPresupuesto") Integer idPresupuesto);


    @Query("DELETE FROM Presupuestos p WHERE p.idPresupuesto = :idPresupuesto")
    void eliminarPresupuestoPorId(@Param("idPresupuesto") Integer idPresupuesto);
}

