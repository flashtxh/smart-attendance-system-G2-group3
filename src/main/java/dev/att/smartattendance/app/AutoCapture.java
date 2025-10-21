package dev.att.smartattendance.app;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import org.opencv.highgui.HighGui;
import java.io.File;

// public class CropDemo {
    
//     static {
//         System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//     }

//     public static void main(String[] args) {
//         String saveFolder = "src\\main\\resources\\images\\person1";
//         String cascadePath = "src\\main\\resources\\fxml\\haarcascade_frontalface_alt.xml";
        
//         // Create save folder if it doesn't exist
//         new File(saveFolder).mkdirs();

//         // Load face detector
//         CascadeClassifier faceDetector = new CascadeClassifier(cascadePath);
//         if (faceDetector.empty()) {
//             System.out.println("Error loading cascade file: " + cascadePath);
//             return;
//         }

//         // Open webcam
//         VideoCapture capture = new VideoCapture(0);
//         if (!capture.isOpened()) {
//             System.out.println("Error opening webcam!");
//             return;
//         }

//         Mat frame = new Mat();
//         Mat gray = new Mat();

//         int imageCount = 0;
//         final int targetImages = 20;
//         long lastCaptureTime = 0;
//         final int intervalMs = 700;

//         while(imageCount < targetImages) {
//             if(!capture.read(frame)) break;
//             Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

//             MatOfRect faces = new MatOfRect();
//             faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(80,80), new Size());

//             Rect[] faceArray = faces.toArray();
//             for(Rect rect : faceArray) {
//                 Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255,0), 2);
//             }

//             if(faceArray.length == 1) {
//                 long now = System.currentTimeMillis();
//                 if(now - lastCaptureTime > intervalMs) {
//                     Rect rect = faceArray[0];
//                     Mat face = gray.submat(rect);
//                     Mat resizedFace = new Mat();
//                     Imgproc.resize(face, resizedFace, new Size(200, 200));

//                     String fileName = saveFolder + "/face_" + imageCount + ".jpg";
//                     if(Imgcodecs.imwrite(fileName, resizedFace)) {
//                         System.out.println("Saved: " + fileName);
//                         imageCount++;
//                     }

//                     resizedFace.release();
//                     face.release();
//                     lastCaptureTime = now;
//                 }
//             }

//             Imgproc.putText(frame, "Capturing..." + imageCount + "/" + targetImages, new Point(10,30), Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 255, 255), 2);

//             HighGui.imshow("Auto Capture (press Q to quit)", frame);
//             int key = HighGui.waitKey(30) & 0xFF;
//             if(key == 'q' || key == 'Q') break;
//         }

//         System.out.println("Capture complete. " + imageCount + "images saved.");
//         capture.release();
//         HighGui.destroyAllWindows();
//         System.exit(0);
//     }

// }

public class AutoCapture {

    private static final int TARGET_IMAGES = 20;
    private static final int INTERVAL_MS = 700;

    public interface OnCaptureComplete {
        void onComplete(int count);
    }

    public static void runAutoCapture(
            String username,
            String baseImagePath,
            CascadeClassifier faceDetector,
            ImageView imageView,
            Label statusLabel,
            OnCaptureComplete callback
    ) {
        File saveDir = new File(baseImagePath + username);
        saveDir.mkdirs();

        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            Platform.runLater(() -> statusLabel.setText("❌ Cannot open camera"));
            return;
        }

        Mat frame = new Mat();
        Mat gray = new Mat();
        long[] lastCapture = {0}; // mutable timestamp holder
        AtomicInteger imageCount = new AtomicInteger(0); // mutable counter

        new Thread(() -> {
            while (capture.isOpened() && imageCount.get() < TARGET_IMAGES) {
                if (!capture.read(frame)) break;

                Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                        new Size(80, 80), new Size());

                Rect[] faceArray = faces.toArray();
                if (faceArray.length == 1) {
                    long now = System.currentTimeMillis();
                    if (now - lastCapture[0] > INTERVAL_MS) {
                        Rect rect = faceArray[0];
                        Mat face = gray.submat(rect);
                        Mat resized = new Mat();
                        Imgproc.resize(face, resized, new Size(200, 200));

                        String filename = saveDir.getAbsolutePath() + "/face_" + imageCount.get() + ".jpg";
                        Imgcodecs.imwrite(filename, resized);

                        int countNow = imageCount.incrementAndGet();
                        lastCapture[0] = now;

                        Platform.runLater(() ->
                                statusLabel.setText("Capturing: " + countNow + "/" + TARGET_IMAGES));

                        face.release();
                        resized.release();
                    }
                }

                Platform.runLater(() ->
                        imageView.setImage(mat2Image(frame)));

                try { Thread.sleep(30); } catch (InterruptedException ignored) {}
            }

            capture.release();
            frame.release();
            gray.release();
            Platform.runLater(() -> callback.onComplete(imageCount.get()));
        }).start();
    }

    private static Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}