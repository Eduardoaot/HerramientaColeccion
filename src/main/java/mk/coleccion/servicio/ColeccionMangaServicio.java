package mk.coleccion.servicio;

import jakarta.transaction.Transactional;
import mk.coleccion.dto.ColeccionMangaDetalleDTO;
import mk.coleccion.dto.SerieInfoDTO;
import mk.coleccion.dto.UsuarioColeccionDTO;
import mk.coleccion.repositorio.ColeccionMangaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColeccionMangaServicio {

    @Autowired
    private ColeccionMangaRepositorio coleccionMangaRepositorio;

    @Value("${app.base.image.url}")
    private String baseImageUrl;

    public List<ColeccionMangaDetalleDTO> obtenerDetallesColeccionManga(Integer idUsuario) {
        List<Object[]> results = coleccionMangaRepositorio.findDetallesColeccionManga(idUsuario);
        return results.stream()
                .map(row -> new ColeccionMangaDetalleDTO(
                        (Integer) row[0], // idUsuario
                        (Integer) row[1], // idManga
                        (Date) row[2],  // mangaDate
                        baseImageUrl + (String) row[3],  // mangaImg
                        (Float) row[4],   // mangaNum
                        (String) row[5],  // estado (cambiado de "descripcion" a "estado")
                        (Float) row[6],   // precio
                        (String) row[7]   // serieNom
                ))
                .collect(Collectors.toList());
    }

    public List<SerieInfoDTO> obtenerSeriesDeColeccion(Integer idUsuario) {
        List<Object[]> results = coleccionMangaRepositorio.findSeriesAndTotalByUserId(idUsuario);

        return results.stream()
                .map(row -> {
                    Integer id_serie = (Integer) row[0];
                    String serieNom = (String) row[1];
                    Integer serieTot = (Integer) row[2];
                    Integer estadoSerie = (row[3] != null) ? (Integer) row[3] : null;
                    Integer tomosUsuario = (row[4] != null) ? ((Number) row[4]).intValue() : 0;
                    String serieImagen = (String) row[5]; // URL de la imagen

                    // Cálculo del porcentaje de tomos adquiridos
                    double seriePorcentaje = (serieTot != null && serieTot > 0)
                            ? ((double) tomosUsuario / serieTot) * 100
                            : 0;

                    // Determinar el estado de la colección según estadoSerie
                    String serieEstado;
                    if (estadoSerie != null) {
                        switch (estadoSerie) {
                            case 1:
                                if (serieTot != null && serieTot > 0) {
                                    int faltantes = serieTot - tomosUsuario;
                                    serieEstado = "Te faltan " + faltantes;
                                } else {
                                    serieEstado = "No completar";
                                }
                                break;
                            case 2:
                                serieEstado = "No completar";
                                break;
                            case 3:
                                serieEstado = "Tomo único";
                                break;
                            case 4:
                                serieEstado = "¡Completado!";
                                break;
                            default:
                                serieEstado = "Desconocido";
                        }
                    } else {
                        serieEstado = "No especificado";
                    }

                    return new SerieInfoDTO(id_serie, serieNom, seriePorcentaje, serieEstado, (baseImageUrl + serieImagen));
                })
                .collect(Collectors.toList());
    }

    public UsuarioColeccionDTO obtenerDetallesColeccionDelUsuario(Integer idUsuario) {
        List<Object[]> results = coleccionMangaRepositorio.findMangasAndSeriesByUserId(idUsuario);

        // Obtener los resultados de la consulta
        Object[] row = results.get(0);  // Solo un resultado debería ser devuelto

        // Convertir los valores de Long a Integer
        Integer totalMangas = row[0] != null ? ((Long) row[0]).intValue() : 0;
        Integer totalSeries = row[1] != null ? ((Long) row[1]).intValue() : 0;
        Integer seriesPorCompletar = row[2] != null ? ((Long) row[2]).intValue() : 0;
        Integer seriesCompletadas = row[3] != null ? ((Long) row[3]).intValue() : 0;

        // Calcular el porcentaje de series completadas
        double porcentajeSeries = (totalSeries > 0)
                ? ((double) seriesCompletadas / totalSeries) * 100
                : 0;

        // Crear y devolver el DTO con los valores calculados
        return new UsuarioColeccionDTO(totalMangas, totalSeries, seriesPorCompletar, seriesCompletadas, porcentajeSeries);
    }

    @Transactional
    public void eliminarMangaYSerieSiEsNecesario(Integer idManga, Integer idUsuario) {
        coleccionMangaRepositorio.eliminarMangaDeColeccion(idManga, idUsuario);
        coleccionMangaRepositorio.eliminarSerieSiNoTieneMangas(idManga, idUsuario);
        coleccionMangaRepositorio.actualizarEstadoSerieSiEra4(idManga, idUsuario);
    }

    @Transactional
    public void agregarMangaYSerieSiEsNecesario(Integer idManga, Integer idUsuario, Float precio) {
        coleccionMangaRepositorio.agregarMangaAColeccion(idManga, idUsuario, precio);
        coleccionMangaRepositorio.agregarSerieSiNoExiste(idManga, idUsuario);
        coleccionMangaRepositorio.actualizarEstadoSerie(idManga, idUsuario);
    }
}