package mk.coleccion.controlador.APP;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
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
import mk.coleccion.servicio.APP.IMangaServicioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    @FXML
    private Label lblDescripcionSerie;

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

        if (lblDescripcionSerie != null && serie.getDescripcionSerie() != null) {
            String descripcion = serie.getDescripcionSerie().getDescriptionSerie();
            lblDescripcionSerie.setText(descripcion != null ? descripcion : "Sin descripción disponible");
            lblDescripcionSerie.setWrapText(true);
        }

        cargarMangas();
    }

    private void cargarMangas() {
        try {
            List<Manga> mangas = mangaServicioapp.buscarMangasPorSerie(serieSeleccionada.getIdSerie());
            System.out.println("Mangas encontrados para serie " + serieSeleccionada.getIdSerie() + ": " + mangas.size());

            mangas.sort(Comparator.comparing(Manga::getVolumeNumber));

            listaMangas = FXCollections.observableArrayList(mangas);

            tilePane.getChildren().clear();

            for (Manga manga : listaMangas) {
                VBox mangaCard = crearTarjetaManga(manga);
                tilePane.getChildren().add(mangaCard);
            }

            System.out.println("Tarjetas de manga creadas: " + tilePane.getChildren().size());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar mangas: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox crearTarjetaManga(Manga manga) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;");
        card.setPrefWidth(180);
        card.setPrefHeight(280);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        boolean imagenCargada = false;
        try {
            if (manga.getMangaImagen() != null) {
                System.out.println("Procesando imagen para manga Vol. " + manga.getVolumeNumber() +
                        " - ID Imagen: " + manga.getMangaImagen().getIdMangaImagen());

                if (manga.getMangaImagen().getMangaImageFile() != null) {
                    Blob blob = manga.getMangaImagen().getMangaImageFile();
                    long blobLength = blob.length();
                    System.out.println("Tamaño del BLOB: " + blobLength + " bytes");

                    if (blobLength > 0) {
                        byte[] imageBytes = blob.getBytes(1, (int) blobLength);

                        // NUEVO: Intentar detectar y mostrar información del formato
                        String formatoDetectado = detectarFormato(imageBytes);
                        System.out.println("Formato detectado para Vol. " + manga.getVolumeNumber() + ": " + formatoDetectado);

                        // NUEVO: Usar ImageIO si JavaFX falla
                        Image image = cargarImagenConFallback(imageBytes, manga.getVolumeNumber());

                        if (image != null && !image.isError()) {
                            imageView.setImage(image);
                            imagenCargada = true;
                            System.out.println("✓ Imagen cargada exitosamente para Vol. " + manga.getVolumeNumber());
                        } else {
                            if (image != null) {
                                System.err.println("✗ Error al crear Image para Vol. " + manga.getVolumeNumber() +
                                        ": " + image.getException());
                            }
                        }
                    } else {
                        System.err.println("✗ BLOB vacío para Vol. " + manga.getVolumeNumber());
                    }
                } else {
                    System.err.println("✗ MangaImageFile es null para Vol. " + manga.getVolumeNumber());
                }
            } else {
                System.err.println("✗ MangaImagen es null para Vol. " + manga.getVolumeNumber());
            }
        } catch (Exception e) {
            System.err.println("✗ Excepción al cargar imagen para Vol. " + manga.getVolumeNumber() + ": " + e.getMessage());
            e.printStackTrace();
        }

        if (!imagenCargada) {
            imageView.setStyle("-fx-background-color: #e0e0e0;");
            Label noImageLabel = new Label("Sin\nImagen");
            noImageLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 14px; -fx-text-alignment: center;");
        }

        Label lblVolumen = new Label("Vol. " + manga.getVolumeNumber());
        lblVolumen.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label lblPrecio = new Label("$" + String.format("%.2f", manga.getMangaPrice()));
        lblPrecio.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(imageView, lblVolumen, lblPrecio);

        card.setOnMouseClicked(event -> {
            mangaSeleccionado = manga;
            tilePane.getChildren().forEach(node ->
                    node.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-background-color: white;"));
            card.setStyle("-fx-border-color: #007bff; -fx-border-width: 2; -fx-background-color: #e7f3ff;");

            if (event.getClickCount() == 2) {
                abrirEditorManga(manga);
            }
        });

        return card;
    }

    /**
     * NUEVO: Detecta el formato de imagen por sus "magic numbers"
     */
    private String detectarFormato(byte[] imageBytes) {
        if (imageBytes.length < 4) {
            return "ARCHIVO DEMASIADO PEQUEÑO";
        }

        // JPEG: FF D8 FF
        if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return "JPEG";
        }

        // PNG: 89 50 4E 47
        if (imageBytes[0] == (byte) 0x89 && imageBytes[1] == (byte) 0x50 &&
                imageBytes[2] == (byte) 0x4E && imageBytes[3] == (byte) 0x47) {
            return "PNG";
        }

        // GIF: 47 49 46 38
        if (imageBytes[0] == (byte) 0x47 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x38) {
            return "GIF";
        }

        // BMP: 42 4D
        if (imageBytes[0] == (byte) 0x42 && imageBytes[1] == (byte) 0x4D) {
            return "BMP";
        }

        // WebP: 52 49 46 46
        if (imageBytes[0] == (byte) 0x52 && imageBytes[1] == (byte) 0x49 &&
                imageBytes[2] == (byte) 0x46 && imageBytes[3] == (byte) 0x46) {
            return "WebP";
        }

        // Mostrar los primeros bytes en hexadecimal para diagnóstico
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < Math.min(8, imageBytes.length); i++) {
            hex.append(String.format("%02X ", imageBytes[i]));
        }

        return "DESCONOCIDO (bytes: " + hex.toString() + ")";
    }

    /**
     * NUEVO: Intenta cargar la imagen primero con JavaFX,
     * si falla usa ImageIO (AWT) como fallback
     */
    private Image cargarImagenConFallback(byte[] imageBytes, Float volumen) {
        // Intento 1: Cargar directamente con JavaFX
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            Image image = new Image(bis);

            if (!image.isError()) {
                System.out.println("  → Cargada con JavaFX directamente");
                return image;
            } else {
                System.out.println("  → JavaFX falló: " + image.getException().getMessage());
            }
        } catch (Exception e) {
            System.out.println("  → Excepción JavaFX: " + e.getMessage());
        }

        // Intento 2: Usar ImageIO (AWT) y convertir a JavaFX
        try {
            System.out.println("  → Intentando con ImageIO/AWT...");
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);

            if (bufferedImage != null) {
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                System.out.println("  → ✓ Cargada con ImageIO y convertida a JavaFX");
                return image;
            } else {
                System.out.println("  → ImageIO.read() retornó null");
            }
        } catch (IOException e) {
            System.out.println("  → IOException con ImageIO: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  → Excepción general con ImageIO: " + e.getMessage());
        }

        System.out.println("  → ✗ Todos los métodos de carga fallaron");
        return null;
    }

    private void anadirManga() {
        try {
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