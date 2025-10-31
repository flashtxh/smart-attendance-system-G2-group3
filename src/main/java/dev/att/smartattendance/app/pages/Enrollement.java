package dev.att.smartattendance.app.pages;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.app.Loader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Enrollement {
    public static Scene createEnrollmentScene(Stage stage, String username) {
        Label titleLabel = new Label("Face Enrollment Required");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label(
                "Welcome, " + username + "! We need to capture your face for attendance verification.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 14px;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Click 'Start Enrollment' to begin");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #0066cc; -fx-font-weight: bold;");

        Button startEnrollBtn = new Button("Start Enrollment");
        startEnrollBtn.getStyleClass().add("btn-togglecam");

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("btn-logout");

        startEnrollBtn.setOnAction(e -> {
            if (!Helper.cameraActive) {
                startEnrollmentProcess(username, webcamView, statusLabel, startEnrollBtn, backBtn, stage);
                startEnrollBtn.setDisable(true);
            }
        });

        backBtn.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(Login.createLoginScene(stage));
        });

        HBox buttonBox = new HBox(15, startEnrollBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, titleLabel, infoLabel, statusLabel, webcamView, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, Helper.getScreenWidth(), Helper.getScreenHeight());
        return scene;
    }

    public static void startEnrollmentProcess(String username, ImageView webcamView, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
        Helper.capturingMode = true;
        Helper.capturePersonName = username;
        Helper.captureCount = 0;

        File personDir = new File(Helper.baseImagePath + username);
        personDir.mkdirs();

        Platform.runLater(() -> statusLabel.setText("Look at the camera - Capturing: 0/8"));

        startCameraForEnrollment(webcamView, statusLabel, enrollBtn, backBtn, stage);
    }

    public static void startCameraForEnrollment(ImageView imageView, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            Helper.showAlert("Camera Error", "Cannot open camera!");
            return;
        }
        Helper.cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();
                int frameCounter = 0;

                while (Helper.cameraActive && Helper.capturingMode) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Imgproc.cvtColor(Helper.currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        Helper.faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                                new Size(30, 30), new Size());

                        Rect[] faceArray = faces.toArray();

                        for (Rect rect : faceArray) {
                            Imgproc.rectangle(Helper.currentFrame, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(255, 165, 0), 3);

                            if (frameCounter % 15 == 0 && Helper.captureCount < 8) {
                                saveFaceForEnrollment(gray, rect, statusLabel, enrollBtn, backBtn, stage);
                            }

                            String text = "Capturing: " + Helper.captureCount + "/8";
                            Imgproc.putText(Helper.currentFrame, text,
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.9,
                                    new Scalar(255, 165, 0), 2);
                        }

                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));

                        frameCounter++;
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                frame.release();
                gray.release();
                return null;
            }
        };

        Thread th = new Thread(frameGrabber);
        th.setDaemon(true);
        th.start();
    }

    public static void saveFaceForEnrollment(Mat gray, Rect rect, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
        Mat face = gray.submat(rect);
        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, new Size(200, 200));

        String fileName = Helper.baseImagePath + Helper.capturePersonName + "/face_" +
                System.currentTimeMillis() + ".jpg";
        Imgcodecs.imwrite(fileName, resizedFace);

        Helper.captureCount++;
        Platform.runLater(() -> statusLabel.setText("Capturing: " + Helper.captureCount + "/8"));

        if (Helper.captureCount >= 8) {
            Helper.capturingMode = false;
            Helper.captureCount = 0;

            // CRITICAL FIX: Load the newly captured images and compute histograms
            List<Mat> newImages = Loader.loadImages(Helper.baseImagePath + Helper.capturePersonName);
            List<Mat> newHistograms = Loader.computeHistograms(newImages);
            Helper.personHistograms.put(Helper.capturePersonName, newHistograms);
            
            // Release loaded images to free memory
            for (Mat img : newImages) {
                img.release();
            }

            Helper.stopCamera();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Enrollment Complete");
                alert.setHeaderText("Success!");
                alert.setContentText("Face enrollment completed for " + Helper.capturePersonName +
                        ". You can now log in with face verification.");
                alert.showAndWait();

                stage.setScene(Login.createLoginScene(stage));
            });
        }

        resizedFace.release();
        face.release();
    }
}