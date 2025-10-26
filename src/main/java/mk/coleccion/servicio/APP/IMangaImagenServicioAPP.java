package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.MangaImagen;

public interface IMangaImagenServicioAPP {
    MangaImagen buscarMangaImagenPorId(Integer idMangaImagen);
    MangaImagen guardarMangaImagen(MangaImagen mangaImagen);
    void eliminarMangaImagen(MangaImagen mangaImagen);
}
