package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import dev.att.smartattendance.model.facedata.FaceDataDAO;
import dev.att.smartattendance.model.student.Student;
import dev.att.smartattendance.model.student.StudentDAO2;
import dev.att.smartattendance.model.professor.ProfessorDAO;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App2 extends Application {

    private VideoCapture capture;
    private volatile boolean cameraActive = false;
    private CascadeClassifier faceDetector;
    private Map<String, List<Mat>> studentFaceHistograms = new HashMap<>();
    private Mat currentFrame = new Mat();
    private String baseImagePath = "src/main/resources/images/students/";
    private String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";
    
    private String loggedInProfessorId = "";
    private String loggedInUsername = "";
    
    private StudentDAO2 studentDAO;
    private FaceDataDAO faceDataDAO;
    private ProfessorDAO professorDAO;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize DAOs
        studentDAO = new StudentDAO2();
        faceDataDAO = new FaceDataDAO();
        professorDAO = new ProfessorDAO();
        
        // Initialize face detector
        faceDetector = new CascadeClassifier(cascadePath);
        if (faceDetector.empty()) {
            showAlert("Error", "Could not load face detection model!");
            return;
        }
        
        // Load existing student faces from database
        loadExistingStudentFaces();
        
        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(createLoginScene(primaryStage));
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> {
            stopCamera();
            System.exit(0);
        });
    }

    private Scene createLoginScene(Stage stage) {
        Label titleLabel = new Label("Professor Login");
        titleLabel.getStyleClass().add("title");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("error");

        loginButton.setOnAction(e -> {
            String email = emailField.getText().strip();
            String password = passwordField.getText().strip();

            if (email.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both email and password");
                return;
            }

            // Check professor credentials in database
            var professor = professorDAO.get_professor_by_email(email);
            if (professor != null && professor.getPassword().equals(password)) {
                loggedInProfessorId = professor.getProfessor_id();
                loggedInUsername = professor.getUsername();
                stage.setScene(createMenuScene(stage));
            } else {
                messageLabel.setText("Invalid email or password");
            }
        });

        VBox layout = new VBox(25, titleLabel, emailField, passwordField, loginButton, messageLabel);
        layout.getStyleClass().add("vbox");

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        return scene;
    }

    private Scene createMenuScene(Stage stage) {
        Label welcomeLabel = new Label("Welcome, Professor " + loggedInUsername + "!");
        welcomeLabel.getStyleClass().add("menu-title");

        Label instructionLabel = new Label("Choose an option below:");
        instructionLabel.getStyleClass().add("menu-label");

        Button registerButton = new Button("Register New Student");
        registerButton.getStyleClass().add("menu-button");
        registerButton.setOnAction(e -> {
            stopCamera();
            stage.setScene(createStudentEnrollmentScene(stage));
        });

        Button attendanceButton = new Button("Take Attendance");
        attendanceButton.getStyleClass().add("menu-button");
        attendanceButton.setOnAction(e -> {
            stopCamera();
            stage.setScene(createAttendanceScene(stage));
        });

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("menu-button");
        logoutButton.setOnAction(e -> {
            stopCamera();
            loggedInProfessorId = "";
            loggedInUsername = "";
            stage.setScene(createLoginScene(stage));
        });

        VBox layout = new VBox(20, welcomeLabel, instructionLabel, registerButton, attendanceButton, logoutButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
        return scene;
    }

    private Scene createStudentEnrollmentScene(Stage stage) {
        Label titleLabel = new Label("Student Registration");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Enter student details and capture their face.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 14px;");

        TextField nameField = new TextField();
        nameField.setPromptText("Student Name");
        nameField.setPrefWidth(300);

        TextField emailField = new TextField();
        emailField.setPromptText("Student Email");
        emailField.setPrefWidth(300);

        VBox inputBox = new VBox(10,
                new Label("Name:"), nameField,
                new Label("Email:"), emailField);
        inputBox.setAlignment(Pos.CENTER);

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Enter student details and click 'Start Enrollment'");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #0066cc; -fx-font-weight: bold;");

        Button startBtn = new Button("Start Enrollment");
        startBtn.getStyleClass().add("btn-togglecam");

        Button backBtn = new Button("Back to Menu");
        backBtn.getStyleClass().add("btn-logout");

        backBtn.setOnAction(e -> {
            stopCamera();
            stage.setScene(createMenuScene(stage));
        });

        startBtn.setOnAction(e -> {
            String studentName = nameField.getText().strip();
            String studentEmail = emailField.getText().strip();

            if (studentName.isEmpty() || studentEmail.isEmpty()) {
                showAlert("Input Required", "Please enter both name and email.");
                return;
            }

            // Check if email already exists
            Student existing = studentDAO.get_student_by_email(studentEmail);
            if (existing != null) {
                showAlert("Email in use", "This email is already registered!");
                return;
            }

            if (!cameraActive) {
                statusLabel.setText("Initializing camera...");
                startEnrollmentProcess(studentName, studentEmail, webcamView, statusLabel, startBtn, backBtn, stage);
                startBtn.setDisable(true);
                nameField.setDisable(true);
                emailField.setDisable(true);
            }
        });

        HBox buttonBox = new HBox(15, startBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, titleLabel, infoLabel, inputBox, statusLabel, webcamView, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        return scene;
    }

    private void startEnrollmentProcess(String name, String email, ImageView webcamView, 
                                       Label statusLabel, Button enrollBtn, Button backBtn, Stage stage) {
        String studentId = UUID.randomUUID().toString();
        String folderPath = baseImagePath + studentId;
        
        File studentDir = new File(folderPath);
        studentDir.mkdirs();
        
        Platform.runLater(() -> statusLabel.setText("Look at the camera - capturing..."));
        
        AutoCapture.runAutoCapture(
            studentId,
            baseImagePath,
            faceDetector,
            webcamView,
            statusLabel,
            count -> {
                if (count >= 20) {
                    // Save student to database
                    studentDAO.insert_student(studentId, name, email);
                    
                    // Save face data reference to database
                    String faceId = UUID.randomUUID().toString();
                    faceDataDAO.insert_face(faceId, studentId, folderPath);
                    
                    // Load histograms for recognition
                    List<Mat> newImages = loadImages(folderPath);
                    studentFaceHistograms.put(studentId, computeHistograms(newImages));
                    
                    Platform.runLater(() -> {
                        showAlert("Enrollment Complete",
                                "Successfully registered " + name + " (" + email + ")\n" +
                                "Captured " + count + " face images.");
                        stage.setScene(createMenuScene(stage));
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Enrollment Incomplete", 
                                "Only captured " + count + " images. Please try again.");
                        // Clean up incomplete registration
                        File studentDir2 = new File(folderPath);
                        deleteDirectory(studentDir2);
                    });
                }
            }
        );
    }

    private Scene createAttendanceScene(Stage stage) {
        Label titleLabel = new Label("Attendance Monitoring");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label statusLabel = new Label("Camera Active - Scanning for students");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #0066cc;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Button backBtn = new Button("Back to Menu");
        backBtn.getStyleClass().add("btn-logout");

        backBtn.setOnAction(e -> {
            stopCamera();
            stage.setScene(createMenuScene(stage));
        });

        VBox layout = new VBox(20, titleLabel, statusLabel, webcamView, backBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        Platform.runLater(() -> startAttendanceMonitoring(webcamView, statusLabel));

        return scene;
    }

    private void startAttendanceMonitoring(ImageView imageView, Label statusLabel) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            statusLabel.setText("Camera unavailable");
            return;
        }
        cameraActive = true;

        // Load ONNX Mask Detection Model (optional)
        OrtEnvironment env = null;
        OrtSession session = null;
        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            session = env.createSession("src/main/resources/models/mask_detector.onnx", opts);
            System.out.println("✅ Mask detector model loaded.");
        } catch (OrtException e) {
            System.out.println("⚠️ Mask detector not available");
        }

        Scalar green = new Scalar(0, 255, 0);
        Scalar red = new Scalar(0, 0, 255);
        Scalar orange = new Scalar(0, 165, 255);

        OrtSession finalSession = session;
        OrtEnvironment finalEnv = env;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();

                while (cameraActive) {
                    if (capture.read(frame)) {
                        currentFrame = frame.clone();
                        Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, 
                                new Size(30, 30), new Size());

                        for (Rect rect : faces.toArray()) {
                            Mat face = gray.submat(rect);
                            Mat resizedFace = new Mat();
                            Imgproc.resize(face, resizedFace, new Size(200, 200));
                            
                            String recognizedStudentId = recognizeFace(resizedFace);
                            
                            Scalar color;
                            String labelText;
                            
                            if (recognizedStudentId.equals("Unknown")) {
                                color = orange;
                                labelText = "Unknown Person";
                            } else {
                                color = green;
                                Student student = studentDAO.get_student_by_id(recognizedStudentId);
                                labelText = student != null ? student.getName() : "ID: " + recognizedStudentId;
                            }

                            Imgproc.rectangle(currentFrame, rect.tl(), rect.br(), color, 2);
                            Imgproc.putText(currentFrame, labelText, new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);

                            resizedFace.release();
                            face.release();
                        }

                        Image imageToShow = mat2Image(currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
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

    private void loadExistingStudentFaces() {
        List<Student> allStudents = studentDAO.get_all_students();
        
        for (Student student : allStudents) {
            String studentId = student.getStudent_id();
            List<Mat> faceImages = faceDataDAO.get_faces_by_student_id(studentId);
            
            if (!faceImages.isEmpty()) {
                // Convert to grayscale and resize
                List<Mat> processedImages = new ArrayList<>();
                for (Mat img : faceImages) {
                    Mat gray = new Mat();
                    Mat resized = new Mat();
                    
                    if (img.channels() == 3) {
                        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
                    } else {
                        gray = img.clone();
                    }
                    
                    Imgproc.resize(gray, resized, new Size(200, 200));
                    processedImages.add(resized);
                    gray.release();
                }
                
                studentFaceHistograms.put(studentId, computeHistograms(processedImages));
                System.out.println("Loaded " + processedImages.size() + " face images for student: " + student.getName());
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

    private String recognizeFace(Mat face) {
        Mat faceHist = computeHistogram(face);
        
        String bestMatch = "Unknown";
        double bestScore = 0.7; // threshold for recognition
        
        for (Map.Entry<String, List<Mat>> entry : studentFaceHistograms.entrySet()) {
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

    private double getScreenWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() * 0.8;
    }

    private double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.8;
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}