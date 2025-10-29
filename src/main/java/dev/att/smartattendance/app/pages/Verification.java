package dev.att.smartattendance.app.pages;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.Helper;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Verification {
    
    public static Scene createVerificationScene(Stage stage, String username) {
        Label titleLabel = new Label("Face Verification");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Please look at the camera to verify your identity");
        infoLabel.setStyle("-fx-font-size: 14px;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Waiting for face detection...");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #0066cc; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-logout");

        cancelBtn.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(Login.createLoginScene(stage));
        });

        VBox layout = new VBox(20, titleLabel, infoLabel, statusLabel, webcamView, cancelBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, Helper.getScreenWidth(), Helper.getScreenHeight());
        // scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        // auto-start camera for verification
        Platform.runLater(() -> startCameraForVerification(webcamView, statusLabel, stage, username));

        return scene;
    }


    public static void startCameraForVerification(ImageView imageView, Label statusLabel,
            Stage stage, String expectedUsername) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            Helper.showAlert("Camera Error", "Cannot open camera!");
            Platform.runLater(() -> stage.setScene(Login.createLoginScene(stage)));
            return;
        }
        Helper.cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();
                int verificationAttempts = 0;
                int maxAttempts = 50; // ~1.5 seconds of attempts

                while (Helper.cameraActive && !Helper.faceVerified && verificationAttempts < maxAttempts) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Imgproc.cvtColor(Helper.currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        Helper.faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                                new Size(30, 30), new Size());

                        Rect[] faceArray = faces.toArray();

                        if (faceArray.length > 0) {
                            Rect rect = faceArray[0];
                            Mat face = gray.submat(rect);
                            Mat resizedFace = new Mat();
                            Imgproc.resize(face, resizedFace, new Size(200, 200));

                            String recognizedName = Helper.recognizeFace(resizedFace);

                            if (recognizedName.equals(expectedUsername)) {
                                // Face matches
                                Imgproc.rectangle(Helper.currentFrame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 255, 0), 3);

                                Imgproc.putText(Helper.currentFrame, "Verified: " + recognizedName,
                                        new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9,
                                        new Scalar(0, 255, 0), 2);

                                Helper.faceVerified = true;

                                Platform.runLater(() -> {
                                    statusLabel.setText("✓ Face Verified! Access Granted");
                                    statusLabel.setStyle(
                                            "-fx-font-size: 16px; -fx-text-fill: #00cc00; -fx-font-weight: bold;");
                                });

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }

                                Helper.stopCamera();
                                Platform.runLater(() -> stage.setScene(Home.createHomeScene(expectedUsername)));

                            } else {
                                // Face doesn't match
                                Imgproc.rectangle(Helper.currentFrame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 0, 255), 3);

                                String errorText = recognizedName.equals("Unknown") ? "Face Not Recognized"
                                        : "Wrong Person: " + recognizedName;

                                Imgproc.putText(Helper.currentFrame, errorText,
                                        new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9,
                                        new Scalar(0, 0, 255), 2);

                                Platform.runLater(() -> statusLabel.setText("⚠ Face verification failed - try again"));
                            }

                            resizedFace.release();
                            face.release();
                            verificationAttempts++;
                        }

                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                // If verification failed after max attempts
                if (!Helper.faceVerified && verificationAttempts >= maxAttempts) {
                    Helper.stopCamera();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Verification Failed");
                        alert.setHeaderText("Face Verification Failed");
                        alert.setContentText("Your face does not match the enrolled data for " +
                                expectedUsername + ". Access denied.");
                        alert.showAndWait();
                        stage.setScene(Login.createLoginScene(stage));
                    });
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
}
