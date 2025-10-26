package mk.coleccion.repositorio.APP;

import mk.coleccion.modelo.DescripcionSerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DescripcionSerieRepositorioAPP extends JpaRepository<DescripcionSerie, Integer> {
}
