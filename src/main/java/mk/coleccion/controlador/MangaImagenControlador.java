package mk.coleccion.controlador;

import mk.coleccion.modelo.MangaImagen;
import mk.coleccion.servicio.MangaImagenServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@RestController
@RequestMapping("/api/mangaImagen")
public class MangaImagenControlador {

    @Autowired
    private MangaImagenServicio mangaImagenServicio;

    // Subir imagen
    @PostMapping("/upload")
    public ResponseEntity<String> subirImagen(@RequestParam("file") MultipartFile file) {
        try {
            MangaImagen imagenGuardada = mangaImagenServicio.guardarImagen(file);
            return ResponseEntity.ok("Imagen subida: " + imagenGuardada.getMangaImageName());
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen");
        }
    }

    //Obtener imagen por nombre
    @GetMapping("/{nombreArchivo}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String nombreArchivo) {
        Optional<MangaImagen> imagenOpt = mangaImagenServicio.obtenerImagenPorNombre(nombreArchivo);

        if (imagenOpt.isPresent()) {
            try {
                MangaImagen imagen = imagenOpt.get();
                byte[] bytes = imagen.getMangaImageFile().getBytes(1, (int) imagen.getMangaImageFile().length());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG); // Cambia según el formato de la imagen

                return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
            } catch (SQLException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
