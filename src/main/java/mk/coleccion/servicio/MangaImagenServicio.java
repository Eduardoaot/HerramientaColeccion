package mk.coleccion.servicio;

import mk.coleccion.modelo.MangaImagen;
import mk.coleccion.repositorio.MangaImagenRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.rowset.serial.SerialBlob;

@Service
public class MangaImagenServicio {

    @Autowired
    private MangaImagenRepositorio mangaImagenRepositorio;

    // Guardar imagen
    public MangaImagen guardarImagen(MultipartFile file) throws IOException, SQLException {
        MangaImagen imagen = new MangaImagen();
        imagen.setMangaImageName(file.getOriginalFilename()); // Guarda el nombre del archivo
        imagen.setMangaImageFile(new SerialBlob(file.getBytes())); // Convierte en BLOB
        return mangaImagenRepositorio.save(imagen);
    }

    // Obtener imagen por nombre
    public Optional<MangaImagen> obtenerImagenPorNombre(String nombreArchivo) {
        return mangaImagenRepositorio.findByMangaImageName(nombreArchivo);
    }
}
