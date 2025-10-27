package mk.coleccion.controlador.APP;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mk.coleccion.modelo.Serie;
import mk.coleccion.servicio.APP.ISerieServicioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class SerieController {

    @Autowired
    private ISerieServicioAPP serieServicio;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @FXML
    private TableView<Serie> tableSeries;

    @FXML
    private TableColumn<Serie, String> colIdSerie;

    @FXML
    private TableColumn<Serie, String> colSerieName;

    @FXML
    private TableColumn<Serie, String> colAuthorName;

    @FXML
    private TableColumn<Serie, String> colTotales;

    @FXML
    private TableColumn<Serie, String> colDescripcion;

    @FXML
    private TextField txtBuscarSerie;

    @FXML
    private Button btnVerDetalles;

    @FXML
    private Button btnAgregarSerie;

    @FXML
    private Button btnEditarSerie;

    @FXML
    private Button btnEliminarSerie;

    private ObservableList<Serie> listaSeries;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarSeries();

        btnVerDetalles.setOnAction(event -> verDetallesSerie());
        btnAgregarSerie.setOnAction(event -> agregarSerie());
        btnEditarSerie.setOnAction(event -> editarSerie());
        btnEliminarSerie.setOnAction(event -> eliminarSerie());
        txtBuscarSerie.textProperty().addListener((observable, oldValue, newValue) -> buscarSerie(newValue));

        tableSeries.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                verDetallesSerie();
            }
        });
    }

    private void configurarTabla() {
        colIdSerie.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIdSerie().toString()));

        colSerieName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSerieName()));

        colAuthorName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAuthorName()));

        colTotales.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSerieTotals() != null ?
                        cellData.getValue().getSerieTotals().toString() : "0"));

        if (colDescripcion != null) {
            colDescripcion.setCellValueFactory(cellData -> {
                String descripcion = "";
                if (cellData.getValue().getDescripcionSerie() != null) {
                    descripcion = cellData.getValue().getDescripcionSerie().getDescriptionSerie();
                    if (descripcion != null && descripcion.length() > 50) {
                        descripcion = descripcion.substring(0, 50) + "...";
                    }
                }
                return new SimpleStringProperty(descripcion != null ? descripcion : "Sin descripción");
            });
        }
    }

    private void cargarSeries() {
        try {
            List<Serie> series = serieServicio.listarSeries();
            listaSeries = FXCollections.observableArrayList(series);
            tableSeries.setItems(listaSeries);
            System.out.println("Series cargadas: " + series.size());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar series: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void recargarSeries() {
        cargarSeries();
    }

    private void buscarSerie(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            cargarSeries();
            return;
        }

        List<Serie> todasSeries = serieServicio.listarSeries();
        ObservableList<Serie> seriesFiltradas = FXCollections.observableArrayList();

        String criterioBusqueda = criterio.toLowerCase().trim();

        for (Serie serie : todasSeries) {
            boolean coincide = false;

            if (serie.getSerieName() != null &&
                    serie.getSerieName().toLowerCase().contains(criterioBusqueda)) {
                coincide = true;
            }
            if (serie.getAuthorName() != null &&
                    serie.getAuthorName().toLowerCase().contains(criterioBusqueda)) {
                coincide = true;
            }
            if (serie.getDescripcionSerie() != null &&
                    serie.getDescripcionSerie().getDescriptionSerie() != null &&
                    serie.getDescripcionSerie().getDescriptionSerie().toLowerCase().contains(criterioBusqueda)) {
                coincide = true;
            }

            if (coincide) {
                seriesFiltradas.add(serie);
            }
        }

        tableSeries.setItems(seriesFiltradas);
    }

    private void agregarSerie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/editor-serie.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            EditorSerieController controller = loader.getController();
            controller.setModoCreacion(true);
            controller.setSerieController(this);

            Stage stage = new Stage();
            stage.setTitle("Agregar Nueva Serie");
            stage.setScene(new Scene(root, 600, 500));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir editor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void editarSerie() {
        Serie serieSeleccionada = tableSeries.getSelectionModel().getSelectedItem();

        if (serieSeleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una serie para editar", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/editor-serie.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            EditorSerieController controller = loader.getController();
            controller.setSerieSeleccionada(serieSeleccionada);
            controller.setModoCreacion(false);
            controller.setSerieController(this);

            Stage stage = new Stage();
            stage.setTitle("Editar Serie: " + serieSeleccionada.getSerieName());
            stage.setScene(new Scene(root, 600, 500));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir editor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarSerie() {
        Serie serieSeleccionada = tableSeries.getSelectionModel().getSelectedItem();

        if (serieSeleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una serie para eliminar", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta serie?");
        confirmacion.setContentText("Serie: " + serieSeleccionada.getSerieName() +
                "\n\nADVERTENCIA: Esto eliminará todos los mangas asociados a esta serie.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                serieServicio.eliminarSerie(serieSeleccionada);
                mostrarAlerta("Éxito", "Serie eliminada correctamente", Alert.AlertType.INFORMATION);
                cargarSeries();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar serie: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void verDetallesSerie() {
        Serie serieSeleccionada = tableSeries.getSelectionModel().getSelectedItem();

        if (serieSeleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una serie para ver los detalles", Alert.AlertType.ERROR);
            return;
        }

        try {
            System.out.println("Abriendo detalles para serie: " + serieSeleccionada.getSerieName());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/detalle-serie.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            DetalleSerieController controller = loader.getController();
            controller.setSerieSeleccionada(serieSeleccionada);

            Stage stage = new Stage();
            stage.setTitle("Detalles de Serie: " + serieSeleccionada.getSerieName());
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir detalles: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}