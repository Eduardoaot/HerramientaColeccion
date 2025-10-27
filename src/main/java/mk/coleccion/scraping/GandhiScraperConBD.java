package mk.coleccion.scraping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GandhiScraperConBD {

    private static final Path CARPETA_IMAGENES = Path.of("imagenes_gandhi");

    // Configuración de base de datos
    private static final String DB_URL = "jdbc:mysql://localhost:3306/manga_app_prueba_integracion";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "admin";

    // Cache para evitar consultas repetidas a BD
    private final Map<String, Integer> cacheSeriesIds = new ConcurrentHashMap<>();
    private final Set<String> cacheMangasExistentes = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws Exception {
        new GandhiScraperConBD().iniciarScraping();
    }

    private void iniciarScraping() throws Exception {
        WebDriverManager.chromedriver().setup();

        if (!Files.exists(CARPETA_IMAGENES)) Files.createDirectory(CARPETA_IMAGENES);

        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-gpu",
                "--window-size=1920,1080"
        );

        WebDriver driver = new ChromeDriver(opciones);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        Connection conn = null;
        try {
            // Conectar a base de datos
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            cargarCacheBD(conn);

            int pagina = 1;
            boolean hayMasPaginas = true;

            while (hayMasPaginas) {
                String urlPagina = "https://www.gandhi.com.mx/distrito-manga?page=" + pagina;
                driver.get(urlPagina);

                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.vtex-search-result-3-x-galleryItem")));
                } catch (TimeoutException e) {
                    System.out.println("\n=== No hay más productos, proceso terminado ===");
                    break; // salir del while
                }

                List<WebElement> productos = driver.findElements(By.cssSelector("div.vtex-search-result-3-x-galleryItem"));
                if (productos.isEmpty()) {
                    hayMasPaginas = false;
                    break;
                }

                System.out.println("\n=== Página " + pagina + " ===\n");

                for (int i = 0; i < productos.size(); i++) {
                    try {
                        productos = driver.findElements(By.cssSelector("div.vtex-search-result-3-x-galleryItem"));
                        WebElement producto = productos.get(i);

                        js.executeScript("arguments[0].scrollIntoView({block: 'center'});", producto);
                        Thread.sleep(500);

                        // ------------------- IMAGEN -------------------
                        WebElement imagenElement = producto.findElement(By.cssSelector("img.vtex-product-summary-2-x-imageInline"));
                        String imagenUrl = imagenElement.getAttribute("src");

                        // ------------------- TÍTULO -------------------
                        WebElement tituloElement = producto.findElement(By.cssSelector("h3.vtex-product-summary-2-x-productNameContainer span.vtex-product-summary-2-x-productBrand"));
                        String tituloCompleto = tituloElement.getText().trim();
                        String tituloObra = tituloCompleto;
                        String numeroTomo = "Desconocido";

                        if (tituloCompleto.matches(".*\\s\\d+$")) {
                            int lastSpace = tituloCompleto.lastIndexOf(' ');
                            tituloObra = tituloCompleto.substring(0, lastSpace).trim();
                            numeroTomo = tituloCompleto.substring(lastSpace + 1).trim();
                        }

                        if (numeroTomo.equals("Desconocido")) {
                            numeroTomo = "1";
                        }

                        // ------------------- AUTOR -------------------
                        String autor = "Autor Desconocido";
                        try {
                            WebElement autorElement = producto.findElement(By.cssSelector("span.gandhi-gandhi-components-1-x-productAuthorLabel"));
                            autor = autorElement.getText().trim();
                        } catch (NoSuchElementException e) {}

                        // ------------------- PRECIO -------------------
                        float precio = 0;
                        try {
                            WebElement precioElement = producto.findElement(By.cssSelector("span.vtex-product-price-1-x-currencyInteger"));
                            precio = Float.parseFloat(precioElement.getText().trim().replaceAll("[^0-9]", ""));
                        } catch (NoSuchElementException | NumberFormatException e) {}

                        // ------------------- LINK DETALLE -------------------
                        String linkDetalle = "";
                        try {
                            linkDetalle = producto.findElement(By.cssSelector("a.vtex-product-summary-2-x-clearLink")).getAttribute("href");
                        } catch (NoSuchElementException e) {}

                        // ------------------- SINOPSIS -------------------
                        String sinopsis = "Sinopsis no disponible.";
                        if (!linkDetalle.isEmpty()) {
                            js.executeScript("window.open(arguments[0], '_blank');", linkDetalle);
                            Thread.sleep(1000);

                            String ventanaOriginal = driver.getWindowHandle();
                            for (String ventana : driver.getWindowHandles()) {
                                if (!ventana.equals(ventanaOriginal)) {
                                    driver.switchTo().window(ventana);
                                    break;
                                }
                            }

                            try {
                                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.gandhi-gandhi-components-1-x-productInfoSinopsisText span")));
                                WebElement sinopsisElement = driver.findElement(By.cssSelector("div.gandhi-gandhi-components-1-x-productInfoSinopsisText span"));
                                sinopsis = sinopsisElement.getText().trim();
                            } catch (NoSuchElementException e) {}

                            driver.close();
                            driver.switchTo().window(ventanaOriginal);
                            Thread.sleep(500);
                        }

                        System.out.println("✓ Título: " + tituloObra);
                        System.out.println("  Tomo: " + numeroTomo);
                        System.out.println("  Autor: " + autor);
                        System.out.println("  Precio: $" + precio);
                        System.out.println("  Sinopsis: " + (sinopsis.length() > 100 ? sinopsis.substring(0, 100) + "..." : sinopsis));

                        // ------------------- DESCARGAR IMAGEN -------------------
                        String nombreBase = (tituloObra + "_Tomo_" + numeroTomo + "_" + autor);
                        String nombreImagen = nombreBase
                                .replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\-]", "_")
                                .replaceAll("__+", "_")
                                + ".jpg";

                        byte[] imagenBytes = new byte[0];
                        if (imagenUrl != null && !imagenUrl.isEmpty()) {
                            imagenBytes = descargarImagenComoBytes(imagenUrl);
                            if (imagenBytes.length > 0) {
                                // Guardar imagen en disco también
                                Path archivoImagen = CARPETA_IMAGENES.resolve(nombreImagen);
                                if (!Files.exists(archivoImagen)) {
                                    Files.write(archivoImagen, imagenBytes);
                                    System.out.println("  ↓ Imagen descargada: " + nombreImagen);
                                }
                            }
                        }

                        // ------------------- GUARDAR EN BASE DE DATOS -------------------
                        float tomoNumero = numeroTomo.equals("Desconocido") ? 0 : Float.parseFloat(numeroTomo);

                        try {

                            int idSerie = obtenerOInsertarSerie(conn, tituloObra, sinopsis, autor, tomoNumero);

                            int idImagen = 0;
                            Integer idExistente = obtenerIdImagenPorNombre(conn, nombreImagen);
                            if (idExistente != null) {
                                idImagen = idExistente;
                            } else {
                                if (imagenBytes.length > 0) {
                                    idImagen = insertarImagen(conn, imagenBytes, nombreImagen);
                                }
                            }

                            int idDescripcionManga = insertarDescripcionManga(conn, sinopsis);

                            String claveManga = idSerie + "-" + tomoNumero;

                            if (!cacheMangasExistentes.contains(claveManga)) {
                                insertarManga(conn, LocalDateTime.now(), precio, tomoNumero, idDescripcionManga, idImagen, idSerie);
                                cacheMangasExistentes.add(claveManga);
                                System.out.println("  ✅ Guardado en BD");
                            } else {
                                actualizarMangaSiCambio(conn, idSerie, tomoNumero, precio, idImagen);
                                System.out.println("  🔄 Actualizado en BD");
                            }
                        } catch (Exception e) {
                            System.err.println("  ✗ Error guardando en BD: " + e.getMessage());
                        }

                    } catch (Exception e) {
                        System.err.println("  ✗ Error procesando producto " + (i + 1) + ": " + e.getMessage());
                    }
                }

                pagina++; // siguiente página
            }

            System.out.println("\n=== PROCESO COMPLETADO ===");

        } finally {
            driver.quit();
            if (conn != null) {
                try { conn.close(); }
                catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void cargarCacheBD(Connection conn) throws SQLException {
        System.out.println("📦 Cargando cache de base de datos...");

        // Cargar series existentes
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_serie, LOWER(REPLACE(serie_name, '  ', ' ')) as nombre FROM serie")) {
            while (rs.next()) {
                cacheSeriesIds.put(rs.getString("nombre"), rs.getInt("id_serie"));
            }
        }

        // Cargar mangas existentes
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_serie, volume_number FROM manga")) {
            while (rs.next()) {
                String clave = rs.getInt("id_serie") + "-" + rs.getFloat("volume_number");
                cacheMangasExistentes.add(clave);
            }
        }

        System.out.println("✅ Cache cargado: " + cacheSeriesIds.size() + " series, " + cacheMangasExistentes.size() + " mangas\n");
    }

    private static byte[] descargarImagenComoBytes(String url) {
        try (InputStream in = new URL(url).openStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = in.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            System.err.println("Error descargando imagen: " + e.getMessage());
            return new byte[0];
        }
    }

    // ===================== BASE DE DATOS =====================

    private Integer obtenerIdImagenPorNombre(Connection conn, String nombreImagen) throws SQLException {
        String sql = "SELECT id_manga_imagen FROM manga_imagen WHERE manga_image_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreImagen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_manga_imagen");
                }
            }
        }
        return null;
    }

    private int obtenerOInsertarSerie(Connection conn, String nombreSerie, String descripcion, String autor, float numeroTomo) throws SQLException {
        String nombreNormalizado = nombreSerie.toLowerCase().replaceAll("\\s+", " ").trim();

        // Buscar en cache primero
        if (cacheSeriesIds.containsKey(nombreNormalizado)) {
            int idSerie = cacheSeriesIds.get(nombreNormalizado);
            actualizarTotalesSerie(conn, idSerie, numeroTomo);
            return idSerie;
        }

        // Buscar en BD
        String select = "SELECT id_serie, serie_totals FROM serie WHERE LOWER(REPLACE(serie_name, '  ', ' ')) = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, nombreNormalizado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idSerie = rs.getInt("id_serie");
                cacheSeriesIds.put(nombreNormalizado, idSerie);
                actualizarTotalesSerie(conn, idSerie, numeroTomo);
                return idSerie;
            }
        }

        // Insertar nueva serie
        int idDesc = insertarDescripcionSerie(conn, descripcion);
        String insert = "INSERT INTO serie (serie_name, serie_totals, author_name, id_descripcion_serie) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreSerie);
            ps.setInt(2, (int) numeroTomo);
            ps.setString(3, autor);
            ps.setInt(4, idDesc);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int idSerie = rs.getInt(1);
                cacheSeriesIds.put(nombreNormalizado, idSerie);
                return idSerie;
            }
        }
        return -1;
    }

    private void actualizarTotalesSerie(Connection conn, int idSerie, float numeroTomo) throws SQLException {
        if (numeroTomo == 0) return;

        String update = "UPDATE serie SET serie_totals = GREATEST(serie_totals, ?) WHERE id_serie = ?";
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setInt(1, (int) numeroTomo);
            ps.setInt(2, idSerie);
            ps.executeUpdate();
        }
    }

    private void actualizarMangaSiCambio(Connection conn, int idSerie, float volumen, float nuevoPrecio, int nuevaImagen) throws SQLException {
        String select = "SELECT manga_price, id_manga_imagen FROM manga WHERE id_serie = ? AND volume_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setInt(1, idSerie);
            ps.setFloat(2, volumen);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                float precioActual = rs.getFloat("manga_price");
                int imagenActual = rs.getInt("id_manga_imagen");

                boolean cambio = false;
                StringBuilder update = new StringBuilder("UPDATE manga SET ");
                List<Object> params = new java.util.ArrayList<>();

                if (Math.abs(precioActual - nuevoPrecio) > 0.01) {
                    update.append("manga_price = ?, ");
                    params.add(nuevoPrecio);
                    cambio = true;
                }

                if (nuevaImagen > 0 && imagenActual != nuevaImagen) {
                    update.append("id_manga_imagen = ?, ");
                    params.add(nuevaImagen);
                    cambio = true;
                }

                if (cambio) {
                    update.setLength(update.length() - 2);
                    update.append(" WHERE id_serie = ? AND volume_number = ?");
                    params.add(idSerie);
                    params.add(volumen);

                    try (PreparedStatement psUpd = conn.prepareStatement(update.toString())) {
                        for (int i = 0; i < params.size(); i++) {
                            psUpd.setObject(i + 1, params.get(i));
                        }
                        psUpd.executeUpdate();
                    }
                }
            }
        }
    }

    private int insertarDescripcionSerie(Connection conn, String descripcion) throws SQLException {
        String insert = "INSERT INTO descripcion_serie (description_serie) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, descripcion);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private int insertarDescripcionManga(Connection conn, String descripcion) throws SQLException {
        String insert = "INSERT INTO descripcion_manga (description_manga) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, descripcion);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private int insertarImagen(Connection conn, byte[] imagen, String nombre) throws SQLException {
        String insert = "INSERT INTO manga_imagen (manga_image_file, manga_image_name) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBytes(1, imagen);
            ps.setString(2, nombre);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private void insertarManga(Connection conn, LocalDateTime fecha, float precio, float volumen,
                               int idDesc, int idImagen, int idSerie) throws SQLException {
        String insert = "INSERT INTO manga (manga_date, manga_price, volume_number, id_desc, id_manga_imagen, id_serie) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setTimestamp(1, Timestamp.valueOf(fecha));
            ps.setFloat(2, precio);
            ps.setFloat(3, volumen);
            ps.setInt(4, idDesc);
            ps.setInt(5, idImagen);
            ps.setInt(6, idSerie);
            ps.executeUpdate();
        }
    }

    private String normalizarNombreSerie(String nombre) {
        if (nombre == null) return "";
        return nombre.trim()
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-z0-9áéíóúñ\\s]", "")
                .strip();
    }

    private boolean serieExiste(Connection conn, String nombreNormalizado) {
        String sql = "SELECT id_serie FROM serie WHERE LOWER(TRIM(serie_name)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreNormalizado);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Integer obtenerIdSerie(Connection conn, String nombreNormalizado) {
        String sql = "SELECT id_serie FROM serie WHERE LOWER(TRIM(serie_name)) = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombreNormalizado);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_serie");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean mangaExiste(Connection conn, int idSerie, float volumen) {
        String sql = "SELECT COUNT(*) FROM manga WHERE id_serie = ? AND volume_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSerie);
            stmt.setFloat(2, volumen);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}