package dev.att.smartattendance.app.pages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import dev.att.smartattendance.app.CustomAlert;
import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.app.Loader;
import dev.att.smartattendance.model.group.Group;
import dev.att.smartattendance.model.group.GroupDAO;
import dev.att.smartattendance.model.student.Student;
import dev.att.smartattendance.model.student.StudentDAO;
import dev.att.smartattendance.util.DatabaseManager;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Class {
        
    private static Map<String, CheckBox> studentCheckboxes = new HashMap<>();
    private static Map<String, Label> studentStatusLabels = new HashMap<>();
    private static Set<String> detectedStudentsInThisSession = new HashSet<>();
        
    public static Scene createClassScene(String groupId, String groupName, String username, Stage stage) {        
        studentCheckboxes.clear();
        studentStatusLabels.clear();
        detectedStudentsInThisSession.clear();
                
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        
        VBox headerSection = new VBox(10);
        headerSection.getStyleClass().add("home-header");
        headerSection.setAlignment(Pos.CENTER);
        
        GroupDAO groupDAO = new GroupDAO();
        String courseCode = "N/A";
        for (Group g : groupDAO.get_all_groups()) {
            if (g.getGroup_id().equals(groupId)) {
                courseCode = g.getcourse_code();
                break;
            }
        }

        Label titleLabel = new Label(courseCode + " (" + groupName + ") - Attendance");
        titleLabel.getStyleClass().add("home-title");
        
        Button backButton = new Button("← Back to Home");
        backButton.getStyleClass().add("back-button");

        backButton.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
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
        
        VBox attendanceSection = new VBox(15);
        attendanceSection.getStyleClass().add("attendance-section");
        attendanceSection.setMinWidth(500);

        Label attendanceTitle = new Label("Student Attendance List");
        attendanceTitle.getStyleClass().add("attendance-title");

        Label sessionLabel = new Label("Session: Week 10 - November 2, 2025");
        sessionLabel.getStyleClass().add("session-label");
        
        VBox studentList = new VBox(12);
        studentList.setStyle("-fx-padding: 15 0;");
        
        List<Student> students = getStudentsForGroup(groupId);

        if (students.isEmpty()) {
            Label noStudentsLabel = new Label("No students enrolled in this class yet");
            noStudentsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            studentList.getChildren().add(noStudentsLabel);
        } else {
            for (Student student : students) {
                String studentName = student.getName();
                                
                if (studentName.equalsIgnoreCase("Admin")) {
                    continue;
                }

                HBox studentRow = new HBox(15);
                studentRow.setAlignment(Pos.CENTER_LEFT);
                studentRow.getStyleClass().add("student-row");

                CheckBox checkBox = new CheckBox();
                checkBox.setStyle("-fx-font-size: 14px;");
                                
                studentCheckboxes.put(studentName, checkBox);

                checkBox.setOnAction(event -> {
                    Label statusIndicator = studentStatusLabels.get(studentName);
                    if (statusIndicator != null) {
                        if (checkBox.isSelected()) {
                            statusIndicator.setText("Present");
                            statusIndicator.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
                        } else {
                            statusIndicator.setText("Absent");
                            statusIndicator.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                        }
                    }
                });

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
            loadExistingAttendance(groupId, students);
        }
        
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setStyle("-fx-padding: 20 0 0 0;");

        Button saveButton = new Button("Save Attendance");
        saveButton.getStyleClass().add("save-button");

        Button exportButton = new Button("Export to Excel");
        exportButton.getStyleClass().add("export-button");

        saveButton.setOnAction(e -> {
            saveAttendance(groupId, groupName);
            // Helper.showAlert("Success", "Attendance saved successfully for " + groupId + "!");
            CustomAlert.showSuccess("Success", "Attendance saved successfully for " + groupName + "!");
        });

        exportButton.setOnAction(e -> {
            exportAttendanceToCSV(groupId, groupName, stage);
        });

        actionButtons.getChildren().addAll(saveButton, exportButton);

        ScrollPane studentScrollPane = new ScrollPane(studentList);
        studentScrollPane.setFitToWidth(true);
        studentScrollPane.setStyle("-fx-background-color: transparent;");
        studentScrollPane.setMaxHeight(400);
        VBox.setVgrow(studentScrollPane, Priority.ALWAYS);

        attendanceSection.getChildren().addAll(attendanceTitle, sessionLabel, studentScrollPane, actionButtons);

        contentSection.getChildren().addAll(cameraSection, attendanceSection);
        
        mainContainer.getChildren().addAll(headerSection, contentSection);

        ScrollPane mainScrollPane = new ScrollPane(mainContainer);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(mainScrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Class.class.getResource("/css/styles.css").toExternalForm());
                
        Platform.runLater(() -> startAttendanceCamera(webcamView, detectionStatus, groupId));

        return scene;
    }
    
    private static List<Student> getStudentsForGroup(String groupId) {
        Loader.loadStudentNames();
        StudentDAO studentDAO = new StudentDAO();
        List<Student> students = new ArrayList<>();

        try {            
            students = studentDAO.get_students_by_group(groupId);

            if (students.isEmpty()) {
                System.out.println("No students found for group: " + groupId);
            } else {
                System.out.println("Found " + students.size() + " students for group: " + groupId);
            }
                        
            for (Student student : students) {
                Helper.emailToNameMap.put(student.getEmail(), student.getName());
                System.out.println("Mapped for display: " + student.getEmail() + " -> " + student.getName());
            }

        } catch (Exception e) {
            System.err.println("Error retrieving students for group " + groupId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return students;
    }

    private static void startAttendanceCamera(ImageView imageView, Label statusLabel, String groupId) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            Platform.runLater(() -> statusLabel.setText("Camera unavailable"));
            return;
        }
        Helper.cameraActive = true;
        
        // Load mask detection model
        OrtEnvironment env;
        OrtSession session;
        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            session = env.createSession("src/main/resources/models/mask_detector.onnx", opts);
            System.out.println("✓ Mask detector model loaded.");
        } catch (OrtException e) {
            e.printStackTrace();
            Platform.runLater(() -> statusLabel.setText("Failed to load mask detection model"));
            return;
        }

        Platform.runLater(() -> statusLabel.setText("Camera active - Position yourself in front of camera"));

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
                                    // MASK DETECTED - Draw red box and warning
                                    Imgproc.rectangle(Helper.currentFrame, rect.tl(), rect.br(), new Scalar(0, 0, 255), 3);
                                    Imgproc.putText(Helper.currentFrame, "Mask Detected - Remove Mask", 
                                            new Point(rect.x, rect.y - 10),
                                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 0, 255), 2);
                                } else {
                                    // NO MASK - Proceed with normal face recognition
                                    Mat face = gray.submat(rect);
                                    Mat resizedFace = new Mat();
                                    Imgproc.resize(face, resizedFace, new Size(200, 200));
                                    
                                    String recognizedEmail = Helper.recognizeFace(resizedFace);
                                    
                                    if (recognizedEmail.equals(lastDetectedEmail) && !recognizedEmail.equals("Unknown")) {
                                        consecutiveDetections++;
                                    } else {
                                        consecutiveDetections = 1;
                                        lastDetectedEmail = recognizedEmail;
                                    }
                                    
                                    String studentName = Helper.emailToNameMap.get(recognizedEmail);
                                    if (studentName == null) {                                    
                                        studentName = recognizedEmail.equals("Unknown") ? "Unknown" : recognizedEmail;
                                    }
                                    
                                    // Mark attendance if detected reliably and checkbox not already checked
                                    if (consecutiveDetections >= 5 && !recognizedEmail.equals("Unknown")) {
                                        CheckBox checkBox = studentCheckboxes.get(studentName);
                                        
                                        if (checkBox != null && !checkBox.isSelected()) {
                                            String finalStudentName = studentName;
                                            Platform.runLater(() -> markStudentPresent(finalStudentName, statusLabel));
                                            consecutiveDetections = 0;
                                        }
                                    }
                                    
                                    Scalar color = studentName.equals("Unknown") 
                                        ? new Scalar(255, 165, 0) 
                                        : new Scalar(0, 255, 0);

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
                                
                                resized.release();
                                faceROI.release();
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
        CheckBox checkBox = studentCheckboxes.get(studentName);
        Label statusIndicator = studentStatusLabels.get(studentName);
        
        if (checkBox != null) {
            checkBox.setSelected(true);
            
            if (statusIndicator != null) {
                statusIndicator.setText("Present (Auto-detected)");
                statusIndicator.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
            
            statusLabel.setText("✓ Attendance marked: " + studentName);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            
            System.out.println("Attendance marked for: " + studentName);
        } else {            
            statusLabel.setText("⚠ " + studentName + " detected but not in this class");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
        }
    }
                                
    private static void saveAttendance(String groupId, String groupName) {
        System.out.println("Saving attendance for group: " + groupName + " (ID: " + groupId + ")");
        
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); 
                        
            String checkSessionSql = "SELECT COUNT(*) FROM attendanceSessions WHERE group_id = ? AND date = ?";
            boolean sessionExists = false;
            
            try (PreparedStatement checkPs = conn.prepareStatement(checkSessionSql)) {
                checkPs.setString(1, groupId);
                checkPs.setString(2, currentDate);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    sessionExists = true;
                }
            }
                        
            if (sessionExists) {                
                System.out.println("Updating existing session for " + groupId + " on " + currentDate);
            } else {                
                String sessionSql = "INSERT INTO attendanceSessions (group_id, date, start_time, end_time, session_type) " +
                                "VALUES (?, ?, NULL, NULL, NULL)";
                
                try (PreparedStatement sessionPs = conn.prepareStatement(sessionSql)) {
                    sessionPs.setString(1, groupId);
                    sessionPs.setString(2, currentDate);
                    sessionPs.executeUpdate();
                    System.out.println("Created new attendance session for " + groupId + " on " + currentDate);
                }
            }
                        
            List<Student> students = getStudentsForGroup(groupId);
            int savedCount = 0;
            int updatedCount = 0;
                        
            String upsertRecordSql = "INSERT INTO attendanceRecords (group_id, date, student_id, status) " +
                                    "VALUES (?, ?, ?, ?) " +
                                    "ON CONFLICT(group_id, date, student_id) " +
                                    "DO UPDATE SET status = excluded.status";
            
            try (PreparedStatement recordPs = conn.prepareStatement(upsertRecordSql)) {
                for (Student student : students) {
                    String studentName = student.getName();
                                        
                    if (studentName.equalsIgnoreCase("Admin")) {
                        continue;
                    }
                                        
                    CheckBox checkBox = studentCheckboxes.get(studentName);
                    String status = (checkBox != null && checkBox.isSelected()) ? "Present" : "Absent";
                                        
                    recordPs.setString(1, groupId);
                    recordPs.setString(2, currentDate);
                    recordPs.setString(3, student.getStudent_id());
                    recordPs.setString(4, status);
                    
                    int rowsAffected = recordPs.executeUpdate();
                    
                    if (sessionExists) {
                        updatedCount++;
                    } else {
                        savedCount++;
                    }
                    
                    System.out.println(studentName + " (" + student.getEmail() + "): " + status);
                }
            }
                        
            conn.commit();
            
            String message = sessionExists 
                ? "Successfully updated attendance for " + updatedCount + " students"
                : "Successfully saved attendance for " + savedCount + " students";
            System.out.println(message);
            
        } catch (SQLException e) {
            System.err.println("Failed to save attendance: " + e.getMessage());
            e.printStackTrace();
                        
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback: " + ex.getMessage());
                }
            }
                        
            Platform.runLater(() -> 
                Helper.showAlert("Save Error", "Failed to save attendance to database:\n" + e.getMessage())
            );
            
        } finally {
            if (conn != null) {
                try {
                conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    private static void loadExistingAttendance(String groupId, List<Student> students) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
        Map<String, String> studentIdToName = new HashMap<>();
        for (Student student : students) {
            studentIdToName.put(student.getStudent_id(), student.getName());
        }
        
        String sql = "SELECT student_id, status FROM attendanceRecords " +
                    "WHERE group_id = ? AND date = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ps.setString(2, currentDate);
            
            ResultSet rs = ps.executeQuery();
            int loadedCount = 0;
            
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String status = rs.getString("status");
                                
                String studentName = studentIdToName.get(studentId);
                
                if (studentName != null && !studentName.equalsIgnoreCase("Admin")) {
                    CheckBox checkBox = studentCheckboxes.get(studentName);
                    Label statusLabel = studentStatusLabels.get(studentName);
                    
                    if (checkBox != null && statusLabel != null) {
                        final String name = studentName;
                        final boolean isPresent = status.equals("Present");
                        
                        Platform.runLater(() -> {
                            checkBox.setSelected(isPresent);
                            if (isPresent) {
                                statusLabel.setText("Present (Previously saved)");
                                statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px; -fx-font-weight: bold;");
                            } else {
                                statusLabel.setText("Absent");
                                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                            }
                        });
                        loadedCount++;
                    }
                }
            }
            
            if (loadedCount > 0) {
                System.out.println("Loaded existing attendance for " + loadedCount + " students on " + currentDate);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to load existing attendance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void exportAttendanceToCSV(String groupId, String groupName, Stage stage) {        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Attendance Report");
                
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String defaultFileName = groupName.replaceAll("[^a-zA-Z0-9]", "_") + "_Attendance_" + timestamp + ".csv";
        fileChooser.setInitialFileName(defaultFileName);
                
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
                
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {
                writeAttendanceCSV(file, groupId, groupName);
                // Helper.showAlert("Export Success", "Attendance exported successfully to:\n" + file.getAbsolutePath());
                CustomAlert.showSuccess("Export Success", "Attendance exported successfully to:\n" + file.getAbsolutePath());
            } catch (IOException ex) {
                // Helper.showAlert("Export Error", "Failed to export attendance:\n" + ex.getMessage());
                CustomAlert.showError("Export Error", "Failed to export attendance:\n" + ex.getMessage());

                ex.printStackTrace();
            }
        }
    }

    private static void writeAttendanceCSV(File file, String groupId, String groupName) throws IOException {        
        GroupDAO groupDAO = new GroupDAO();
        Group group = null;
                
        for (Group g : groupDAO.get_all_groups()) {
            if (g.getGroup_id().equals(groupId)) {
                group = g;
                break;
            }
        }
        
        String courseCode = (group != null) ? group.getcourse_code() : "N/A";
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String currentDay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE")); 
        
        try (FileWriter writer = new FileWriter(file)) {            
            writer.append("Name,Email,Course Code,Group,Date,Day,Attendance\n");
                        
            List<Student> students = getStudentsForGroup(groupId);
                        
            for (Student student : students) {
                String studentName = student.getName();
                String studentEmail = student.getEmail();
                                
                if (studentName.equalsIgnoreCase("Admin")) {
                    continue;
                }
                                
                CheckBox checkBox = studentCheckboxes.get(studentName);
                String attendance;
                
                if (checkBox != null && checkBox.isSelected()) {
                    attendance = "Present";
                } else {
                    attendance = "Absent";
                }
                                
                writer.append(escapeCSV(studentName))
                    .append(',')
                    .append(escapeCSV(studentEmail))
                    .append(',')
                    .append(escapeCSV(courseCode))
                    .append(',')
                    .append(escapeCSV(groupName))
                    .append(',')
                    .append(currentDate)
                    .append(',')
                    .append(currentDay)
                    .append(',')
                    .append(attendance)
                    .append('\n');
            }
            
            writer.flush();
            System.out.println("Attendance CSV exported successfully: " + file.getAbsolutePath());
        }
    }
    
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
                
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {            
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
}