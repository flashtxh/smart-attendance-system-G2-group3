package dev.att.smartattendance.app;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.dnn.*;
import ai.onnxruntime.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecognitionDemo {
    static {
        // Load OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // if (args.length < 3) {
        //     System.out.println("Usage: java FaceRecognitionDemo <person1-dir> <person2-dir> <haarcascade-path>");
        //     System.out.println("Example: java FaceRecognitionDemo D:\\person1 D:\\person2 .\\haarcascade_frontalface_alt.xml");
        //     return;
                // }
        String maskModelPath = "src\\main\\resources\\models\\mask_detector.onnx";
        Net maskNet = Dnn.readNetFromONNX(maskModelPath);
        System.out.println("Mask detection model loaded: " + !maskNet.empty());

        String person1Dir = "src\\main\\resources\\images\\person1"; // args[0];
        String person2Dir = "src\\main\\resources\\images\\person2"; // args[1];
        // String cascadePath = "src\\main\\resources\\fxml\\haarcascade_frontalface_alt.xml"; // args[2];
        
        // Load face detector
        CascadeClassifier faceDetector = new CascadeClassifier();
        if(!faceDetector.load("src\\main\\resources\\fxml\\haarcascade_frontalface_alt.xml")) {
            System.out.println("Could not load face xml");
            return;
        }


        Scalar green = new Scalar(0,128,0);
        Scalar red = new Scalar(0,0,255);

        // Load training images and compute histograms
        List<Mat> person1Images = loadImages(person1Dir);
        List<Mat> person2Images = loadImages(person2Dir);
        if (person1Images.isEmpty() || person2Images.isEmpty()) {
            System.out.println("No training images found in one or both directories!");
            return;
        }

        List<Mat> person1Histograms = computeHistograms(person1Images);
        List<Mat> person2Histograms = computeHistograms(person2Images);

        // Open webcam
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            System.out.println("Error opening webcam!");
            return;
        }

        // Create display window
        JFrame frame = new JFrame("Real-Time Face Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        frame.add(label);
        frame.setSize(640, 480);
        frame.setVisible(true);

        Mat webcamFrame = new Mat();

        OrtEnvironment env = null;
        OrtSession session = null;
        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            session = env.createSession("src\\main\\resources\\models\\mask_detector.onnx", opts);
        } catch (OrtException e) {
            System.err.println("ONNX initialization failed: " + e.getMessage());
            return;
        }

        while (frame.isVisible() && capture.read(webcamFrame)) {

            Mat gray = new Mat();
            Imgproc.cvtColor(webcamFrame, gray, Imgproc.COLOR_BGR2GRAY);

            // Detect faces
            MatOfRect faces = new MatOfRect();
            faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size());

            for (Rect rect : faces.toArray()) {

                //Mask Detection via ONNX model
                Mat faceROI = new Mat(webcamFrame, rect);
                Mat resized = new Mat();
                Imgproc.cvtColor(faceROI, resized, Imgproc.COLOR_BGR2RGB);
                Imgproc.resize(resized, resized, new Size(224, 224));

                resized.convertTo(resized, CvType.CV_32FC3, 1.0/255.0);
                int width = 224, height = 224, channels = 3;
                float[] nhwc = new float[width * height * channels];
                resized.get(0,0, nhwc);
                
                boolean maskDetected = false;

                // Purpose of placing these in the try block is for automatic cleanup.
                try(
                    OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(nhwc), new long[]{1, height, width, channels}); 
                    // Runs the model
                    OrtSession.Result result = session.run(Map.of(session.getInputNames().iterator().next(), inputTensor))
                ) {
                    float[][] probs = (float[][]) result.get(0).getValue();
                    maskDetected = probs[0][0] > probs[0][1];
                } catch (OrtException e) {
                    if (e.getMessage() == null || !e.getMessage().contains("DefaultLogger")) {
                        e.printStackTrace();
                    }
                }

                if(maskDetected) {
                    Imgproc.rectangle(webcamFrame, rect.tl(), rect.br(), red, 2);
                    Imgproc.putText(webcamFrame, "Mask Detected", new Point(rect.x, rect.y - 10), Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, red);
                } else {
                    // Draw rectangle
                    Imgproc.rectangle(webcamFrame, new Point(rect.x, rect.y),
                            new Point(rect.x + rect.width, rect.y + rect.height),
                            green, 2);

                    // Crop and resize face
                    Mat face = gray.submat(rect);
                    Imgproc.resize(face, face, new Size(200, 200));
                    Mat faceHist = computeHistogram(face);

                    // Compare with training histograms
                    double bestScore1 = getBestHistogramScore(faceHist, person1Histograms);
                    double bestScore2 = getBestHistogramScore(faceHist, person2Histograms);

                    // Label based on best score (correlation: higher is better)
                    String displayText = bestScore1 > bestScore2 && bestScore1 > 0.7 ? "ZHANG Zhiyuan" :
                            bestScore2 > bestScore1 && bestScore2 > 0.7 ? "ZHANG Zhiyuan" : "Unknown";
                    Imgproc.putText(webcamFrame, displayText, new Point(rect.x, rect.y - 10),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, green, 2);
                }
            }

            // Display frame
            BufferedImage image = matToBufferedImage(webcamFrame);
            label.setIcon(new ImageIcon(image));
            label.repaint();
        }

        // Cleanup
        capture.release();
        frame.dispose();
    }

    // Load images from a directory
    private static List<Mat> loadImages(String dirPath) {
        List<Mat> images = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        if (files != null) {
            for (File file : files) {
                Mat img = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                if (!img.empty()) {
                    Imgproc.resize(img, img, new Size(200, 200));
                    images.add(img);
                } else {
                    System.out.println("Failed to load image: " + file.getAbsolutePath());
                }
            }
        }
        return images;
    }

    // Compute histogram for a single image
    private static Mat computeHistogram(Mat image) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);
        Imgproc.calcHist(List.of(image), channels, new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        return hist;
    }

    // Compute histograms for a list of images
    private static List<Mat> computeHistograms(List<Mat> images) {
        List<Mat> histograms = new ArrayList<>();
        for (Mat img : images) {
            histograms.add(computeHistogram(img));
        }
        return histograms;
    }

    // Get best histogram comparison score
    private static double getBestHistogramScore(Mat faceHist, List<Mat> histograms) {
        double bestScore = 0;
        for (Mat hist : histograms) {
            double score = Imgproc.compareHist(faceHist, hist, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }

    // Convert Mat to BufferedImage for display
    private static BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
            // Convert BGR to RGB
            Mat rgbMat = new Mat();
            Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_BGR2RGB);
            mat = rgbMat;
        }

        BufferedImage image = new BufferedImage(width, height, type);
        byte[] data = new byte[width * height * (int)mat.elemSize()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }
}
