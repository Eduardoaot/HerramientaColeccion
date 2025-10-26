package mk.coleccion.repositorio;

import mk.coleccion.modelo.MonetarioAhorros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonetarioAhorrosRepositorio extends JpaRepository<MonetarioAhorros, Integer> {

    @Query("SELECT COALESCE(SUM(cm.totalVolumenSave), 0) FROM ColeccionManga cm " +
            "WHERE cm.usuario2.idUsuario = :idUsuario")
    Float sumarTotalAhorrado(@Param("idUsuario") Integer idUsuario);


    // Repositorio: obtenemos solo los campos relevantes
    @Query("SELECT m.TotalVolumesSaved, m.TotalSavedBudget, m.SavingsDate FROM MonetarioAhorros m WHERE m.usuario4.idUsuario = :idUsuario ORDER BY m.TotalSavedBudget DESC")
    List<Object[]> obtenerMejoresAhorros(@Param("idUsuario") Integer idUsuario);


}

