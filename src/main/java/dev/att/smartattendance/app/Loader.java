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
                String personEmail = personDir.getName();
                List<Mat> images = loadImages(personDir.getAbsolutePath());
                if (!images.isEmpty()) {
                    // Store histogram features
                    Helper.personHistograms.put(personEmail, computeHistograms(images));
                    
                    // Also store LBP features for improved recognition
                    ImprovedRecognitionHelper.storeLBPFeatures(personEmail, images);
                    
                    System.out.println("Loaded " + images.size() + " face images for " + personEmail);
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
