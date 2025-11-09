package dev.att.smartattendance.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class ImprovedRecognitionHelper {
    
    private static Map<String, List<Mat>> personLBPFeatures = new HashMap<>();
    
    public static String recognizeFaceImproved(Mat face) {
        
        if (!isFaceQualitySufficient(face)) {
            return "Unknown";
        }
        
        
        Mat faceHist = Loader.computeHistogram(face);
        Mat lbpFeatures = computeLBPFeatures(face);
        
        String bestMatch = "Unknown";
        double bestScore = 0.0;
        
        
        double HISTOGRAM_THRESHOLD = 0.85; 
        double LBP_THRESHOLD = 0.80;
        
        for (Map.Entry<String, List<Mat>> entry : Helper.personHistograms.entrySet()) {
            
            double histScore = getBestHistogramScore(faceHist, entry.getValue());
            
            
            if (histScore > HISTOGRAM_THRESHOLD * 0.8) { 
                double lbpScore = getLBPScore(lbpFeatures, entry.getKey());
                
                
                double combinedScore = (histScore * 0.6) + (lbpScore * 0.4);
                
                if (combinedScore > bestScore && 
                    histScore > HISTOGRAM_THRESHOLD && 
                    lbpScore > LBP_THRESHOLD) {
                    bestScore = combinedScore;
                    bestMatch = entry.getKey();
                }
            }
        }
        
        faceHist.release();
        lbpFeatures.release();
        
        return bestMatch;
    }
        
    public static boolean isFaceQualitySufficient(Mat face) {
        if (face == null || face.empty() || face.rows() < 50 || face.cols() < 50) {
            return false;
        }
        
        
        MatOfDouble meanMat = new MatOfDouble();
        MatOfDouble stdMat = new MatOfDouble();
        Core.meanStdDev(face, meanMat, stdMat);
        double[] mean = meanMat.get(0, 0);
        meanMat.release();
        stdMat.release();
        
        if (mean[0] < 30 || mean[0] > 220) { 
            return false;
        }
        
        
        Mat laplacian = new Mat();
        Imgproc.Laplacian(face, laplacian, face.depth());
        MatOfDouble mu = new MatOfDouble();
        MatOfDouble sigma = new MatOfDouble();
        Core.meanStdDev(laplacian, mu, sigma);
        double[] sigmaArr = sigma.get(0, 0);
        double variance = sigmaArr[0] * sigmaArr[0];
        
        laplacian.release();
        mu.release();
        sigma.release();
        
        
        return variance > 100; 
    }
        
    private static Mat computeLBPFeatures(Mat face) {
        Mat lbp = new Mat(face.rows() - 2, face.cols() - 2, face.type());
        
        for (int i = 1; i < face.rows() - 1; i++) {
            for (int j = 1; j < face.cols() - 1; j++) {
                double center = face.get(i, j)[0];
                int code = 0;
                
                code |= (face.get(i-1, j-1)[0] >= center) ? 128 : 0;
                code |= (face.get(i-1, j)[0] >= center) ? 64 : 0;
                code |= (face.get(i-1, j+1)[0] >= center) ? 32 : 0;
                code |= (face.get(i, j+1)[0] >= center) ? 16 : 0;
                code |= (face.get(i+1, j+1)[0] >= center) ? 8 : 0;
                code |= (face.get(i+1, j)[0] >= center) ? 4 : 0;
                code |= (face.get(i+1, j-1)[0] >= center) ? 2 : 0;
                code |= (face.get(i, j-1)[0] >= center) ? 1 : 0;
                
                lbp.put(i-1, j-1, code);
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
        
    public static void storeLBPFeatures(String email, List<Mat> faceImages) {
        List<Mat> lbpFeatures = new ArrayList<>();
        for (Mat face : faceImages) {
            lbpFeatures.add(computeLBPFeatures(face));
        }
        personLBPFeatures.put(email, lbpFeatures);
    }
        
    private static double getLBPScore(Mat lbpFeature, String personEmail) {
        if (!personLBPFeatures.containsKey(personEmail)) {
            return 0.0;
        }
        
        double bestScore = 0.0;
        for (Mat storedLBP : personLBPFeatures.get(personEmail)) {
            double score = Imgproc.compareHist(lbpFeature, storedLBP, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }
        
    private static double getBestHistogramScore(Mat faceHist, List<Mat> histograms) {
        double bestScore = 0;
        for (Mat hist : histograms) {
            double score = Imgproc.compareHist(faceHist, hist, Imgproc.HISTCMP_CORREL);
            bestScore = Math.max(bestScore, score);
        }
        return bestScore;
    }
        
    public static boolean isValidFaceForEnrollment(Mat frame, Mat gray, Rect faceRect) {
        
        if (faceRect.width < 80 || faceRect.height < 80) {
            return false;
        }
        
        
        double centerX = faceRect.x + faceRect.width / 2.0;
        double centerY = faceRect.y + faceRect.height / 2.0;
        double imgCenterX = gray.cols() / 2.0;
        double imgCenterY = gray.rows() / 2.0;
        
        double distanceFromCenter = Math.sqrt(
            Math.pow(centerX - imgCenterX, 2) + 
            Math.pow(centerY - imgCenterY, 2)
        );
        
        double maxDistance = Math.min(gray.cols(), gray.rows()) / 3.0;
        if (distanceFromCenter > maxDistance) {
            return false; 
        }
        
        
        double aspectRatio = (double) faceRect.width / faceRect.height;
        if (aspectRatio < 0.7 || aspectRatio > 1.4) {
            return false; 
        }
        
        
        Mat faceROI = gray.submat(faceRect);
        boolean qualityOK = isFaceQualitySufficient(faceROI);
        faceROI.release();
        
        return qualityOK;
    }
        
    public static void clearLBPFeatures() {
        for (List<Mat> features : personLBPFeatures.values()) {
            for (Mat feature : features) {
                feature.release();
            }
        }
        personLBPFeatures.clear();
    }

    public static Map<String, List<Mat>> getPersonLBPFeatures() {
        return personLBPFeatures;
    }

    public static void setPersonLBPFeatures(Map<String, List<Mat>> personLBPFeatures) {
        ImprovedRecognitionHelper.personLBPFeatures = personLBPFeatures;
    }
}