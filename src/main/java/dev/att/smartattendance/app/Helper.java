package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgproc.Imgproc;
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
    public static Map<String, List<Mat>> personHOGDescriptors = new HashMap<>();
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
        RecognitionResult result = recognizeFaceWithScore(face);
        return result.label;
    }

    public static class RecognitionResult {
        public final String label;
        public final double score;
        public RecognitionResult(String label, double score) {
            this.label = label;
            this.score = score;
        }
    }

    public static RecognitionResult recognizeFaceWithScore(Mat face) {
        Mat lbpFeatures = Loader.computeLBPFeatures(face);
        Mat hogFeatures = Loader.computeHOGFeatures(face);

        String bestMatch = "Unknown";
        double bestScore = 0.0;
        String secondBest = "Unknown";
        double secondBestScore = 0.0;

        double adaptiveThreshold = getAdaptiveThreshold(personHistograms.size());

        System.out.println("=== Recognition Debug ===");
        for (Map.Entry<String, List<Mat>> entry : personHistograms.entrySet()) {
            String person = entry.getKey();
            List<Mat> lbpTemplates = entry.getValue();
            List<Mat> hogTemplates = personHOGDescriptors.getOrDefault(person, List.of());

            double lbpScore = getTopKAvgHistogramScore(lbpFeatures, lbpTemplates, 3);
            double hogScore = getTopKAvgCosineScore(hogFeatures, hogTemplates, 3);
            double fusedScore = (0.3 * lbpScore) + (0.7 * hogScore);

            System.out.println("Person: " + person + " LBP: " + String.format("%.3f", lbpScore) +
                               " HOG: " + String.format("%.3f", hogScore) +
                               " Fused: " + String.format("%.3f", fusedScore));

            if (fusedScore > bestScore) {
                secondBestScore = bestScore;
                secondBest = bestMatch;
                bestScore = fusedScore;
                bestMatch = person;
            } else if (fusedScore > secondBestScore) {
                secondBestScore = fusedScore;
                secondBest = person;
            }
        }

        // Confidence margin: reject if top-2 gap < 0.015
        if ((bestScore - secondBestScore) < 0.015) {
            System.out.println("Ambiguous match (gap " + String.format("%.3f", bestScore - secondBestScore) + "), rejecting.");
            bestMatch = "Unknown";
        }

        // Adaptive thresholds by DB size
        if (bestScore < adaptiveThreshold) {
            System.out.println("Below adaptive threshold (" + String.format("%.2f", adaptiveThreshold) + "), rejecting.");
            bestMatch = "Unknown";
        }

        System.out.println("Final Match: " + bestMatch + " (Score: " + String.format("%.3f", bestScore) + 
                           ", Second: " + secondBest + " " + String.format("%.3f", secondBestScore) + ")");

        lbpFeatures.release();
        hogFeatures.release();
        return new RecognitionResult(bestMatch, bestScore);
    }
    

     // Get best histogram match score
     
    private static double getTopKAvgHistogramScore(Mat queryHist, List<Mat> templates, int k) {
        if (templates == null || templates.isEmpty()) return 0.0;
        double[] scores = new double[templates.size()];
        for (int i = 0; i < templates.size(); i++) {
            scores[i] = Imgproc.compareHist(queryHist, templates.get(i), Imgproc.HISTCMP_CORREL);
        }
        java.util.Arrays.sort(scores);
        // take top-k from end
        int take = Math.min(k, scores.length);
        double sum = 0.0;
        for (int i = 0; i < take; i++) {
            sum += scores[scores.length - 1 - i];
        }
        return sum / take;
    }

    private static double getTopKAvgCosineScore(Mat queryVec, List<Mat> templates, int k) {
        if (templates == null || templates.isEmpty() || queryVec.empty()) return 0.0;
        double[] scores = new double[templates.size()];
        for (int i = 0; i < templates.size(); i++) {
            scores[i] = cosineSimilarity(queryVec, templates.get(i));
        }
        java.util.Arrays.sort(scores);
        int take = Math.min(k, scores.length);
        double sum = 0.0;
        for (int i = 0; i < take; i++) {
            sum += scores[scores.length - 1 - i];
        }
        return sum / take;
    }

    private static double cosineSimilarity(Mat a, Mat b) {
        if (a.empty() || b.empty()) return 0.0;
        // Ensure both are 1xN float
        Mat af = a.reshape(1, 1);
        Mat bf = b.reshape(1, 1);
        double dot = af.dot(bf);
        double na = Math.sqrt(af.dot(af));
        double nb = Math.sqrt(bf.dot(bf));
        if (na == 0 || nb == 0) return 0.0;
        return dot / (na * nb);
    }

    private static double getAdaptiveThreshold(int numPersons) {
        if (numPersons < 5) return 0.85;
        if (numPersons <= 15) return 0.88;
        return 0.92;
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