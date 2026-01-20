module com.complexity.analyzer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.complexity.analyzer to javafx.fxml;
    exports com.complexity.analyzer;
}