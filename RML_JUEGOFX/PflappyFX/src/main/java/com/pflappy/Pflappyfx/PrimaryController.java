package com.pflappy.Pflappyfx;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PrimaryController {

    @FXML
private Button btnPlay;

@FXML
private Button btnExit;

@FXML
private void onPlay() throws IOException {
    App.setRoot("secondary");

}

@FXML
private void onExit() {
    System.exit(0);
}

}
