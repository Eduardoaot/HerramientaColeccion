package mk.coleccion.servicio;

import mk.coleccion.dto.MangaDetallesDTO;
import mk.coleccion.dto.MangaLecturaDTO;
import mk.coleccion.dto.MangaPendienteDTO;
import mk.coleccion.dto.MangaSinLeerDTO;
import mk.coleccion.repositorio.MangaRepositorio;
import mk.coleccion.response.MangaEstadoLecturaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MangaServicio {

    @Autowired
    private MangaRepositorio mangaRepositorio;

    @Value("${app.base.image.url}")
    private String baseImageUrl;

    public List<MangaDetallesDTO> obtenerDetallesManga(Integer idManga, Integer idUsuario) {
        List<Object[]> results = mangaRepositorio.buscarDetallesManga(idManga, idUsuario);
        return results.stream()
                .map(row -> new MangaDetallesDTO(
                        (String) row[0],  // tituloSerie
                        (Float) row[1],   // numeroManga
                        (String) row[2],  // estadoLectura
                        (String) row[3],  // descripcion
                        (String) row[4],  // nombreAutor
                        baseImageUrl + (String) row[5],  // imagenManga
                        (Float) row[6],   // precioManga
                        (Long) row[7]     // estadoAgregado
                ))
                .collect(Collectors.toList());
    }

    public MangaEstadoLecturaResponse actualizarEstadoLectura(Integer idManga, Integer idUsuario, Integer estadoLectura) {
        int filasAfectadas = mangaRepositorio.actualizarEstadoLectura(estadoLectura, idManga, idUsuario);

        // Si la consulta afecta una fila, significa que la actualización fue exitosa
        if (filasAfectadas > 0) {
            return new MangaEstadoLecturaResponse("Estado de lectura actualizado exitosamente", estadoLectura);
        } else {
            return new MangaEstadoLecturaResponse("No se pudo actualizar el estado de lectura", estadoLectura);
        }
    }

    public List<MangaPendienteDTO> obtenerMangasPendientes(Integer idUsuario) {
        // Obtener los resultados desde el repositorio
        List<Object[]> resultsMangasPendientes = mangaRepositorio.obtenerMangasPendientes(idUsuario);

        // Inicializamos la lista de mangas pendientes
        List<MangaPendienteDTO> mangasPendientes = new ArrayList<>();

        // Recorremos los resultados y procesamos los datos
        for (Object[] row : resultsMangasPendientes) {
            // Extraemos los valores de la fila
            Integer idManga = (Integer) row[0];  // ID del manga (Integer)
            Float numeroManga = (Float) row[1];  // Número del manga (Float)
            String imagenManga = (String) row[2];  // Imagen del manga (String)

            // Creamos el objeto MangaPendienteDTO con los datos obtenidos
            MangaPendienteDTO mangaPendienteDTO = new MangaPendienteDTO(idManga, numeroManga, (baseImageUrl + imagenManga));

            // Añadimos el objeto a la lista
            mangasPendientes.add(mangaPendienteDTO);
        }

        return mangasPendientes;
    }



    public MangaLecturaDTO obtenerDetallesLectura(Integer idUsuario) {
        // Obtener los resultados desde el repositorio
        List<Object[]> resultsMangaSinLeer = mangaRepositorio.obtenerMangasSinLeer(idUsuario);

        // Inicializamos la lista de mangas sin leer
        List<MangaSinLeerDTO> mangasSinLeer = new ArrayList<>();

        // Recorremos los resultados y procesamos los datos
        for (Object[] row : resultsMangaSinLeer) {
            // Extraemos los valores de la fila
            Integer idManga = (Integer) row[0];  // ID del manga (Long)
            String serieNom = (String) row[1];  // Nombre de la serie (String)
            Float mangaSum = (Float) row[2];  // Número del manga (Float)
            String direccionManga = (String) row[3];  // Dirección de la imagen (String)
            // Creamos el objeto MangaSinLeerDTO con los datos obtenidos
            MangaSinLeerDTO mangaSinLeerDTO = new MangaSinLeerDTO(idManga, serieNom, mangaSum, (baseImageUrl + direccionManga));
            // Añadimos el objeto a la lista
            mangasSinLeer.add(mangaSinLeerDTO);
        }

        // Obtener los resultados de estadísticas de lectura
        List<Object[]> resultsEstadisticas = mangaRepositorio.obtenerEstadisticasLecturaMangas(idUsuario);

        MangaLecturaDTO mangaLecturaDTO = new MangaLecturaDTO();
        mangaLecturaDTO.setListaMangasSinLeer(mangasSinLeer);

        if (!resultsEstadisticas.isEmpty()) {
            Object[] row = resultsEstadisticas.get(0);  // Tomamos la primera fila

            Long totalMangasEstado3 = (Long) row[0];  // Total de mangas con estado 3
            Long mangasLeidosMesActual = (Long) row[1];  // Mangas leídos en el mes actual
            Long mangasLeidosAnoActual = (Long) row[2];  // Mangas leídos en el año actual

            // Asignamos los valores al DTO
            mangaLecturaDTO.setTotalMangasLeidos(totalMangasEstado3);
            mangaLecturaDTO.setTotalMangaLeidosMes(mangasLeidosMesActual);
            mangaLecturaDTO.setTotalMangaLeidosAnio(mangasLeidosAnoActual);
        }

        List<String> ultimosSeisMeses = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 4; i >= 0; i--) {
            LocalDate mes = today.minusMonths(i);
            String mesAbreviado = mes.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
            ultimosSeisMeses.add(mesAbreviado);
        }

        mangaLecturaDTO.setListaMangasMeses(ultimosSeisMeses);

        List<Integer> mangasAgregados = new ArrayList<>(Collections.nCopies(5, 0));
        List<Integer> mangasLeidos = new ArrayList<>(Collections.nCopies(5, 0));

        int anioActual = today.getYear();
        int mesActual = today.getMonthValue();

// Mapear los últimos 5 meses a índices de la lista
        Map<String, Integer> mesAnioIndiceMap = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            LocalDate fecha = today.minusMonths(4 - i);
            int mes = fecha.getMonthValue();
            int anio = fecha.getYear();
            mesAnioIndiceMap.put(mes + "-" + anio, i);
        }

        List<Object[]> resultsMeses = mangaRepositorio.obtenerMangasFecha(idUsuario);

