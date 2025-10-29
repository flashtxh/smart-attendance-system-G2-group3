package dev.att.smartattendance.app.pages;

import java.nio.FloatBuffer;
import java.util.Map;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import dev.att.smartattendance.app.Helper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Class {

    private static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static Scene createClassScene(String className, String username, Stage stage) {
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
            Helper.stopCamera();
            stage.setScene(Home.createHomeScene(username));
        });

        // Camera event handlers
        startCameraBtn.setOnAction(e -> {
            startCameraInClassScene(cameraView, recognitionStatus, className);
            startCameraBtn.setDisable(true);
            stopCameraBtn.setDisable(false);
        });

        stopCameraBtn.setOnAction(e -> {
            Helper.stopCamera();
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

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());

        // Maintain the same window size and position
        Platform.runLater(() -> {
            // Keep the stage at the same size it was
            if (!stage.isMaximized()) {
                stage.setWidth(Helper.getScreenWidth());
                stage.setHeight(Helper.getScreenHeight());
            }
        });

        return scene;
    }

    public static void startCameraInClassScene(ImageView imageView, Label statusLabel, String username) {
        Helper.capture = new VideoCapture(0);
        System.out.println("Camera opened: " + Helper.capture.isOpened());

        if (!Helper.capture.isOpened()) {
            statusLabel.setText("Camera unavailable");
            return;
        }
        Helper.cameraActive = true;

        // Load ONNX Mask Detection Model
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

                while (Helper.cameraActive) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Imgproc.cvtColor(Helper.currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        Helper.faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0, new Size(30, 30), new Size());

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
                                Imgproc.rectangle(Helper.currentFrame, rect.tl(), rect.br(), red, 2);
                                Imgproc.putText(Helper.currentFrame, "Mask Detected", new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, red, 2);
                            } else {
                                // perform recognition only if no mask
                                Mat face = gray.submat(rect);
                                Mat resizedFace = new Mat();
                                Imgproc.resize(face, resizedFace, new Size(200, 200));
                                String recognizedName = Helper.recognizeFace(resizedFace);

                                Scalar color = recognizedName.equals("Unknown") ? orange : green;
                                String labelText = recognizedName.equals("Unknown")
                                        ? "Unknown"
                                        : "Recognized: " + recognizedName;

                                Imgproc.rectangle(Helper.currentFrame, rect.tl(), rect.br(), color, 2);
                                Imgproc.putText(Helper.currentFrame, labelText, new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);

                                resizedFace.release();
                                face.release();
                            }
                            resized.release();
                            faceROI.release();
                        }

                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);  
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                    } else {
                        System.out.println("❌ Frame not read");
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
}
