package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static String capturePersonName = "";
    public static boolean capturingMode = false;
    public static boolean cameraActive = false;
    public static VideoCapture capture;
    public static Mat currentFrame;
    public static CascadeClassifier faceDetector;
    public static String loggedInUsername = "";
    public static boolean faceVerified = false;
    public static Map<String, String> userCredentials = new HashMap<>(); // username -> password
    public static Map<String, List<Mat>> personHistograms = new HashMap<>();
    public static String baseImagePath = "src/main/resources/images/";
    public static int captureCount = 0;

    public static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static double getScreenWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() * 0.8;
    }

    public static double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.7;
    }

    public static void stopCamera() {
        cameraActive = false;
        capturingMode = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
    }

    public static Image mat2Image(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            System.err.println("Cannot convert Mat object: " + e);
            return null;
        }
        // System.out.println("mat2Image called");

        // if (frame == null) {
        //     System.out.println("⚠️ frame is null");
        //     return null;
        // }

        // if (frame.empty()) {
        //     System.out.println("⚠️ frame is empty");
        //     return null;
        // }

        // System.out.println("Frame channels: " + frame.channels() + ", type: " + frame.type());

        // try {
        //     // Convert to BGRA (4 channels)
        //     Mat converted = new Mat();
        //     if (frame.channels() == 1) {
        //         System.out.println("Converting grayscale to BGRA...");
        //         Imgproc.cvtColor(frame, converted, Imgproc.COLOR_GRAY2BGRA);
        //     } else {
        //         System.out.println("Converting BGR to BGRA...");
        //         Imgproc.cvtColor(frame, converted, Imgproc.COLOR_BGR2BGRA);
        //     }

        //     System.out.println("Conversion done: " + converted.size() + ", channels: " + converted.channels());

        //     int width = converted.cols();
        //     int height = converted.rows();
        //     byte[] data = new byte[width * height * 4];
        //     converted.get(0, 0, data);
        //     System.out.println("Got pixel data, length: " + data.length);

        //     javafx.scene.image.WritableImage image = new javafx.scene.image.WritableImage(width, height);
        //     javafx.scene.image.PixelWriter pw = image.getPixelWriter();
        //     pw.setPixels(0, 0, width, height,
        //             javafx.scene.image.PixelFormat.getByteBgraPreInstance(),
        //             data, 0, width * 4);

        //     converted.release();
        //     System.out.println("mat2Image success ✅");
        //     return image;

        // } catch (Exception e) {
        //     System.err.println("❌ mat2Image failed: " + e.getMessage());
        //     e.printStackTrace();
        //     return null;
        // }
    }

    public static double getBestHistogramScore(Mat faceHist, List<Mat> histograms) {
        double bestScore = 0;
        for (Mat hist : histograms) {
            double score = Imgproc.compareHist(faceHist, hist, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }

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
        return bestMatch;
    }
}
