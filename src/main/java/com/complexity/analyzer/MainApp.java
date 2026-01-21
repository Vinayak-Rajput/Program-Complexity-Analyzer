package com.complexity.analyzer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.lang.reflect.Method;

public class MainApp extends Application {

    private DynamicLoader currentLoader;
    private final ComboBox<Method> methodDropdown = new ComboBox<>();

    // ... Charts (same as before) ...
    private final XYChart.Series<Number, Number> timeSeries = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        // UI Components
        Button btnLoad = new Button("1. Load Class");
        Button btnRun = new Button("2. Analyze");
        Label lblStatus = new Label("Ready");

        // Customize Dropdown to show readable names
        methodDropdown.setPromptText("Select Method");
        methodDropdown.setButtonCell(new MethodListCell());
        methodDropdown.setCellFactory(p -> new MethodListCell());
        methodDropdown.setPrefWidth(300);

        // Chart Setup (Same as before)
        NumberAxis xAxis = new NumberAxis(); xAxis.setLabel("N");
        NumberAxis yAxis = new NumberAxis(); yAxis.setLabel("Time (ns)");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getData().add(timeSeries);
        timeSeries.setName("Time Complexity");

        // Logic
        btnLoad.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Class", "*.class"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                try {
                    currentLoader = new DynamicLoader(file);
                    // Populate Dropdown
                    methodDropdown.getItems().setAll(currentLoader.getMethods());
                    lblStatus.setText("Loaded: " + file.getName());
                } catch (Exception ex) {
                    lblStatus.setText("Error: " + ex.getMessage());
                }
            }
        });

        btnRun.setOnAction(e -> {
            Method selectedMethod = methodDropdown.getValue();
            if (currentLoader == null || selectedMethod == null) {
                lblStatus.setText("Select a class and method first!");
                return;
            }
            btnRun.setDisable(true);
            runAnalysis(selectedMethod, lblStatus, btnRun);
        });

        HBox controls = new HBox(10, btnLoad, methodDropdown, btnRun);
        BorderPane root = new BorderPane();
        root.setTop(controls);
        root.setCenter(chart);
        root.setBottom(lblStatus);

        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    private void runAnalysis(Method method, Label status, Button btn) {
        timeSeries.getData().clear();

        new Thread(() -> {
            try {
                // "Adaptive Range"
                // Start small, go BIG (up to 100,000)
                // But we will BREAK early if it gets too slow.
                for (int n = 1000; n <= 100000; n += 2000) {
                    try {
                        Profiler.Metric m = Profiler.analyze(currentLoader, method, n);

                        // Update Chart
                        Platform.runLater(() -> timeSeries.getData().add(new XYChart.Data<>(m.inputSize(), m.timeNs())));

                        // SAFETY BREAK:
                        // If a single run takes longer than 100ms (0.1s), STOP increasing N.
                        // This lets Fast Algos go to N=100k, but stops Bubble Sort at N=5k.
                        if (m.timeNs() > 100_000_000) {
                            Platform.runLater(() -> status.setText("Stopping early: Algorithm is getting slow."));
                            break;
                        }

                    } catch (Exception ex) {
                        // ... Error handling ...
                        break;
                    }
                }
                Platform.runLater(() -> status.setText("Analysis Complete."));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> btn.setDisable(false));
            }
        }).start();
    }

    // Helper to display method signatures nicely
    private static class MethodListCell extends ListCell<Method> {
        @Override
        protected void updateItem(Method item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                // Format: returnType name(param1, param2)
                StringBuilder sb = new StringBuilder();
                sb.append(item.getReturnType().getSimpleName()).append(" ");
                sb.append(item.getName()).append("(");
                var params = item.getParameterTypes();
                for (int i = 0; i < params.length; i++) {
                    sb.append(params[i].getSimpleName());
                    if (i < params.length - 1) sb.append(", ");
                }
                sb.append(")");
                setText(sb.toString());
            }
        }
    }

    public static void main(String[] args) { launch(); }
}