package dev.att.smartattendance.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.pages.Login;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class App extends Application {

    private VideoCapture capture;
    @SuppressWarnings("unused")
    private volatile boolean cameraActive = false;
    @SuppressWarnings("unused")
    private CascadeClassifier faceDetector;
    @SuppressWarnings("unused")
    private final Map<String, List<Mat>> personHistograms = new HashMap<>();
    @SuppressWarnings("unused")
    private final Map<String, String> userCredentials = new HashMap<>(); // username -> password
    @SuppressWarnings("unused")
    private Mat currentFrame;
    @SuppressWarnings("unused")
    private final String baseImagePath = "src/main/resources/images/";
    private final String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";

    @SuppressWarnings("unused")
    private final int captureCount = 0;
    @SuppressWarnings("unused")
    private final String capturePersonName = "";
    @SuppressWarnings("unused")
    private boolean capturingMode = false;
    @SuppressWarnings("unused")
    private final String loggedInUsername = "";
    @SuppressWarnings("unused")
    private final boolean faceVerified = false;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            // System.load("/usr/local/opencv/share/java/opencv4/libopencv_java480.dylib");
            // // For MAC
        } catch (UnsatisfiedLinkError e) {
            System.err.println("OpenCV library not found. Camera features will be disabled.");
        }

    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // initialize face detector
        Helper.faceDetector = new CascadeClassifier(cascadePath);
        if (Helper.faceDetector.empty()) {
            showAlert("Error", "Could not load face detection model!");
            return;
        }

        // load existing persons
        Loader.loadExistingPersons();
        Login.initializeCredentials();
        Loader.loadStudentNames();

        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(Login.createLoginScene(primaryStage));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            stopCamera();
            System.exit(0);
        });
    }

    private void stopCamera() {
        cameraActive = false;
        capturingMode = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
