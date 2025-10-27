package mk.coleccion.controlador.APP;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import org.springframework.stereotype.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Controller
public class AutomatizacionController {

    @FXML
    private Button btnWebScrapingPanini;

    @FXML
    private Button btnWebScrapingKamite;

    @FXML
    private Button btnWebScrapingGandhi;

    @FXML
    private TextArea txtLog;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        btnWebScrapingPanini.setOnAction(event -> ejecutarScraperPanini());
        btnWebScrapingKamite.setOnAction(event -> ejecutarScraperKamite());
        btnWebScrapingGandhi.setOnAction(event -> ejecutarScraperGandhi());

        progressIndicator.setVisible(false);
    }

    private void ejecutarScraperPanini() {
        ejecutarScraper("Panini", "PruebaConexionBaseDeDatos.java");
    }

    private void ejecutarScraperKamite() {
        ejecutarScraper("Kamite", "KamiteScraperPrueba.java");
    }

    private void ejecutarScraperGandhi() {
        ejecutarScraper("Gandhi / Distrito", "GandhiScraperConBD.java");
    }

    private void ejecutarScraper(String nombreScraper, String archivoJava) {
        // Confirmar antes de ejecutar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Ejecución");
        confirmacion.setHeaderText("¿Ejecutar Web Scraping de " + nombreScraper + "?");
        confirmacion.setContentText("Este proceso puede tardar varios minutos.\n\n" +
                "ADVERTENCIA: Asegúrese de que:\n" +
                "- La conexión a internet esté activa\n" +
                "- La base de datos esté disponible\n" +
                "- No haya otros procesos de scraping ejecutándose");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // Deshabilitar botones durante la ejecución
        btnWebScrapingPanini.setDisable(true);
        btnWebScrapingKamite.setDisable(true);
        btnWebScrapingGandhi.setDisable(true);
        progressIndicator.setVisible(true);

        txtLog.clear();
        agregarLog("=== INICIANDO WEB SCRAPING DE " + nombreScraper.toUpperCase() + " ===");
        agregarLog("Archivo: " + archivoJava);
        agregarLog("Hora de inicio: " + java.time.LocalDateTime.now());
        agregarLog("");

        // Ejecutar en un hilo separado para no bloquear la UI
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Ruta al archivo Java (ajustar según la estructura del proyecto)
                    String rutaArchivo = "src/main/java/mk/coleccion/scraping/" + archivoJava;

                    agregarLog("Compilando " + archivoJava + "...");

                    // Compilar el archivo Java
                    ProcessBuilder compilerBuilder = new ProcessBuilder(
                            "javac",
                            "-cp", getClasspath(),
                            rutaArchivo
                    );
                    compilerBuilder.redirectErrorStream(true);
                    Process compileProcess = compilerBuilder.start();

                    capturarSalida(compileProcess);
                    int compileExitCode = compileProcess.waitFor();

                    if (compileExitCode != 0) {
                        agregarLog("ERROR: Falló la compilación");
                        return null;
                    }

                    agregarLog("Compilación exitosa. Ejecutando scraper...");
                    agregarLog("");

                    // Ejecutar el programa
                    String className = archivoJava.replace(".java", "");
                    ProcessBuilder runBuilder = new ProcessBuilder(
                            "java",
                            "-cp", getClasspath(),
                            "mk.coleccion.scraping." + className
                    );
                    runBuilder.redirectErrorStream(true);
                    Process runProcess = runBuilder.start();

                    capturarSalida(runProcess);
                    int runExitCode = runProcess.waitFor();

                    agregarLog("");
                    if (runExitCode == 0) {
                        agregarLog("=== SCRAPING COMPLETADO EXITOSAMENTE ===");
                    } else {
                        agregarLog("=== SCRAPING TERMINÓ CON ERRORES (Código: " + runExitCode + ") ===");
                    }

                } catch (Exception e) {
                    agregarLog("ERROR CRÍTICO: " + e.getMessage());
                    e.printStackTrace();
                }

                agregarLog("Hora de finalización: " + java.time.LocalDateTime.now());
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    btnWebScrapingPanini.setDisable(false);
                    btnWebScrapingKamite.setDisable(false);
                    btnWebScrapingGandhi.setDisable(false);
                    progressIndicator.setVisible(false);

                    mostrarAlerta("Proceso Completado",
                            "El web scraping de " + nombreScraper + " ha finalizado.\n" +
                                    "Revise el log para más detalles.",
                            Alert.AlertType.INFORMATION);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    btnWebScrapingPanini.setDisable(false);
                    btnWebScrapingKamite.setDisable(false);
                    btnWebScrapingGandhi.setDisable(false);
                    progressIndicator.setVisible(false);

                    mostrarAlerta("Error",
                            "Ocurrió un error durante la ejecución.\n" +
                                    "Revise el log para más detalles.",
                            Alert.AlertType.ERROR);
                });
            }
        };

        new Thread(task).start();
    }

    private void capturarSalida(Process process) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    agregarLog(finalLine);
                }
            } catch (Exception e) {
                agregarLog("Error leyendo salida: " + e.getMessage());
            }
        }).start();
    }

    private void agregarLog(String mensaje) {
        Platform.runLater(() -> {
            txtLog.appendText(mensaje + "\n");
        });
    }

    private String getClasspath() {
        // Obtener el classpath actual del proyecto
        // Esto incluirá las dependencias de Maven/Gradle
        return System.getProperty("java.class.path") +
                ":target/classes" +
                ":src/main/java";
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}