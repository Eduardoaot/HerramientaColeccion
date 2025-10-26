package mk.coleccion.repositorio;

import mk.coleccion.modelo.MangaImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MangaImagenRepositorio extends JpaRepository<MangaImagen, Integer> {
    Optional<MangaImagen> findByMangaImageName(String mangaImageName); // Buscar por nombre del archivo
}
