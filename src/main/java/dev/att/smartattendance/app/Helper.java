package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;

public class Helper {
    // Camera and detection
    public static VideoCapture capture;
    public static volatile boolean cameraActive = false;
    public static CascadeClassifier faceDetector;
    public static Mat currentFrame;
    
    // Face recognition data
    public static Map<String, List<Mat>> personHistograms = new HashMap<>();
    public static Map<String, String> userCredentials = new HashMap<>();
    
    // Enrollment
    public static int captureCount = 0;
    public static String capturePersonName = "";
    public static boolean capturingMode = false;
    public static String capturePersonEmail = "";
    
    // Authentication
    public static String loggedInUsername = "";
    public static boolean faceVerified = false;
    
    // Attendance tracking
    public static Set<String> markedStudentsToday = new HashSet<>();
    
    // Paths
    public static String baseImagePath = "src/main/resources/images/";
    public static String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";
    

    public static Map<String, String> emailToNameMap = new HashMap<>();
     // Get screen width

    public static double getScreenWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth();
    }
    

     // Get screen height

    public static double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight();
    }
    

     // Convert OpenCV Mat to JavaFX Image

    public static Image mat2Image(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            System.err.println("Cannot convert Mat object: " + e);
            return null;
        }
    }
    

     // Stop camera and release resources

    public static void stopCamera() {
        cameraActive = false;
        capturingMode = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }
    

     // Show alert dialog

    public static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    

     // Recognize face from Mat image
     
    public static String recognizeFace(Mat face) {
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
        return bestMatch + " " + String.format("%.2f", bestScore * 100);
    }
    

     // Get best histogram match score
     
    private static double getBestHistogramScore(Mat faceHist, List<Mat> histograms) {
        double bestScore = 0;
        for (Mat hist : histograms) {
            double score = Imgproc.compareHist(faceHist, hist, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }
    

     // Mark student as present for today
     
    public static void markStudentPresent(String studentName) {
        markedStudentsToday.add(studentName);
        System.out.println("Student marked present: " + studentName);
    }
    

     // Check if student is marked present
     
    public static boolean isStudentMarkedPresent(String studentName) {
        return markedStudentsToday.contains(studentName);
    }
    

     // Clear today's attendance (call at start of new session)
     
    public static void clearTodayAttendance() {
        markedStudentsToday.clear();
    }
    

     // Get all marked students
     
    public static Set<String> getMarkedStudents() {
        return new HashSet<>(markedStudentsToday);
    }
}