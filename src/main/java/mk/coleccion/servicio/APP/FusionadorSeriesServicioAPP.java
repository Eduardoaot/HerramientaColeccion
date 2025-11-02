package mk.coleccion.servicio.APP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * Servicio para detectar series duplicadas/similares y fusionarlas.
 * Resuelve el problema de series fragmentadas por web scraping.
 */
@Service
public class FusionadorSeriesServicioAPP {

    @Autowired
    private DataSource dataSource;

    /**
     * Representa una serie candidata a fusión
     */
    public static class SerieCandidato {
        private Integer idSerie;
        private String nombreSerie;
        private Integer volumenesDeclarados;
        private Integer mangasEnBD;
        private String autor;
        private List<Float> volumenesExistentes;

        public SerieCandidato(Integer idSerie, String nombreSerie, Integer volumenesDeclarados,
                              Integer mangasEnBD, String autor) {
            this.idSerie = idSerie;
            this.nombreSerie = nombreSerie;
            this.volumenesDeclarados = volumenesDeclarados;
            this.mangasEnBD = mangasEnBD;
            this.autor = autor;
            this.volumenesExistentes = new ArrayList<>();
        }

        // Getters y Setters
        public Integer getIdSerie() { return idSerie; }
        public String getNombreSerie() { return nombreSerie; }
        public Integer getVolumenesDeclarados() { return volumenesDeclarados; }
        public Integer getMangasEnBD() { return mangasEnBD; }
        public String getAutor() { return autor; }
        public List<Float> getVolumenesExistentes() { return volumenesExistentes; }
        public void setVolumenesExistentes(List<Float> volumenes) {
            this.volumenesExistentes = volumenes;
        }
    }

    /**
     * Representa un grupo de series similares que pueden fusionarse
     */
    public static class GrupoFusion {
        private String nombreBase;
        private List<SerieCandidato> series;
        private double similaridad;

        public GrupoFusion(String nombreBase) {
            this.nombreBase = nombreBase;
            this.series = new ArrayList<>();
        }

        public void addSerie(SerieCandidato serie) {
            this.series.add(serie);
        }

        public String getNombreBase() { return nombreBase; }
        public List<SerieCandidato> getSeries() { return series; }
        public double getSimilaridad() { return similaridad; }
        public void setSimilaridad(double similaridad) { this.similaridad = similaridad; }

        public int getTotalMangas() {
            return series.stream().mapToInt(SerieCandidato::getMangasEnBD).sum();
        }

        public int getMaxVolumenesDeclarados() {
            return series.stream()
                    .mapToInt(s -> s.getVolumenesDeclarados() != null ? s.getVolumenesDeclarados() : 0)
                    .max()
                    .orElse(0);
        }
    }

    /**
     * Previsualización del resultado de fusión
     */
    public static class PrevisualizacionFusion {
        private GrupoFusion grupo;
        private SerieCandidato serieDestino;
        private List<SerieCandidato> seriesOrigen;
        private Set<Float> volumenesFinales;
        private int totalMangasFusion;
        private boolean tieneConflictos;
        private List<String> advertencias;

        public PrevisualizacionFusion(GrupoFusion grupo, SerieCandidato destino) {
            this.grupo = grupo;
            this.serieDestino = destino;
            this.seriesOrigen = new ArrayList<>();
            this.volumenesFinales = new TreeSet<>();
            this.advertencias = new ArrayList<>();

            // Agregar volúmenes del destino
            volumenesFinales.addAll(destino.getVolumenesExistentes());

            // Agregar volúmenes de las series a fusionar
            for (SerieCandidato serie : grupo.getSeries()) {
                if (!serie.getIdSerie().equals(destino.getIdSerie())) {
                    seriesOrigen.add(serie);
                    volumenesFinales.addAll(serie.getVolumenesExistentes());
                }
            }

            this.totalMangasFusion = volumenesFinales.size();
            detectarConflictos();
        }

