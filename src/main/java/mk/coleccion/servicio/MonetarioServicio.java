package mk.coleccion.servicio;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mk.coleccion.dto.CrearMonetarioAhorrosRequestDTO;
import mk.coleccion.dto.EstadisticasResponseDTO;
import mk.coleccion.dto.MejorAhorroDTO;
import mk.coleccion.modelo.MonetarioAhorros;
import mk.coleccion.modelo.Usuario;
import mk.coleccion.repositorio.ColeccionMangaRepositorio;
import mk.coleccion.repositorio.MonetarioAhorrosRepositorio;
import mk.coleccion.repositorio.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonetarioServicio {

    @Autowired
    private MonetarioAhorrosRepositorio monetarioAhorrosRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private ColeccionMangaRepositorio coleccionMangaRepositorio;

    @Transactional
    public void guardarMonetarioAhorro(CrearMonetarioAhorrosRequestDTO requestDTO) {
        Optional<Usuario> usuarioOptional = usuarioRepositorio.findById(requestDTO.getIdUsuario());

        if (usuarioOptional.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado.");
        }

        MonetarioAhorros nuevoAhorro = new MonetarioAhorros();
        nuevoAhorro.setUsuario4(usuarioOptional.get());
        nuevoAhorro.setTotalSavedBudget(requestDTO.getTotalAhorrado());
        nuevoAhorro.setTotalVolumesSaved(requestDTO.getTotalMangas());

        monetarioAhorrosRepositorio.save(nuevoAhorro);
    }

    public EstadisticasResponseDTO obtenerEstadisticasUsuario(Integer idUsuario) {
        int totalComprados = coleccionMangaRepositorio.contarPorUsuario(idUsuario);
        int totalCompradosAnio = coleccionMangaRepositorio.contarPorUsuarioEnAnio(idUsuario, Year.now().getValue());
        int totalMes = coleccionMangaRepositorio.contarPorUsuarioEnMes(idUsuario, YearMonth.now().getYear(), YearMonth.now().getMonthValue());

        Float valorTotal = coleccionMangaRepositorio.sumarPrecioMangasPorUsuario(idUsuario);
        Float ahorroTotal = monetarioAhorrosRepositorio.sumarTotalAhorrado(idUsuario);
        Float gastoTotal = (valorTotal != null ? valorTotal : 0) - (ahorroTotal != null ? ahorroTotal : 0);

        List<Object[]> mejoresAhorros = monetarioAhorrosRepositorio.obtenerMejoresAhorros(idUsuario);
        List<MejorAhorroDTO> listaMejoresAhorros = new ArrayList<>();
        int top = 1;

        // Formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Object[] obj : mejoresAhorros) {
            Integer numeroMangas = (Integer) obj[0];
            Float totalAhorrado = (Float) obj[1];
            Date fechaDeAhorro = (Date) obj[2];  // Obtener la fecha de ahorro

            // Formateamos la fecha
            String fechaFormateada = sdf.format(fechaDeAhorro);

            // Ahora creamos el DTO con la fecha formateada
            listaMejoresAhorros.add(new MejorAhorroDTO(top++, numeroMangas, totalAhorrado, fechaFormateada));
        }

        return new EstadisticasResponseDTO("success", totalComprados, totalCompradosAnio, totalMes, valorTotal, ahorroTotal, gastoTotal, listaMejoresAhorros);
    }
}

