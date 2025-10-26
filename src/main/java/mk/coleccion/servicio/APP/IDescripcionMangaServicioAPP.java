package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.DescripcionManga;

public interface IDescripcionMangaServicioAPP {
    DescripcionManga buscarDescripcionMangaPorId(Integer idDescripcionManga);
    DescripcionManga guardarDescripcionManga(DescripcionManga descripcionManga);
    void eliminarDescripcionManga(DescripcionManga descripcionManga);
}
