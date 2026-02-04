module com.pflappy.Pflappyfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.pflappy.Pflappyfx to javafx.fxml;
    exports com.pflappy.Pflappyfx;
    requires javafx.mediaEmpty;
}
