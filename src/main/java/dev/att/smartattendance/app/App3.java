package dev.att.smartattendance.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Core;
import org.opencv.core.CvType;
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

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import dev.att.smartattendance.model.student.Student;
import dev.att.smartattendance.model.student.StudentDAO;
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

public class App3 extends Application {

    private VideoCapture capture;
    private volatile boolean cameraActive = false;
    private CascadeClassifier faceDetector;
    private Map<String, List<Mat>> personHistograms = new HashMap<>();
    private Map<String, String> userCredentials = new HashMap<>(); // username -> password
    private Mat currentFrame = new Mat();
    private String baseImagePath = "src/main/resources/images/";
    private String cascadePath = "src/main/resources/fxml/haarcascade_frontalface_alt.xml";
    
    private int captureCount = 0;
    private String capturePersonName = "";
    private boolean capturingMode = false;
    private String loggedInUsername = "";
    private boolean faceVerified = false;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // System.load("/usr/local/opencv/share/java/opencv4/libopencv_java480.dylib"); // For MAC
    }

    public static void main(String[] args) {
        launch();
        // RecognitionDemo.main(args);

        // ProfessorDAO pdao = new ProfessorDAO();
        // for(String prof : pdao.get_all_professors()) {
        //     System.out.println(prof);
        // }
        // Professor kyong = pdao.get_professor_by_email("kyong@smu.edu.sg");
        // System.out.println(kyong);

        // CourseDAO cdao = new CourseDAO();
        // for(Course course : cdao.get_all_courses()) {
        //     System.out.println(course);
        // }
        
        // GroupDAO gdao = new GroupDAO();
        // for(Group group : gdao.get_all_groups()) {
        //     System.out.println(group);
        // }

        // StudentDAO sdao = new StudentDAO();
        // for(Student student : sdao.get_all_students()) {
        //     System.out.println(student);
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
        
        // Scene loginScene = createLoginScene(primaryStage);
        primaryStage.setTitle("Smart Attendance System");
        primaryStage.setScene(createLoginScene(primaryStage));
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> {
            stopCamera();
            System.exit(0);
        });
    }

    private Scene createMenuScene(Stage stage) {
        Label welcomeLabel = new Label("Welcome, " + loggedInUsername + "!");
        welcomeLabel.getStyleClass().add("menu-title");

        Label instructionLabel = new Label("Choose an option below:");
        instructionLabel.getStyleClass().add("menu-label");

        Button registerButton = new Button("Register New Student");
        registerButton.getStyleClass().add("menu-button");
        registerButton.setOnAction(e -> {
            stopCamera();
            stage.setScene(createDirectEnrollmentScene(stage));
        });

        Button homeButton = new Button("Go to Home Page");
        homeButton.getStyleClass().add("menu-button");
        homeButton.setOnAction(e -> {
            stopCamera();
            stage.setScene(createHomeScene(loggedInUsername));
        });

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("menu-button");
        logoutButton.setOnAction(e -> {
            stopCamera();
            loggedInUsername = "";
            stage.setScene(createLoginScene(stage));
        });

        VBox layout = new VBox(20, welcomeLabel, instructionLabel, registerButton, homeButton, logoutButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
        return scene;
    }


    //Uses startEnrollmentProcess and startCameraForEnrollment (new)
    private Scene createDirectEnrollmentScene(Stage stage) {
        Label titleLabel = new Label("Face Enrollment - Professor Mode");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Enter student details below and click 'Start Enrollment' to begin auto face capture.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 14px;");

        TextField emailField = new TextField();
        emailField.setPromptText("Student Email");
        emailField.setPrefWidth(300);

        HBox inputBox = new HBox(10,
                new Label("Email:"), emailField);
        inputBox.setAlignment(Pos.CENTER);

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Label statusLabel = new Label("Waiting for input...");
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
            String studentEmail = emailField.getText().strip();
            StudentDAO sdao = new StudentDAO();
            if (studentEmail.isEmpty()) {
                showAlert("Input Required", "Please enter the student's email before starting enrollment.");
                return;
            }

            Student existing = sdao.get_student_by_email(studentEmail);
            if(existing != null) {
                showAlert("Email in use", "This email is already enrolled!");
            } else {
                if (!cameraActive) {
                    statusLabel.setText("Initializing camera...");
                    startEnrollmentProcess(studentEmail, webcamView, statusLabel, startBtn, backBtn, stage);
                    startBtn.setDisable(true);
                }
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

    private void startEnrollmentProcess(String username, ImageView webcamView, Label statusLabel, Button enrollBtn, Button backBtn, Stage stage) {
        capturingMode = true;
        capturePersonName = username;
        captureCount = 0;
        
        File personDir = new File(baseImagePath + username);
        personDir.mkdirs();
        
        Platform.runLater(() -> 
            statusLabel.setText("Look at the camera"));
        
        startCameraForEnrollment(webcamView, statusLabel, enrollBtn, backBtn, stage);
    }

    private void startCameraForEnrollment(ImageView imageView, Label statusLabel, Button enrollBtn, Button backBtn, Stage stage) {
        AutoCapture.runAutoCapture(
            capturePersonName,
            baseImagePath,
            faceDetector,
            imageView,
            statusLabel,
            count -> {
                // called when done
                if (count >= 20) {
                    List<Mat> newImages = loadImages(baseImagePath + capturePersonName);
                    personHistograms.put(capturePersonName, computeHistograms(newImages));
                    Platform.runLater(() -> {
                        showAlert("Enrollment Complete",
                                "Successfully captured " + count + " images for " + capturePersonName);
                        stage.setScene(createMenuScene(stage));
                    });
                } else {
                    Platform.runLater(() ->
                        showAlert("Enrollment Incomplete", "Only captured " + count + " images."));
                }
            }
        );
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
                
                loggedInUsername = username;
                stage.setScene(createMenuScene(stage));
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
        Label welcomeLabel = new Label("✓ Access Granted - Welcome, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #00cc00;");

        Label statusLabel = new Label("Camera Active - Monitoring");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #0066cc;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);

        Button backBtn = new Button("Back to Menu");
        backBtn.getStyleClass().add("btn-logout");

        backBtn.setOnAction(e -> {
            stopCamera();
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(createMenuScene(stage));
        });

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-logout");

        logoutButton.setOnAction(e -> {
            stopCamera();
            loggedInUsername = "";
            faceVerified = false;
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(createLoginScene(stage));
        });

        VBox layout = new VBox(20, welcomeLabel, statusLabel, webcamView, backBtn, logoutButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        // auto-start camera in home scene
        Platform.runLater(() -> startCameraInHomeScene(webcamView, statusLabel, username));

        return scene;
    }

    private void startCameraInHomeScene(ImageView imageView, Label statusLabel, String username) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            statusLabel.setText("Camera unavailable");
            return;
        }
        cameraActive = true;

        // ====== Load ONNX Mask Detection Model ======
        OrtEnvironment env;
        OrtSession session;
        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            session = env.createSession("src/main/resources/models/mask_detector.onnx", opts);
            System.out.println("✅ Mask detector model loaded.");
        } catch (OrtException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to load mask detection model");
            return;
        }

        Scalar green = new Scalar(0, 255, 0);
        Scalar red = new Scalar(0, 0, 255);
        Scalar orange = new Scalar(0, 165, 255);

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
                        faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size());

                        for (Rect rect : faces.toArray()) {
                            // Extract region of interest for mask detection
                            Mat faceROI = new Mat(frame, rect);
                            Mat resized = new Mat();
                            Imgproc.cvtColor(faceROI, resized, Imgproc.COLOR_BGR2RGB);
                            Imgproc.resize(resized, resized, new Size(224, 224));
                            resized.convertTo(resized, CvType.CV_32FC3, 1.0 / 255.0);

                            float[] nhwc = new float[224 * 224 * 3];
                            resized.get(0, 0, nhwc);
                            boolean maskDetected = false;

                            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(nhwc), new long[]{1, 224, 224, 3});
                                OrtSession.Result result = session.run(Map.of(session.getInputNames().iterator().next(), inputTensor))) {
                                float[][] probs = (float[][]) result.get(0).getValue();
                                maskDetected = probs[0][0] > probs[0][1];
                            } catch (OrtException e) {
                                e.printStackTrace();
                            }

                            if (maskDetected) {
                                Imgproc.rectangle(currentFrame, rect.tl(), rect.br(), red, 2);
                                Imgproc.putText(currentFrame, "Mask Detected", new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, red, 2);
                            } else {
                                // perform recognition only if no mask
                                Mat face = gray.submat(rect);
                                Mat resizedFace = new Mat();
                                Imgproc.resize(face, resizedFace, new Size(200, 200));
                                String recognizedName = recognizeFace(resizedFace);

                                Scalar color = recognizedName.equals("Unknown") ? orange : green;
                                String labelText = recognizedName.equals("Unknown")
                                        ? "Unknown"
                                        : "Recognized: " + recognizedName;

                                Imgproc.rectangle(currentFrame, rect.tl(), rect.br(), color, 2);
                                Imgproc.putText(currentFrame, labelText, new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);

                                resizedFace.release();
                                face.release();
                            }
                            resized.release();
                            faceROI.release();
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
                // session.close();
                return null;
            }
        };

        Thread th = new Thread(frameGrabber);
        th.setDaemon(true);
        th.start();
    }

    //GAP






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
        return screenBounds.getWidth() * 0.8;
    }

    private double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.8;
    }

    private Scene createLoginSceneOriginal(Stage stage) {
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
                if (!personHistograms.containsKey(username)) {
                    messageLabel.setText("No face data found. Please enroll your face first.");
                    // redirect to enrollment
                    stage.setScene(createEnrollmentScene(stage, username));
                } else {
                    // proceed to face verification
                    loggedInUsername = username;
                    faceVerified = false;
                    stage.setScene(createVerificationScene(stage, username));
                }
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

    private Scene createEnrollmentScene(Stage stage, String username) {
        Label titleLabel = new Label("Face Enrollment Required");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Welcome, " + username + "! We need to capture your face for attendance verification.");
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

    private void startCameraForEnrollmentOriginal(ImageView imageView, Label statusLabel, Button enrollBtn, Button backBtn, Stage stage) {
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
        Platform.runLater(() -> 
            statusLabel.setText("Capturing: " + captureCount + "/8"));
        
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
                                    statusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #00cc00; -fx-font-weight: bold;");
                                });
                                
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {}
                                
                                stopCamera();
                                Platform.runLater(() -> stage.setScene(createHomeScene(expectedUsername)));
                                
                            } else {
                                // Face doesn't match
                                Imgproc.rectangle(currentFrame, new Point(rect.x, rect.y),
                                    new Point(rect.x + rect.width, rect.y + rect.height),
                                    new Scalar(0, 0, 255), 3);
                                
                                String errorText = recognizedName.equals("Unknown") ? 
                                    "Face Not Recognized" : "Wrong Person: " + recognizedName;
                                
                                Imgproc.putText(currentFrame, errorText, 
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, 
                                    new Scalar(0, 0, 255), 2);
                                
                                Platform.runLater(() -> 
                                    statusLabel.setText("⚠ Face verification failed - try again"));
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

    private void startCameraInHomeSceneOriginal(ImageView imageView, Label statusLabel, String username) {
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
                            
                            Scalar color = recognizedName.equals(username) ? 
                                new Scalar(0, 255, 0) : new Scalar(255, 165, 0);
                            
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}