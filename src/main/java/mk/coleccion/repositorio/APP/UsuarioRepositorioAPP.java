package mk.coleccion.repositorio.APP;

import mk.coleccion.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepositorioAPP extends JpaRepository<Usuario, Integer> {
}
