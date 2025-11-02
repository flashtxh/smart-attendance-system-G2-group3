package dev.att.smartattendance.app.pages;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.model.group.Group;
import dev.att.smartattendance.model.group.GroupDAO;
import dev.att.smartattendance.model.professor.Professor;
import dev.att.smartattendance.model.professor.ProfessorDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Home {

    public static Scene createHomeScene(String username) {        
        ProfessorDAO professorDAO = new ProfessorDAO();
        Professor professor = professorDAO.get_professor_by_email(username);
        
        final String professorId;
        final String displayName;
        
        if (professor != null) {
            professorId = professor.getProfessor_id();
            displayName = professor.getUsername();
        } else {
            professorId = null;
            displayName = username;
        }
                
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        
        VBox headerSection = new VBox(10);
        headerSection.setStyle(
                "-fx-background-color: white; -fx-padding: 30; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        headerSection.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("SMART ATTENDANCE SYSTEM");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        VBox avatarSection = new VBox(5);
        avatarSection.setAlignment(Pos.CENTER);

        Label avatarLabel = new Label(displayName.substring(0, 1).toUpperCase());
        avatarLabel.getStyleClass().add("avatar-circle");

        Label usernameLabel = new Label(displayName);
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        avatarSection.getChildren().addAll(avatarLabel, usernameLabel);
        
        VBox profSection = new VBox(2);
        profSection.setAlignment(Pos.CENTER_RIGHT);
        Label profLabel = new Label("Prof. " + displayName);
        profLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        profSection.getChildren().add(profLabel);
        
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER);
        javafx.scene.layout.Region leftSpacer = new javafx.scene.layout.Region();
        javafx.scene.layout.Region rightSpacer = new javafx.scene.layout.Region();
        HBox.setHgrow(leftSpacer, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, javafx.scene.layout.Priority.ALWAYS);

        headerTop.getChildren().addAll(avatarSection, leftSpacer, titleLabel, rightSpacer, profSection);
        headerSection.getChildren().add(headerTop);
        
        VBox contentSection = new VBox(20);
        contentSection.setStyle("-fx-padding: 40;");
        contentSection.setAlignment(Pos.CENTER_LEFT);
        
        HBox semesterSection = new HBox(10);
        semesterSection.setAlignment(Pos.CENTER_LEFT);

        Label semesterLabel = new Label("Current Semester:");
        semesterLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        Label semesterValue = new Label("AY 25/26 Sem 1 ▼");
        semesterValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        semesterSection.getChildren().addAll(semesterLabel, semesterValue);
        
        Label classLabel = new Label("Classes you teach:");
        classLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        
        FlowPane classButtons = new FlowPane(10, 10);
        classButtons.setAlignment(Pos.CENTER_LEFT);
        
        GroupDAO groupDAO = new GroupDAO();
        List<Group> professorGroups;
        
        if (professorId != null) {
            professorGroups = groupDAO.get_groups_by_professor(professorId);
        } else {            
            professorGroups = groupDAO.get_all_groups();
        }

        if (professorGroups.isEmpty()) {
            Label noClassesLabel = new Label("No classes assigned yet");
            noClassesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            classButtons.getChildren().add(noClassesLabel);
        } else {
            for (Group group : professorGroups) {                                
                VBox classButtonContainer = new VBox(5);
                classButtonContainer.setAlignment(Pos.CENTER);
                
                String buttonLabel = group.getcourse_code() + " (" + group.getGroup_name() + ")";
                Button classBtn = new Button(buttonLabel);
                classBtn.getStyleClass().add("class-button");
                                
                String tooltipText = group.getAcademic_year() + " " + group.getTerm();
                classBtn.setStyle(classBtn.getStyle() + "-fx-tooltip: '" + tooltipText + "';");
                                
                classBtn.setOnAction(e -> {
                    Helper.stopCamera();
                    Stage stage = (Stage) classBtn.getScene().getWindow();
                    stage.setScene(Class.createClassScene(
                    group.getGroup_id(),      
                    group.getGroup_name(),    
                    displayName,              
                        stage
                    ));
                });
                
                classButtonContainer.getChildren().add(classBtn);
                                
                if (username.equals("Admin")) {
                    Button manageBtn = new Button("⚙ Manage");
                    manageBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                            "-fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 6; " +
                            "-fx-cursor: hand; -fx-font-weight: 600;");
                    
                    manageBtn.setOnMouseEntered(e -> {
                        manageBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 6; " +
                                "-fx-cursor: hand; -fx-font-weight: 600;");
                    });
                    
                    manageBtn.setOnMouseExited(e -> {
                        manageBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 6; " +
                                "-fx-cursor: hand; -fx-font-weight: 600;");
                    });
                    
                    manageBtn.setOnAction(e -> {
                        Helper.stopCamera();
                        Stage stage = (Stage) manageBtn.getScene().getWindow();
                        stage.setScene(ClassManagement.createManageClassScene(stage, group.getGroup_id()));
                    });
                    
                    classButtonContainer.getChildren().add(manageBtn);
                }
                
                classButtons.getChildren().add(classButtonContainer);
            }
        }
        
        Button enrollStudentBtn = new Button("Enroll Student");
        enrollStudentBtn.getStyleClass().add("enroll-student-button");

        HBox classRow = new HBox(20);
        classRow.setAlignment(Pos.CENTER_LEFT);
        classRow.getChildren().addAll(classLabel);

        HBox actionButtonsRow = new HBox(15);
        actionButtonsRow.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsRow.getChildren().add(enrollStudentBtn);
                
        if (username.equals("Admin")) {
            Button newClassBtn = new Button("New Class");
            newClassBtn.getStyleClass().add("new-class-button");
            
            newClassBtn.setOnAction(e -> {
                Helper.stopCamera();
                Stage stage = (Stage) newClassBtn.getScene().getWindow();
                stage.setScene(ClassManagement.createNewClassScene(stage));
            });
            
            actionButtonsRow.getChildren().add(newClassBtn);
        }

        contentSection.getChildren().addAll(semesterSection, classRow, classButtons, actionButtonsRow);
        
        VBox bottomSection = new VBox(20);
        bottomSection.setAlignment(Pos.CENTER);
        bottomSection.setStyle("-fx-padding: 20;");
        
        ImageView webcamView = new ImageView();
        webcamView.setFitWidth(400);
        webcamView.setFitHeight(300);
        webcamView.setPreserveRatio(true);
        webcamView.setStyle("-fx-border-color: #bdc3c7; -fx-border-width: 2;");

        Label cameraLabel = new Label("Live Camera Feed (Optional)");
        cameraLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");

        logoutButton.setOnAction(e -> {
            Helper.stopCamera();
            Helper.loggedInUsername = "";
            Helper.faceVerified = false;
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(Login.createLoginScene(stage));
        });

        bottomSection.getChildren().addAll(cameraLabel, webcamView, logoutButton);

        enrollStudentBtn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) enrollStudentBtn.getScene().getWindow();
            stage.setScene(Enrollement.createEnrollmentInfoScene(stage));
        });
        
        mainContainer.getChildren().addAll(headerSection, contentSection, bottomSection);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
                
        scene.getStylesheets().add(Home.class.getResource("/css/styles.css").toExternalForm());
        return scene;
    }
    
    public static void startSimpleCameraFeed(ImageView imageView) {
        Helper.capture = new VideoCapture(0);
        if (!Helper.capture.isOpened()) {
            return;
        }
        Helper.cameraActive = true;

        Task<Void> frameGrabber = new Task<>() {
            @Override
            protected Void call() {
                Mat frame = new Mat();

                while (Helper.cameraActive) {
                    if (Helper.capture.read(frame)) {
                        Helper.currentFrame = frame.clone();
                        Image imageToShow = Helper.mat2Image(Helper.currentFrame);
                        Platform.runLater(() -> imageView.setImage(imageToShow));
                    }

                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                frame.release();
                return null;
            }
        };

        Thread th = new Thread(frameGrabber);
        th.setDaemon(true);
        th.start();
    }
}