// Procesar los resultados de la base de datos
        for (Object[] row : resultsMeses) {
            Integer mesAgregado = ((Long) row[0]).intValue();
            Integer anioAgregado = ((Long) row[1]).intValue();
            Integer mesLeido = row[2] != null ? ((Long) row[2]).intValue() : null;
            Integer anioLeido = row[3] != null ? ((Long) row[3]).intValue() : null;

            // Verificar que el mes está en los últimos 5 meses antes de sumarlo
            String claveAgregado = mesAgregado + "-" + anioAgregado;
            if (mesAnioIndiceMap.containsKey(claveAgregado)) {
                int index = mesAnioIndiceMap.get(claveAgregado);
                mangasAgregados.set(index, mangasAgregados.get(index) + 1);
            }

            if (mesLeido != null && anioLeido != null) {
                String claveLeido = mesLeido + "-" + anioLeido;
                if (mesAnioIndiceMap.containsKey(claveLeido)) {
                    int index = mesAnioIndiceMap.get(claveLeido);
                    mangasLeidos.set(index, mangasLeidos.get(index) + 1);
                }
            }
        }
        mangaLecturaDTO.setListaMangasCompradosData(mangasAgregados);
        mangaLecturaDTO.setListaMangasLeidosData(mangasLeidos);

        return mangaLecturaDTO;
    }

}


