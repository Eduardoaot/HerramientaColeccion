package mk.coleccion.servicio;

import mk.coleccion.dto.UsuarioPerfilDTO;
import mk.coleccion.modelo.Usuario;
import mk.coleccion.repositorio.UsuarioRepositorio;
import mk.coleccion.response.LecturaYMetaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioServicio implements IUsuarioServicio{

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    public UsuarioPerfilDTO obtenerPerfilPorId(int idUsuario) {
        // Buscar el usuario por id
        Usuario usuario = usuarioRepositorio.findById(idUsuario).orElse(null);
        if (usuario != null) {
            // Mapear los datos a un DTO
            return new UsuarioPerfilDTO(usuario.getUserEmail(), usuario.getFullName(), usuario.getUserName());
        }
        return null; // O retornar una respuesta vacía o un error si no se encuentra el usuario
    }

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        // Verificar si el email o el usuario ya existen en la base de datos
        boolean existe = usuarioRepositorio.existsByUserEmailOrUserName(usuario.getUserEmail(), usuario.getUserName());

        if (existe) {
            throw new RuntimeException("El correo electrónico o el nombre de usuario ya están en uso.");
        }

        // Guarda el usuario y devuelve el usuario guardado con su ID generado
        return usuarioRepositorio.save(usuario);
    }


    public Usuario autenticarUsuario(String emailOrUser, String contrasena) {
        // Buscar al usuario por email o nombre de usuario
        Usuario usuario = usuarioRepositorio.findByUserEmailOrUserName(emailOrUser, emailOrUser);

        // Verificar si el usuario existe y si la contraseña es correcta
        if (usuario != null && usuario.getUserPassword().equals(contrasena)) {
            return usuario; // Retorna el usuario si la contraseña es correcta
        }
        return null; // Retorna null si el usuario no existe o la contraseña es incorrecta
    }

    public void actualizarMeta(int idUsuario, String meta) {
        usuarioRepositorio.actualizarMeta(idUsuario, meta);
    }

    public LecturaYMetaResponse obtenerLecturaYMeta(int idUsuario) {
        int meta = usuarioRepositorio.getMetaById(idUsuario);
        // Obtener los resultados de la base de datos
        List<Object[]> resultsMeses = usuarioRepositorio.obtenerMangasLecturaFecha(idUsuario);

// Crear una lista para almacenar los mangas leídos para el mes y año
        int mangasLeidosMes = 0; // Inicializamos en 0

// Obtener el mes y el año actuales
        LocalDate today = LocalDate.now();
        int mesActual = today.getMonthValue(); // Mes actual
        int anioActual = today.getYear(); // Año actual

// Procesamos los resultados obtenidos de la base de datos
        for (Object[] row : resultsMeses) {
            // Comprobamos si los valores de mes y año son no nulos antes de proceder
            Integer mesLeido = row[0] != null ? ((Long) row[0]).intValue() : null;
            Integer anioLeido = row[1] != null ? ((Long) row[1]).intValue() : null;

            // Verificamos si el mes y año de lectura coinciden con el mes y año actuales
            if (mesLeido != null && anioLeido != null) {
                if (mesLeido == mesActual && anioLeido == anioActual) {
                    mangasLeidosMes++; // Incrementamos el contador de mangas leídos en el mes actual
                }
            }
        }

// Ahora 'mangasLeidosMes' tiene el número de mangas leídos en el mes y año actual

        return new LecturaYMetaResponse(mangasLeidosMes, meta);
    }


}
