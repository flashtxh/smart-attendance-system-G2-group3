package dev.att.smartattendance.app.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.model.student.Student;
import dev.att.smartattendance.model.student.StudentDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Class {
    
    // Store checkboxes and status labels for each student
    private static Map<String, CheckBox> studentCheckboxes = new HashMap<>();
    private static Map<String, Label> studentStatusLabels = new HashMap<>();
    private static Set<String> detectedInThisClass = new HashSet<>();
    
    public static Scene createClassScene(String className, String username, Stage stage) {
        // Clear previous data
        studentCheckboxes.clear();
        studentStatusLabels.clear();
        detectedInThisClass.clear();
        
        // Main container
        // VBox mainContainer = new VBox();
        // mainContainer.setStyle("-fx-background-color: #f5f5f5;");
        VBox mainContainer = new VBox();
    mainContainer.setStyle("-fx-background-color: #0f172a;");

        // Header section
        VBox headerSection = new VBox(10);
        // headerSection.setStyle(
        //         "-fx-background-color: white; -fx-padding: 30; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        headerSection.getStyleClass().add("home-header");
        headerSection.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(className + " - Attendance");
        // titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.getStyleClass().add("home-title");

        // Back button
        Button backButton = new Button("← Back to Home");
        // backButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; " +
        //         "-fx-border-radius: 5; -fx-background-radius: 5; " +
        //         "-fx-padding: 8 16; -fx-font-size: 14px;");
        backButton.getStyleClass().add("back-button");

        backButton.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(Home.createHomeScene(username));
        });

        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerTop.getChildren().addAll(backButton, spacer, titleLabel, new Region());

        headerSection.getChildren().add(headerTop);

        // HBox contentSection = new HBox(30);
        // contentSection.setStyle("-fx-padding: 40;");
        HBox contentSection = new HBox(30);
        contentSection.setStyle("-fx-padding: 40; -fx-background-color: #0f172a;");
        contentSection.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE: Camera Feed
        VBox cameraSection = new VBox(15);
        // cameraSection.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: #e0e0e0; " +
        //         "-fx-border-width: 1; -fx-border-radius: 5;");
        cameraSection.getStyleClass().add("camera-section");
        cameraSection.setAlignment(Pos.CENTER);
        cameraSection.setMinWidth(500);

        Label cameraTitle = new Label("Live Face Detection");
        // cameraTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        cameraTitle.getStyleClass().add("camera-title");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(450);
        webcamView.setFitHeight(350);
        webcamView.setPreserveRatio(true);
        // webcamView.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2;");
        webcamView.getStyleClass().add("camera-view");

        Label detectionStatus = new Label("Starting camera...");
        // detectionStatus.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        detectionStatus.getStyleClass().add("detection-status");
        detectionStatus.setWrapText(true);
        detectionStatus.setMaxWidth(450);
        detectionStatus.setAlignment(Pos.CENTER);

        cameraSection.getChildren().addAll(cameraTitle, webcamView, detectionStatus);

        // RIGHT SIDE: Student Attendance List
        VBox attendanceSection = new VBox(15);
        // attendanceSection.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: #e0e0e0; " +
        //         "-fx-border-width: 1; -fx-border-radius: 5;");
        attendanceSection.getStyleClass().add("attendance-section");
        attendanceSection.setMinWidth(500);

        Label attendanceTitle = new Label("Student Attendance List");
        // attendanceTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        attendanceTitle.getStyleClass().add("attendance-title");

        Label sessionLabel = new Label("Session: Week 10 - November 1, 2025");
        // sessionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        sessionLabel.getStyleClass().add("session-label");

        // Student list with checkboxes
        VBox studentList = new VBox(12);
        studentList.setStyle("-fx-padding: 15 0;");

        // Get students for this class (excluding Admin)
        List<String> students = getStudentsForClass(className);

        for (String student : students) {
            // Skip Admin from the checkbox list
            if (student.equalsIgnoreCase("Admin")) {
                continue;
            }

            HBox studentRow = new HBox(15);
            studentRow.setAlignment(Pos.CENTER_LEFT);
            // studentRow.setStyle("-fx-padding: 10; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");
            studentRow.getStyleClass().add("student-row");

            CheckBox checkBox = new CheckBox();
            checkBox.setStyle("-fx-font-size: 14px;");
            
            studentCheckboxes.put(student, checkBox);

            Label nameLabel = new Label(student);
            // nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-min-width: 150;");
            nameLabel.getStyleClass().add("student-name");

            Label statusIndicator = new Label("Absent");
            statusIndicator.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            studentStatusLabels.put(student, statusIndicator);

            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            studentRow.getChildren().addAll(checkBox, nameLabel, spacer2, statusIndicator);
            studentList.getChildren().add(studentRow);
        }

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setStyle("-fx-padding: 20 0 0 0;");

        Button saveButton = new Button("Save Attendance");
        // saveButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
        //         "-fx-border-radius: 5; -fx-background-radius: 5; " +
        //         "-fx-padding: 10 20; -fx-font-size: 14px;");
        saveButton.getStyleClass().add("save-button");

        Button exportButton = new Button("Export to Excel");
        // exportButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
        //         "-fx-border-radius: 5; -fx-background-radius: 5; " +
        //         "-fx-padding: 10 20; -fx-font-size: 14px;");
        exportButton.getStyleClass().add("export-button");

        saveButton.setOnAction(e -> {
            saveAttendance();
            Helper.showAlert("Success", "Attendance saved successfully!");
        });

        exportButton.setOnAction(e -> {
            Helper.showAlert("Export", "Exporting attendance to Excel...");
        });

        actionButtons.getChildren().addAll(saveButton, exportButton);

        ScrollPane studentScrollPane = new ScrollPane(studentList);
        studentScrollPane.setFitToWidth(true);
        studentScrollPane.setStyle("-fx-background-color: transparent;");
        studentScrollPane.setMaxHeight(400);
        VBox.setVgrow(studentScrollPane, Priority.ALWAYS);

        attendanceSection.getChildren().addAll(attendanceTitle, sessionLabel, studentScrollPane, actionButtons);

        contentSection.getChildren().addAll(cameraSection, attendanceSection);

        // Main layout
        mainContainer.getChildren().addAll(headerSection, contentSection);

        ScrollPane mainScrollPane = new ScrollPane(mainContainer);
        mainScrollPane.setFitToWidth(true);
        // mainScrollPane.setStyle("-fx-background-color: #f5f5f5;");
        mainScrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(mainScrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Class.class.getResource("/css/styles.css").toExternalForm());
        // Start camera with face detection immediately
        Platform.runLater(() -> startAttendanceCamera(webcamView, detectionStatus, className));

        return scene;
    }

    private static List<String> getStudentsForClass(String className) {
        // Return different student lists based on class, must get from SQL instead
        StudentDAO studentDAO = new StudentDAO();
        List<String> studentNames = new ArrayList<>();

        try {
            List<Student> students = studentDAO.get_students_by_group(className); // returns all of us rn

            if (students.isEmpty()) {
                System.out.println("No students found for class: " + className);
            }

            for (Student s : students) {
                studentNames.add(s.getName());
            }

        } catch (Exception e) {
            System.err.println("Error retrieving students for " + className + ": " + e.getMessage());
        }

        return studentNames;
    }

    private static void startAttendanceCamera(ImageView imageView, Label statusLabel, String className) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            Platform.runLater(() -> statusLabel.setText("Camera unavailable"));
            return;
        }
        Helper.cameraActive = true;

        Platform.runLater(() -> statusLabel.setText("Yayy Camera active - Position yourself in front of camera"));

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();
                int consecutiveDetections = 0;
                String lastDetectedName = "";

                while (Helper.cameraActive) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Imgproc.cvtColor(Helper.currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        Helper.faceDetector.detectMultiScale(gray, faces, 1.1, 3, 0,
                                new Size(30, 30), new Size());

                        Rect[] faceArray = faces.toArray();

                        if (faceArray.length > 0) {
                            for (Rect rect : faceArray) {
                                Mat face = gray.submat(rect);
                                Mat resizedFace = new Mat();
                                Imgproc.resize(face, resizedFace, new Size(200, 200));

                                String recognizedName = Helper.recognizeFace(resizedFace);

                                // Track consecutive detections for reliability
                                if (recognizedName.equals(lastDetectedName) && !recognizedName.equals("Unknown")) {
                                    consecutiveDetections++;
                                } else {
                                    consecutiveDetections = 1;
                                    lastDetectedName = recognizedName;
                                }

                                // Mark attendance if detected reliably and not already marked
                                if (consecutiveDetections >= 5 && !recognizedName.equals("Unknown") 
                                    && !detectedInThisClass.contains(recognizedName)) {
                                    
                                    detectedInThisClass.add(recognizedName);
                                    
                                    // Auto-check the checkbox
                                    Platform.runLater(() -> markStudentPresent(recognizedName, statusLabel));
                                    
                                    consecutiveDetections = 0;
                                }

                                // Visual feedback
                                Scalar color = recognizedName.equals("Unknown") 
                                        ? new Scalar(255, 165, 0)  // Orange for unknown
                                        : new Scalar(0, 255, 0);   // Green for recognized

                                Imgproc.rectangle(Helper.currentFrame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        color, 3);

                                String displayText = recognizedName.equals("Unknown") 
                                        ? "Unknown Person" 
                                        : recognizedName;
                                
                                Imgproc.putText(Helper.currentFrame, displayText,
                                        new Point(rect.x, rect.y - 10),
                                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.9, color, 2);

                                resizedFace.release();
                                face.release();
                            }
                        }

                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                    }

                    try {
                        Thread.sleep(33); // ~30 FPS
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

    private static void markStudentPresent(String studentName, Label statusLabel) {
        // Get the checkbox for this student
        CheckBox checkBox = studentCheckboxes.get(studentName);
        Label statusIndicator = studentStatusLabels.get(studentName);
        
        if (checkBox != null) {
            checkBox.setSelected(true);
            
            if (statusIndicator != null) {
                statusIndicator.setText("Present (Auto-detected)");
                statusIndicator.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
            
            statusLabel.setText("Attendance marked: " + studentName);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            
            System.out.println("Attendance marked for: " + studentName);
        } else {
            // Student not in this class
            statusLabel.setText("Oops: " + studentName + " detected but not in this class");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }
    }

    private static void saveAttendance() {
        System.out.println("Saving attendance:");
        for (Map.Entry<String, CheckBox> entry : studentCheckboxes.entrySet()) {
            String student = entry.getKey();
            boolean present = entry.getValue().isSelected();
            System.out.println(student + ": " + (present ? "Present" : "Absent"));
        }
    }
}