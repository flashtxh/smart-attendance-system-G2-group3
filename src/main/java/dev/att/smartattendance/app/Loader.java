package dev.att.smartattendance.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;
import org.opencv.objdetect.HOGDescriptor;

import dev.att.smartattendance.model.student.Student;
import dev.att.smartattendance.model.student.StudentDAO;

public class Loader {

    public static void loadStudentNames() {
        Helper.emailToNameMap.clear();
        StudentDAO studentDAO = new StudentDAO();
        try {
            for (Student student : studentDAO.get_all_students()) {
                Helper.emailToNameMap.put(student.getEmail(), student.getName());
            }
        } catch (Exception e) {
            System.err.println("Error loading student email-name mapping: " + e.getMessage());
        }
    }
    public static void loadExistingPersons() {
        File baseDir = new File(Helper.baseImagePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
            return;
        }

        File[] personDirs = baseDir.listFiles(File::isDirectory);
        if (personDirs != null) {
            for (File personDir : personDirs) {
                String personName = personDir.getName();
                List<Mat> images = loadImages(personDir.getAbsolutePath());
                if (!images.isEmpty()) {
                    Helper.personHistograms.put(personName, computeLBPDescriptors(images));
                    Helper.personHOGDescriptors.put(personName, computeHOGDescriptors(images));
                    System.out.println("Loaded " + images.size() + " face images for " + personName);
                }
            }
        }
    }

    public static List<Mat> loadImages(String dirPath) {
        List<Mat> images = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir
                .listFiles((d, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (files != null) {
            for (File file : files) {
                Mat img = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                if (!img.empty()) {
                    Mat resized = new Mat();
                    Imgproc.resize(img, resized, new Size(200, 200));
                    images.add(resized);
                }
            }
        }
        return images;
    }

    public static List<Mat> computeLBPDescriptors(List<Mat> images) {
        List<Mat> features = new ArrayList<>();
        for (Mat img : images) {
            features.add(computeLBPFeatures(img));
        }
        return features;
    }

    public static Mat computeLBPFeatures(Mat face) {
        Mat lbp = new Mat(face.size(), CvType.CV_8UC1);
        for (int i = 1; i < face.rows() - 1; i++) {
            for (int j = 1; j < face.cols() - 1; j++) {
                double center = face.get(i, j)[0];
                int lbpValue = 0;
                if (face.get(i - 1, j - 1)[0] >= center) lbpValue |= 1;
                if (face.get(i - 1, j)[0] >= center) lbpValue |= 2;
                if (face.get(i - 1, j + 1)[0] >= center) lbpValue |= 4;
                if (face.get(i, j + 1)[0] >= center) lbpValue |= 8;
                if (face.get(i + 1, j + 1)[0] >= center) lbpValue |= 16;
                if (face.get(i + 1, j)[0] >= center) lbpValue |= 32;
                if (face.get(i + 1, j - 1)[0] >= center) lbpValue |= 64;
                if (face.get(i, j - 1)[0] >= center) lbpValue |= 128;
                lbp.put(i, j, lbpValue);
            }
        }
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);
        Imgproc.calcHist(List.of(lbp), channels, new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        lbp.release();
        return hist;
    }

    public static List<Mat> computeHOGDescriptors(List<Mat> images) {
        List<Mat> features = new ArrayList<>();
        for (Mat img : images) {
            features.add(computeHOGFeatures(img));
        }
        return features;
    }

    public static Mat computeHOGFeatures(Mat face) {
        Mat resized = new Mat();
        // Normalize size for HOG window (square to keep pipeline simple)
        Imgproc.resize(face, resized, new Size(128, 128));
        HOGDescriptor hog = new HOGDescriptor(
            new Size(128, 128),   // winSize
            new Size(16, 16),     // blockSize
            new Size(8, 8),       // blockStride
            new Size(8, 8),       // cellSize
            9                     // nbins
        );
        MatOfFloat descriptors = new MatOfFloat();
        hog.compute(resized, descriptors);
        Mat vec = new Mat(1, descriptors.rows(), CvType.CV_32F);
        // descriptors is a column vector; copy to a 1xN row vector
        float[] data = new float[(int) descriptors.total()];
        descriptors.get(0, 0, data);
        vec.put(0, 0, data);
        Core.normalize(vec, vec);
        resized.release();
        descriptors.release();
        return vec;
    }

    public static List<Mat> computeHistograms(List<Mat> images) {
        List<Mat> histograms = new ArrayList<>();
        for (Mat img : images) {
            histograms.add(computeHistogram(img));
        }
        return histograms;
    }

    public static Mat computeHistogram(Mat image) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);
        Imgproc.calcHist(List.of(image), channels, new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        return hist;
    }
}
