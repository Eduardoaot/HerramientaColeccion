package mk.coleccion.servicio;

import jakarta.transaction.Transactional;
import mk.coleccion.dto.ActualizarPresupuestoRequestDTO;
import mk.coleccion.dto.CrearPresupuestoRequestDTO;
import mk.coleccion.dto.MangaPresupuestoDTO;
import mk.coleccion.dto.PresupuestoResponseDTO;
import mk.coleccion.modelo.Manga;
import mk.coleccion.modelo.Presupuestos;
import mk.coleccion.modelo.PresupuestosManga;
import mk.coleccion.modelo.Usuario;
import mk.coleccion.repositorio.MangaRepositorio;
import mk.coleccion.repositorio.PresupuestoMangaRepositorio;
import mk.coleccion.repositorio.PresupuestoRepositorio;
import mk.coleccion.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PresupuestoServicio {

    @Autowired
    private PresupuestoRepositorio presupuestoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private MangaRepositorio mangaRepositorio;

    @Autowired
    private PresupuestoMangaRepositorio presupuestoMangaRepositorio;

    @Value("${app.base.image.url}")
    private String baseImageUrl;

    @Transactional
    public void crearPresupuestoConMangas(CrearPresupuestoRequestDTO request) {
        // 1. Buscar el usuario
        Usuario usuario = usuarioRepositorio.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Crear el presupuesto con los datos recibidos
        Presupuestos presupuesto = new Presupuestos();
        presupuesto.setNameBudget(request.getNombreBolsa());
        presupuesto.setUsuario(usuario);
        presupuesto.setDiscount(request.getDescuento());
        presupuesto.setDateBudgetCreated(new Date());

        presupuesto = presupuestoRepositorio.save(presupuesto); // Guarda y obtiene el ID

        // 3. Insertar los mangas asociados
        List<PresupuestosManga> presupuestosMangas = new ArrayList<>();

        for (Integer idManga : request.getMangas_bolsa()) {
            Manga manga = mangaRepositorio.findById(idManga)
                    .orElseThrow(() -> new RuntimeException("Manga con ID " + idManga + " no encontrado"));

            PresupuestosManga presupuestoManga = new PresupuestosManga();
            presupuestoManga.setPresupuestos(presupuesto);
            presupuestoManga.setManga(manga);

            presupuestosMangas.add(presupuestoManga);
        }

        presupuesto.setPresupuestosMangas(presupuestosMangas);
        presupuestoRepositorio.save(presupuesto); // Guarda nuevamente con la relación
    }

    public List<PresupuestoResponseDTO> obtenerPresupuestosPorUsuario(Integer idUsuario) {
        List<Object[]> resultados = presupuestoRepositorio.obtenerPresupuestosPorUsuario(idUsuario);

        Map<Integer, PresupuestoResponseDTO> presupuestosMap = new LinkedHashMap<>();

        for (Object[] row : resultados) {
            Integer idPresupuesto = (Integer) row[0];

            presupuestosMap.putIfAbsent(idPresupuesto, new PresupuestoResponseDTO(
                    idPresupuesto,
                    (String) row[1],  // nombrePresupuesto
                    (Integer) row[2], // idUsuario
                    (Float) row[3],   // descuento
                    (String) row[4],  // fechaPresupuestoCreado
                    new ArrayList<>() // Lista de mangas
            ));

            if (row[5] != null) { // Si hay mangas en el presupuesto
                presupuestosMap.get(idPresupuesto).getListaMangasPresupuesto().add(new MangaPresupuestoDTO(
                        (Integer) row[5],  // idManga
                        (Float) row[6],    // mangaNum
                        baseImageUrl + (String) row[7],   // direccionMangaImg
                        (Float) row[8],    // precio
                        (String) row[9]    // serieNom
                ));
            }
        }

        return new ArrayList<>(presupuestosMap.values());
    }

    public PresupuestoResponseDTO obtenerPresupuestoPorId(Integer idPresupuesto) {
        List<Object[]> resultados = presupuestoRepositorio.obtenerPresupuestoPorId(idPresupuesto);

        if (resultados.isEmpty()) {
            return null; // Devuelve null si el presupuesto no existe
        }

        PresupuestoResponseDTO presupuesto = new PresupuestoResponseDTO();
        List<MangaPresupuestoDTO> listaMangas = new ArrayList<>();

        for (Object[] row : resultados) {
            if (presupuesto.getIdPresupuesto() == null) {
                presupuesto.setIdPresupuesto((Integer) row[0]);
                presupuesto.setNombrePresupuesto((String) row[1]);
                presupuesto.setIdUsuario((Integer) row[2]);
                presupuesto.setDescuento((Float) row[3]);
                presupuesto.setFechaPresupuestoCreado((String) row[4]);
                presupuesto.setListaMangasPresupuesto(listaMangas);
            }

            if (row[5] != null) { // Si hay mangas en el presupuesto
                listaMangas.add(new MangaPresupuestoDTO(
                        (Integer) row[5],  // idManga
                        (Float) row[6],    // mangaNum
                        baseImageUrl + (String) row[7],   // direccionMangaImg
                        (Float) row[8],    // precio
                        (String) row[9]    // serieNom
                ));
            }
        }

        return presupuesto;
    }

    @Transactional
    public void eliminarPresupuesto(Integer idPresupuesto) {
        try {
            // Primero eliminar los registros de presupuestos_manga relacionados con el idPresupuesto
            presupuestoMangaRepositorio.eliminarPorIdPresupuesto(idPresupuesto);

            // Después eliminar el presupuesto
            presupuestoRepositorio.deleteById(idPresupuesto);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el presupuesto: " + e.getMessage());
        }
    }

    @Transactional
    public void actualizarPresupuesto(ActualizarPresupuestoRequestDTO actualizarPresupuestoRequestDTO) {
        // Verificar que el presupuesto existe
        Optional<Presupuestos> presupuestoOptional = presupuestoRepositorio.findById(actualizarPresupuestoRequestDTO.getIdPresupuesto());

        if (presupuestoOptional.isEmpty()) {
            throw new RuntimeException("Presupuesto no encontrado.");
        }

        // Obtener el presupuesto actual
        Presupuestos presupuesto = presupuestoOptional.get();

        // Actualizar los campos del presupuesto
        presupuesto.setNameBudget(actualizarPresupuestoRequestDTO.getNombrePresupuesto());
        presupuesto.setDiscount(actualizarPresupuestoRequestDTO.getDescuento());

        // Eliminar los mangas asociados al presupuesto
        presupuestoMangaRepositorio.deleteAllByIdPresupuesto(actualizarPresupuestoRequestDTO.getIdPresupuesto());

        // Añadir los nuevos mangas
        List<Integer> idsMangas = actualizarPresupuestoRequestDTO.getMangas_bolsa();
        for (Integer idManga : idsMangas) {
            Optional<Manga> mangaOptional = mangaRepositorio.findById(idManga);
            if (mangaOptional.isPresent()) {
                Manga manga = mangaOptional.get();
                PresupuestosManga nuevoPresupuestoManga = new PresupuestosManga();
                nuevoPresupuestoManga.setPresupuestos(presupuesto);
                nuevoPresupuestoManga.setManga(manga);
                presupuestoMangaRepositorio.save(nuevoPresupuestoManga);
            }
        }

        // Guardar el presupuesto actualizado
        presupuestoRepositorio.save(presupuesto);
    }
}



