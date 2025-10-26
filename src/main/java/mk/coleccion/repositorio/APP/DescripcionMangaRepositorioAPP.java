package mk.coleccion.repositorio.APP;

import mk.coleccion.modelo.DescripcionManga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DescripcionMangaRepositorioAPP extends JpaRepository<DescripcionManga, Integer> {
}
