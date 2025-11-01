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
import dev.att.smartattendance.app.Loader;
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
    
    // Store checkboxes and status labels for each student (key = student name)
    private static Map<String, CheckBox> studentCheckboxes = new HashMap<>();
    private static Map<String, Label> studentStatusLabels = new HashMap<>();
    private static Set<String> detectedStudentsInThisSession = new HashSet<>();
    
    public static Scene createClassScene(String className, String username, Stage stage) {
        // Clear previous data
        studentCheckboxes.clear();
        studentStatusLabels.clear();
        detectedStudentsInThisSession.clear();
        
        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #0f172a;");

        // Header section
        VBox headerSection = new VBox(10);
        headerSection.getStyleClass().add("home-header");
        headerSection.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(className + " - Attendance");
        titleLabel.getStyleClass().add("home-title");

        // Back button
        Button backButton = new Button("← Back to Home");
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

        HBox contentSection = new HBox(30);
        contentSection.setStyle("-fx-padding: 40; -fx-background-color: #0f172a;");
        contentSection.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE: Camera Feed
        VBox cameraSection = new VBox(15);
        cameraSection.getStyleClass().add("camera-section");
        cameraSection.setAlignment(Pos.CENTER);
        cameraSection.setMinWidth(500);

        Label cameraTitle = new Label("Live Face Detection");
        cameraTitle.getStyleClass().add("camera-title");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(450);
        webcamView.setFitHeight(350);
        webcamView.setPreserveRatio(true);
        webcamView.getStyleClass().add("camera-view");

        Label detectionStatus = new Label("Starting camera...");
        detectionStatus.getStyleClass().add("detection-status");
        detectionStatus.setWrapText(true);
        detectionStatus.setMaxWidth(450);
        detectionStatus.setAlignment(Pos.CENTER);

        cameraSection.getChildren().addAll(cameraTitle, webcamView, detectionStatus);

        // RIGHT SIDE: Student Attendance List
        VBox attendanceSection = new VBox(15);
        attendanceSection.getStyleClass().add("attendance-section");
        attendanceSection.setMinWidth(500);

        Label attendanceTitle = new Label("Student Attendance List");
        attendanceTitle.getStyleClass().add("attendance-title");

        Label sessionLabel = new Label("Session: Week 10 - November 1, 2025");
        sessionLabel.getStyleClass().add("session-label");

        // Student list with checkboxes
        VBox studentList = new VBox(12);
        studentList.setStyle("-fx-padding: 15 0;");

        // Get students for this class
        List<Student> students = getStudentsForClass(className);

        for (Student student : students) {
            String studentName = student.getName();
            
            // Skip Admin from the checkbox list
            if (studentName.equalsIgnoreCase("Admin")) {
                continue;
            }

            HBox studentRow = new HBox(15);
            studentRow.setAlignment(Pos.CENTER_LEFT);
            studentRow.getStyleClass().add("student-row");

            CheckBox checkBox = new CheckBox();
            checkBox.setStyle("-fx-font-size: 14px;");
            
            // Store checkbox with student name as key
            studentCheckboxes.put(studentName, checkBox);

            Label nameLabel = new Label(studentName);
            nameLabel.getStyleClass().add("student-name");

            Label statusIndicator = new Label("Absent");
            statusIndicator.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            studentStatusLabels.put(studentName, statusIndicator);

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
        saveButton.getStyleClass().add("save-button");

        Button exportButton = new Button("Export to Excel");
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
        mainScrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(mainScrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Class.class.getResource("/css/styles.css").toExternalForm());
        
        // Start camera with face detection immediately
        Platform.runLater(() -> startAttendanceCamera(webcamView, detectionStatus, className));

        return scene;
    }

    private static List<Student> getStudentsForClass(String className) {
        Loader.loadStudentNames();
        StudentDAO studentDAO = new StudentDAO();
        List<Student> students = new ArrayList<>();

        try {
            students = studentDAO.get_students_by_group(className);

            if (students.isEmpty()) {
                System.out.println("No students found for class: " + className);
            }
            
            // Ensure emailToNameMap is populated for this class
            for (Student student : students) {
                Helper.emailToNameMap.put(student.getEmail(), student.getName());
                System.out.println("Mapped for display: " + student.getEmail() + " -> " + student.getName());
            }

        } catch (Exception e) {
            System.err.println("Error retrieving students for " + className + ": " + e.getMessage());
        }

        return students;
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
                String lastDetectedEmail = "";

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

                                // recognizeFace returns email
                                String recognizedEmail = Helper.recognizeFace(resizedFace);

                                // Track consecutive detections for reliability
                                if (recognizedEmail.equals(lastDetectedEmail) && !recognizedEmail.equals("Unknown")) {
                                    consecutiveDetections++;
                                } else {
                                    consecutiveDetections = 1;
                                    lastDetectedEmail = recognizedEmail;
                                }

                                // Get student name from email
                                String studentName = Helper.emailToNameMap.get(recognizedEmail);
                                if (studentName == null) {
                                    // If mapping not found, use email as fallback for debugging
                                    studentName = recognizedEmail.equals("Unknown") ? "Unknown" : recognizedEmail;
                                }

                                // Mark attendance if detected reliably and not already marked
                                if (consecutiveDetections >= 5 && !recognizedEmail.equals("Unknown") 
                                    && !detectedStudentsInThisSession.contains(studentName)) {
                                    
                                    detectedStudentsInThisSession.add(studentName);
                                    
                                    // Auto-check the checkbox
                                    String finalStudentName = studentName;
                                    Platform.runLater(() -> markStudentPresent(finalStudentName, statusLabel));
                                    
                                    consecutiveDetections = 0;
                                }

                                // Visual feedback - use student name for display
                                Scalar color = studentName.equals("Unknown") 
                                        ? new Scalar(255, 165, 0)  // Orange for unknown
                                        : new Scalar(0, 255, 0);   // Green for recognized

                                Imgproc.rectangle(Helper.currentFrame, new Point(rect.x, rect.y),
                                        new Point(rect.x + rect.width, rect.y + rect.height),
                                        color, 3);

                                String displayText = studentName.equals("Unknown") 
                                        ? "Unknown Person" 
                                        : studentName;
                                
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
        // Get the checkbox for this student (using name as key)
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
            String studentName = entry.getKey();
            boolean present = entry.getValue().isSelected();
            System.out.println(studentName + ": " + (present ? "Present" : "Absent"));
        }
    }
}