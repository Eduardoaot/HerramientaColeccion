package mk.coleccion.repositorio.APP;

import mk.coleccion.modelo.MangaImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaImagenRepositorioAPP extends JpaRepository<MangaImagen, Integer> {
}