        private void detectarConflictos() {
            // Detectar volúmenes duplicados
            Map<Float, Integer> conteoVolumenes = new HashMap<>();

            volumenesFinales.forEach(vol ->
                    conteoVolumenes.put(vol, conteoVolumenes.getOrDefault(vol, 0) + 1)
            );

            List<Float> duplicados = conteoVolumenes.entrySet().stream()
                    .filter(e -> e.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .toList();

            if (!duplicados.isEmpty()) {
                tieneConflictos = true;
                advertencias.add("⚠️ Volúmenes duplicados: " + duplicados);
            }

            // Verificar si el total excede lo esperado
            int maxEsperado = grupo.getMaxVolumenesDeclarados();
            if (maxEsperado > 0 && totalMangasFusion > maxEsperado) {
                advertencias.add("⚠️ Total de mangas (" + totalMangasFusion +
                        ") excede lo esperado (" + maxEsperado + ")");
            }
        }

        // Getters
        public GrupoFusion getGrupo() { return grupo; }
        public SerieCandidato getSerieDestino() { return serieDestino; }
        public List<SerieCandidato> getSeriesOrigen() { return seriesOrigen; }
        public Set<Float> getVolumenesFinales() { return volumenesFinales; }
        public int getTotalMangasFusion() { return totalMangasFusion; }
        public boolean isTieneConflictos() { return tieneConflictos; }
        public List<String> getAdvertencias() { return advertencias; }
    }

