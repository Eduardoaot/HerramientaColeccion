package mk.coleccion.controlador.APP;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mk.coleccion.modelo.Manga;
import mk.coleccion.modelo.Serie;
import mk.coleccion.servicio.APP.IMangaImagenServicioAPP;
import mk.coleccion.servicio.APP.IMangaServicioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class DetalleSerieController {

    @Autowired
    private IMangaServicioAPP mangaServicioapp;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @FXML
    private Label lblSerieName;

    @FXML
    private Label lblAutorName;

    @FXML
    private ScrollPane scrollMangas;

    @FXML
    private Button btnAnadirManga;

    @FXML
    private Button btnEliminarManga;

    private Serie serieSeleccionada;
    private ObservableList<Manga> listaMangas;
    private Manga mangaSeleccionado;
    private TilePane tilePane;

    @FXML
    public void initialize() {
        btnAnadirManga.setOnAction(event -> anadirManga());
        btnEliminarManga.setOnAction(event -> eliminarManga());

        tilePane = new TilePane();
        tilePane.setPadding(new Insets(10));
        tilePane.setHgap(15);
        tilePane.setVgap(15);
        tilePane.setPrefColumns(4);

        scrollMangas.setContent(tilePane);
        scrollMangas.setFitToWidth(true);
    }

    public void setSerieSeleccionada(Serie serie) {
        this.serieSeleccionada = serie;
        lblSerieName.setText(serie.getSerieName());
        lblAutorName.setText("Autor: " + serie.getAuthorName());
        cargarMangas();
    }

    private void cargarMangas() {
        List<Manga> mangas = mangaServicioapp.buscarMangasPorSerie(serieSeleccionada.getIdSerie());

        // Ordenar por volumen de menor a mayor
        mangas.sort(Comparator.comparing(Manga::getVolumeNumber));

        listaMangas = FXCollections.observableArrayList(mangas);

        tilePane.getChildren().clear();

        for (Manga manga : listaMangas) {
            VBox mangaCard = crearTarjetaManga(manga);
            tilePane.getChildren().add(mangaCard);
        }
    }

    private VBox crearTarjetaManga(Manga manga) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        card.setPrefWidth(180);
        card.setPrefHeight(280);

        // Imagen
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        try {
            if (manga.getMangaImagen() != null && manga.getMangaImagen().getMangaImageFile() != null) {
                Blob blob = manga.getMangaImagen().getMangaImageFile();
                byte[] imageBytes = blob.getBytes(1, (int) blob.length());
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                imageView.setImage(image);
            } else {
                // Imagen por defecto si no hay imagen
                imageView.setStyle("-fx-background-color: #e0e0e0;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setStyle("-fx-background-color: #e0e0e0;");
        }

        // Información
        Label lblVolumen = new Label("Vol. " + manga.getVolumeNumber());
        lblVolumen.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label lblPrecio = new Label("$" + String.format("%.2f", manga.getMangaPrice()));
        lblPrecio.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(imageView, lblVolumen, lblPrecio);

        // Click para seleccionar y editar
        card.setOnMouseClicked(event -> {
            mangaSeleccionado = manga;
            // Resaltar seleccionado
            tilePane.getChildren().forEach(node ->
                    node.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;"));
            card.setStyle("-fx-border-color: #007bff; -fx-border-width: 2; -fx-background-color: #e7f3ff;");

            if (event.getClickCount() == 2) {
                abrirEditorManga(manga);
            }
        });

        return card;
    }

    private void anadirManga() {
        try {
            // CAMBIADO: /fxml/ a /templates/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/editor-manga.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            EditorMangaController controller = loader.getController();
            controller.setSerieSeleccionada(serieSeleccionada);
            controller.setModoCreacion(true);
            controller.setDetalleSerieController(this);

            Stage stage = new Stage();
            stage.setTitle("Añadir Nuevo Manga");
            stage.setScene(new Scene(root, 600, 700));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir editor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarManga() {
        if (mangaSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un manga para eliminar", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este manga?");
        confirmacion.setContentText("Volumen: " + mangaSeleccionado.getVolumeNumber());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                mangaServicioapp.eliminarManga(mangaSeleccionado);
                mostrarAlerta("Éxito", "Manga eliminado correctamente", Alert.AlertType.INFORMATION);
                mangaSeleccionado = null;
                cargarMangas();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar manga: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void abrirEditorManga(Manga manga) {
        try {
            // CAMBIADO: /fxml/ a /templates/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/templates/editor-manga.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            EditorMangaController controller = loader.getController();
            controller.setMangaSeleccionado(manga);
            controller.setModoCreacion(false);
            controller.setDetalleSerieController(this);

            Stage stage = new Stage();
            stage.setTitle("Editar Manga - Vol. " + manga.getVolumeNumber());
            stage.setScene(new Scene(root, 600, 700));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al abrir editor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void recargarMangas() {
        cargarMangas();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}