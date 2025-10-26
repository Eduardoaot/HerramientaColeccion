package mk.coleccion.repositorio;

import mk.coleccion.modelo.PresupuestosManga;
import mk.coleccion.modelo.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SerieRepositorio extends JpaRepository<Serie, Integer> {  // Cambio aquí a Integer

    @Query(value = "SELECT " +
            "s.id_serie, " +                    // idSerie
            "s.serie_name, " +                   // nombre
            "s.serie_totals, " +                   // totalTomos
            "mi.manga_image_name " +         // imagenPrimerTomo
            "FROM serie s " +
            "JOIN manga m ON s.id_serie = m.id_serie " +
            "JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "WHERE LOWER(s.serie_name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND m.volume_number = 1", nativeQuery = true)
    List<Object[]> buscarSeriesConImagenYTotalTomos(@Param("query") String query);

    @Query(value = "SELECT " +
            "COUNT(cm.id_manga) AS total_mangas_en_coleccion, " +
            "COUNT(CASE WHEN cm.id_estado_lectura IN (1, 2) THEN 1 END) AS total_mangas_sin_leer, " +
            "s.serie_name AS nombre_serie, " +
            "s.serie_totals AS total_mangas_serie, " +
            "m.id_manga, " +
            "m.volume_number, " +
            "mi.manga_image_name, " +
            "ds.description_serie AS description_serie, " +
            "s.author_name AS autor_serie " +
            "FROM manga m " +
            "JOIN serie s ON m.id_serie = s.id_serie " +
            "JOIN descripcion_serie ds ON s.id_descripcion_serie = ds.id_descripcion_serie " +
            "LEFT JOIN coleccion_manga cm ON m.id_manga = cm.id_manga AND cm.id_usuario = :idUsuario " +
            "LEFT JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "WHERE s.id_serie = :idSerie " +
            "GROUP BY s.id_serie, ds.description_serie, s.author_name, s.serie_name, s.serie_totals, m.id_manga, m.volume_number, mi.manga_image_name " +
            "ORDER BY m.volume_number ASC", nativeQuery = true)
    List<Object[]> obtenerDetallesSerieConMangas(
            @Param("idSerie") Integer idSerie,
            @Param("idUsuario") Integer idUsuario
    );


}

