package mk.coleccion.servicio;

import mk.coleccion.modelo.Usuario;
import mk.coleccion.response.LecturaYMetaResponse;

public interface IUsuarioServicio {

    public Usuario guardarUsuario(Usuario usuario);

    public Usuario autenticarUsuario(String email, String password);

    public void actualizarMeta(int idUsuario, String meta);

    public LecturaYMetaResponse obtenerLecturaYMeta(int idUsuario);
}
