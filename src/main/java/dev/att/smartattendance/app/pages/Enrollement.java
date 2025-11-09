package dev.att.smartattendance.app.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.app.ImprovedRecognitionHelper;
import dev.att.smartattendance.app.Loader;
import dev.att.smartattendance.app.pages.customAlert.CustomAlert;
import dev.att.smartattendance.model.course.Course;
import dev.att.smartattendance.model.course.CourseDAO;
import dev.att.smartattendance.model.group.Group;
import dev.att.smartattendance.model.group.GroupDAO;
import dev.att.smartattendance.model.student.StudentDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Enrollement {
        
    private static boolean isEmailExists(String email) {
        StudentDAO studentDAO = new StudentDAO();
        return studentDAO.email_exists(email);
    }
    
    private static String insertStudent(String name, String email) {
        String studentId = UUID.randomUUID().toString();
        StudentDAO studentDAO = new StudentDAO();
        
        studentDAO.insert_student(studentId, name, email);
        System.out.println("Student inserted: " + name + " (" + email + ")");
        return studentId;
    }
    
    private static boolean assignStudentToGroups(String studentId, List<String> groupIds) {
        StudentDAO studentDAO = new StudentDAO();
        return studentDAO.assign_student_to_groups(studentId, groupIds);
    }
        
    public static Scene createEnrollmentInfoScene(Stage stage) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Student Enrollment");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        
        Label subtitleLabel = new Label("Enter student information and assign to classes");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #1e293b; -fx-padding: 40; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 20, 0, 0, 5);");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(600);
        
        
        Label nameLabel = new Label("Student Name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        nameField.setPrefWidth(500);
        
        Label emailLabel = new Label("Student Email:");
        emailLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        emailField.setPrefWidth(500);
        
        
        boolean isAdmin = "Admin".equalsIgnoreCase(Helper.loggedInUsername);
        
        Map<String, ComboBox<String>> courseComboBoxes = new HashMap<>();
        Map<String, Map<String, String>> courseGroupMaps = new HashMap<>();
        
        
        if (isAdmin) {
            Label classesLabel = new Label("Assign to Classes (Optional):");
            classesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600; -fx-padding: 20 0 0 0;");
            
            Label classesSubtitle = new Label("Select one class per course - Type to search");
            classesSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            
            
            TextField courseSearchField = new TextField();
            courseSearchField.setPromptText("Search courses by code or name...");
            courseSearchField.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #0f172a; " +
                    "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                    "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            courseSearchField.setPrefWidth(460);
            
            VBox classSelectionBox = new VBox(15);
            classSelectionBox.setStyle("-fx-padding: 15; -fx-background-color: #0f172a; " +
                    "-fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; -fx-border-radius: 8;");
            
            CourseDAO courseDAO = new CourseDAO();
            GroupDAO groupDAO = new GroupDAO();
            List<Course> allCourses = courseDAO.get_all_courses();
            
            
            List<VBox> allCourseBoxes = new ArrayList<>();
            
            if (allCourses.isEmpty()) {
                Label noCoursesLabel = new Label("No courses/classes available yet");
                noCoursesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
                classSelectionBox.getChildren().add(noCoursesLabel);
            } else {
                for (Course course : allCourses) {
                    List<Group> courseGroups = new ArrayList<>();
                    for (Group group : groupDAO.get_all_groups()) {
                        if (group.getcourse_code().equals(course.getCourse_id())) {
                            courseGroups.add(group);
                        }
                    }
                    
                    if (!courseGroups.isEmpty()) {
                        VBox courseBox = new VBox(8);
                        
                        Label courseLabel = new Label(course.getCourse_code() + " - " + course.getCourse_name());
                        courseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #60a5fa; -fx-font-weight: 600;");
                        
                        ComboBox<String> groupComboBox = new ComboBox<>();
                        groupComboBox.setPromptText("Select a class (optional)");
                        groupComboBox.setStyle("-fx-font-size: 13px; -fx-pref-width: 460; -fx-pref-height: 40; " +
                                "-fx-background-color: #1e293b; -fx-text-fill: #f1f5f9; -fx-border-color: #3b82f6; " +
                                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
                        
                        groupComboBox.getItems().add("None - Don't assign to this course");
                        
                        Map<String, String> groupMap = new HashMap<>();
                        for (Group group : courseGroups) {
                            String displayText = group.getGroup_name() + " (" + group.getAcademic_year() + " " + group.getTerm() + ")";
                            groupComboBox.getItems().add(displayText);
                            groupMap.put(displayText, group.getGroup_id());
                        }
                        
                        groupComboBox.setValue("None - Don't assign to this course");
                        
                        courseComboBoxes.put(course.getCourse_id(), groupComboBox);
                        courseGroupMaps.put(course.getCourse_id(), groupMap);
                        
                        courseBox.getChildren().addAll(courseLabel, groupComboBox);
                        courseBox.setUserData(course); 
                        allCourseBoxes.add(courseBox);
                        classSelectionBox.getChildren().add(courseBox);
                    }
                }
            }
            
            
            courseSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String searchText = newVal.toLowerCase().trim();
                classSelectionBox.getChildren().clear();
                
                if (searchText.isEmpty()) {
                    
                    classSelectionBox.getChildren().addAll(allCourseBoxes);
                } else {
                    
                    for (VBox courseBox : allCourseBoxes) {
                        Course course = (Course) courseBox.getUserData();
                        boolean matches = course.getCourse_code().toLowerCase().contains(searchText) ||
                                        course.getCourse_name().toLowerCase().contains(searchText);
                        
                        if (matches) {
                            classSelectionBox.getChildren().add(courseBox);
                        }
                    }
                    
                    
                    if (classSelectionBox.getChildren().isEmpty()) {
                        Label noResultsLabel = new Label("No courses found matching \"" + searchText + "\"");
                        noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
                        classSelectionBox.getChildren().add(noResultsLabel);
                    }
                }
            });
            
            ScrollPane classScrollPane = new ScrollPane(classSelectionBox);
            classScrollPane.setFitToWidth(true);
            classScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            classScrollPane.setMaxHeight(300);
            
            formContainer.getChildren().addAll(
                nameLabel, nameField, 
                emailLabel, emailField,
                classesLabel, classesSubtitle, courseSearchField, classScrollPane
            );
        } else {
            formContainer.getChildren().addAll(
                nameLabel, nameField, 
                emailLabel, emailField
            );
        }
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);
        
        formContainer.getChildren().add(errorLabel);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button nextBtn = new Button("Next: Capture Face");
        nextBtn.setDefaultButton(true);
        nextBtn.getStyleClass().add("enroll-student-button");
        nextBtn.setPrefWidth(200);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("logout-button");
        cancelBtn.setPrefWidth(200);

        buttonBox.getChildren().addAll(nextBtn, cancelBtn);
        
        nextBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                errorLabel.setText("Please fill in all fields");
                errorLabel.setVisible(true);
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                errorLabel.setText("Please enter a valid email address");
                errorLabel.setVisible(true);
                return;
            }
            
            if (isEmailExists(email)) {
                errorLabel.setText("Student with this email is already enrolled in the database");
                errorLabel.setVisible(true);
                return;
            }
            
            File studentDir = new File(Helper.baseImagePath + email);
            if (studentDir.exists() && studentDir.listFiles() != null && studentDir.listFiles().length > 0) {
                errorLabel.setText("Student with this email already has face data enrolled");
                errorLabel.setVisible(true);
                return;
            }
            
            String studentId = insertStudent(name, email);
            
            if (studentId == null) {
                errorLabel.setText("Failed to add student to database. Please try again.");
                errorLabel.setVisible(true);
                return;
            }
            
            if (isAdmin) {
                List<String> selectedGroupIds = new ArrayList<>();
                for (Map.Entry<String, ComboBox<String>> entry : courseComboBoxes.entrySet()) {
                    String courseId = entry.getKey();
                    ComboBox<String> comboBox = entry.getValue();
                    String selectedValue = comboBox.getValue();
                    
                    if (selectedValue != null && !selectedValue.startsWith("None")) {
                        Map<String, String> groupMap = courseGroupMaps.get(courseId);
                        String groupId = groupMap.get(selectedValue);
                        if (groupId != null) {
                            selectedGroupIds.add(groupId);
                        }
                    }
                }
                
                if (!selectedGroupIds.isEmpty()) {
                    boolean assignSuccess = assignStudentToGroups(studentId, selectedGroupIds);
                    if (!assignSuccess) {
                        CustomAlert.showWarning("Partial Success", 
                            "Student was created but failed to assign to some classes. You can assign them manually later.");
                    }
                }
            }
            
            stage.setScene(createEnrollmentScene(stage, name, email));
        });

        cancelBtn.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
        });

        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, formContainer, buttonBox);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Enrollement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
    
    public static Scene createEnrollmentScene(Stage stage, String username, String email) {
        VBox mainContainer = new VBox(25);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));

        Label titleLabel = new Label("Face Enrollment");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");

        Label infoLabel = new Label(
                "Student: " + username + " (" + email + ")");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");

        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(640);
        webcamView.setFitHeight(480);
        webcamView.setPreserveRatio(true);
        webcamView.setStyle("-fx-border-color: #3b82f6; -fx-border-width: 3; -fx-background-radius: 10;");

        Label statusLabel = new Label("Click 'Start Enrollment' to begin");
        statusLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        Button startEnrollBtn = new Button("Start Enrollment");
        startEnrollBtn.getStyleClass().add("enroll-student-button");
        startEnrollBtn.setPrefWidth(200);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("logout-button");
        backBtn.setPrefWidth(200);

        startEnrollBtn.setOnAction(e -> {
            if (!Helper.cameraActive) {
                startEnrollmentProcess(username, email, webcamView, statusLabel, startEnrollBtn, backBtn, stage);
                startEnrollBtn.setDisable(true);
            }
        });

        backBtn.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
        });

        HBox buttonBox = new HBox(15, startEnrollBtn, backBtn);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, titleLabel, infoLabel, statusLabel, webcamView, buttonBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        mainContainer.getChildren().add(layout);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Enrollement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
    
    public static void startEnrollmentProcess(String username, String email, ImageView webcamView, Label statusLabel,
            Button enrollBtn, Button backBtn, Stage stage) {
        Helper.capturingMode = true;
                
        Helper.capturePersonEmail = email;
                
        Helper.capturePersonName = username;
        Helper.captureCount = 0;
        
        File personDir = new File(Helper.baseImagePath + email);
        personDir.mkdirs();
        
        System.out.println("Creating enrollment folder: " + personDir.getAbsolutePath());

        Platform.runLater(() -> statusLabel.setText("Look at the camera - Capturing: 0/8"));

        startCameraForEnrollment(webcamView, statusLabel, enrollBtn, backBtn, stage);
    }

    public static void saveFaceForEnrollment(Mat gray, Rect rect, Label statusLabel,
        Button enrollBtn, Button backBtn, Stage stage) {
        
        if (!ImprovedRecognitionHelper.isValidFaceForEnrollment(null, gray, rect)) {
            Platform.runLater(() -> {
                statusLabel.setText("⚠️ Face quality insufficient - Adjust position/lighting");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
            });
            return;
        }
        
        Mat face = gray.submat(rect);
        Mat resizedFace = new Mat();
        Imgproc.resize(face, resizedFace, new Size(200, 200));
        
        // Double-check quality after resize
        if (!ImprovedRecognitionHelper.isFaceQualitySufficient(resizedFace)) {
            Platform.runLater(() -> {
                statusLabel.setText("Image too blurry - Hold still and ensure good lighting");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
            });
            resizedFace.release();
            face.release();
            return;
        }
        
        String fileName = Helper.baseImagePath + Helper.capturePersonEmail + "/face_" +
                System.currentTimeMillis() + ".jpg";
        Imgcodecs.imwrite(fileName, resizedFace);

        Helper.captureCount++;
        final int currentCount = Helper.captureCount;
        Platform.runLater(() -> {
            statusLabel.setText("✓ Good capture! " + currentCount + "/8");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        });

        if (Helper.captureCount >= 8) {
            Helper.capturingMode = false;
            Helper.captureCount = 0;
            
            List<Mat> newImages = Loader.loadImages(Helper.baseImagePath + Helper.capturePersonEmail);
            List<Mat> newHistograms = Loader.computeHistograms(newImages);
            
            Helper.personHistograms.put(Helper.capturePersonEmail, newHistograms);
            
            ImprovedRecognitionHelper.storeLBPFeatures(Helper.capturePersonEmail, newImages);
            
            for (Mat img : newImages) {
                img.release();
            }

            Helper.stopCamera();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Enrollment Complete");
                alert.setHeaderText("Success!");
                alert.setContentText("High-quality face enrollment completed for " + Helper.capturePersonName +
                        " (" + Helper.capturePersonEmail + ").\n\nStudent has been added to the database.");
                alert.showAndWait();

                stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
            });
        }

        resizedFace.release();
        face.release();
    }

    public static void startCameraForEnrollment(ImageView imageView, Label statusLabel,
        Button enrollBtn, Button backBtn, Stage stage) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            Helper.showAlert("Camera Error", "Cannot open camera!");
            return;
        }
        Helper.cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();
                Mat gray = new Mat();
                int frameCounter = 0;
                int captureSpacing = 20; 

                while (Helper.cameraActive && Helper.capturingMode) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Imgproc.cvtColor(Helper.currentFrame, gray, Imgproc.COLOR_BGR2GRAY);

                        MatOfRect faces = new MatOfRect();
                        Helper.faceDetector.detectMultiScale(gray, faces, 1.1, 5, 0,
                                new Size(80, 80), new Size()); 
                        Rect[] faceArray = faces.toArray();

                        if (faceArray.length == 1) {
                            Rect rect = faceArray[0];
                            boolean isGoodQuality = ImprovedRecognitionHelper
                                .isValidFaceForEnrollment(null, gray, rect);
                            
                            Scalar color = isGoodQuality ? 
                                new Scalar(16, 185, 129) : new Scalar(255, 165, 0);
                            
                            Imgproc.rectangle(Helper.currentFrame, rect.tl(), rect.br(), color, 3);

                            if (frameCounter % captureSpacing == 0 && 
                                Helper.captureCount < 8 && 
                                isGoodQuality) {
                                saveFaceForEnrollment(gray, rect, statusLabel, 
                                    enrollBtn, backBtn, stage);
                            }

                            String text = "Capturing: " + Helper.captureCount + "/8";
                            String quality = isGoodQuality ? "Good" : "Adjust position";
                            Imgproc.putText(Helper.currentFrame, text + " - " + quality,
                                    new Point(rect.x, rect.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, color, 2);
                        } else if (faceArray.length > 1) {
                            Platform.runLater(() -> {
                                statusLabel.setText("âš  Multiple faces detected - Only one person allowed");
                                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
                            });
                            
                            for (Rect rect : faceArray) {
                                Imgproc.rectangle(Helper.currentFrame, rect.tl(), rect.br(), 
                                    new Scalar(0, 0, 255), 3);
                            }
                        }

                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));

                        frameCounter++;
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