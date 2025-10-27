package mk.coleccion.scraping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class PruebaConexionBaseDeDatos {

    private static final int PAGINA_INICIO = 1;
    private static final int PAGINA_FIN = 449;
    private static final int NUM_HILOS = 24;
    private static final int TIMEOUT = 30000;

    // Configuración de la BD
    private static final String DB_URL = "jdbc:mysql://localhost:3306/manga_app_prueba_integracion";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "admin";

    private static final Path CARPETA_FALLIDOS = Path.of("Mangas faltantes");
    private static final Path ARCHIVO_FALLIDOS = CARPETA_FALLIDOS.resolve("mangas_fallidos.txt");
    private final List<String> mangasFallidos = new ArrayList<>();

    public static void main(String[] args) {
        new PruebaConexionBaseDeDatos().iniciarScraping();
    }

    private void iniciarScraping() {
        // Inicializar carpeta y archivo de mangas fallidos
        inicializarArchivoFallidos();

        long inicio = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_HILOS);

        WebDriverManager.chromedriver().setup();
        ChromeOptions opcionesBase = getOpcionesHeadless();

        System.out.println("✅ Iniciando scraping sin conexión persistente");

        try {
            for (int pagina = PAGINA_INICIO; pagina <= PAGINA_FIN; pagina++) {
                String url = "https://tiendapanini.com.mx/coleccionables/item-3?p=" + pagina;
                System.out.println("\n=== Procesando página " + pagina + " ===");

                WebDriver driver = crearDriverConReintentos(opcionesBase);
                driver.get(url);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(TIMEOUT));
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("li.item.product.product-item")));

                List<WebElement> productos = driver.findElements(By.cssSelector("li.item.product.product-item"));
                System.out.println("Productos encontrados: " + productos.size());

                for (WebElement producto : productos) {
                    String html = producto.getAttribute("outerHTML");
                    int paginaActual = pagina; // Necesario para usar dentro de lambda
                    executor.submit(() -> procesarProducto(html, opcionesBase, paginaActual));
                    try { Thread.sleep(100 + (int) (Math.random() * 400)); } catch (InterruptedException ignored) {}
                }

                driver.quit();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            while (!executor.isTerminated()) {}
            System.out.println("\n✅ Finalizado en " + (System.currentTimeMillis() - inicio) / 1000.0 + " s");
        }
    }


    private void procesarProducto(String html, ChromeOptions opciones, int pagina) {
        WebDriver driver = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            driver = new ChromeDriver(opciones);
            driver.get("data:text/html;charset=utf-8," + html);

            // ===== Nombre y enlace del producto =====
            WebElement nombreTag = driver.findElement(By.cssSelector("a.product-item-link"));
            String nombreProducto = nombreTag.getText().trim();
            String enlaceProducto = nombreTag.getAttribute("href");

            // ===== Número de tomo =====
            Matcher matcher = Pattern.compile("N\\.(\\d+)").matcher(nombreProducto);
            String numeroTomo = matcher.find() ? matcher.group(1) : "0";
            float tomoNumero = Float.parseFloat(numeroTomo);

            // ===== Imagen principal =====
            WebElement imgTag = driver.findElement(By.cssSelector("img.product-image-photo"));
            String imagenUrl = imgTag.getAttribute("src").split("\\?")[0] +
                    "?optimize=none&fit=bounds&width=320&height=452&canvas=320:452";

            // Verificar placeholder
            if (imagenUrl.contains("panini-placeholder.png")) {
                String mensajeFallido = "Página " + pagina + " - " + nombreProducto + " - Imagen placeholder";
                registrarMangaFallido(mensajeFallido);
                System.out.println("❌ Manga con imagen placeholder: " + nombreProducto);
                return;
            }

            // Descargar imagen
            byte[] imagenBytes = descargarImagenComoBytes(imagenUrl);
            if (imagenBytes.length == 0) {
                String mensajeFallido = "Página " + pagina + " - " + nombreProducto + " - Error descargando imagen";
                registrarMangaFallido(mensajeFallido);
                return;
            }

            String nombreImagen = numeroTomo + "_" + nombreProducto.replace(" ", "_").replace("/", "-") + ".jpg";

            // ===== Ir al detalle del producto =====
            try {
                driver.get(enlaceProducto);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(TIMEOUT));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("span.base[itemprop=name]")));
            } catch (TimeoutException e) {
                String mensajeFallido = "Página " + pagina + " - " + nombreProducto + " - No se encontró nombre del manga";
                registrarMangaFallido(mensajeFallido);
                return;
            }

            String titulo = obtenerTextoSeguro(driver, "span.base[itemprop=name]");
            if (titulo.equals("No disponible")) {
                String mensajeFallido = "Página " + pagina + " - " + nombreProducto + " - Título no disponible";
                registrarMangaFallido(mensajeFallido);
                return;
            }

            String descripcion = obtenerTextoSeguro(driver, "div.value[itemprop=description]");
            String precioStr = obtenerAtributoSeguro(driver, "meta[itemprop=price]", "content");
            float precio = 0;
            try { precio = Float.parseFloat(precioStr); } catch (Exception ignored) {}

            String nombreSerie = titulo.replaceAll("(?i)Tomo.*", "").trim();
            nombreSerie = nombreSerie.replaceAll("\\s*N\\.?\\s*\\d+", "").trim();
            String autor = "Desconocido";

            // ===== Guardar en BD =====
            int idSerie = obtenerOInsertarSerie(conn, nombreSerie, descripcion, autor, tomoNumero);
            int idImagen = insertarImagen(conn, imagenBytes, nombreImagen);
            int idDescripcionManga = insertarDescripcionManga(conn, descripcion);

            if (!mangaExiste(conn, idSerie, tomoNumero)) {
                insertarManga(conn, LocalDateTime.now(), precio, tomoNumero, idDescripcionManga, idImagen, idSerie);
                synchronized (System.out) {
                    System.out.println("✅ Insertado manga: " + titulo + " (" + nombreSerie + ")");
                }
            } else {
                synchronized (System.out) {
                    System.out.println("⚠️ Manga ya existente: " + titulo);
                }
            }

        } catch (Exception e) {
            String mensajeFallido = "Página " + pagina + " - Error procesando manga: " + e.getMessage();
            registrarMangaFallido(mensajeFallido);
            System.out.println("❌ " + mensajeFallido);
        } finally {
            if (driver != null) driver.quit();
        }
    }


    private void logError(String mensaje) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Mangas_faltantes/error_log.txt", true))) {
            writer.write(mensaje);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error al escribir en el archivo de log: " + e.getMessage());
        }
    }


    // ========== MÉTODOS DE BASE DE DATOS ==========

    private int obtenerOInsertarSerie(Connection conn, String nombreSerie, String descripcion, String autor, float numeroTomo) throws SQLException {
        String selectQuery = "SELECT id_serie, serie_totals, id_descripcion_serie FROM serie WHERE serie_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectQuery)) {
            ps.setString(1, nombreSerie);
            ResultSet rs = ps.executeQuery();

            // Ya existe la serie
            if (rs.next()) {
                int idSerie = rs.getInt("id_serie");
                int serieTotals = rs.getInt("serie_totals");
                int idDescSerie = rs.getInt("id_descripcion_serie");

                // ✅ 1. Actualizar serie_totals si encontramos un tomo mayor
                if (numeroTomo > serieTotals) {
                    try (PreparedStatement psUpdate = conn.prepareStatement(
                            "UPDATE serie SET serie_totals = ? WHERE id_serie = ?")) {
                        psUpdate.setInt(1, (int) numeroTomo);
                        psUpdate.setInt(2, idSerie);
                        psUpdate.executeUpdate();
                    }
                    synchronized (System.out) {
                        System.out.println("🔁 Actualizado total de tomos para " + nombreSerie + ": " + (int) numeroTomo);
                    }
                }

                // ✅ 2. Si es tomo 1 y la descripción está vacía, actualizar descripción_serie
                if (numeroTomo == 1 && descripcion != null && !descripcion.isEmpty()) {
                    try (PreparedStatement psCheckDesc = conn.prepareStatement(
                            "SELECT description_serie FROM descripcion_serie WHERE id_descripcion_serie = ?")) {
                        psCheckDesc.setInt(1, idDescSerie);
                        ResultSet rsDesc = psCheckDesc.executeQuery();
                        if (rsDesc.next() && (rsDesc.getString(1) == null || rsDesc.getString(1).isBlank())) {
                            try (PreparedStatement psUpdateDesc = conn.prepareStatement(
                                    "UPDATE descripcion_serie SET description_serie = ? WHERE id_descripcion_serie = ?")) {
                                psUpdateDesc.setString(1, descripcion);
                                psUpdateDesc.setInt(2, idDescSerie);
                                psUpdateDesc.executeUpdate();
                                synchronized (System.out) {
                                    System.out.println("📝 Descripción de serie actualizada con la del tomo 1: " + nombreSerie);
                                }
                            }
                        }
                    }
                }

                return idSerie;
            }
        }

        // ✅ Si la serie NO existe, la creamos normalmente
        int idDesc = insertarDescripcionSerie(conn, (numeroTomo == 1) ? descripcion : "");
        String insert = "INSERT INTO serie (serie_name, serie_totals, author_name, id_descripcion_serie) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreSerie);
            ps.setInt(2, (int) numeroTomo);
            ps.setString(3, autor);
            ps.setInt(4, idDesc);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
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

    private boolean mangaExiste(Connection conn, int idSerie, float numeroTomo) throws SQLException {
        String query = "SELECT COUNT(*) FROM manga WHERE id_serie = ? AND volume_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSerie);
            ps.setFloat(2, numeroTomo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
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

    // ========== UTILIDADES ==========

    private void inicializarArchivoFallidos() {
        try {
            if (!Files.exists(CARPETA_FALLIDOS)) Files.createDirectory(CARPETA_FALLIDOS);
            if (!Files.exists(ARCHIVO_FALLIDOS)) Files.createFile(ARCHIVO_FALLIDOS);
        } catch (IOException e) {
            System.out.println("❌ No se pudo crear carpeta o archivo para mangas fallidos: " + e.getMessage());
        }
    }

    private void registrarMangaFallido(String mensaje) {
        mangasFallidos.add(mensaje);
        try {
            Files.writeString(ARCHIVO_FALLIDOS, mensaje + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("❌ No se pudo escribir en el archivo de mangas fallidos: " + e.getMessage());
        }
    }

    private static byte[] descargarImagenComoBytes(String url) {
        try (InputStream in = new BufferedInputStream(new URL(url).openStream());
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[2048];
            int nRead;
            while ((nRead = in.read(data)) != -1) buffer.write(data, 0, nRead);
            return buffer.toByteArray();
        } catch (Exception e) {
            System.out.println("Error descargando imagen: " + e.getMessage());
            return new byte[0];
        }
    }

    private static WebDriver crearDriverConReintentos(ChromeOptions opciones) throws Exception {
        for (int i = 1; i <= 3; i++) {
            try { return new ChromeDriver(opciones); }
            catch (Exception e) {
                System.out.println("⚠️ Falló inicio Chrome (intento " + i + ")");
                Thread.sleep(2000);
            }
        }
        throw new Exception("No se pudo iniciar Chrome");
    }

    private static ChromeOptions getOpcionesHeadless() {
        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
        return opciones;
    }

    private static String obtenerTextoSeguro(WebDriver driver, String selector) {
        try { return driver.findElement(By.cssSelector(selector)).getText().trim(); }
        catch (Exception e) { return "No disponible"; }
    }

    private static String obtenerAtributoSeguro(WebDriver driver, String selector, String attr) {
        try { return driver.findElement(By.cssSelector(selector)).getAttribute(attr); }
        catch (Exception e) { return "0"; }
    }
}

