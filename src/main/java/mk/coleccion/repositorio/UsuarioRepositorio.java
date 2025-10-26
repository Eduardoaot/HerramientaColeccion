package mk.coleccion.repositorio;

import mk.coleccion.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    Usuario findByUserEmailOrUserName(String emailName, String userName);

    boolean existsByUserEmailOrUserName(String email, String userName);

    // Método para actualizar el campo 'meta' del usuario por su id
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.userMeta = :meta WHERE u.idUsuario = :idUsuario")
    void actualizarMeta(@Param("idUsuario") int idUsuario, @Param("meta") String meta);

    @Query("SELECT u.userMeta FROM Usuario u WHERE u.idUsuario = :idUsuario")
    int getMetaById(@Param("idUsuario") int idUsuario);

    @Query(value = "SELECT " +
            "CASE WHEN id_estado_lectura = 3 THEN MONTH(reading_date) END AS mes_leido, " +
            "CASE WHEN id_estado_lectura = 3 THEN YEAR(reading_date) END AS anio_leido " +
            "FROM coleccion_manga " +
            "WHERE id_usuario = :idUsuario",
            nativeQuery = true)
    List<Object[]> obtenerMangasLecturaFecha(@Param("idUsuario") Integer idUsuario);


}

