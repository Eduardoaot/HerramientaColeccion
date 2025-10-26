package mk.coleccion.controlador.APP;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mk.coleccion.modelo.*;
import mk.coleccion.servicio.*;
import mk.coleccion.servicio.APP.IDescripcionMangaServicioAPP;
import mk.coleccion.servicio.APP.IMangaImagenServicioAPP;
import mk.coleccion.servicio.APP.IMangaServicioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Blob;
import java.util.Date;

@Controller
public class EditorMangaController {

    @Autowired
    private IMangaServicioAPP mangaServicio;

    @Autowired
    private IMangaImagenServicioAPP mangaImagenServicio;

    @Autowired
    private IDescripcionMangaServicioAPP descripcionMangaServicio;

    @FXML
    private TextField txtVolumen;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private ImageView imgManga;

    @FXML
    private Button btnSeleccionarImagen;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    @FXML
    private Label lblNombreImagen;

    private Manga mangaSeleccionado;
    private Serie serieSeleccionada;
    private boolean modoCreacion;
    private byte[] imagenSeleccionada;
    private String nombreImagen;
    private DetalleSerieController detalleSerieController;

    @FXML
    public void initialize() {
        btnSeleccionarImagen.setOnAction(event -> seleccionarImagen());
        btnGuardar.setOnAction(event -> guardarManga());
        btnCancelar.setOnAction(event -> cerrarVentana());
    }

    public void setMangaSeleccionado(Manga manga) {
        this.mangaSeleccionado = manga;
        cargarDatosManga();
    }

    public void setSerieSeleccionada(Serie serie) {
        this.serieSeleccionada = serie;
    }

    public void setModoCreacion(boolean modoCreacion) {
        this.modoCreacion = modoCreacion;
    }

    public void setDetalleSerieController(DetalleSerieController controller) {
        this.detalleSerieController = controller;
    }

    private void cargarDatosManga() {
        if (mangaSeleccionado != null) {
            txtVolumen.setText(mangaSeleccionado.getVolumeNumber().toString());
            txtPrecio.setText(mangaSeleccionado.getMangaPrice().toString());

            if (mangaSeleccionado.getDescripcionManga() != null) {
                txtDescripcion.setText(mangaSeleccionado.getDescripcionManga().getDescriptionManga());
            }

            // Cargar imagen desde la base de datos
            try {
                if (mangaSeleccionado.getMangaImagen() != null &&
                        mangaSeleccionado.getMangaImagen().getMangaImageFile() != null) {

                    Blob blob = mangaSeleccionado.getMangaImagen().getMangaImageFile();
                    imagenSeleccionada = blob.getBytes(1, (int) blob.length());

                    Image image = new Image(new ByteArrayInputStream(imagenSeleccionada));
                    imgManga.setImage(image);

                    lblNombreImagen.setText(mangaSeleccionado.getMangaImagen().getMangaImageName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al cargar imagen: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Manga");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) btnSeleccionarImagen.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                FileInputStream fis = new FileInputStream(file);
                imagenSeleccionada = fis.readAllBytes();
                fis.close();

                nombreImagen = file.getName();
                lblNombreImagen.setText(nombreImagen);

                Image image = new Image(new ByteArrayInputStream(imagenSeleccionada));
                imgManga.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlerta("Error", "Error al cargar imagen: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void guardarManga() {
        if (!validarCampos()) {
            return;
        }

        try {
            Manga manga;

            if (modoCreacion) {
                manga = new Manga();
                manga.setSerie(serieSeleccionada);
            } else {
                manga = mangaSeleccionado;
            }

            manga.setVolumeNumber(Float.parseFloat(txtVolumen.getText()));
            manga.setMangaPrice(Float.parseFloat(txtPrecio.getText()));
            manga.setMangaDate(new Date());

            // Guardar o actualizar descripción
            DescripcionManga descripcion;
            if (manga.getDescripcionManga() != null) {
                descripcion = manga.getDescripcionManga();
            } else {
                descripcion = new DescripcionManga();
            }
            descripcion.setDescriptionManga(txtDescripcion.getText());
            descripcion = descripcionMangaServicio.guardarDescripcionManga(descripcion);
            manga.setDescripcionManga(descripcion);

            // Guardar o actualizar imagen
            if (imagenSeleccionada != null) {
                MangaImagen mangaImagen;
                if (manga.getMangaImagen() != null) {
                    mangaImagen = manga.getMangaImagen();
                } else {
                    mangaImagen = new MangaImagen();
                }

                mangaImagen.setMangaImageName(nombreImagen != null ? nombreImagen : "imagen.jpg");
                Blob blob = new SerialBlob(imagenSeleccionada);
                mangaImagen.setMangaImageFile(blob);

                mangaImagen = mangaImagenServicio.guardarMangaImagen(mangaImagen);
                manga.setMangaImagen(mangaImagen);
            }

            mangaServicio.guardarManga(manga);

            String mensaje = modoCreacion ? "Manga creado correctamente" : "Manga actualizado correctamente";
            mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);

            if (detalleSerieController != null) {
                detalleSerieController.recargarMangas();
            }

            cerrarVentana();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar manga: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarCampos() {
        if (txtVolumen.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar el número de volumen", Alert.AlertType.ERROR);
            return false;
        }

        if (txtPrecio.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar el precio", Alert.AlertType.ERROR);
            return false;
        }

        try {
            Float.parseFloat(txtVolumen.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El volumen debe ser un número válido", Alert.AlertType.ERROR);
            return false;
        }

        try {
            Float.parseFloat(txtPrecio.getText());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El precio debe ser un número válido", Alert.AlertType.ERROR);
            return false;
        }

        if (modoCreacion && imagenSeleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una imagen", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
