package mk.coleccion.scraping;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KamiteScraperPrueba {

    private static final int PAGINA_INICIO = 3;
    private static final int PAGINA_FIN = 3;
    private static final int NUM_THREADS_NAVEGADORES = 1; // Múltiples navegadores en paralelo

    private static final String DB_URL = "jdbc:mysql://localhost:3306/manga_app_prueba_integracion";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "admin";

    private static final Path CARPETA_FALLIDOS = Path.of("Mangas_fallidos");
    private static final Path CARPETA_IMAGENES = Path.of("imagenes_kamite");
    private static final Path ARCHIVO_FALLIDOS = CARPETA_FALLIDOS.resolve("mangas_fallidos_Kamite.txt");

    private final ExecutorService navegadorExecutor = Executors.newFixedThreadPool(NUM_THREADS_NAVEGADORES);
    private final ExecutorService imgExecutor = Executors.newFixedThreadPool(8);

    // Cache para evitar consultas repetidas a BD
    private final Map<String, Integer> cacheSeriesIds = new ConcurrentHashMap<>();
    private final Set<String> cacheMangasExistentes = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        new KamiteScraperPrueba().iniciarScraping();
    }

    private void iniciarScraping() {
        inicializarArchivos();
        long inicio = System.currentTimeMillis();

        WebDriverManager.chromedriver().setup();
        System.out.println("✅ Iniciando scraping Kamite con " + NUM_THREADS_NAVEGADORES + " navegadores paralelos");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            cargarCacheBD(conn);

            List<Future<?>> tareas = new ArrayList<>();

            for (int pagina = PAGINA_INICIO; pagina <= PAGINA_FIN; pagina++) {
                final int paginaFinal = pagina;
                Future<?> tarea = navegadorExecutor.submit(() -> procesarPagina(paginaFinal));
                tareas.add(tarea);
            }

            // Esperar a que todas las tareas terminen
            for (Future<?> tarea : tareas) {
                try { tarea.get(); }
                catch (Exception e) { e.printStackTrace(); }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            navegadorExecutor.shutdown();
            imgExecutor.shutdown();
            try {
                navegadorExecutor.awaitTermination(5, TimeUnit.MINUTES);
                imgExecutor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\n✅ Finalizado en " + (System.currentTimeMillis() - inicio)/1000.0 + " s");
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

        System.out.println("✅ Cache cargado: " + cacheSeriesIds.size() + " series, " + cacheMangasExistentes.size() + " mangas");
    }

    private void procesarPagina(int pagina) {
        ChromeOptions opciones = getOpcionesHeadless();
        WebDriver driver = null;
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            String url = "https://www.kamite.com.mx/mangas/page/" + pagina + "/";
            System.out.println("\n[Página " + pagina + "] Iniciando...");

            driver = crearDriverConReintentos(opciones);
            driver.get(url);

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("div.js-item-description.item-description")));

            List<WebElement> productos = driver.findElements(By.cssSelector("div.js-item-description.item-description"));
            if (productos.isEmpty()) {
                System.out.println("[Página " + pagina + "] ❌ No se encontraron productos");
                return;
            }

            // Extraer todos los enlaces
            List<String> enlaces = new ArrayList<>();
            for (WebElement producto : productos) {
                try {
                    String enlaceProducto = producto.findElement(By.cssSelector("a.item-link")).getAttribute("href");
                    enlaces.add(enlaceProducto);
                } catch (Exception e) {
                    System.out.println("[Página " + pagina + "] ⚠️ Error extrayendo enlace");
                }
            }

            System.out.println("[Página " + pagina + "] 📋 Encontrados " + enlaces.size() + " productos");

            // Procesar cada enlace
            for (int i = 0; i < enlaces.size(); i++) {
                String enlaceProducto = enlaces.get(i);
                try {
                    procesarManga(driver, conn, enlaceProducto, pagina, i + 1, enlaces.size());
                } catch (Exception e) {
                    String errorMsg = "Página " + pagina + " - Enlace " + (i+1) + " - Error: " + e.getMessage();
                    registrarMangaFallido(errorMsg);
                    System.out.println("[Página " + pagina + "] ❌ " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("[Página " + pagina + "] ❌ Error general: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
            if (conn != null) {
                try { conn.close(); }
                catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void procesarManga(WebDriver driver, Connection conn, String url, int pagina, int num, int total) throws Exception {
        driver.get(url);

        // Extraer nombre primero para logs de error
        String nombreSerieOriginal = "Desconocido";
        String numeroTomo = "Desconocido";

        try {
            // Intentamos primero con el h1
            nombreSerieOriginal = driver.findElement(By.cssSelector("h1[class*='js-product-name']")).getText().trim();

            // Si está vacío, pasamos a tomar el título
            if (nombreSerieOriginal.isEmpty()) {
                throw new Exception("Vacío, intentar con <title>");
            }
        } catch (Exception e) {
            try {
                // Si falla el h1, tomamos el contenido del <title>
                String tituloPagina = driver.getTitle().trim();


                tituloPagina = tituloPagina.replaceFirst("\\s*\\d+.*", "");

                // Asignamos el resultado limpio
                nombreSerieOriginal = tituloPagina.trim();

            } catch (Exception ex) {
                throw new Exception("No se pudo extraer el nombre del manga ni desde el h1 ni desde el título: " + ex.getMessage());
            }
        }


        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        int reintentos = 0;
        boolean cargada = false;

        while (reintentos < 3 && !cargada) {
            try {
                // Esperamos que aparezca la descripción
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.px-md-3.pb-md-4")));

                // Verificamos que no sea una página de bloqueo
                if (driver.getTitle().contains("Just a moment") || driver.getPageSource().contains("Just a moment")) {
                    throw new Exception("Página bloqueada temporalmente");
                }

                cargada = true; // Se cargó correctamente

            } catch (Exception e) {
                reintentos++;
                if (reintentos < 3) {
                    System.out.println("⚠️ Intento " + reintentos + " fallido (" + nombreSerieOriginal + "). Reintentando...");
                    Thread.sleep(5000); // Espera 5 segundos antes de reintentar
                    driver.navigate().refresh(); // Recarga la página
                } else {
                    throw new Exception("❌ Timeout esperando descripción - Manga: " + nombreSerieOriginal);
                }
            }
        }


        // Extraer datos
        String nombreSerie = limpiarNombreSerie(nombreSerieOriginal);
        String descripcionHtml = driver.findElement(By.cssSelector("div.px-md-3.pb-md-4")).getAttribute("innerHTML");

        numeroTomo = extraerNumeroTomo(descripcionHtml, nombreSerieOriginal);
        String autor = extraerAutor(descripcionHtml);
        String sinopsis = extraerSinopsis(descripcionHtml);
        float precio = extraerPrecio(driver, nombreSerie);

        WebElement imgTag = driver.findElement(By.cssSelector("img.js-product-slide-img"));
        String imagenUrl = imgTag.getAttribute("src");
        if (imagenUrl.startsWith("//")) imagenUrl = "https:" + imagenUrl;

        byte[] imagenBytes = descargarImagenComoBytes(imagenUrl);
        if (imagenBytes.length == 0) {
            throw new Exception("Error descargando imagen - Manga: " + nombreSerie + " Tomo " + numeroTomo);
        }

        // Limpiar nombre para archivo (quitar caracteres ilegales de Windows)
        String nombreImagen = limpiarNombreArchivo(nombreSerie) + "_Tomo_" + numeroTomo + ".webp";
        Path archivoImagen = CARPETA_IMAGENES.resolve(nombreImagen);

        // Guardar imagen en paralelo
        byte[] imagenBytesFinal = imagenBytes;
        imgExecutor.submit(() -> {
            try { Files.write(archivoImagen, imagenBytesFinal); }
            catch (IOException e) { System.out.println("❌ Error guardando imagen: " + e.getMessage()); }
        });

        // Insertar/Actualizar en DB
        float tomoNumero = numeroTomo.equals("Desconocido") ? 0 : Float.parseFloat(numeroTomo);

        try {
            synchronized (this) {
                int idSerie = obtenerOInsertarSerie(conn, nombreSerie, sinopsis, autor, tomoNumero);

                int idImagen = 0;
                Integer idExistente = obtenerIdImagenPorNombre(conn, nombreImagen);
                if (idExistente != null) {
                    idImagen = idExistente;
                    System.out.println("⚠️ Imagen ya existe: " + nombreImagen);
                } else {
                    idImagen = insertarImagen(conn, imagenBytes, nombreImagen);
                }

                int idDescripcionManga = insertarDescripcionManga(conn, sinopsis);

                String claveManga = idSerie + "-" + tomoNumero;

                if (!cacheMangasExistentes.contains(claveManga)) {
                    insertarManga(conn, LocalDateTime.now(), precio, tomoNumero, idDescripcionManga, idImagen, idSerie);
                    cacheMangasExistentes.add(claveManga);
                    System.out.println("[" + pagina + "|" + num + "/" + total + "] ✅ " + nombreSerie + " #" + numeroTomo + " $" + precio);
                } else {
                    // Actualizar precio si cambió
                    actualizarMangaSiCambio(conn, idSerie, tomoNumero, precio, idImagen);
                    System.out.println("[" + pagina + "|" + num + "/" + total + "] 🔄 Actualizado: " + nombreSerie + " #" + numeroTomo);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error BD - Manga: " + nombreSerie + " Tomo " + numeroTomo + " - " + e.getMessage());
        }
    }

    // ===================== UTILIDADES =====================

    /**
     * Limpia caracteres ilegales para nombres de archivo en Windows
     * Mantiene el texto original para la base de datos
     */
    private Integer obtenerIdImagenPorNombre(Connection conn, String nombreImagen) throws SQLException {
        String sql = "SELECT id_manga_imagen FROM manga_imagen WHERE manga_image_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreImagen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_manga_imagen"); // Retorna el id existente
                }
            }
        }
        return null; // No existe
    }


    private String limpiarNombreArchivo(String nombre) {
        // Caracteres ilegales en Windows: < > : " / \ | ? * y caracteres especiales
        String limpio = nombre
                .replace("<", "")
                .replace(">", "")
                .replace(":", "")
                .replace("\"", "")
                .replace("/", "-")
                .replace("\\", "-")
                .replace("|", "-")
                .replace("?", "")
                .replace("*", "")
                .replace("¿", "")
                .replace("¡", "")
                .replace("!", "")
                .replace(" ", "_");

        // Eliminar cualquier otro caracter no ASCII que pueda causar problemas
        limpio = limpio.replaceAll("[^a-zA-Z0-9_\\-áéíóúÁÉÍÓÚñÑ]", "");

        return limpio.trim();
    }

    private String limpiarNombreSerie(String nombreOriginal) {
        String limpio = nombreOriginal.trim();

        // Quitar "NOVELA LIGERA", "TOMO ÚNICO" (case insensitive)
        limpio = limpio.replaceAll("(?i)\\s*NOVELA\\s+LIGERA\\s*", " ");
        limpio = limpio.replaceAll("(?i)\\s*TOMO\\s+ÚNICO\\s*", " ");
        limpio = limpio.replaceAll("(?i)\\s*TOMO\\s+UNICO\\s*", " ");
        limpio = limpio.replaceAll("(?i)\\s*MANGA\\s*", " ");


        // Quitar número al final
        limpio = limpio.replaceAll("\\s+\\d+\\s*$", "");

        return limpio.trim();
    }

    private String extraerNumeroTomo(String descripcionHtml, String nombreSerieOriginal) {
        // Patrón 1: En HTML con formato <p><strong>TOMO X </strong> o <strong>TOMO X&nbsp;</strong>
        Matcher tomoMatcher1 = Pattern.compile("<p><strong>TOMO\\s+(\\d+)\\s*(?:</strong>|&nbsp;)").matcher(descripcionHtml);
        if (tomoMatcher1.find()) {
            return tomoMatcher1.group(1);
        }

        // Patrón 2: <strong>TOMO X&nbsp;</strong> (sin <p>)
        Matcher tomoMatcher2 = Pattern.compile("<strong>TOMO\\s+(\\d+)\\s*(?:</strong>|&nbsp;)").matcher(descripcionHtml);
        if (tomoMatcher2.find()) {
            return tomoMatcher2.group(1);
        }

        // Patrón 3: En HTML con formato <p class="MsoNormal"><strong>TOMO X</strong></p>
        Matcher tomoMatcher3 = Pattern.compile("<p class=\"MsoNormal\"><strong>TOMO\\s+(\\d+)\\s*</strong></p>").matcher(descripcionHtml);
        if (tomoMatcher3.find()) {
            return tomoMatcher3.group(1);
        }

        // Patrón 4: En el título al final
        Matcher tomoTituloMatcher = Pattern.compile("\\s+(\\d+)\\s*$").matcher(nombreSerieOriginal);
        if (tomoTituloMatcher.find()) {
            return tomoTituloMatcher.group(1);
        }

        return "Desconocido";
    }

    private String extraerAutor(String descripcionHtml) {
        // Patrón 1: <p><strong>AUTOR</strong>:&nbsp; seguido de texto
        Matcher autorMatcher1 = Pattern.compile(
                "<p><strong>AUTOR</strong>:&nbsp;\\s*(.*?)</p>",
                Pattern.DOTALL
        ).matcher(descripcionHtml);
        if (autorMatcher1.find()) {
            return limpiarHtmlTags(autorMatcher1.group(1).trim());
        }

        // Patrón 2: <p class="MsoNormal"><strong>AUTOR</strong>:&nbsp;</p> seguido de <p class="MsoNormal">contenido</p>
        Matcher autorMatcher2 = Pattern.compile(
                "<p class=\"MsoNormal\"><strong>AUTOR</strong>:&nbsp;</p>\\s*<p class=\"MsoNormal\">(.*?)</p>",
                Pattern.DOTALL
        ).matcher(descripcionHtml);
        if (autorMatcher2.find()) {
            return limpiarHtmlTags(autorMatcher2.group(1).trim());
        }

        // Patrón 3: <p><strong>AUTOR</strong>:</p> seguido de <p>contenido</p> (sin &nbsp;)
        Matcher autorMatcher3 = Pattern.compile(
                "<p><strong>AUTOR</strong>:</p>\\s*<p>(.*?)</p>",
                Pattern.DOTALL
        ).matcher(descripcionHtml);
        if (autorMatcher3.find()) {
            return limpiarHtmlTags(autorMatcher3.group(1).trim());
        }

        return "Desconocido";
    }


    private String extraerSinopsis(String descripcionHtml) {
        // Patrón 1: <p><strong>SINOPSIS</strong>:</p> seguido de <p>contenido</p>
        Matcher sinopsisMatcher1 = Pattern.compile(
                "<p><strong>SINOPSIS</strong>:</p>\\s*<p>(.*?)</p>",
                Pattern.DOTALL
        ).matcher(descripcionHtml);
        if (sinopsisMatcher1.find()) {
            return limpiarHtmlTags(sinopsisMatcher1.group(1).trim());
        }

        // Patrón 2: <p class="MsoNormal"><strong>SINOPSIS</strong>:</p> seguido de <p class="MsoNormal">contenido</p>
        Matcher sinopsisMatcher2 = Pattern.compile(
                "<p class=\"MsoNormal\"><strong>SINOPSIS</strong>:</p>\\s*<p class=\"MsoNormal\">(.*?)</p>",
                Pattern.DOTALL
        ).matcher(descripcionHtml);
        if (sinopsisMatcher2.find()) {
            return limpiarHtmlTags(sinopsisMatcher2.group(1).trim());
        }

        // Patrón 3: <p class="MsoNormal"><strong>SINOPSIS</strong>:</p> seguido de <p>contenido</p> (sin clase)
        Matcher sinopsisMatcher3 = Pattern.compile(
                "<p class=\"MsoNormal\"><strong>SINOPSIS</strong>:</p>\\s*<p>(.*?)</p>",
                Pattern.DOTALL
        ).matcher(descripcionHtml);
        if (sinopsisMatcher3.find()) {
            return limpiarHtmlTags(sinopsisMatcher3.group(1).trim());
        }

        return "Sin sinopsis";
    }

    /**
     * Limpia tags HTML y entidades del texto
     */
    private String limpiarHtmlTags(String html) {
        return html
                .replaceAll("<[^>]+>", "") // Quitar tags HTML
                .replace("&nbsp;", " ")     // Reemplazar espacios HTML
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replaceAll("\\s+", " ")    // Normalizar espacios múltiples
                .trim();
    }

    private float extraerPrecio(WebDriver driver, String nombreSerie) {
        try {
            WebElement precioElement = driver.findElement(By.cssSelector("span.js-price-display[data-product-price]"));
            String precioStr = precioElement.getAttribute("data-product-price");
            return Float.parseFloat(precioStr) / 100;
        } catch (Exception e) {
            return 0;
        }
    }

    private void inicializarArchivos() {
        try {
            if (!Files.exists(CARPETA_FALLIDOS)) Files.createDirectory(CARPETA_FALLIDOS);
            if (!Files.exists(ARCHIVO_FALLIDOS)) Files.createFile(ARCHIVO_FALLIDOS);
            if (!Files.exists(CARPETA_IMAGENES)) Files.createDirectory(CARPETA_IMAGENES);
        } catch (IOException e) {
            System.out.println("❌ Error creando carpetas: " + e.getMessage());
        }
    }

    private synchronized void registrarMangaFallido(String mensaje) {
        try { Files.writeString(ARCHIVO_FALLIDOS, mensaje + "\n", StandardOpenOption.APPEND); }
        catch (IOException e) { System.out.println("❌ Error escribiendo fallidos: " + e.getMessage()); }
    }

    private static byte[] descargarImagenComoBytes(String url) {
        try (InputStream in = new BufferedInputStream(new URL(url).openStream());
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = in.read(data)) != -1) buffer.write(data, 0, nRead);
            return buffer.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static ChromeOptions getOpcionesHeadless() {
        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments(
                "--headless=new",
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-images", // No cargar imágenes en navegador (más rápido)
                "--blink-settings=imagesEnabled=false",
                "--window-size=1920,1080"
        );
        opciones.setPageLoadStrategy(PageLoadStrategy.EAGER); // No esperar recursos completos
        return opciones;
    }

    private static WebDriver crearDriverConReintentos(ChromeOptions opciones) throws Exception {
        for (int i = 1; i <= 3; i++) {
            try { return new ChromeDriver(opciones); }
            catch (Exception e) {
                Thread.sleep(1000);
            }
        }
        throw new Exception("No se pudo iniciar Chrome");
    }

    // ===================== BASE DE DATOS =====================

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
                List<Object> params = new ArrayList<>();

                if (Math.abs(precioActual - nuevoPrecio) > 0.01) {
                    update.append("manga_price = ?, ");
                    params.add(nuevoPrecio);
                    cambio = true;
                }

                if (imagenActual != nuevaImagen) {
                    update.append("id_manga_imagen = ?, ");
                    params.add(nuevaImagen);
                    cambio = true;
                }

                if (cambio) {
                    update.setLength(update.length() - 2); // Quitar última coma
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
}