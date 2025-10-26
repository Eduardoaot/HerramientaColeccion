package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.DescripcionManga;
import mk.coleccion.repositorio.APP.DescripcionMangaRepositorioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DescripcionMangaServicioAPP implements IDescripcionMangaServicioAPP {

    @Autowired
    private DescripcionMangaRepositorioAPP descripcionMangaRepositorio;

    @Override
    public DescripcionManga buscarDescripcionMangaPorId(Integer idDescripcionManga) {
        return descripcionMangaRepositorio.findById(idDescripcionManga).orElse(null);
    }

    @Override
    public DescripcionManga guardarDescripcionManga(DescripcionManga descripcionManga) {
        return descripcionMangaRepositorio.save(descripcionManga);
    }

    @Override
    public void eliminarDescripcionManga(DescripcionManga descripcionManga) {
        descripcionMangaRepositorio.delete(descripcionManga);
    }
}
