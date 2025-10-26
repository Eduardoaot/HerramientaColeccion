package mk.coleccion.controlador.APP;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import mk.coleccion.ColeccionApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class IndexAPP {

    @FXML
    private Button btnUsuario;

    @FXML
    private Button btnManga;

    @FXML
    private Button btnAutomatizacion;

    private ConfigurableApplicationContext springContext;

    public void setSpringContext(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void initialize() {
        btnUsuario.setOnAction(event -> abrirVentanaUsuarios());
        btnManga.setOnAction(event -> abrirVentanaSeries());
        btnAutomatizacion.setOnAction(event -> abrirVentanaAutomatizacion());
    }

    private void abrirVentanaUsuarios() {
        try {
            // CAMBIADO: /fxml/ a /templates/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/usuarios.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Usuarios");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar usuarios.fxml: " + e.getMessage());
        }
    }

    private void abrirVentanaSeries() {
        try {
            // CAMBIADO: /fxml/ a /templates/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/series.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Series y Mangas");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar series.fxml: " + e.getMessage());
        }
    }

    private void abrirVentanaAutomatizacion() {
        // Implementar según tus necesidades
        System.out.println("Ventana de Automatización - Por implementar");
    }
}
