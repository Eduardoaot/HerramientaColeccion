package mk.coleccion.controlador.APP;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import mk.coleccion.modelo.DescripcionSerie;
import mk.coleccion.modelo.Serie;
import mk.coleccion.servicio.APP.IDescripcionSerieServicioAPP;
import mk.coleccion.servicio.APP.ISerieServicioAPP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class EditorSerieController {

    @Autowired
    private ISerieServicioAPP serieServicio;

    @Autowired
    private IDescripcionSerieServicioAPP descripcionSerieServicio;

    @FXML
    private TextField txtNombreSerie;

    @FXML
    private TextField txtAutor;

    @FXML
    private TextField txtTotalVolumenes;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCancelar;

    private Serie serieSeleccionada;
    private boolean modoCreacion;
    private SerieController serieController;

    @FXML
    public void initialize() {
        btnGuardar.setOnAction(event -> guardarSerie());
        btnCancelar.setOnAction(event -> cerrarVentana());
    }

    public void setSerieSeleccionada(Serie serie) {
        this.serieSeleccionada = serie;
        cargarDatosSerie();
    }

    public void setModoCreacion(boolean modoCreacion) {
        this.modoCreacion = modoCreacion;
    }

    public void setSerieController(SerieController controller) {
        this.serieController = controller;
    }

    private void cargarDatosSerie() {
        if (serieSeleccionada != null) {
            txtNombreSerie.setText(serieSeleccionada.getSerieName());
            txtAutor.setText(serieSeleccionada.getAuthorName());
            txtTotalVolumenes.setText(serieSeleccionada.getSerieTotals() != null ?
                    serieSeleccionada.getSerieTotals().toString() : "");

            if (serieSeleccionada.getDescripcionSerie() != null) {
                txtDescripcion.setText(serieSeleccionada.getDescripcionSerie().getDescriptionSerie());
            }
        }
    }

    private void guardarSerie() {
        if (!validarCampos()) {
            return;
        }

        try {
            Serie serie;

            if (modoCreacion) {
                serie = new Serie();
            } else {
                serie = serieSeleccionada;
            }

            serie.setSerieName(txtNombreSerie.getText().trim());
            serie.setAuthorName(txtAutor.getText().trim());

            String totalVolStr = txtTotalVolumenes.getText().trim();
            if (!totalVolStr.isEmpty()) {
                serie.setSerieTotals(Integer.parseInt(totalVolStr));
            }

            // Guardar o actualizar descripción
            DescripcionSerie descripcion;
            if (serie.getDescripcionSerie() != null) {
                descripcion = serie.getDescripcionSerie();
            } else {
                descripcion = new DescripcionSerie();
            }
            descripcion.setDescriptionSerie(txtDescripcion.getText().trim());
            descripcion = descripcionSerieServicio.guardarDescripcionSerie(descripcion);
            serie.setDescripcionSerie(descripcion);

            serieServicio.guardarSerie(serie);

            String mensaje = modoCreacion ? "Serie creada correctamente" : "Serie actualizada correctamente";
            mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);

            if (serieController != null) {
                serieController.recargarSeries();
            }

            cerrarVentana();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al guardar serie: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validarCampos() {
        if (txtNombreSerie.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar el nombre de la serie", Alert.AlertType.ERROR);
            return false;
        }

        if (txtAutor.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Debe ingresar el autor", Alert.AlertType.ERROR);
            return false;
        }

        String totalVolStr = txtTotalVolumenes.getText().trim();
        if (!totalVolStr.isEmpty()) {
            try {
                Integer.parseInt(totalVolStr);
            } catch (NumberFormatException e) {
                mostrarAlerta("Error", "El total de volúmenes debe ser un número válido", Alert.AlertType.ERROR);
                return false;
            }
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