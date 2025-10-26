package mk.coleccion.repositorio;

import jakarta.transaction.Transactional;
import mk.coleccion.modelo.PresupuestosManga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresupuestoMangaRepositorio extends JpaRepository<PresupuestosManga, Integer> {

    @Query(value = "SELECT " +
            "p.id_presupuesto AS idPresupuesto, " +
            "p.name_budget AS nombrePresupuesto, " +
            "m.volume_number AS mangaNum, " +
            "s.serie_name AS serieNom, " +
            "m.manga_price AS precio " +
            "FROM presupuestos_manga pm " +
            "INNER JOIN presupuestos p ON pm.id_presupuesto = p.id_presupuesto " +
            "INNER JOIN usuario u ON p.id_usuario = u.id_usuario " +
            "INNER JOIN manga m ON pm.id_manga = m.id_manga " +
            "INNER JOIN serie s ON m.id_serie = s.serie " +
            "WHERE u.id_usuario = :idUsuario " +
            "AND p.id_presupuesto = :idPresupuesto", nativeQuery = true)
    List<Object[]> findDetallesPresupuestoManga(@Param("idUsuario") Integer idUsuario, @Param("idPresupuesto") Integer idPresupuesto);

    @Modifying
    @Transactional
    @Query("DELETE FROM PresupuestosManga pm WHERE pm.presupuestos.idPresupuesto = :idPresupuesto")
    void eliminarPorIdPresupuesto(@Param("idPresupuesto") Integer idPresupuesto);

    @Modifying
    @Query("DELETE FROM PresupuestosManga pm WHERE pm.presupuestos.idPresupuesto = :idPresupuesto")
    void deleteAllByIdPresupuesto(@Param("idPresupuesto") Integer idPresupuesto);

}
