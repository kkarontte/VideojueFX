package com.pflappy.Pflappyfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SecondaryController {

    @FXML
    private Pane gamePane;
    @FXML
    private Label scoreLabel;
    @FXML
    private Pane gameOverPane;
    @FXML
    private Label gameOverLabel;
    @FXML
    private Button restartButton;

    // ---------- ESTADO ----------
    private boolean started = false;
    private boolean gameOver = false;

    // ---------- FONDO ----------
    private ImageView backgroundImg;

    // ---------- PÁJARO ----------
    private ImageView birdImg;
    private final Image birdImage = new Image(getClass().getResourceAsStream("bird.png"));
    private double velocity = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -6;

    // ---------- TUBERÍAS ----------
    private final double pipeWidth = 50;
    private final double gapHeight = 150;
    private final double pipeSpeed = 2;
    private final Image pipeTopImage = new Image(getClass().getResourceAsStream("pipe_top.png"));
    private final Image pipeBottomImage = new Image(getClass().getResourceAsStream("pipe_bottom.png"));
    private final List<ImageView[]> pipes = new ArrayList<>();
    private final Random rand = new Random();

    // ---------- MÚSICA ----------
    private MediaPlayer musicPlayer;

    private Timeline gameLoop;
    private int score = 0;

    @FXML
    public void initialize() {

        gamePane.setFocusTraversable(true);
        gameOverPane.setVisible(false);

        // ---------- FONDO ----------
        backgroundImg = new ImageView(new Image(getClass().getResourceAsStream("background.png")));
        backgroundImg.setPreserveRatio(false);
        backgroundImg.fitWidthProperty().bind(gamePane.widthProperty());
        backgroundImg.fitHeightProperty().bind(gamePane.heightProperty());
        gamePane.getChildren().add(backgroundImg);

        // ---------- PÁJARO ----------
        birdImg = new ImageView(birdImage);
        birdImg.setFitWidth(60);
        birdImg.setFitHeight(60);
        birdImg.setLayoutX(100);
        birdImg.setLayoutY(200);
        gamePane.getChildren().add(birdImg);

        scoreLabel.setText("Score: 0");

        // ---------- MÚSICA ----------
        Media music = new Media(getClass().getResource("music.mp3").toExternalForm());
        musicPlayer = new MediaPlayer(music);
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.setVolume(0.4);
        musicPlayer.play();

        // ---------- INPUT TÁCTIL / RATÓN ----------
        gamePane.setOnMousePressed(e -> {
            if (!started) {
                started = true;
            }
            if (!gameOver) {
                velocity = jumpStrength;
            }
        });

        // ---------- INPUT TECLADO ----------
        gamePane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.SPACE) {
                        if (!started) started = true;
                        if (!gameOver) velocity = jumpStrength;
                    }
                });
                gamePane.requestFocus();
            }
        });

        // ---------- CREAR TUBERÍAS ----------
        gamePane.heightProperty().addListener((obs, o, n) -> {
            if (pipes.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    ImageView[] pair = createPipePair(gamePane.getWidth() + i * 250);
                    pipes.add(pair);
                }
            }
        });

        // ---------- GAME LOOP ----------
        gameLoop = new Timeline(new KeyFrame(Duration.millis(20), e -> updateGame()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void updateGame() {
        if (!started || gameOver) return;

        // ---------- GRAVEDAD ----------
        velocity += gravity;
        birdImg.setLayoutY(birdImg.getLayoutY() + velocity);

        if (birdImg.getLayoutY() < 0) {
            birdImg.setLayoutY(0);
            velocity = 0;
        } else if (birdImg.getLayoutY() > gamePane.getHeight() - birdImg.getFitHeight()) {
            triggerGameOver();
        }

        // ---------- TUBERÍAS ----------
        for (ImageView[] pair : pipes) {
            pair[0].setLayoutX(pair[0].getLayoutX() - pipeSpeed);
            pair[1].setLayoutX(pair[1].getLayoutX() - pipeSpeed);

            if (birdImg.getBoundsInParent().intersects(pair[0].getBoundsInParent()) ||
                birdImg.getBoundsInParent().intersects(pair[1].getBoundsInParent())) {
                triggerGameOver();
            }

            if (!Boolean.TRUE.equals(pair[0].getUserData()) &&
                pair[0].getLayoutX() + pipeWidth < birdImg.getLayoutX()) {
                score++;
                scoreLabel.setText("Score: " + score);
                pair[0].setUserData(true);
            }

            if (pair[0].getLayoutX() + pipeWidth < 0) {
                resetPipe(pair, 0);
            }
        }
    }

    private ImageView[] createPipePair(double startX) {
        double h = gamePane.getHeight();
        double gapY = 50 + rand.nextDouble() * (h - gapHeight - 100);

        ImageView top = new ImageView(pipeTopImage);
        top.setFitWidth(pipeWidth);
        top.setFitHeight(gapY);
        top.setLayoutX(startX);
        top.setLayoutY(0);
        top.setUserData(false);

        ImageView bottom = new ImageView(pipeBottomImage);
        bottom.setFitWidth(pipeWidth);
        bottom.setFitHeight(h - gapY - gapHeight);
        bottom.setLayoutX(startX);
        bottom.setLayoutY(gapY + gapHeight);
        bottom.setUserData(false);

        gamePane.getChildren().addAll(top, bottom);
        return new ImageView[]{top, bottom};
    }

    private void resetPipe(ImageView[] pair, double offsetX) {
        double h = gamePane.getHeight();
        double gapY = 50 + rand.nextDouble() * (h - gapHeight - 100);

        pair[0].setFitHeight(gapY);
        pair[0].setLayoutX(gamePane.getWidth() + offsetX);
        pair[0].setUserData(false);

        pair[1].setFitHeight(h - gapY - gapHeight);
        pair[1].setLayoutX(gamePane.getWidth() + offsetX);
        pair[1].setLayoutY(gapY + gapHeight);
        pair[1].setUserData(false);
    }

    private void triggerGameOver() {
        gameOver = true;
        gameOverPane.setVisible(true);
        gameOverPane.toFront();
        musicPlayer.pause();
        gameOverLabel.setText("GAME OVER\nScore: " + score);
    }

    @FXML
    private void onRestart() {
        started = false;
        gameOver = false;
        score = 0;
        velocity = 0;

        birdImg.setLayoutY(200);
        scoreLabel.setText("Score: 0");
        gameOverPane.setVisible(false);

        musicPlayer.stop();
        musicPlayer.play();

        for (int i = 0; i < pipes.size(); i++) {
            resetPipe(pipes.get(i), i * 250);
        }
    }
}
