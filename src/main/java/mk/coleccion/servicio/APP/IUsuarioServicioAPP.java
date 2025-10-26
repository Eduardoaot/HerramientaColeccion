package mk.coleccion.servicio.APP;

import mk.coleccion.modelo.Usuario;
import java.util.List;

public interface IUsuarioServicioAPP {
    List<Usuario> listarUsuarios();
    Usuario buscarUsuarioPorId(Integer idUsuario);
    Usuario guardarUsuario(Usuario usuario);
    void eliminarUsuario(Usuario usuario);
}
