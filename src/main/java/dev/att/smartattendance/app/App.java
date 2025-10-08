package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
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
import javafx.scene.control.TextInputDialog;
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
    private Mat currentFrame = new Mat();
    private String baseImagePath = "src/main/resources/images/";
    private String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";
    
    private int captureCount = 0;
    private String capturePersonName = "";
    private boolean capturingMode = false;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        // initialize face detector
        faceDetector = new CascadeClassifier(cascadePath);
        if (faceDetector.empty()) {
            showAlert("Error", "Could not load face detection model!");
            return;
        }
        
        // load existing persons
        loadExistingPersons();
        
        Scene loginScene = createLoginScene(primaryStage);
        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(loginScene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> {
            stopCamera();
            System.exit(0);
        });
    }

    private void loadExistingPersons() {
        File baseDir = new File(baseImagePath);
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
                    personHistograms.put(personName, computeHistograms(images));
                    System.out.println("Loaded " + images.size() + " images for " + personName);
                }
            }
        }
    }

    private List<Mat> loadImages(String dirPath) {
        List<Mat> images = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> 
            name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        
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

    private List<Mat> computeHistograms(List<Mat> images) {
        List<Mat> histograms = new ArrayList<>();
        for (Mat img : images) {
            histograms.add(computeHistogram(img));
        }
        return histograms;
    }

    private Mat computeHistogram(Mat image) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt channels = new MatOfInt(0);
        Imgproc.calcHist(List.of(image), channels, new Mat(), hist, histSize, ranges);
        Core.normalize(hist, hist, 0, 1, Core.NORM_MINMAX);
        return hist;
    }

    private double getScreenWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() * 0.6;
    }

    private double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.7;
    }

    private Scene createLoginScene(Stage stage) {
        Label titleLabel = new Label("Smart Attendance Login");
        titleLabel.getStyleClass().add("title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("error");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().strip();
            String password = passwordField.getText().strip();

            if (!username.isEmpty() && !password.isEmpty()) {
                stage.setScene(createHomeScene(username));
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });

        VBox layout = new VBox(25, titleLabel, usernameField, passwordField, loginButton, messageLabel);
        layout.getStyleClass().add("vbox");

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        return scene;
    }

    private Scene createHomeScene(String username) {
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.getStyleClass().add("welcome-label");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Camera Off");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button toggleCamBtn = new Button("Turn Camera On");
        toggleCamBtn.getStyleClass().add("btn-togglecam");

        Button enrollBtn = new Button("Enroll New Face");
        enrollBtn.getStyleClass().add("btn-enroll");
        enrollBtn.setDisable(true);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-logout");

        // toggle camera (idk why a bit buggy pls figure it out later)
        toggleCamBtn.setOnAction(e -> {
            if (!cameraActive) {
                startCamera(webcamView, statusLabel);
                toggleCamBtn.setText("Turn Camera Off");
                enrollBtn.setDisable(false);
                statusLabel.setText("Camera Active - Recognizing Faces");
            } else {
                stopCamera();
                toggleCamBtn.setText("Turn Camera On");
                enrollBtn.setDisable(true);
                webcamView.setImage(null);
                statusLabel.setText("Camera Off");
            }
        });

        // enroll new face
        enrollBtn.setOnAction(e -> {
            if (cameraActive) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Enroll New Person");
                dialog.setHeaderText("Enter the person's name:");
                dialog.setContentText("Name:");
                
                dialog.showAndWait().ifPresent(name -> {
                    if (!name.trim().isEmpty()) {
                        startEnrollment(name.trim(), statusLabel);
                    }
                });
            }
        });

        // logout
        logoutButton.setOnAction(e -> {
            stopCamera();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(createLoginScene(stage));
        });

        HBox buttonBox = new HBox(15, toggleCamBtn, enrollBtn, logoutButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, welcomeLabel, statusLabel, webcamView, buttonBox);
        layout.getStyleClass().add("home-layout");
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        return scene;
    }

    private void startEnrollment(String personName, Label statusLabel) {
        capturingMode = true;
        capturePersonName = personName;
        captureCount = 0;
        
        File personDir = new File(baseImagePath + personName);
        personDir.mkdirs();
        
        Platform.runLater(() -> 
            statusLabel.setText("Enrolling " + personName + " - Look at camera (0/8 captured)"));
    }

    private void startCamera(ImageView imageView, Label statusLabel) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            showAlert("Camera Error", "Cannot open camera!");
            return;
        }
        cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();
                int frameCounter = 0;
                
                while (cameraActive) {
                    if (capture.read(frame)) {
                        currentFrame = frame.clone();
                        Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_BGR2GRAY);
                        
                        // detect faces
                        MatOfRect faces = new MatOfRect();
                        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, 
                            new Size(30, 30), new Size());
                        
                        Rect[] faceArray = faces.toArray();
                        
                        // Process faces
                        for (Rect rect : faceArray) {
                            if (capturingMode) {
                                //enrollment mode - capture faces
                                Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(255, 165, 0), 3);
                                
                                // capture every 10f
                                if (frameCounter % 10 == 0 && captureCount < 8) {
                                    saveFace(gray, rect);
                                }
                                
                                String text = "Capturing: " + captureCount + "/8";
                                Imgproc.putText(currentFrame, text, 
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, 
                                    new Scalar(255, 165, 0), 2);
                            } else {
                                // recognition mode
                                Mat face = gray.submat(rect);
                                Mat resizedFace = new Mat();
                                Imgproc.resize(face, resizedFace, new Size(200, 200));
                                
                                String recognizedName = recognizeFace(resizedFace);
                                
                                Scalar color = recognizedName.equals("Unknown") ? 
                                    new Scalar(0, 0, 255) : new Scalar(0, 255, 0);
                                
                                Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),
                                    color, 3);
                                
                                Imgproc.putText(currentFrame, recognizedName, 
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, color, 2);
                                
                                resizedFace.release();
                                face.release();
                            }
                        }
                        
                        Image imageToShow = mat2Image(currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                        
                        frameCounter++;
                    }
                    
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                
                frame.release();
                gray.release();
                return null;
            }
        };

        Thread th = new Thread(frameGrabber);
        th.setDaemon(true);
        th.start();
    }

    private void saveFace(Mat gray, Rect rect) {
        Mat face = gray.submat(rect);
        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, new Size(200, 200));
        
        String fileName = baseImagePath + capturePersonName + "/face_" + 
            System.currentTimeMillis() + ".jpg";
        Imgcodecs.imwrite(fileName, resizedFace);
        
        captureCount++;
        System.out.println("Saved face " + captureCount + "/8");
        
        if (captureCount >= 8) {
            capturingMode = false;
            captureCount = 0;
            
            // reload the persons data
            List<Mat> newImages = loadImages(baseImagePath + capturePersonName);
            personHistograms.put(capturePersonName, computeHistograms(newImages));
            
            Platform.runLater(() -> {
                showAlert("Success", "Successfully enrolled " + capturePersonName + "!");
            });
        }
        
        resizedFace.release();
        face.release();
    }

    private String recognizeFace(Mat face) {
        Mat faceHist = computeHistogram(face);
        
        String bestMatch = "Unknown";
        double bestScore = 0.7; // threshold
        
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
        captureCount = 0;
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