package mk.coleccion.repositorio;

import jakarta.transaction.Transactional;
import mk.coleccion.modelo.Manga;
import mk.coleccion.modelo.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MangaRepositorio extends JpaRepository<Manga, Integer> {

        @Modifying
        @Transactional
        @Query(value = "UPDATE coleccion_manga SET id_estado_lectura = :estadoLectura, reading_date = NOW() WHERE id_manga = :idManga AND id_usuario = :idUsuario; ", nativeQuery = true)
        int actualizarEstadoLectura(@Param("estadoLectura") Integer estadoLectura,
                                    @Param("idManga") Integer idManga,
                                    @Param("idUsuario") Integer idUsuario);


    // La consulta existente para obtener los detalles del manga
    @Query(value = "SELECT " +
            "s.serie_name AS titulo_serie, " +
            "m.volume_number AS numero_manga, " +
            "IFNULL(el.reading_status, 'No definido') AS reading_status, " +
            "IFNULL(dm.description_Manga, 'Sin descripción') AS descripcion, " +
            "s.author_name AS nombre_autor, " +
            "mi.manga_image_name AS imagen_manga, " +
            "m.manga_price AS precio_manga, " +  // Cambio aquí
            "CASE " +
            "WHEN cm.id_usuario IS NOT NULL THEN TRUE " +
            "ELSE FALSE " +
            "END AS EstadoAgregado " +
            "FROM manga m " +
            "JOIN serie s ON m.id_serie = s.id_serie " +
            "JOIN descripcion_manga dm ON m.id_desc = dm.id_descripcion_manga " +
            "LEFT JOIN estado_lectura el ON el.id_estado_lectura = " +
            "(SELECT id_estado_lectura FROM coleccion_manga WHERE id_manga = m.id_manga AND id_usuario = :idUsuario LIMIT 1) " +
            "LEFT JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "LEFT JOIN coleccion_manga cm ON cm.id_manga = m.id_manga AND cm.id_usuario = :idUsuario " +
            "WHERE m.id_manga = :idManga " +
            "LIMIT 1", nativeQuery = true)
    List<Object[]> buscarDetallesManga(@Param("idManga") Integer idManga, @Param("idUsuario") Integer idUsuario);



    @Query(value = "SELECT " +
            "m.id_manga, " +
            "s.serie_name AS titulo_serie, " +
            "m.volume_number AS numero_manga, " +
            "mi.manga_image_name AS imagen_manga " +
            "FROM coleccion_manga cm " +
            "JOIN manga m ON cm.id_manga = m.id_manga " +
            "JOIN serie s ON m.id_serie = s.id_serie " +
            "JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "WHERE cm.id_usuario = :idUsuario AND cm.id_estado_lectura = 2", nativeQuery = true)
    List<Object[]> obtenerMangasSinLeer(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT " +
            "m.id_manga, " +
            "m.volume_number AS numero_manga, " +
            "mi.manga_image_name AS imagen_manga " +
            "FROM coleccion_manga cm " +
            "JOIN manga m ON cm.id_manga = m.id_manga " +
            "JOIN serie s ON m.id_serie = s.id_serie " +
            "JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "WHERE cm.id_usuario = :idUsuario AND cm.id_estado_lectura = 1 " +
            "ORDER BY cm.added_manga_date ASC", nativeQuery = true)
    List<Object[]> obtenerMangasPendientes(@Param("idUsuario") Integer idUsuario);


    @Query(value = "SELECT " +
            "COUNT(CASE WHEN cm.id_estado_lectura = 3 THEN 1 END) AS total_mangas_estado_3, " +
            "COUNT(CASE WHEN cm.id_estado_lectura = 3 AND MONTH(cm.reading_date) = MONTH(CURRENT_DATE) AND YEAR(cm.reading_date) = YEAR(CURRENT_DATE) THEN 1 END) AS mangas_leidos_mes_actual, " +
            "COUNT(CASE WHEN cm.id_estado_lectura = 3 AND YEAR(cm.reading_date) = YEAR(CURRENT_DATE) THEN 1 END) AS mangas_leidos_ano_actual " +
            "FROM coleccion_manga cm " +
            "WHERE cm.id_usuario = :idUsuario " +
            "AND cm.id_estado_lectura = 3", nativeQuery = true)
    List<Object[]> obtenerEstadisticasLecturaMangas(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT " +
            "MONTH(added_manga_date) AS mes_agregado, " +
            "YEAR(added_manga_date) AS anio_agregado, " +
            "CASE WHEN id_estado_lectura = 3 THEN MONTH(reading_date) END AS mes_leido, " +
            "CASE WHEN id_estado_lectura = 3 THEN YEAR(reading_date) END AS anio_leido " +
            "FROM coleccion_manga " +
            "WHERE id_usuario = :idUsuario",
            nativeQuery = true)
    List<Object[]> obtenerMangasFecha(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT " +
            "m.id_manga, " +
            "m.volume_number, " +
            "mi.manga_image_name, " +
            "m.manga_price AS precio, " +  // Cambio aquí
            "s.serie_name, " +
            "CASE " +
            "WHEN cm.id_usuario IS NOT NULL THEN TRUE " +
            "ELSE FALSE " +
            "END AS EstadoAgregado " +
            "FROM manga m " +
            "JOIN coleccion_serie cs ON m.id_serie = cs.id_serie " +
            "LEFT JOIN coleccion_manga cm ON m.id_manga = cm.id_manga AND cm.id_usuario = cs.id_usuario " +
            "JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "JOIN serie s ON m.id_serie = s.id_serie " +
            "WHERE cs.id_usuario = :idUsuario " +
            "AND cm.id_manga IS NULL " +
            "ORDER BY m.manga_date DESC", nativeQuery = true)
    List<Object[]> pendienteBuscarMangasFaltantes(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT " +
            "m.id_manga, " +
            "m.volume_number, " +
            "mi.manga_image_name, " +
            "s.serie_name, " +
            "m.manga_price AS precio " +  // Cambio aquí
            "FROM manga m " +
            "JOIN serie s ON m.id_serie = s.id_serie " +
            "JOIN manga_imagen mi ON m.id_manga_imagen = mi.id_manga_imagen " +
            "WHERE m.id_manga IN (:listaIds)", nativeQuery = true)
    List<Object[]> buscarMangasConListasDeId(@Param("listaIds") List<Integer> listaIds);


    Optional<Manga> findById(Integer idManga);

}
