package mk.coleccion.controlador.APP;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import mk.coleccion.modelo.Usuario;
import mk.coleccion.servicio.APP.IUsuarioServicioAPP;
import mk.coleccion.servicio.IUsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private IUsuarioServicioAPP usuarioServicio;

    @FXML
    private TableView<Usuario> tableUsuarios;

    @FXML
    private TableColumn<Usuario, String> colId;

    @FXML
    private TableColumn<Usuario, String> colUserName;

    @FXML
    private TableColumn<Usuario, String> colFullName;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, String> colPassword;

    @FXML
    private TableColumn<Usuario, String> colMeta;

    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    private ObservableList<Usuario> listaUsuarios;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarUsuarios();

        btnActualizar.setOnAction(event -> actualizarUsuario());
        btnEliminar.setOnAction(event -> eliminarUsuario());
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> buscarUsuario(newValue));
    }

    private void configurarTabla() {
        // Hacer la tabla editable
        tableUsuarios.setEditable(true);

        // Configurar columnas
        colId.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIdUsuario().toString()));

        colUserName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserName()));
        colUserName.setCellFactory(TextFieldTableCell.forTableColumn());
        colUserName.setOnEditCommit(event -> {
            event.getRowValue().setUserName(event.getNewValue());
        });

        colFullName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));
        colFullName.setCellFactory(TextFieldTableCell.forTableColumn());
        colFullName.setOnEditCommit(event -> {
            event.getRowValue().setFullName(event.getNewValue());
        });

        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserEmail()));
        colEmail.setCellFactory(TextFieldTableCell.forTableColumn());
        colEmail.setOnEditCommit(event -> {
            event.getRowValue().setUserEmail(event.getNewValue());
        });

        colPassword.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserPassword()));
        colPassword.setCellFactory(TextFieldTableCell.forTableColumn());
        colPassword.setOnEditCommit(event -> {
            event.getRowValue().setUserPassword(event.getNewValue());
        });

        colMeta.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserMeta()));
        colMeta.setCellFactory(TextFieldTableCell.forTableColumn());
        colMeta.setOnEditCommit(event -> {
            event.getRowValue().setUserMeta(event.getNewValue());
        });
    }

    private void cargarUsuarios() {
        List<Usuario> usuarios = usuarioServicio.listarUsuarios();
        listaUsuarios = FXCollections.observableArrayList(usuarios);
        tableUsuarios.setItems(listaUsuarios);
    }

    private void actualizarUsuario() {
        Usuario usuarioSeleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un usuario para actualizar", Alert.AlertType.ERROR);
            return;
        }

        try {
            usuarioServicio.guardarUsuario(usuarioSeleccionado);
            mostrarAlerta("Éxito", "Usuario actualizado correctamente", Alert.AlertType.INFORMATION);
            cargarUsuarios();
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al actualizar usuario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarUsuario() {
        Usuario usuarioSeleccionado = tableUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un usuario para eliminar", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este usuario?");
        confirmacion.setContentText("Usuario: " + usuarioSeleccionado.getFullName());

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                usuarioServicio.eliminarUsuario(usuarioSeleccionado);
                mostrarAlerta("Éxito", "Usuario eliminado correctamente", Alert.AlertType.INFORMATION);
                cargarUsuarios();
            } catch (Exception e) {
                mostrarAlerta("Error", "Error al eliminar usuario: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void buscarUsuario(String criterio) {
        if (criterio == null || criterio.trim().isEmpty()) {
            cargarUsuarios();
            return;
        }

        List<Usuario> todosUsuarios = usuarioServicio.listarUsuarios();
        ObservableList<Usuario> usuariosFiltrados = FXCollections.observableArrayList();

        String criterioBusqueda = criterio.toLowerCase().trim();

        for (Usuario usuario : todosUsuarios) {
            boolean coincide = false;

            if (usuario.getFullName() != null &&
                    usuario.getFullName().toLowerCase().contains(criterioBusqueda)) {
                coincide = true;
            }
            if (usuario.getUserEmail() != null &&
                    usuario.getUserEmail().toLowerCase().contains(criterioBusqueda)) {
                coincide = true;
            }
            if (usuario.getUserName() != null &&
                    usuario.getUserName().toLowerCase().contains(criterioBusqueda)) {
                coincide = true;
            }

            if (coincide) {
                usuariosFiltrados.add(usuario);
            }
        }

        tableUsuarios.setItems(usuariosFiltrados);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}