package dev.att.smartattendance.app.pages;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.Helper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Home {

    public static Scene createHomeScene(String username) {
        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        headerSection.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("SMART ATTENDANCE SYSTEM");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // User avatar section
        VBox avatarSection = new VBox(5);
        avatarSection.setAlignment(Pos.CENTER);

        Label avatarLabel = new Label(username.substring(0, 1).toUpperCase());
        avatarLabel.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 20px; " +
                "-fx-font-weight: bold; -fx-min-width: 50; -fx-min-height: 50; " +
                "-fx-max-width: 50; -fx-max-height: 50; -fx-background-radius: 25; " +
                "-fx-alignment: center;");

        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        avatarSection.getChildren().addAll(avatarLabel, usernameLabel);

        // Professor info section
        VBox profSection = new VBox(2);
        profSection.setAlignment(Pos.CENTER_RIGHT);
        Label profLabel = new Label("Prof. "+username);
        profLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        profSection.getChildren().add(profLabel);

        // Header layout
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER);
        javafx.scene.layout.Region leftSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.Region rightSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        headerTop.getChildren().addAll(avatarSection, leftSpacer, titleLabel, rightSpacer, profSection);
        headerSection.getChildren().add(headerTop);

        // Content section
        VBox contentSection = new VBox(20);
        contentSection.setStyle("-fx-padding: 40;");
        contentSection.setAlignment(Pos.CENTER_LEFT);

        // Current Semester section
        HBox semesterSection = new HBox(10);
        semesterSection.setAlignment(Pos.CENTER_LEFT);

        Label semesterLabel = new Label("Current Semester:");
        semesterLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Label semesterValue = new Label("AY 25/26 Sem 1 ▼");
        semesterValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        semesterSection.getChildren().addAll(semesterLabel, semesterValue);

        // Classes section
        Label classLabel = new Label("Class you are belong to:");
        classLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // Class buttons
        HBox classButtons = new HBox(10);
        classButtons.setAlignment(Pos.CENTER_LEFT);

        Button cs102Btn = new Button("CS102");
        Button is216Btn = new Button("IS216");
        Button cs440Btn = new Button("CS440");

        String buttonStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-padding: 8 16; -fx-font-size: 14px;";

        cs102Btn.setStyle(buttonStyle);
        is216Btn.setStyle(buttonStyle);
        cs440Btn.setStyle(buttonStyle);

        classButtons.getChildren().addAll(cs102Btn, is216Btn, cs440Btn);

        // New Class button
        Button newClassBtn = new Button("New Class");
        newClassBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        HBox classRow = new HBox(20);
        classRow.setAlignment(Pos.CENTER_LEFT);
        classRow.getChildren().addAll(classLabel, classButtons);

        HBox newClassRow = new HBox();
        newClassRow.setAlignment(Pos.CENTER_RIGHT);
        newClassRow.getChildren().add(newClassBtn);

        contentSection.getChildren().addAll(semesterSection, classRow, newClassRow);

        // Bottom section with camera and logout
        VBox bottomSection = new VBox(20);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setStyle("-fx-padding: 20;");

        // Camera view
        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(400);
        webcamView.setFitHeight(300);
        webcamView.setPreserveRatio(true);
        webcamView.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2;");

        Label cameraLabel = new Label("Live Camera Feed (Optional)");
        cameraLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 10 20; -fx-font-size: 14px;");

        logoutButton.setOnAction(e -> {
            Helper.stopCamera();
            Helper.loggedInUsername = "";
            Helper.faceVerified = false;
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(Login.createLoginScene(stage));
        });

        bottomSection.getChildren().addAll(cameraLabel, webcamView, logoutButton);

        // Add event handlers for class buttons
        cs102Btn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) cs102Btn.getScene().getWindow();
            stage.setScene(Class.createClassScene("CS102", username, stage));
        });
        is216Btn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) is216Btn.getScene().getWindow();
            stage.setScene(Class.createClassScene("IS216", username, stage));
        });
        cs440Btn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) cs440Btn.getScene().getWindow();
            stage.setScene(Class.createClassScene("CS440", username, stage));
        });
        newClassBtn.setOnAction(e -> Helper.showAlert("New Class", "Creating new class..."));

        // Main layout
        mainContainer.getChildren().addAll(headerSection, contentSection, bottomSection);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());

        // Optional: Auto-start camera in home (disabled by default)
        // Platform.runLater(() -> startSimpleCameraFeed(webcamView));

        return scene;
    }

    // Simple camera feed without face recognition (optional for home screen)
    public static void startSimpleCameraFeed(ImageView imageView) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            return;
        }
        Helper.cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();

                while (Helper.cameraActive) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
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
}