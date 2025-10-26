package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.Manga;
import java.util.List;

public interface IMangaServicioAPP {
    List<Manga> listarMangas();
    Manga buscarMangaPorId(Integer idManga);
    List<Manga> buscarMangasPorSerie(Integer idSerie);
    Manga guardarManga(Manga manga);
    void eliminarManga(Manga manga);
}
