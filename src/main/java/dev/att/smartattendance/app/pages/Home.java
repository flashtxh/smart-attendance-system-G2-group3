package dev.att.smartattendance.app.pages;

import java.util.ArrayList;
import java.util.List;

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
            // ADMIN VIEW - Show courses as buttons
            contentSection.getChildren().add(createAdminCoursesView(displayName));
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
    
    // ADMIN VIEW - Show all courses as clickable cards
    private static VBox createAdminCoursesView(String displayName) {
        VBox adminView = new VBox(20);
        adminView.setAlignment(Pos.CENTER_LEFT);
        
        Label sectionTitle = new Label("Select a Course");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        CourseDAO courseDAO = new CourseDAO();
        List<Course> allCourses = courseDAO.get_all_courses();
        
        FlowPane coursesFlow = new FlowPane(20, 20);
        coursesFlow.setAlignment(Pos.CENTER_LEFT);
        
        if (allCourses.isEmpty()) {
            Label noCoursesLabel = new Label("No courses available yet");
            noCoursesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            coursesFlow.getChildren().add(noCoursesLabel);
        } else {
            for (Course course : allCourses) {
                VBox courseCard = createCourseButton(course, displayName);
                coursesFlow.getChildren().add(courseCard);
            }
        }
        
        adminView.getChildren().addAll(sectionTitle, coursesFlow);
        return adminView;
    }
    
    // Create clickable course card
    private static VBox createCourseButton(Course course, String displayName) {
        VBox courseCard = new VBox(12);
        courseCard.setAlignment(Pos.CENTER_LEFT);
        courseCard.setStyle("-fx-background-color: #1e293b; -fx-padding: 25; " +
                "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 15; -fx-min-width: 220; -fx-max-width: 220; " +
                "-fx-min-height: 130; -fx-cursor: hand;");
        
        courseCard.setOnMouseEntered(e -> {
            courseCard.setStyle("-fx-background-color: #334155; -fx-padding: 25; " +
                    "-fx-background-radius: 15; -fx-border-color: #60a5fa; -fx-border-width: 2.5; " +
                    "-fx-border-radius: 15; -fx-min-width: 220; -fx-max-width: 220; " +
                    "-fx-min-height: 130; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(96, 165, 250, 0.5), 20, 0, 0, 5);");
        });
        
        courseCard.setOnMouseExited(e -> {
            courseCard.setStyle("-fx-background-color: #1e293b; -fx-padding: 25; " +
                    "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                    "-fx-border-radius: 15; -fx-min-width: 220; -fx-max-width: 220; " +
                    "-fx-min-height: 130; -fx-cursor: hand;");
        });
        
        courseCard.setOnMouseClicked(e -> {
            Stage stage = (Stage) courseCard.getScene().getWindow();
            stage.setScene(createCourseDetailScene(course, displayName, stage));
        });
        
        Label courseCodeLabel = new Label(course.getCourse_code());
        courseCodeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #60a5fa; -fx-font-weight: bold;");
        
        Label courseNameLabel = new Label(course.getCourse_name());
        courseNameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        courseNameLabel.setWrapText(true);
        courseNameLabel.setMaxWidth(240);
        
        Label courseInfoLabel = new Label(course.getYear() + " • " + course.getSemester());
        courseInfoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
        
        // Get class count
        GroupDAO groupDAO = new GroupDAO();
        int classCount = 0;
        for (Group g : groupDAO.get_all_groups()) {
            if (g.getcourse_code().equals(course.getCourse_id())) {
                classCount++;
            }
        }
        
        Label classCountLabel = new Label(classCount + " class" + (classCount != 1 ? "es" : ""));
        classCountLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        courseCard.getChildren().addAll(courseCodeLabel, courseNameLabel, spacer, courseInfoLabel, classCountLabel);
        
        return courseCard;
    }
    
    // Course detail scene - shows all groups for a course
    public static Scene createCourseDetailScene(Course course, String displayName, Stage stage) {
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        
        // Header
        VBox headerSection = new VBox(15);
        headerSection.getStyleClass().add("home-header");
        headerSection.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(course.getCourse_code() + " - " + course.getCourse_name());
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        
        Label subtitleLabel = new Label(course.getYear() + " • " + course.getSemester());
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        
        Button backButton = new Button("← Back to Courses");
        backButton.getStyleClass().add("back-button");
        
        backButton.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(createHomeScene(Helper.loggedInUsername));
        });
        
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER_LEFT);
        headerTop.setStyle("-fx-padding: 20 40;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerTop.getChildren().addAll(backButton, spacer);
        
        headerSection.getChildren().addAll(headerTop, titleLabel, subtitleLabel);
        
        // Content - Show all groups for this course
        VBox contentSection = new VBox(20);
        contentSection.setStyle("-fx-padding: 40;");
        contentSection.setAlignment(Pos.CENTER_LEFT);
        
        Label groupsTitle = new Label("Classes in this Course");
        groupsTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        GroupDAO groupDAO = new GroupDAO();
        List<Group> courseGroups = new ArrayList<>();
        
        for (Group group : groupDAO.get_all_groups()) {
            if (group.getcourse_code().equals(course.getCourse_id())) {
                courseGroups.add(group);
            }
        }
        
        FlowPane groupsFlow = new FlowPane(15, 15);
        groupsFlow.setAlignment(Pos.CENTER_LEFT);
        
        if (courseGroups.isEmpty()) {
            Label noGroupsLabel = new Label("No classes created for this course yet");
            noGroupsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
            groupsFlow.getChildren().add(noGroupsLabel);
        } else {
            for (Group group : courseGroups) {
                VBox groupBox = createGroupCard(group, displayName);
                groupsFlow.getChildren().add(groupBox);
            }
        }
        
        contentSection.getChildren().addAll(groupsTitle, groupsFlow);
        
        mainContainer.getChildren().addAll(headerSection, contentSection);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");
        
        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Home.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
    
    // Create group card with buttons
    private static VBox createGroupCard(Group group, String displayName) {
        VBox groupBox = new VBox(12);
        groupBox.setAlignment(Pos.CENTER);
        groupBox.setStyle("-fx-background-color: #1e293b; -fx-padding: 20; " +
                "-fx-background-radius: 12; -fx-border-color: #475569; -fx-border-width: 2; " +
                "-fx-border-radius: 12; -fx-min-width: 220;");
        
        Label groupNameLabel = new Label(group.getGroup_name());
        groupNameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        Label termLabel = new Label(group.getAcademic_year() + " • " + group.getTerm());
        termLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        Button attendanceBtn = new Button("📋 Take Attendance");
        attendanceBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 180;");
        
        attendanceBtn.setOnMouseEntered(e -> {
            attendanceBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 180;");
        });
        
        attendanceBtn.setOnMouseExited(e -> {
            attendanceBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 180;");
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
        
        Button manageBtn = new Button("⚙ Manage Class");
        manageBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 180;");
        
        manageBtn.setOnMouseEntered(e -> {
            manageBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 180;");
        });
        
        manageBtn.setOnMouseExited(e -> {
            manageBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                    "-fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: 600; -fx-pref-width: 180;");
        });
        
        manageBtn.setOnAction(e -> {
            Helper.stopCamera();
            Stage stage = (Stage) manageBtn.getScene().getWindow();
            stage.setScene(ClassManagement.createManageClassScene(stage, group.getGroup_id()));
        });
        
        groupBox.getChildren().addAll(groupNameLabel, termLabel, attendanceBtn, manageBtn);
        
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