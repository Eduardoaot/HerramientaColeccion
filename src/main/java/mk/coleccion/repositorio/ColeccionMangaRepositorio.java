package mk.coleccion.repositorio;

import jakarta.transaction.Transactional;
import mk.coleccion.modelo.ColeccionManga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ColeccionMangaRepositorio extends JpaRepository<ColeccionManga, Integer> {

    @Query(value = "SELECT " +
            "cm.id_usuario AS idUsuario, " +
            "m.id_manga AS idManga, " +
            "m.manga_date AS mangaDate, " +
            "mi.manga_image_name AS mangaImageName, " +
            "m.volume_number AS mangaNum, " +
            "el.reading_status AS estado, " +
            "m.manga_price AS precio, " + // <-- aquí cambio
            "s.serie_name AS serieNom " +
            "FROM coleccion_manga cm " +
            "INNER JOIN manga m ON cm.id_manga = m.id_manga " +
            "INNER JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "INNER JOIN estado_lectura el ON cm.id_estado_lectura = el.id_estado_lectura " +
            "INNER JOIN serie s ON m.id_serie = s.id_serie " +
            "WHERE cm.id_usuario = :idUsuario " +
            "ORDER BY cm.added_manga_date DESC;", nativeQuery = true)
    List<Object[]> findDetallesColeccionManga(@Param("idUsuario") Integer idUsuario);


    @Query(value = "SELECT " +
            "s.id_serie AS idSerie, " +
            "s.serie_name AS serieNom, " +
            "s.serie_totals AS serieTot, " +
            "cs.id_estado_serie AS estadoSerie, " +
            "COUNT(cm.id_manga) AS tomosUsuario, " +
            "(SELECT mi.manga_image_name " +
            " FROM manga m_img " +
            " INNER JOIN manga_imagen mi ON m_img.id_manga_imagen = mi.id_manga_imagen " +
            " WHERE m_img.id_serie = s.id_serie AND m_img.volume_number = 1 " +
            " LIMIT 1) AS serieImagen " +
            "FROM coleccion_manga cm " +
            "INNER JOIN manga m ON cm.id_manga = m.id_manga " +
            "INNER JOIN serie s ON m.id_serie = s.id_serie " +
            "LEFT JOIN coleccion_serie cs ON cs.id_serie = s.id_serie AND cs.id_usuario = cm.id_usuario " +
            "WHERE cm.id_usuario = :idUsuario " +
            "GROUP BY s.id_serie, s.serie_name, s.serie_totals, cs.id_estado_serie, cs.serie_added_date " +
            "ORDER BY cs.serie_added_date DESC",
            nativeQuery = true)
    List<Object[]> findSeriesAndTotalByUserId(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT " +
            "COUNT(cm.id_manga) AS totalMangas, " +
            "COUNT(DISTINCT s.id_serie) AS totalSeries, " +
            "COUNT(DISTINCT CASE WHEN cs.id_estado_serie = 1 THEN s.id_serie END) AS seriesPorCompletar, " +
            "COUNT(DISTINCT CASE WHEN cs.id_estado_serie IN (3, 4) THEN s.id_serie END) AS seriesCompletadas " +
            "FROM coleccion_manga cm " +
            "INNER JOIN manga m ON cm.id_manga = m.id_manga " +
            "INNER JOIN serie s ON m.id_serie = s.id_serie " +
            "LEFT JOIN coleccion_serie cs ON cs.id_serie = s.id_serie AND cs.id_usuario = cm.id_usuario " +
            "WHERE cm.id_usuario = :idUsuario",
            nativeQuery = true)
    List<Object[]> findMangasAndSeriesByUserId(@Param("idUsuario") Integer idUsuario);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM coleccion_manga " +
            "WHERE id_manga = :idManga AND id_usuario = :idUsuario",
            nativeQuery = true)
    void eliminarMangaDeColeccion(@Param("idManga") Integer idManga, @Param("idUsuario") Integer idUsuario);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM coleccion_serie " +
            "WHERE id_serie = (SELECT id_serie FROM manga WHERE id_manga = :idManga) " +
            "AND id_usuario = :idUsuario " +
            "AND NOT EXISTS ( " +
            "    SELECT 1 FROM coleccion_manga cm " +
            "    JOIN manga m ON cm.id_manga = m.id_manga " +
            "    WHERE m.id_serie = (SELECT id_serie FROM manga WHERE id_manga = :idManga) " +
            "    AND cm.id_usuario = :idUsuario)",
            nativeQuery = true)
    void eliminarSerieSiNoTieneMangas(@Param("idManga") Integer idManga, @Param("idUsuario") Integer idUsuario);

    @Modifying
    @Transactional
    @Query(value = "UPDATE coleccion_serie cs " +
            "SET cs.id_estado_serie = 1 " +
            "WHERE cs.id_serie = (SELECT m.id_serie FROM manga m WHERE m.id_manga = :idManga) " +
            "AND cs.id_usuario = :idUsuario " +
            "AND cs.id_estado_serie = 4", nativeQuery = true)
    void actualizarEstadoSerieSiEra4(@Param("idManga") Integer idManga, @Param("idUsuario") Integer idUsuario);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO coleccion_manga (id_estado_lectura, id_manga, id_usuario, added_manga_date, total_volumen_save) " +
            "SELECT 1, :idManga, :idUsuario, NOW(), :precio " +
            "FROM DUAL " +
            "WHERE NOT EXISTS ( " +
            "    SELECT 1 FROM coleccion_manga cm " +
            "    WHERE cm.id_manga = :idManga AND cm.id_usuario = :idUsuario" +
            ")",
            nativeQuery = true)
    void agregarMangaAColeccion(@Param("idManga") Integer idManga,
                                @Param("idUsuario") Integer idUsuario,
                                @Param("precio") Float precio);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO coleccion_serie (serie_added_date, id_serie, id_estado_serie, id_usuario) " +
            "SELECT NOW(), s.id_serie, " +
            "       CASE " +
            "           WHEN s.serie_totals = 1 THEN 3 " +
            "           ELSE 1 " +
            "       END, :idUsuario " +
            "FROM serie s " +
            "WHERE s.id_serie = (SELECT id_serie FROM manga WHERE id_manga = :idManga) " +
            "  AND NOT EXISTS ( " +
            "      SELECT 1 FROM coleccion_serie " +
            "      WHERE id_serie = s.id_serie AND id_usuario = :idUsuario" +
            "  )",
            nativeQuery = true)
    void agregarSerieSiNoExiste(@Param("idManga") Integer idManga, @Param("idUsuario") Integer idUsuario);

    @Modifying
    @Transactional
    @Query(value = "UPDATE coleccion_serie " +
            "SET id_estado_serie = 4 " +
            "WHERE id_serie = ( " +
            "    SELECT id_serie " +
            "    FROM manga " +
            "    WHERE id_manga = :idManga " +
            ") " +
            "AND id_usuario = :idUsuario " +
            "AND EXISTS ( " +
            "    SELECT 1 " +
            "    FROM coleccion_manga cm " +
            "    WHERE cm.id_usuario = :idUsuario " +
            "      AND cm.id_manga IN ( " +
            "          SELECT id_manga " +
            "          FROM manga " +
            "          WHERE id_serie = (SELECT id_serie FROM manga WHERE id_manga = :idManga) " +
            "      ) " +
            "    GROUP BY cm.id_usuario " +
            "    HAVING COUNT(cm.id_manga) = ( " +
            "        SELECT serie_totals " +
            "        FROM serie " +
            "        WHERE id_serie = (SELECT id_serie FROM manga WHERE id_manga = :idManga) " +
            "    ) " +
            ") " +
            "AND id_estado_serie NOT IN (2, 3)",
            nativeQuery = true)
    void actualizarEstadoSerie(@Param("idManga") Integer idManga, @Param("idUsuario") Integer idUsuario);


    @Query("SELECT COUNT(cm) FROM ColeccionManga cm " +
            "JOIN cm.manga2 m " +
            "WHERE cm.usuario2.idUsuario = :idUsuario " +
            "AND (cm.totalVolumenSave IS NULL OR cm.totalVolumenSave <> m.mangaPrice)")
    int contarPorUsuario(@Param("idUsuario") Integer idUsuario);


    @Query("SELECT COUNT(cm) FROM ColeccionManga cm " +
            "JOIN cm.manga2 m " +
            "WHERE cm.usuario2.idUsuario = :idUsuario " +
            "AND YEAR(cm.addedMangaDate) = :anio " +
            "AND (cm.totalVolumenSave IS NULL OR cm.totalVolumenSave <> m.mangaPrice)")
    int contarPorUsuarioEnAnio(@Param("idUsuario") Integer idUsuario, @Param("anio") int anio);

    @Query("SELECT COUNT(cm) FROM ColeccionManga cm " +
            "JOIN cm.manga2 m " +
            "WHERE cm.usuario2.idUsuario = :idUsuario " +
            "AND YEAR(cm.addedMangaDate) = :anio " +
            "AND MONTH(cm.addedMangaDate) = :mes " +
            "AND (cm.totalVolumenSave IS NULL OR cm.totalVolumenSave <> m.mangaPrice)")
    int contarPorUsuarioEnMes(@Param("idUsuario") Integer idUsuario, @Param("anio") int anio, @Param("mes") int mes);

    @Query("SELECT COALESCE(SUM(m.mangaPrice), 0) FROM ColeccionManga cm " +
            "JOIN cm.manga2 m " +
            "WHERE cm.usuario2.idUsuario = :idUsuario")
    Float sumarPrecioMangasPorUsuario(@Param("idUsuario") Integer idUsuario);

}