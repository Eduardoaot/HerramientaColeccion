package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.Manga;
import mk.coleccion.repositorio.APP.MangaRepositorioAPP;
import mk.coleccion.repositorio.MangaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MangaServicioAPP implements IMangaServicioAPP {

    @Autowired
    private MangaRepositorioAPP mangaRepositorio;

    @Override
    public List<Manga> listarMangas() {
        return mangaRepositorio.findAll();
    }

    @Override
    public Manga buscarMangaPorId(Integer idManga) {
        return mangaRepositorio.findById(idManga).orElse(null);
    }

    @Override
    public List<Manga> buscarMangasPorSerie(Integer idSerie) {
        return mangaRepositorio.findBySerieIdSerie(idSerie);
    }

    @Override
    public Manga guardarManga(Manga manga) {
        return mangaRepositorio.save(manga);
    }

    @Override
    public void eliminarManga(Manga manga) {
        mangaRepositorio.delete(manga);
    }
}
