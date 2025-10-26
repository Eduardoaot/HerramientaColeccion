package mk.coleccion.servicio;

import mk.coleccion.modelo.PresupuestosManga;

import java.util.List;

public interface IPresupuestoMangaServicio {

    public void guardarPresupuestoManga(PresupuestosManga presupuestosManga);

    public void eliminarPresupuestoMangaPorId(Integer idPresupuestoManga);
}
