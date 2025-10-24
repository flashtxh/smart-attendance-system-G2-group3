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