package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.MangaImagen;
import mk.coleccion.repositorio.APP.MangaImagenRepositorioAPP;
import mk.coleccion.repositorio.MangaImagenRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MangaImagenServicioAPP implements IMangaImagenServicioAPP {

    @Autowired
    private MangaImagenRepositorioAPP mangaImagenRepositorio;

    @Override
    public MangaImagen buscarMangaImagenPorId(Integer idMangaImagen) {
        return mangaImagenRepositorio.findById(idMangaImagen).orElse(null);
    }

    @Override
    public MangaImagen guardarMangaImagen(MangaImagen mangaImagen) {
        return mangaImagenRepositorio.save(mangaImagen);
    }

    @Override
    public void eliminarMangaImagen(MangaImagen mangaImagen) {
        mangaImagenRepositorio.delete(mangaImagen);
    }
}
