package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import dev.att.smartattendance.app.pages.Login;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    private VideoCapture capture;
    private volatile boolean cameraActive = false;
    private CascadeClassifier faceDetector;
    private Map<String, List<Mat>> personHistograms = new HashMap<>();
    private Map<String, String> userCredentials = new HashMap<>(); // username -> password
    private Mat currentFrame;
    private String baseImagePath = "src/main/resources/images/";
    private String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";

    private int captureCount = 0;
    private String capturePersonName = "";
    private boolean capturingMode = false;
    private String loggedInUsername = "";
    private boolean faceVerified = false;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("OpenCV library not found. Camera features will be disabled.");
        }
        // System.load("/usr/local/opencv/share/java/opencv4/libopencv_java480.dylib");
        // // For MAC
    }

    public static void main(String[] args) {
        launch(args);
        // CropDemo.main(args);
        // RecognitionDemo.main(args);

        // ProfessorDAO pdao = new ProfessorDAO();
        // for(String prof : pdao.get_all_professors()) {
        // System.out.println(prof);
        // }
        // Professor kyong = pdao.get_professor_by_email("kyong@smu.edu.sg");
        // System.out.println(kyong);

        // CourseDAO cdao = new CourseDAO();
        // for(Course course : cdao.get_all_courses()) {
        // System.out.println(course);
        // }

        // GroupDAO gdao = new GroupDAO();
        // for(Group group : gdao.get_all_groups()) {
        // System.out.println(group);
        // }

        // StudentDAO sdao = new StudentDAO();
        // for(Student student : sdao.get_all_students()) {
        // System.out.println(student);
        // }

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

        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(Login.createLoginScene(primaryStage));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            stopCamera();
            System.exit(0);
        });
    }

    private String recognizeFace(Mat face) {
        Mat faceHist = Loader.computeHistogram(face);

        String bestMatch = "Unknown";
        double bestScore = 0.7; // threshold for recognition

        for (Map.Entry<String, List<Mat>> entry : personHistograms.entrySet()) {
            double score = getBestHistogramScore(faceHist, entry.getValue());
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry.getKey();
            }
        }

        faceHist.release();
        return bestMatch;
    }

    private double getBestHistogramScore(Mat faceHist, List<Mat> histograms) {
        double bestScore = 0;
        for (Mat hist : histograms) {
            double score = Imgproc.compareHist(faceHist, hist, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }

    private void stopCamera() {
        cameraActive = false;
        capturingMode = false;
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