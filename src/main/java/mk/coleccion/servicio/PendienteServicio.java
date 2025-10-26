package mk.coleccion.servicio;


import mk.coleccion.dto.PendienteMangasFaltantesDTO;
import mk.coleccion.dto.PlanDTO;
import mk.coleccion.repositorio.MangaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PendienteServicio {

    @Autowired
    private MangaRepositorio mangaRepositorio;

    @Value("${app.base.image.url}")
    private String baseImageUrl;

    public PendienteMangasFaltantesDTO obtenerMangasFaltantes(Integer idUsuario) {
        // Obtener los resultados desde el repositorio
        List<Object[]> resultsMangasFaltantes = mangaRepositorio.pendienteBuscarMangasFaltantes(idUsuario);

        // Inicializamos la lista de mangas faltantes
        List<PendienteMangasFaltantesDTO.MangaFaltanteDTO> mangasFaltantes = new ArrayList<>();

        // Recorremos los resultados y procesamos los datos
        for (Object[] row : resultsMangasFaltantes) {
            // Extraemos los valores de la fila
            Integer idManga = (Integer) row[0];  // ID del manga (Integer)
            Float numeroManga = (Float) row[1];  // Número del manga (Float)
            String imagenManga = (String) row[2];  // Imagen del manga (String)
            Float precio = (Float) row[3];  // Precio (Float)
            String nombreSerie = (String) row[4];  // Nombre de la serie (String)

            // Creamos el objeto MangaFaltanteDTO con los datos obtenidos
            PendienteMangasFaltantesDTO.MangaFaltanteDTO mangaFaltanteDTO = new PendienteMangasFaltantesDTO.MangaFaltanteDTO(idManga, numeroManga, (baseImageUrl + imagenManga), precio, nombreSerie);

            // Añadimos el objeto a la lista
            mangasFaltantes.add(mangaFaltanteDTO);
        }

        // Creamos el DTO final con el resultado y la lista de mangas faltantes
        PendienteMangasFaltantesDTO pendienteMangasFaltantesDTO = new PendienteMangasFaltantesDTO("success", mangasFaltantes);

        return pendienteMangasFaltantesDTO;
    }

    public PendienteServicio(MangaRepositorio mangaRepositorio) {
        this.mangaRepositorio = mangaRepositorio;
    }

    public List<PlanDTO> obtenerMangasPorIds(List<Integer> listaIds) {
        // 1. Obtener resultados sin ordenar
        List<Object[]> results = mangaRepositorio.buscarMangasConListasDeId(listaIds);

        // 2. Crear un mapa para búsqueda rápida por ID
        Map<Integer, Object[]> mapaResultados = results.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],  // id_manga es la primera columna
                        row -> row
                ));

        // 3. Reconstruir la lista en el orden de listaIds
        return listaIds.stream()
                .map(mapaResultados::get)         // Obtener cada resultado en orden
                .filter(Objects::nonNull)         // Ignorar IDs no encontrados
                .map(row -> new PlanDTO(
                        (Integer) row[0],            // idManga
                        (Float) row[1],              // numeroManga
                        baseImageUrl + (String) row[2],              // imagenManga
                        (Float) row[4],               // precio
                        (String) row[3]               // nombreSerie
                ))
                .collect(Collectors.toList());
    }

}

