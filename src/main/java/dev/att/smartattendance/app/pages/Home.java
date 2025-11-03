package dev.att.smartattendance.app.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.model.course.Course;
import dev.att.smartattendance.model.course.CourseDAO;
import dev.att.smartattendance.model.group.Group;
import dev.att.smartattendance.model.group.GroupDAO;
import dev.att.smartattendance.model.professor.Professor;
import dev.att.smartattendance.model.professor.ProfessorDAO;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
                
        boolean isTA = professorDAO.is_ta(username);
        String titlePrefix = isTA ? "T.A. " : (username.equals("Admin") ? "" : "Prof. ");
        
        Label profLabel = new Label(titlePrefix + displayName);
        profLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        profSection.getChildren().add(profLabel);
        
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER);
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        headerTop.getChildren().addAll(avatarSection, leftSpacer, titleLabel, rightSpacer, profSection);
        headerSection.getChildren().add(headerTop);
        
        VBox contentSection = new VBox(20);
        contentSection.setStyle("-fx-padding: 40;");
        contentSection.setAlignment(Pos.CENTER_LEFT);
        
        // Check if admin
        boolean isAdmin = username.equals("Admin");
        
        if (isAdmin) {
            // ADMIN VIEW - Organized by courses
            contentSection.getChildren().add(createAdminView(displayName));
        } else {
            // PROFESSOR/TA VIEW - Simple list
            contentSection.getChildren().add(createProfessorView(professorId, displayName, isTA));
        }
        
        // Action buttons
        Button enrollStudentBtn = new Button("Enroll Student");
        enrollStudentBtn.getStyleClass().add("enroll-student-button");

        HBox actionButtonsRow = new HBox(15);
        actionButtonsRow.setAlignment(Pos.CENTER_RIGHT);
        actionButtonsRow.getChildren().add(enrollStudentBtn);
                    
        if (isAdmin) {
            Button newClassBtn = new Button("New Class");
            newClassBtn.getStyleClass().add("new-class-button");
            
            newClassBtn.setOnAction(e -> {
                Helper.stopCamera();
                Stage stage = (Stage) newClassBtn.getScene().getWindow();
                stage.setScene(ClassManagement.createNewClassScene(stage));
            });
            
            actionButtonsRow.getChildren().add(newClassBtn);
        }

        enrollStudentBtn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) enrollStudentBtn.getScene().getWindow();
            stage.setScene(Enrollement.createEnrollmentInfoScene(stage));
        });
                
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");

        logoutButton.setOnAction(e -> {
            Helper.stopCamera();
            Helper.loggedInUsername = "";
            Helper.faceVerified = false;
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(Login.createLoginScene(stage));
        });
        
        Region logoutSpacer = new Region();
        logoutSpacer.setMinHeight(40);
        
        HBox logoutRow = new HBox();
        logoutRow.setAlignment(Pos.CENTER);
        logoutRow.setStyle("-fx-padding: 20 0 20 0;");
        logoutRow.getChildren().add(logoutButton);
        
        contentSection.getChildren().addAll(
            actionButtonsRow,
            logoutSpacer,
            logoutRow
        );
                
        mainContainer.getChildren().addAll(headerSection, contentSection);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
                
        scene.getStylesheets().add(Home.class.getResource("/css/styles.css").toExternalForm());
        return scene;
    }
    
    private static VBox createAdminView(String displayName) {
        VBox adminView = new VBox(30);
        adminView.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("All Classes (Organized by Course)");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        // Get all courses and groups
        CourseDAO courseDAO = new CourseDAO();
        GroupDAO groupDAO = new GroupDAO();
        
        List<Course> allCourses = courseDAO.get_all_courses();
        List<Group> allGroups = groupDAO.get_all_groups();
        
        // Organize groups by course
        Map<String, List<Group>> groupsByCourse = new HashMap<>();
        for (Group group : allGroups) {
            String courseId = group.getcourse_code();
            groupsByCourse.computeIfAbsent(courseId, k -> new ArrayList<>()).add(group);
        }
        
        VBox coursesContainer = new VBox(25);
        
        if (allCourses.isEmpty()) {
            Label noCoursesLabel = new Label("No courses available yet");
            noCoursesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            coursesContainer.getChildren().add(noCoursesLabel);
        } else {
            for (Course course : allCourses) {
                VBox courseCard = createCourseCard(course, groupsByCourse.get(course.getCourse_id()), displayName);
                coursesContainer.getChildren().add(courseCard);
            }
        }
        
        adminView.getChildren().addAll(sectionTitle, coursesContainer);
        return adminView;
    }
    
    private static VBox createCourseCard(Course course, List<Group> groups, String displayName) {
        VBox courseCard = new VBox(15);
        courseCard.setStyle("-fx-background-color: #1e293b; -fx-padding: 25; " +
                "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 15, 0, 0, 3);");
        
        // Course header
        HBox courseHeader = new HBox(15);
        courseHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label courseCodeLabel = new Label(course.getCourse_code());
        courseCodeLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #60a5fa; -fx-font-weight: bold;");
        
        Label courseNameLabel = new Label(course.getCourse_name());
        courseNameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        Label courseInfoLabel = new Label(course.getYear() + " • " + course.getSemester());
        courseInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        courseHeader.getChildren().addAll(courseCodeLabel, courseNameLabel, spacer, courseInfoLabel);
        
        // Groups section
        if (groups == null || groups.isEmpty()) {
            Label noGroupsLabel = new Label("No classes created for this course yet");
            noGroupsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 10 0 0 20;");
            courseCard.getChildren().addAll(courseHeader, noGroupsLabel);
        } else {
            Label groupsLabel = new Label("Classes (" + groups.size() + "):");
            groupsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 5 0 0 5;");
            
            FlowPane groupsFlow = new FlowPane(12, 12);
            groupsFlow.setStyle("-fx-padding: 5 0 0 5;");
            
            for (Group group : groups) {
                VBox groupBox = createGroupBox(group, displayName);
                groupsFlow.getChildren().add(groupBox);
            }
            
            courseCard.getChildren().addAll(courseHeader, groupsLabel, groupsFlow);
        }
        
        return courseCard;
    }
    
    private static VBox createGroupBox(Group group, String displayName) {
        VBox groupBox = new VBox(8);
        groupBox.setAlignment(Pos.CENTER);
        groupBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 15; " +
                "-fx-background-radius: 10; -fx-border-color: #475569; -fx-border-width: 1.5; " +
                "-fx-border-radius: 10; -fx-min-width: 180;");
        
        groupBox.setOnMouseEntered(e -> {
            groupBox.setStyle("-fx-background-color: #1e293b; -fx-padding: 15; " +
                    "-fx-background-radius: 10; -fx-border-color: #60a5fa; -fx-border-width: 2; " +
                    "-fx-border-radius: 10; -fx-min-width: 180; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(96, 165, 250, 0.4), 10, 0, 0, 2);");
        });
        
        groupBox.setOnMouseExited(e -> {
            groupBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 15; " +
                    "-fx-background-radius: 10; -fx-border-color: #475569; -fx-border-width: 1.5; " +
                    "-fx-border-radius: 10; -fx-min-width: 180;");
        });
        
        Label groupNameLabel = new Label(group.getGroup_name());
        groupNameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        Button attendanceBtn = new Button("📋 Attendance");
        attendanceBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 150;");
        
        attendanceBtn.setOnMouseEntered(e -> {
            attendanceBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 150;");
        });
        
        attendanceBtn.setOnMouseExited(e -> {
            attendanceBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 150;");
        });
        
        attendanceBtn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) attendanceBtn.getScene().getWindow();
            stage.setScene(Class.createClassScene(
                group.getGroup_id(),
                group.getGroup_name(),
                displayName,
                stage
            ));
        });
        
        Button manageBtn = new Button("⚙ Manage");
        manageBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 150;");
        
        manageBtn.setOnMouseEntered(e -> {
            manageBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 150;");
        });
        
        manageBtn.setOnMouseExited(e -> {
            manageBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                    "-fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 6; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 150;");
        });
        
        manageBtn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) manageBtn.getScene().getWindow();
            stage.setScene(ClassManagement.createManageClassScene(stage, group.getGroup_id()));
        });
        
        groupBox.getChildren().addAll(groupNameLabel, attendanceBtn, manageBtn);
        
        return groupBox;
    }
    
    private static VBox createProfessorView(String professorId, String displayName, boolean isTA) {
        VBox professorView = new VBox(15);
        professorView.setAlignment(Pos.CENTER_LEFT);
        
        Label classLabel = new Label("Classes you teach:");
        classLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        
        FlowPane classButtons = new FlowPane(10, 10);
        classButtons.setAlignment(Pos.CENTER_LEFT);
        
        GroupDAO groupDAO = new GroupDAO();
        List<Group> professorGroups;
        
        if (professorId != null) {
            if (isTA) {                
                professorGroups = groupDAO.get_groups_by_ta(professorId);
            } else {                
                professorGroups = groupDAO.get_groups_by_professor(professorId);
            }
        } else {            
            professorGroups = groupDAO.get_all_groups();
        }

        if (professorGroups.isEmpty()) {
            Label noClassesLabel = new Label("No classes assigned yet");
            noClassesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            classButtons.getChildren().add(noClassesLabel);
        } else {
            CourseDAO courseDAO = new CourseDAO();
            
            for (Group group : professorGroups) {                                
                VBox classButtonContainer = new VBox(5);
                classButtonContainer.setAlignment(Pos.CENTER);
                
                String courseCode = courseDAO.getCourseCodeById(group.getcourse_code());
                String buttonLabel = courseCode + " (" + group.getGroup_name() + ")";
                
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
                classButtons.getChildren().add(classButtonContainer);
            }
        }
        
        professorView.getChildren().addAll(classLabel, classButtons);
        return professorView;
    }
}