package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class App extends Application {

    private VideoCapture capture;
    private volatile boolean cameraActive = false;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {
        launch();

        // For this to work, uncomment CropDemo and save some images first. Try saving around 8-10.
        // Afterwards, comment it out and uncomment RecognitionDemo to see if it works and detects.
        // After testing, you can delete the images from the person1 folder.
        // person2 folder contains an image of a random person in order for this code to work.
        
        // CropDemo.main(args);
        // RecognitionDemo.main(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Label label = new Label("Hello, JavaFX + Maven!");
        // Scene scene = new Scene(label, 400, 200);
        Scene loginScene = createLoginScene(primaryStage);

        primaryStage.setTitle("JavaFX App");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private double getScreenWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() * 0.5;
    }

    private double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.5;
    }

    private Scene createLoginScene(Stage stage) {
        Label titleLabel = new Label("Smart Attendance Login");
        titleLabel.getStyleClass().add("title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("error");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().strip();
            String password = passwordField.getText().strip();

            if (!username.isEmpty() && !password.isEmpty()) {
                stage.setScene(createHomeScene(username));
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });


        VBox layout = new VBox(25, titleLabel, usernameField, passwordField, loginButton, messageLabel);
        layout.getStyleClass().add("vbox");

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        return scene;
    }



    private Scene createHomeScene(String username) {
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.getStyleClass().add("welcome-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-logout");

        Button toggleCamBtn = new Button("Turn Webcam On");
        toggleCamBtn.getStyleClass().add("btn-togglecam");

        // When logout clicked, go back to login scene
        logoutButton.setOnAction(e -> {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(createLoginScene(stage));
        });
        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);
        webcamView.setSmooth(true);
        webcamView.setCache(true);

        VBox layout = new VBox(20, welcomeLabel, toggleCamBtn, webcamView, logoutButton);
        layout.getStyleClass().add("home-layout");
        layout.setAlignment(Pos.CENTER);

        // Logout button action
        logoutButton.setOnAction(e -> {
            stopCamera();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(createLoginScene(stage));
        });

        // Toggle camera button action
        toggleCamBtn.setOnAction(e -> {
            if (!cameraActive) {
                startCamera(webcamView);
                toggleCamBtn.setText("Turn Webcam Off");
            } else {
                stopCamera();
                toggleCamBtn.setText("Turn Webcam On");
                webcamView.setImage(null);
            }
        });

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        return scene;
    }
    private void startCamera(ImageView imageView) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Cannot open camera!");
            return;
        }
        cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                while (cameraActive) {
                    if (capture.read(frame)) {
                        Image imageToShow = mat2Image(frame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                    }
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                frame.release();
                return null;
            }
        };

        Thread th = new Thread(frameGrabber);
        th.setDaemon(true);
        th.start();
    }

    private void stopCamera() {
        cameraActive = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }

    private Image mat2Image(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            System.err.println("Cannot convert Mat object: " + e);
            return null;
        }
    }

}