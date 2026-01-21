package com.complexity.analyzer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Random;

public class MainApp extends Application {

    private File selectedFile;
    private final XYChart.Series<Number, Number> timeSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> spaceSeries = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        // --- 1. Top Controls ---
        Button btnLoad = new Button("Select .class File");
        TextField txtMethod = new TextField();
        txtMethod.setPromptText("Method Name (e.g. bubbleSort)");
        Button btnRun = new Button("Analyze");
        Label lblStatus = new Label("Ready");

        HBox controls = new HBox(10, btnLoad, txtMethod, btnRun);
        controls.setPadding(new Insets(10));
        controls.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        // --- 2. Charts ---
        // Time Chart
        NumberAxis xAxisTime = new NumberAxis();
        xAxisTime.setLabel("Input Size (N)");
        NumberAxis yAxisTime = new NumberAxis();
        yAxisTime.setLabel("Time (ns)");
        LineChart<Number, Number> timeChart = new LineChart<>(xAxisTime, yAxisTime);
        timeChart.setTitle("Time Complexity");
        timeChart.getData().add(timeSeries);
        timeSeries.setName("Time");

        // Space Chart (Optional enhancement)
        NumberAxis xAxisSpace = new NumberAxis();
        xAxisSpace.setLabel("Input Size (N)");
        NumberAxis yAxisSpace = new NumberAxis();
        yAxisSpace.setLabel("Memory (Bytes)");
        LineChart<Number, Number> spaceChart = new LineChart<>(xAxisSpace, yAxisSpace);
        spaceChart.setTitle("Space Complexity");
        spaceChart.getData().add(spaceSeries);
        spaceSeries.setName("Space");

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Time Complexity", timeChart));
        tabs.getTabs().add(new Tab("Space Complexity", spaceChart));
        for(Tab t : tabs.getTabs()) t.setClosable(false);

        // --- 3. Layout ---
        BorderPane root = new BorderPane();
        root.setTop(new VBox(controls, lblStatus));
        root.setCenter(tabs);

        // --- 4. Logic ---
        btnLoad.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Class", "*.class"));
            selectedFile = fc.showOpenDialog(stage);
            if(selectedFile != null) lblStatus.setText("Loaded: " + selectedFile.getName());
        });

        btnRun.setOnAction(e -> {
            if (selectedFile == null || txtMethod.getText().isEmpty()) {
                lblStatus.setText("Error: Missing file or method name.");
                return;
            }
            runAnalysis(txtMethod.getText(), lblStatus);
        });

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Algorithm Complexity Visualizer");
        stage.setScene(scene);
        stage.show();
    }

    private void runAnalysis(String methodName, Label status) {
        timeSeries.getData().clear();
        spaceSeries.getData().clear();

        new Thread(() -> {
            try {
                DynamicLoader loader = new DynamicLoader(selectedFile, methodName);

                // Analyze for N = 100 to 2000
                for (int n = 0; n <= 50000; n += 2000) {
                    int[] data = new int[n];
                    Random r = new Random();
                    for(int i=0; i<n; i++) data[i] = r.nextInt(10000);

                    // Clone data because sort algorithms modify the array in place
                    int[] input = data.clone();

                    Profiler.Metric metric = Profiler.analyze(loader, input);

                    if(n==0) continue;

                    // Update UI on JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        timeSeries.getData().add(new XYChart.Data<>(metric.inputSize(), metric.timeNs()));
                        spaceSeries.getData().add(new XYChart.Data<>(metric.inputSize(), metric.memoryBytes()));
                    });

                    // Small delay to keep UI responsive
                    Thread.sleep(10);
                }
                javafx.application.Platform.runLater(() -> status.setText("Analysis Complete."));

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> status.setText("Error: " + e.getCause()));
            }
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}