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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javafx.scene.control.CheckBox;

import javafx.scene.control.ComboBox;

public class App extends Application {

    private VideoCapture capture;
    private volatile boolean cameraActive = false;
    private CascadeClassifier faceDetector;
    private Map<String, List<Mat>> personHistograms = new HashMap<>();
    private Map<String, String> userCredentials = new HashMap<>(); // username -> password
    private Mat currentFrame;
    private String baseImagePath = "src/main/resources/images/";
    private String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";

    private int captureCount = 0;
    private String capturePersonName = "";
    private boolean capturingMode = false;
    private String loggedInUsername = "";
    private boolean faceVerified = false;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("OpenCV library not found. Camera features will be disabled.");
        }
        // System.load("/usr/local/opencv/share/java/opencv4/libopencv_java480.dylib");
        // // For MAC
    }

    public static void main(String[] args) {
        launch(args);
        // CropDemo.main(args);
        // RecognitionDemo.main(args);

        // ProfessorDAO pdao = new ProfessorDAO();
        // for(String prof : pdao.get_all_professors()) {
        // System.out.println(prof);
        // }
        // Professor kyong = pdao.get_professor_by_email("kyong@smu.edu.sg");
        // System.out.println(kyong);

        // CourseDAO cdao = new CourseDAO();
        // for(Course course : cdao.get_all_courses()) {
        // System.out.println(course);
        // }

        // GroupDAO gdao = new GroupDAO();
        // for(Group group : gdao.get_all_groups()) {
        // System.out.println(group);
        // }

        // StudentDAO sdao = new StudentDAO();
        // for(Student student : sdao.get_all_students()) {
        // System.out.println(student);
        // }

    }

    @Override
    public void start(Stage primaryStage) {
        // initialize face detector
        faceDetector = new CascadeClassifier(cascadePath);
        if (faceDetector.empty()) {
            showAlert("Error", "Could not load face detection model!");
            return;
        }

        // initialize dummy credentials (In production, use a database)
        initializeCredentials();

        // load existing persons
        loadExistingPersons();

        Scene loginScene = createLoginScene(primaryStage);
        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(createLoginScene(primaryStage));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            stopCamera();
            System.exit(0);
        });
    }

    private void initializeCredentials() {
        // sample credentials - in production, load from database
        userCredentials.put("Naren", "123");
        userCredentials.put("Admin", "admin");

        // add more users as needed
        System.out.println("Initialized " + userCredentials.size() + " user accounts");
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
                    System.out.println("Loaded " + images.size() + " face images for " + personName);
                }
            }
        }
    }

    private List<Mat> loadImages(String dirPath) {
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
        return screenBounds.getWidth() * 0.8;
    }

    private double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.7;
    }

    private Scene createClassScene(String className, String username, Stage stage) {
        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header section (same as before)
        VBox headerSection = new VBox(10);
        headerSection.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        headerSection.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("SMART ATTENDANCE SYSTEM");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // User avatar section
        VBox avatarSection = new VBox(5);
        avatarSection.setAlignment(Pos.CENTER);

        Label avatarLabel = new Label(username.substring(0, 1).toUpperCase());
        avatarLabel.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 20px; " +
                "-fx-font-weight: bold; -fx-min-width: 50; -fx-min-height: 50; " +
                "-fx-max-width: 50; -fx-max-height: 50; -fx-background-radius: 25; " +
                "-fx-alignment: center;");

        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        avatarSection.getChildren().addAll(avatarLabel, usernameLabel);

        // Professor info
        VBox profSection = new VBox(2);
        profSection.setAlignment(Pos.CENTER_RIGHT);
        Label profLabel = new Label("Prof. ZHANG Zhiyuan");
        profLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        profSection.getChildren().add(profLabel);

        // Header layout
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER);
        javafx.scene.layout.Region leftSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.Region rightSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        headerTop.getChildren().addAll(avatarSection, leftSpacer, titleLabel, rightSpacer, profSection);
        headerSection.getChildren().add(headerTop);

        // Content section with navigation
        VBox contentSection = new VBox(20);
        contentSection.setStyle("-fx-padding: 40;");
        contentSection.setAlignment(Pos.CENTER_LEFT);

        // Navigation elements (semester, class buttons, etc.)
        HBox semesterSection = new HBox(10);
        semesterSection.setAlignment(Pos.CENTER_LEFT);

        Label semesterLabel = new Label("Current Semester:");
        semesterLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Label semesterValue = new Label("AY 25/26 Sem 1 ▼");
        semesterValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        semesterSection.getChildren().addAll(semesterLabel, semesterValue);

        // Class buttons with current class highlighted
        HBox classSection = new HBox(15);
        classSection.setAlignment(Pos.CENTER_LEFT);

        Label classLabel = new Label("Class you are belong to:");
        classLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        HBox classButtons = new HBox(10);
        classButtons.setAlignment(Pos.CENTER_LEFT);

        Button cs102Btn = new Button("CS102");
        Button is216Btn = new Button("IS216");
        Button cs440Btn = new Button("CS440");

        String buttonStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-padding: 8 16; -fx-font-size: 14px;";

        String activeButtonStyle = "-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-border-color: #2980b9; -fx-border-width: 1; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-padding: 8 16; -fx-font-size: 14px;";

        cs102Btn.setStyle(className.equals("CS102") ? activeButtonStyle : buttonStyle);
        is216Btn.setStyle(className.equals("IS216") ? activeButtonStyle : buttonStyle);
        cs440Btn.setStyle(className.equals("CS440") ? activeButtonStyle : buttonStyle);

        classButtons.getChildren().addAll(cs102Btn, is216Btn, cs440Btn);

        // Group and Week section - REPLACE THE ENTIRE SECTION
        HBox groupWeekSection = new HBox(20);
        groupWeekSection.setAlignment(Pos.CENTER_LEFT);

        Label groupLabel = new Label("Group: 3");
        groupLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // Create week dropdown directly here
        HBox weekBox = new HBox(5);
        weekBox.setAlignment(Pos.CENTER_LEFT);

        Label weekText = new Label("Week:");
        weekText.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        javafx.scene.control.ComboBox<String> weekDropdown = new javafx.scene.control.ComboBox<>();
        weekDropdown.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
                "16");
        weekDropdown.setValue("1"); // Default selection
        weekDropdown.setStyle(
                "-fx-font-size: 16px; -fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5;");
        weekDropdown.setPrefWidth(80);

        weekBox.getChildren().addAll(weekText, weekDropdown);

        // Add to groupWeekSection
        groupWeekSection.getChildren().addAll(groupLabel, weekBox);

        Button exportBtn = new Button("Export");
        exportBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        HBox exportRow = new HBox();
        exportRow.setAlignment(Pos.CENTER_RIGHT);
        exportRow.getChildren().add(exportBtn);

        // Add event handler for week dropdown
        weekDropdown.setOnAction(e -> {
            String selectedWeek = weekDropdown.getValue();
            System.out.println("Selected week: " + selectedWeek);
            showAlert("Week Changed", "Switched to Week " + selectedWeek + " for " + className);
        });

        contentSection.getChildren().addAll(semesterSection,
                new HBox(20, classLabel, classButtons),
                exportRow,
                groupWeekSection);

        // Main content area with student list and camera side by side
        HBox mainContentArea = new HBox(30);
        mainContentArea.setStyle("-fx-padding: 40;");
        mainContentArea.setAlignment(Pos.TOP_CENTER);

        // Student list section (left side)
        VBox studentSection = new VBox(15);
        studentSection.setPrefWidth(400);

        Label studentListTitle = new Label("student list");
        studentListTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox studentList = new VBox(5);
        studentList.setStyle(
                "-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

        String[] students = { "Anson", "Naren", "Flash", "Minuk", "Jiale", "Eugene", "Geri" };
        boolean[] attendance = { true, false, true, false, true, false, true };

        for (int i = 0; i < students.length; i++) {
            HBox studentRow = new HBox(10);
            studentRow.setAlignment(Pos.CENTER_LEFT);

            Label numberLabel = new Label((i + 1) + ".");
            numberLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-min-width: 20;");

            Label nameLabel = new Label(students[i]);
            nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-min-width: 100;");

            javafx.scene.control.CheckBox attendanceBox = new javafx.scene.control.CheckBox();
            attendanceBox.setSelected(attendance[i]);

            studentRow.getChildren().addAll(numberLabel, nameLabel, attendanceBox);
            studentList.getChildren().add(studentRow);
        }

        studentSection.getChildren().addAll(studentListTitle, studentList);

        // Camera section (right side)
        VBox cameraSection = new VBox(15);
        cameraSection.setAlignment(Pos.CENTER);
        cameraSection.setPrefWidth(500);

        Label cameraTitle = new Label("Live Face Recognition");
        cameraTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Camera view
        ImageView cameraView = new ImageView();
        cameraView.setFitWidth(450);
        cameraView.setFitHeight(350);
        cameraView.setPreserveRatio(true);
        cameraView.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2; -fx-background-color: #f8f9fa;");

        // Camera controls
        HBox cameraControls = new HBox(10);
        cameraControls.setAlignment(Pos.CENTER);

        Button startCameraBtn = new Button("Start Camera");
        startCameraBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        Button stopCameraBtn = new Button("Stop Camera");
        stopCameraBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        Button markAttendanceBtn = new Button("Mark Attendance");
        markAttendanceBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        cameraControls.getChildren().addAll(startCameraBtn, stopCameraBtn, markAttendanceBtn);

        // Recognition status
        Label recognitionStatus = new Label("Recognition Status: Ready");
        recognitionStatus.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        cameraSection.getChildren().addAll(cameraTitle, cameraView, cameraControls, recognitionStatus);

        // Add both sections to main content area
        mainContentArea.getChildren().addAll(studentSection, cameraSection);

        // Bottom section with navigation
        HBox bottomSection = new HBox(20);
        bottomSection.setAlignment(Pos.CENTER_LEFT);
        bottomSection.setStyle("-fx-padding: 20 40;");

        Button backBtn = new Button("← Back to Home");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-border-color: #3498db; -fx-border-width: 1; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-padding: 8 16; -fx-font-size: 14px;");

        Button registerStudentBtn = new Button("Register New Student");
        registerStudentBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        bottomSection.getChildren().addAll(backBtn, registerStudentBtn);

        // Event handlers
        cs102Btn.setOnAction(e -> stage.setScene(createClassScene("CS102", username, stage)));
        is216Btn.setOnAction(e -> stage.setScene(createClassScene("IS216", username, stage)));
        cs440Btn.setOnAction(e -> stage.setScene(createClassScene("CS440", username, stage)));

        backBtn.setOnAction(e -> {
            stopCamera();
            stage.setScene(createHomeScene(username));
        });

        // Camera event handlers
        startCameraBtn.setOnAction(e -> {
            startCameraInClassScene(cameraView, recognitionStatus, className);
            startCameraBtn.setDisable(true);
            stopCameraBtn.setDisable(false);
        });

        stopCameraBtn.setOnAction(e -> {
            stopCamera();
            startCameraBtn.setDisable(false);
            stopCameraBtn.setDisable(true);
            recognitionStatus.setText("Recognition Status: Camera Stopped");
        });

        markAttendanceBtn.setOnAction(e -> {
            showAlert("Attendance", "Attendance marked for recognized students!");
        });

        exportBtn.setOnAction(e -> showAlert("Export", "Exporting attendance data for " + className + "..."));
        registerStudentBtn
                .setOnAction(e -> showAlert("Register", "Opening student registration for " + className + "..."));

        // Initial camera button states
        stopCameraBtn.setDisable(true);

        // Main layout
        mainContainer.getChildren().addAll(headerSection, contentSection, mainContentArea, bottomSection);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(scrollPane, getScreenWidth(), getScreenHeight());

        // Maintain the same window size and position
        Platform.runLater(() -> {
            // Keep the stage at the same size it was
            if (!stage.isMaximized()) {
                stage.setWidth(getScreenWidth());
                stage.setHeight(getScreenHeight());
            }
        });

        return scene;
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

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password");
                return;
            }

            // check credentials
            if (userCredentials.containsKey(username) &&
                    userCredentials.get(username).equals(password)) {

                // check if user has face data enrolled
                // if (!personHistograms.containsKey(username)) {
                // messageLabel.setText("No face data found. Please enroll your face first.");
                // // redirect to enrollment
                // stage.setScene(createEnrollmentScene(stage, username));
                // } else {
                // // proceed to face verification
                // loggedInUsername = username;
                // faceVerified = true;
                // stage.setScene(createVerificationScene(stage, username));
                loggedInUsername = username;
                faceVerified = true;
                stage.setScene(createHomeScene(username));
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });

        VBox layout = new VBox(25, titleLabel, usernameField, passwordField, loginButton,
                messageLabel);
        layout.getStyleClass().add("vbox");

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        // scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        return scene;
    }

    private Scene createEnrollmentScene(Stage stage, String username) {
        Label titleLabel = new Label("Face Enrollment Required");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label(
                "Welcome, " + username + "! We need to capture your face for attendance verification.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 14px;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Click 'Start Enrollment' to begin");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #0066cc; -fx-font-weight: bold;");

        Button startEnrollBtn = new Button("Start Enrollment");
        startEnrollBtn.getStyleClass().add("btn-togglecam");

        Button backBtn = new Button("Back to Login");
        backBtn.getStyleClass().add("btn-logout");

        startEnrollBtn.setOnAction(e -> {
            if (!cameraActive) {
                startEnrollmentProcess(username, webcamView, statusLabel, startEnrollBtn, backBtn, stage);
                startEnrollBtn.setDisable(true);
            }
        });

        backBtn.setOnAction(e -> {
            stopCamera();
            stage.setScene(createLoginScene(stage));
        });

        HBox buttonBox = new HBox(15, startEnrollBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, titleLabel, infoLabel, statusLabel, webcamView, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        return scene;
    }

    private void startEnrollmentProcess(String username, ImageView webcamView, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
        capturingMode = true;
        capturePersonName = username;
        captureCount = 0;

        File personDir = new File(baseImagePath + username);
        personDir.mkdirs();

        Platform.runLater(() -> statusLabel.setText("Look at the camera - Capturing: 0/8"));

        startCameraForEnrollment(webcamView, statusLabel, enrollBtn, backBtn, stage);
    }

    private Scene createVerificationScene(Stage stage, String username) {
        Label titleLabel = new Label("Face Verification");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Please look at the camera to verify your identity");
        infoLabel.setStyle("-fx-font-size: 14px;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Waiting for face detection...");
        statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #0066cc; -fx-font-weight: bold;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-logout");

        cancelBtn.setOnAction(e -> {
            stopCamera();
            stage.setScene(createLoginScene(stage));
        });

        VBox layout = new VBox(20, titleLabel, infoLabel, statusLabel, webcamView, cancelBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        // auto-start camera for verification
        Platform.runLater(() -> startCameraForVerification(webcamView, statusLabel, stage, username));

        return scene;
    }

    private void startCameraForEnrollment(ImageView imageView, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
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

                while (cameraActive && capturingMode) {
                    if (capture.read(frame)) {
                        currentFrame = frame.clone();
                        Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                                new Size(30, 30), new Size());

                        Rect[] faceArray = faces.toArray();

                        for (Rect rect : faceArray) {
                            Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(255, 165, 0), 3);

                            if (frameCounter % 15 == 0 && captureCount < 8) {
                                saveFaceForEnrollment(gray, rect, statusLabel, enrollBtn, backBtn, stage);
                            }

                            String text = "Capturing: " + captureCount + "/8";
                            Imgproc.putText(currentFrame, text,
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.9,
                                    new Scalar(255, 165, 0), 2);
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

    private void saveFaceForEnrollment(Mat gray, Rect rect, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
        Mat face = gray.submat(rect);
        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, new Size(200, 200));

        String fileName = baseImagePath + capturePersonName + "/face_" +
                System.currentTimeMillis() + ".jpg";
        Imgcodecs.imwrite(fileName, resizedFace);

        captureCount++;
        Platform.runLater(() -> statusLabel.setText("Capturing: " + captureCount + "/8"));

        if (captureCount >= 8) {
            capturingMode = false;
            captureCount = 0;

            List<Mat> newImages = loadImages(baseImagePath + capturePersonName);
            personHistograms.put(capturePersonName, computeHistograms(newImages));

            stopCamera();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Enrollment Complete");
                alert.setHeaderText("Success!");
                alert.setContentText("Face enrollment completed for " + capturePersonName +
                        ". You can now log in with face verification.");
                alert.showAndWait();

                stage.setScene(createLoginScene(stage));
            });
        }

        resizedFace.release();
        face.release();
    }

    private void startCameraForVerification(ImageView imageView, Label statusLabel,
            Stage stage, String expectedUsername) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            showAlert("Camera Error", "Cannot open camera!");
            Platform.runLater(() -> stage.setScene(createLoginScene(stage)));
            return;
        }
        cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();
                int verificationAttempts = 0;
                int maxAttempts = 50; // ~1.5 seconds of attempts

                while (cameraActive && !faceVerified && verificationAttempts < maxAttempts) {
                    if (capture.read(frame)) {
                        currentFrame = frame.clone();
                        Imgproc.cvtColor(currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                                new Size(30, 30), new Size());

                        Rect[] faceArray = faces.toArray();

                        if (faceArray.length > 0) {
                            Rect rect = faceArray[0];
                            Mat face = gray.submat(rect);
                            Mat resizedFace = new Mat();
                            Imgproc.resize(face, resizedFace, new Size(200, 200));

                            String recognizedName = recognizeFace(resizedFace);

                            if (recognizedName.equals(expectedUsername)) {
                                // Face matches
                                Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 255, 0), 3);

                                Imgproc.putText(currentFrame, "Verified: " + recognizedName,
                                        new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9,
                                        new Scalar(0, 255, 0), 2);

                                faceVerified = true;

                                Platform.runLater(() -> {
                                    statusLabel.setText("✓ Face Verified! Access Granted");
                                    statusLabel.setStyle(
                                            "-fx-font-size: 16px; -fx-text-fill: #00cc00; -fx-font-weight: bold;");
                                });

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                }

                                stopCamera();
                                Platform.runLater(() -> stage.setScene(createHomeScene(expectedUsername)));

                            } else {
                                // Face doesn't match
                                Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 0, 255), 3);

                                String errorText = recognizedName.equals("Unknown") ? "Face Not Recognized"
                                        : "Wrong Person: " + recognizedName;

                                Imgproc.putText(currentFrame, errorText,
                                        new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9,
                                        new Scalar(0, 0, 255), 2);

                                Platform.runLater(() -> statusLabel.setText("⚠ Face verification failed - try again"));
                            }

                            resizedFace.release();
                            face.release();
                            verificationAttempts++;
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

                // If verification failed after max attempts
                if (!faceVerified && verificationAttempts >= maxAttempts) {
                    stopCamera();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Verification Failed");
                        alert.setHeaderText("Face Verification Failed");
                        alert.setContentText("Your face does not match the enrolled data for " +
                                expectedUsername + ". Access denied.");
                        alert.showAndWait();
                        stage.setScene(createLoginScene(stage));
                    });
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

    private Scene createHomeScene(String username) {
        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Header section
        VBox headerSection = new VBox(10);
        headerSection.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        headerSection.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("SMART ATTENDANCE SYSTEM");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // User avatar section (circular background with initial)
        VBox avatarSection = new VBox(5);
        avatarSection.setAlignment(Pos.CENTER);

        // Create circular avatar with user initial
        Label avatarLabel = new Label(username.substring(0, 1).toUpperCase());
        avatarLabel.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-size: 20px; " +
                "-fx-font-weight: bold; -fx-min-width: 50; -fx-min-height: 50; " +
                "-fx-max-width: 50; -fx-max-height: 50; -fx-background-radius: 25; " +
                "-fx-alignment: center;");

        Label usernameLabel = new Label(username);
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        avatarSection.getChildren().addAll(avatarLabel, usernameLabel);

        // Professor info section (top right)
        VBox profSection = new VBox(2);
        profSection.setAlignment(Pos.CENTER_RIGHT);
        Label profLabel = new Label("Prof. ZHANG Zhiyuan");
        profLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        profSection.getChildren().add(profLabel);

        // Header layout with avatar and prof info
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER);
        javafx.scene.layout.Region leftSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.Region rightSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        headerTop.getChildren().addAll(avatarSection, leftSpacer, titleLabel, rightSpacer, profSection);

        headerSection.getChildren().add(headerTop);

        // Content section
        VBox contentSection = new VBox(20);
        contentSection.setStyle("-fx-padding: 40;");
        contentSection.setAlignment(Pos.CENTER_LEFT);

        // Current Semester section
        HBox semesterSection = new HBox(10);
        semesterSection.setAlignment(Pos.CENTER_LEFT);

        Label semesterLabel = new Label("Current Semester:");
        semesterLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Label semesterValue = new Label("AY 25/26 Sem 1 ▼");
        semesterValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        semesterSection.getChildren().addAll(semesterLabel, semesterValue);

        // Classes section
        HBox classSection = new HBox(15);
        classSection.setAlignment(Pos.CENTER_LEFT);

        Label classLabel = new Label("Class you are belong to:");
        classLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // Class buttons
        HBox classButtons = new HBox(10);
        classButtons.setAlignment(Pos.CENTER_LEFT);

        Button cs102Btn = new Button("CS102");
        Button is216Btn = new Button("IS216");
        Button cs440Btn = new Button("CS440");

        String buttonStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                "-fx-border-color: #bdc3c7; -fx-border-width: 1; -fx-border-radius: 5; " +
                "-fx-background-radius: 5; -fx-padding: 8 16; -fx-font-size: 14px;";

        cs102Btn.setStyle(buttonStyle);
        is216Btn.setStyle(buttonStyle);
        cs440Btn.setStyle(buttonStyle);

        classButtons.getChildren().addAll(cs102Btn, is216Btn, cs440Btn);

        // New Class button
        Button newClassBtn = new Button("New Class");
        newClassBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 8 16; -fx-font-size: 14px;");

        HBox classRow = new HBox(20);
        classRow.setAlignment(Pos.CENTER_LEFT);
        classRow.getChildren().addAll(classLabel, classButtons);

        // New Class button positioned to the right
        HBox newClassRow = new HBox();
        newClassRow.setAlignment(Pos.CENTER_RIGHT);
        newClassRow.getChildren().add(newClassBtn);

        contentSection.getChildren().addAll(semesterSection, classRow, newClassRow);

        // Bottom section with camera and logout
        VBox bottomSection = new VBox(20);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setStyle("-fx-padding: 20;");

        // Camera view (smaller and centered)
        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(400);
        webcamView.setFitHeight(300);
        webcamView.setPreserveRatio(true);
        webcamView.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2;");

        Label cameraLabel = new Label("Live Camera Feed");
        cameraLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-padding: 10 20; -fx-font-size: 14px;");

        logoutButton.setOnAction(e -> {
            stopCamera();
            loggedInUsername = "";
            faceVerified = false;
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(createLoginScene(stage));
        });

        bottomSection.getChildren().addAll(cameraLabel, webcamView, logoutButton);

        // Add event handlers for class buttons
        cs102Btn.setOnAction(e -> {
            Stage stage = (Stage) cs102Btn.getScene().getWindow();
            stage.setScene(createClassScene("CS102", username, stage));
        });
        is216Btn.setOnAction(e -> {
            Stage stage = (Stage) is216Btn.getScene().getWindow();
            stage.setScene(createClassScene("IS216", username, stage));
        });
        cs440Btn.setOnAction(e -> {
            Stage stage = (Stage) cs440Btn.getScene().getWindow();
            stage.setScene(createClassScene("CS440", username, stage));
        });
        newClassBtn.setOnAction(e -> showAlert("New Class", "Creating new class..."));

        // Main layout
        mainContainer.getChildren().addAll(headerSection, contentSection, bottomSection);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(scrollPane, getScreenWidth(), getScreenHeight());

        // Optional: Add custom CSS if you have it
        // try {
        // scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
        // } catch (Exception ex) {
        // // CSS file not found, continue without it
        // }

        // Auto-start camera in home scene (optional - you can comment this out too)
        // Platform.runLater(() -> startCameraInHomeScene(webcamView, cameraLabel,
        // username));

        return scene;
    }

    private void startCameraInHomeScene(ImageView imageView, Label statusLabel, String username) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            statusLabel.setText("Camera unavailable");
            return;
        }
        cameraActive = true;

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

                        Rect[] faceArray = faces.toArray();

                        for (Rect rect : faceArray) {
                            Mat face = gray.submat(rect);
                            Mat resizedFace = new Mat();
                            Imgproc.resize(face, resizedFace, new Size(200, 200));

                            String recognizedName = recognizeFace(resizedFace);

                            Scalar color = recognizedName.equals(username) ? new Scalar(0, 255, 0)
                                    : new Scalar(255, 165, 0);

                            Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),
                                    color, 3);

                            String displayText = "Welcome, " + recognizedName + "!";
                            Imgproc.putText(currentFrame, displayText,
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, color, 2);

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

    // ...existing camera methods like startCameraInHomeScene, stopCamera, etc...

    // ADD THE NEW METHOD HERE:
    private void startCameraInClassScene(ImageView imageView, Label statusLabel, String className) {
        try {
            if (currentFrame == null) {
                currentFrame = new Mat();
            }

            capture = new VideoCapture(0);

            if (!capture.isOpened()) {
                statusLabel.setText("Recognition Status: Camera not available");
                return;
            }

            cameraActive = true;
            statusLabel.setText("Recognition Status: Camera Active - Looking for faces...");

            Task<Void> frameGrabber = new Task<>() {
                @Override
                protected Void call() {
                    Mat frame = new Mat();

                    while (cameraActive && !isCancelled()) {
                        capture.read(frame);

                        if (!frame.empty()) {
                            // Detect faces
                            String recognizedPerson = "Unknown";
                            if (faceDetector != null) {
                                recognizedPerson = recognizeFace(frame);
                            }

                            // Update status on UI thread
                            final String finalRecognized = recognizedPerson;
                            Platform.runLater(() -> {
                                if (!finalRecognized.equals("Unknown")) {
                                    statusLabel.setText("Recognition Status: Recognized - " + finalRecognized);
                                    statusLabel.setStyle(
                                            "-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
                                } else {
                                    statusLabel.setText("Recognition Status: Camera Active - Looking for faces...");
                                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                                }

                                // Convert and display frame
                                Image imageToShow = mat2Image(frame);
                                imageView.setImage(imageToShow);
                            });
                        }

                        try {
                            Thread.sleep(33); // ~30 FPS
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    return null;
                }
            };

            Thread frameThread = new Thread(frameGrabber);
            frameThread.setDaemon(true);
            frameThread.start();

        } catch (Exception e) {
            statusLabel.setText("Recognition Status: Error starting camera");
            System.err.println("Error starting camera: " + e.getMessage());
        }
    }

    // ...rest of your existing methods...

    private String recognizeFace(Mat face) {
        Mat faceHist = computeHistogram(face);

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