    /**
     * Detecta series candidatas a fusión basándose en similitud de nombres
     */
    public List<GrupoFusion> detectarSeriesSimilares() {
        Map<String, GrupoFusion> grupos = new HashMap<>();

        String query = """
            SELECT 
                s.id_serie,
                s.serie_name,
                s.serie_totals,
                s.author_name,
                COUNT(m.id_manga) as mangas_en_bd
            FROM serie s
            LEFT JOIN manga m ON s.id_serie = m.id_serie
            GROUP BY s.id_serie, s.serie_name, s.serie_totals, s.author_name
            HAVING COUNT(m.id_manga) > 0
            ORDER BY s.serie_name
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Integer idSerie = rs.getInt("id_serie");
                String nombreSerie = rs.getString("serie_name");
                Integer volumenesDeclarados = rs.getInt("serie_totals");
                Integer mangasEnBD = rs.getInt("mangas_en_bd");
                String autor = rs.getString("author_name");

                SerieCandidato candidato = new SerieCandidato(
                        idSerie, nombreSerie, volumenesDeclarados, mangasEnBD, autor
                );

                // Obtener volúmenes existentes
                candidato.setVolumenesExistentes(obtenerVolumenesExistentes(conn, idSerie));

                // Normalizar nombre para agrupar
                String nombreBase = normalizarNombreSerie(nombreSerie);

                grupos.computeIfAbsent(nombreBase, k -> new GrupoFusion(k))
                        .addSerie(candidato);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Filtrar solo grupos con más de una serie
        return grupos.values().stream()
                .filter(g -> g.getSeries().size() > 1)
                .sorted((g1, g2) -> g2.getTotalMangas() - g1.getTotalMangas())
                .toList();
    }

    /**
     * Normaliza el nombre de una serie para detectar similitudes
     */
    private String normalizarNombreSerie(String nombre) {
        if (nombre == null) return "";

        return nombre.toLowerCase()
                .replaceAll("\\s*\\(.*?\\)\\s*", "") // Quitar paréntesis y su contenido
                .replaceAll("\\s*\\[.*?\\]\\s*", "") // Quitar corchetes
                .replaceAll("\\s+de\\s+\\d+\\s*$", "") // Quitar "de XX" al final
                .replaceAll("\\s+vol\\.?\\s*\\d+.*$", "") // Quitar "vol. X"
                .replaceAll("\\s+tomo\\s+\\d+.*$", "") // Quitar "tomo X"
                .replaceAll("[^a-z0-9\\s]", "") // Quitar caracteres especiales
                .replaceAll("\\s+", " ") // Normalizar espacios
                .trim();
    }

    /**
     * Obtiene los volúmenes existentes de una serie
     */
    private List<Float> obtenerVolumenesExistentes(Connection conn, Integer idSerie) {
        List<Float> volumenes = new ArrayList<>();
        String query = "SELECT volume_number FROM manga WHERE id_serie = ? ORDER BY volume_number";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSerie);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                volumenes.add(rs.getFloat("volume_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return volumenes;
    }

    /**
     * Genera una previsualización de la fusión
     */
    public PrevisualizacionFusion previsualizarFusion(GrupoFusion grupo, Integer idSerieDestino) {
        SerieCandidato destino = grupo.getSeries().stream()
                .filter(s -> s.getIdSerie().equals(idSerieDestino))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Serie destino no encontrada"));

        return new PrevisualizacionFusion(grupo, destino);
    }

    /**
     * Ejecuta la fusión de series en la base de datos
     */
    @org.springframework.transaction.annotation.Transactional
    public ResultadoFusion fusionarSeries(PrevisualizacionFusion preview) {
        ResultadoFusion resultado = new ResultadoFusion();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Integer idDestino = preview.getSerieDestino().getIdSerie();

                for (SerieCandidato origen : preview.getSeriesOrigen()) {
                    Integer idOrigen = origen.getIdSerie();

                    // 1. Actualizar mangas de la serie origen al destino
                    String updateMangas =
                            "UPDATE manga SET id_serie = ? WHERE id_serie = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateMangas)) {
                        ps.setInt(1, idDestino);
                        ps.setInt(2, idOrigen);
                        int mangasMovidos = ps.executeUpdate();
                        resultado.addMangasMovidos(mangasMovidos);
                    }

                    // 2. Actualizar coleccion_serie
                    String updateColeccionSerie =
                            "UPDATE coleccion_serie SET id_serie = ? " +
                                    "WHERE id_serie = ? AND NOT EXISTS (" +
                                    "  SELECT 1 FROM coleccion_serie cs2 " +
                                    "  WHERE cs2.id_serie = ? AND cs2.id_usuario = coleccion_serie.id_usuario" +
                                    ")";
                    try (PreparedStatement ps = conn.prepareStatement(updateColeccionSerie)) {
                        ps.setInt(1, idDestino);
                        ps.setInt(2, idOrigen);
                        ps.setInt(3, idDestino);
                        ps.executeUpdate();
                    }

                    // 3. Eliminar coleccion_serie duplicados
                    String deleteColeccionSerie =
                            "DELETE FROM coleccion_serie WHERE id_serie = ?";
                    try (PreparedStatement ps = conn.prepareStatement(deleteColeccionSerie)) {
                        ps.setInt(1, idOrigen);
                        ps.executeUpdate();
                    }

                    // 4. Eliminar la serie origen
                    String deleteSerie = "DELETE FROM serie WHERE id_serie = ?";
                    try (PreparedStatement ps = conn.prepareStatement(deleteSerie)) {
                        ps.setInt(1, idOrigen);
                        ps.executeUpdate();
                        resultado.addSerieEliminada(origen.getNombreSerie());
                    }
                }

                // 5. Actualizar serie_totals del destino
                int nuevoTotal = preview.getVolumenesFinales().size();
                String updateTotales =
                        "UPDATE serie SET serie_totals = ? WHERE id_serie = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTotales)) {
                    ps.setInt(1, nuevoTotal);
                    ps.setInt(2, idDestino);
                    ps.executeUpdate();
                }

                conn.commit();
                resultado.setExito(true);
                resultado.setMensaje("Fusión completada exitosamente");

            } catch (SQLException e) {
                conn.rollback();
                resultado.setExito(false);
                resultado.setMensaje("Error en la fusión: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            resultado.setExito(false);
            resultado.setMensaje("Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }

        return resultado;
    }

    /**
     * Resultado de una operación de fusión
     */
    public static class ResultadoFusion {
        private boolean exito;
        private String mensaje;
        private int mangasMovidos;
        private List<String> seriesEliminadas;

        public ResultadoFusion() {
            this.seriesEliminadas = new ArrayList<>();
        }

        public boolean isExito() { return exito; }
        public void setExito(boolean exito) { this.exito = exito; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }

        public int getMangasMovidos() { return mangasMovidos; }
        public void addMangasMovidos(int cantidad) { this.mangasMovidos += cantidad; }

        public List<String> getSeriesEliminadas() { return seriesEliminadas; }
        public void addSerieEliminada(String nombre) { this.seriesEliminadas.add(nombre); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(exito ? "✅ " : "❌ ").append(mensaje).append("\n");
            if (exito) {
                sb.append("📦 Mangas movidos: ").append(mangasMovidos).append("\n");
                sb.append("🗑️ Series eliminadas: ").append(seriesEliminadas.size()).append("\n");
                seriesEliminadas.forEach(s -> sb.append("   - ").append(s).append("\n"));
            }
            return sb.toString();
        }
    }
}