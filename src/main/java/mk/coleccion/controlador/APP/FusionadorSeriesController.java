package mk.coleccion.controlador.APP;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import mk.coleccion.servicio.APP.FusionadorSeriesServicioAPP;
import mk.coleccion.servicio.APP.FusionadorSeriesServicioAPP.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class FusionadorSeriesController {

    @Autowired
    private FusionadorSeriesServicioAPP fusionadorServicio;

    @FXML
    private ListView<GrupoFusion> listGrupos;

    @FXML
    private VBox panelDetalle;

    @FXML
    private Button btnDetectar;

    @FXML
    private Button btnFusionar;

    @FXML
    private Label lblEstado;

    @FXML
    private TextArea txtLog;

    @FXML
    private ProgressIndicator progressIndicator;

    private ObservableList<GrupoFusion> gruposFusion;
    private GrupoFusion grupoSeleccionado;
    private PrevisualizacionFusion previsualizacionActual;

    @FXML
    public void initialize() {
        gruposFusion = FXCollections.observableArrayList();
        listGrupos.setItems(gruposFusion);

        // Configurar render personalizado para los grupos
        listGrupos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GrupoFusion grupo, boolean empty) {
                super.updateItem(grupo, empty);
                if (empty || grupo == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(String.format("📚 %s (%d series, %d mangas)",
                            grupo.getNombreBase(),
                            grupo.getSeries().size(),
                            grupo.getTotalMangas()));
                }
            }
        });

        // Listener para selección de grupo
        listGrupos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        mostrarDetalleGrupo(newVal);
                    }
                }
        );

        btnDetectar.setOnAction(e -> detectarSeriesSimilares());
        btnFusionar.setOnAction(e -> ejecutarFusion());

        progressIndicator.setVisible(false);
        btnFusionar.setDisable(true);
    }

    private void detectarSeriesSimilares() {
        progressIndicator.setVisible(true);
        btnDetectar.setDisable(true);
        lblEstado.setText("🔍 Detectando series similares...");
        txtLog.clear();

        new Thread(() -> {
            try {
                List<GrupoFusion> grupos = fusionadorServicio.detectarSeriesSimilares();

                Platform.runLater(() -> {
                    gruposFusion.clear();
                    gruposFusion.addAll(grupos);

                    lblEstado.setText(String.format("✅ Encontrados %d grupos de series similares", grupos.size()));
                    txtLog.appendText(String.format("Total de grupos detectados: %d\n\n", grupos.size()));

                    if (!grupos.isEmpty()) {
                        txtLog.appendText("📋 RESUMEN DE GRUPOS:\n");
                        txtLog.appendText("━".repeat(60) + "\n");
                        for (GrupoFusion grupo : grupos) {
                            txtLog.appendText(String.format("• %s\n", grupo.getNombreBase()));
                            txtLog.appendText(String.format("  Series: %d | Mangas totales: %d\n",
                                    grupo.getSeries().size(), grupo.getTotalMangas()));
                        }
                        txtLog.appendText("\n💡 Selecciona un grupo para ver detalles y fusionar.\n");
                    } else {
                        txtLog.appendText("✅ No se encontraron series duplicadas.\n");
                        txtLog.appendText("¡Tu base de datos está limpia!\n");
                    }

                    progressIndicator.setVisible(false);
                    btnDetectar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblEstado.setText("❌ Error al detectar series");
                    txtLog.appendText("ERROR: " + ex.getMessage() + "\n");
                    progressIndicator.setVisible(false);
                    btnDetectar.setDisable(false);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void mostrarDetalleGrupo(GrupoFusion grupo) {
        grupoSeleccionado = grupo;
        panelDetalle.getChildren().clear();

        // Título
        Label titulo = new Label("📊 Detalles del Grupo: " + grupo.getNombreBase());
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        panelDetalle.getChildren().add(titulo);

        panelDetalle.getChildren().add(new Separator());

        // Información del grupo
        VBox infoGrupo = new VBox(5);
        infoGrupo.getChildren().addAll(
                new Label(String.format("Series en este grupo: %d", grupo.getSeries().size())),
                new Label(String.format("Total de mangas: %d", grupo.getTotalMangas())),
                new Label(String.format("Volúmenes esperados: %d", grupo.getMaxVolumenesDeclarados()))
        );
        panelDetalle.getChildren().add(infoGrupo);

        panelDetalle.getChildren().add(new Separator());

        // Lista de series
        Label lblSeries = new Label("🔍 Series en este grupo:");
        lblSeries.setStyle("-fx-font-weight: bold;");
        panelDetalle.getChildren().add(lblSeries);

        VBox listaSeries = new VBox(10);
        for (SerieCandidato serie : grupo.getSeries()) {
            VBox tarjetaSerie = crearTarjetaSerie(serie, grupo);
            listaSeries.getChildren().add(tarjetaSerie);
        }

        ScrollPane scrollSeries = new ScrollPane(listaSeries);
        scrollSeries.setFitToWidth(true);
        scrollSeries.setPrefHeight(300);
        panelDetalle.getChildren().add(scrollSeries);

        btnFusionar.setDisable(false);
    }

    private VBox crearTarjetaSerie(SerieCandidato serie, GrupoFusion grupo) {
        VBox tarjeta = new VBox(5);
        tarjeta.setPadding(new Insets(10));
        tarjeta.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; " +
                "-fx-background-color: #f9f9f9; -fx-border-radius: 5;");

        // Nombre de la serie
        Label lblNombre = new Label("📖 " + serie.getNombreSerie());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Información
        String info = String.format("ID: %d | Declarados: %d | En BD: %d | Autor: %s",
                serie.getIdSerie(),
                serie.getVolumenesDeclarados() != null ? serie.getVolumenesDeclarados() : 0,
                serie.getMangasEnBD(),
                serie.getAutor() != null ? serie.getAutor() : "Desconocido"
        );
        Label lblInfo = new Label(info);
        lblInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        // Volúmenes existentes (resumidos)
        String volumenes = serie.getVolumenesExistentes().size() <= 10
                ? serie.getVolumenesExistentes().toString()
                : String.format("[%s, ..., %s] (%d total)",
                serie.getVolumenesExistentes().get(0),
                serie.getVolumenesExistentes().get(serie.getVolumenesExistentes().size() - 1),
                serie.getVolumenesExistentes().size());

        Label lblVolumenes = new Label("📚 Volúmenes: " + volumenes);
        lblVolumenes.setStyle("-fx-font-size: 11px;");

        // Botón para previsualizar fusión con esta serie como destino
        Button btnPreview = new Button("👁️ Usar como serie destino");
        btnPreview.setStyle("-fx-font-size: 11px;");
        btnPreview.setOnAction(e -> previsualizarFusionCon(grupo, serie.getIdSerie()));

        tarjeta.getChildren().addAll(lblNombre, lblInfo, lblVolumenes, btnPreview);
        return tarjeta;
    }

    private void previsualizarFusionCon(GrupoFusion grupo, Integer idSerieDestino) {
        try {
            previsualizacionActual = fusionadorServicio.previsualizarFusion(grupo, idSerieDestino);

            txtLog.clear();
            txtLog.appendText("═".repeat(70) + "\n");
            txtLog.appendText("           PREVISUALIZACIÓN DE FUSIÓN\n");
            txtLog.appendText("═".repeat(70) + "\n\n");

            txtLog.appendText(String.format("🎯 SERIE DESTINO: %s (ID: %d)\n",
                    previsualizacionActual.getSerieDestino().getNombreSerie(),
                    previsualizacionActual.getSerieDestino().getIdSerie()));
            txtLog.appendText(String.format("   Volúmenes actuales: %d\n\n",
                    previsualizacionActual.getSerieDestino().getMangasEnBD()));

            txtLog.appendText("📦 SERIES A FUSIONAR:\n");
            for (SerieCandidato origen : previsualizacionActual.getSeriesOrigen()) {
                txtLog.appendText(String.format("   • %s (ID: %d) - %d mangas\n",
                        origen.getNombreSerie(),
                        origen.getIdSerie(),
                        origen.getMangasEnBD()));
            }

            txtLog.appendText("\n");
            txtLog.appendText("─".repeat(70) + "\n");
            txtLog.appendText(String.format("📊 RESULTADO DESPUÉS DE LA FUSIÓN:\n"));
            txtLog.appendText(String.format("   Total de mangas: %d\n",
                    previsualizacionActual.getTotalMangasFusion()));
            txtLog.appendText(String.format("   Volúmenes: %s\n",
                    previsualizacionActual.getVolumenesFinales().size() <= 20
                            ? previsualizacionActual.getVolumenesFinales().toString()
                            : String.format("[%d volúmenes del 1 al %d]",
                            previsualizacionActual.getVolumenesFinales().size(),
                            previsualizacionActual.getVolumenesFinales().stream()
                                    .mapToInt(Float::intValue).max().orElse(0))));

            if (previsualizacionActual.isTieneConflictos()) {
                txtLog.appendText("\n⚠️ ADVERTENCIAS:\n");
                for (String advertencia : previsualizacionActual.getAdvertencias()) {
                    txtLog.appendText("   " + advertencia + "\n");
                }
            }

            txtLog.appendText("\n");
            txtLog.appendText("═".repeat(70) + "\n");
            txtLog.appendText("💡 Haz clic en 'Ejecutar Fusión' para confirmar.\n");

            lblEstado.setText("✅ Previsualización lista. Puedes ejecutar la fusión.");

        } catch (Exception ex) {
            lblEstado.setText("❌ Error en previsualización");
            txtLog.appendText("ERROR: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private void ejecutarFusion() {
        if (previsualizacionActual == null) {
            mostrarAlerta("Error",
                    "Debes seleccionar una serie destino primero.\n" +
                            "Haz clic en 'Usar como serie destino' en alguna de las series.",
                    Alert.AlertType.WARNING);
            return;
        }

        // Confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Fusión");
        confirmacion.setHeaderText("¿Estás seguro de fusionar estas series?");
        confirmacion.setContentText(String.format(
                "Esto moverá %d mangas a la serie '%s' y eliminará %d series.\n\n" +
                        "Esta acción NO se puede deshacer.\n\n¿Continuar?",
                previsualizacionActual.getTotalMangasFusion() -
                        previsualizacionActual.getSerieDestino().getMangasEnBD(),
                previsualizacionActual.getSerieDestino().getNombreSerie(),
                previsualizacionActual.getSeriesOrigen().size()
        ));

        Optional<ButtonType> respuestaConfirmacion = confirmacion.showAndWait();
        if (respuestaConfirmacion.isEmpty() || respuestaConfirmacion.get() != ButtonType.OK) {
            return;
        }

        // Ejecutar fusión
        progressIndicator.setVisible(true);
        btnFusionar.setDisable(true);
        lblEstado.setText("⏳ Ejecutando fusión...");

        new Thread(() -> {
            try {
                ResultadoFusion resultadoFusion = fusionadorServicio.fusionarSeries(previsualizacionActual);

                Platform.runLater(() -> {
                    txtLog.appendText("\n\n");
                    txtLog.appendText("═".repeat(70) + "\n");
                    txtLog.appendText("           RESULTADO DE LA FUSIÓN\n");
                    txtLog.appendText("═".repeat(70) + "\n");
                    txtLog.appendText(resultadoFusion.toString());

                    if (resultadoFusion.isExito()) {
                        lblEstado.setText("✅ Fusión completada exitosamente");
                        mostrarAlerta("Éxito",
                                resultadoFusion.getMensaje() + "\n\n" +
                                        "Mangas movidos: " + resultadoFusion.getMangasMovidos() + "\n" +
                                        "Series eliminadas: " + resultadoFusion.getSeriesEliminadas().size(),
                                Alert.AlertType.INFORMATION);

                        // Recargar grupos
                        detectarSeriesSimilares();
                        previsualizacionActual = null;
                        panelDetalle.getChildren().clear();
                    } else {
                        lblEstado.setText("❌ Error en la fusión");
                        mostrarAlerta("Error", resultadoFusion.getMensaje(), Alert.AlertType.ERROR);
                    }

                    progressIndicator.setVisible(false);
                    btnFusionar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblEstado.setText("❌ Error crítico");
                    txtLog.appendText("\n❌ ERROR CRÍTICO: " + ex.getMessage() + "\n");
                    mostrarAlerta("Error", "Error crítico: " + ex.getMessage(), Alert.AlertType.ERROR);
                    progressIndicator.setVisible(false);
                    btnFusionar.setDisable(false);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}