package dev.att.smartattendance.app.pages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.app.pages.customAlert.CustomAlert;
import dev.att.smartattendance.app.pages.customAlert.CustomConfirmDialog;
import dev.att.smartattendance.model.course.Course;
import dev.att.smartattendance.model.course.CourseDAO;
import dev.att.smartattendance.model.group.Group;
import dev.att.smartattendance.model.group.GroupDAO;
import dev.att.smartattendance.model.professor.Professor;
import dev.att.smartattendance.model.professor.ProfessorDAO;
import dev.att.smartattendance.model.student.Student;
import dev.att.smartattendance.model.student.StudentDAO;
import dev.att.smartattendance.util.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClassManagement {
        
    public static Scene createNewClassScene(Stage stage) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
                
        Label titleLabel = new Label("Create New Class");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; " +
                "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);");
        
        Label subtitleLabel = new Label("Configure class details and assign students");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
                
        VBox formContainer = new VBox(25);
        formContainer.setStyle("-fx-background-color: #1e293b; -fx-padding: 40; " +
                "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 20, 0, 0, 5);");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(800);
                
        Label courseLabel = new Label("Select Course:");
        courseLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Choose a course");
        courseComboBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 400; -fx-pref-height: 45; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-border-color: #3b82f6; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        CourseDAO courseDAO = new CourseDAO();
        Map<String, Course> courseMap = new HashMap<>();
        for (Course course : courseDAO.get_all_courses()) {
            String displayText = course.getCourse_code() + " - " + course.getCourse_name();
            courseComboBox.getItems().add(displayText);
            courseMap.put(displayText, course);
        }
                
        Label groupNameLabel = new Label("Group Name:");
        groupNameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField groupNameField = new TextField();
        groupNameField.setPromptText("e.g., G1, G2, Morning Class");
        groupNameField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-pref-width: 400; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; " +
                "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
                
        Label professorLabel = new Label("Assign Professor:");
        professorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        ComboBox<String> professorComboBox = new ComboBox<>();
        professorComboBox.setPromptText("Choose a professor");
        professorComboBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 400; -fx-pref-height: 45; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-border-color: #3b82f6; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        ProfessorDAO professorDAO = new ProfessorDAO();
        Map<String, String> professorMap = new HashMap<>();
        for (Professor prof : professorDAO.get_all_professors()) {
            if (!professorDAO.is_ta(prof.getEmail())) {
                String displayText = prof.getUsername() + " (" + prof.getEmail() + ")";
                professorComboBox.getItems().add(displayText);
                professorMap.put(displayText, prof.getProfessor_id());
            }
        }
        
        // Info label showing course details when selected
        Label courseInfoLabel = new Label("");
        courseInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #60a5fa; -fx-font-weight: 600; " +
                "-fx-padding: 10; -fx-background-color: rgba(59, 130, 246, 0.1); " +
                "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 1; -fx-border-radius: 8;");
        courseInfoLabel.setVisible(false);
        
        // Update info label when course is selected
        courseComboBox.setOnAction(e -> {
            String selected = courseComboBox.getValue();
            if (selected != null) {
                Course course = courseMap.get(selected);
                courseInfoLabel.setText("Year: " + course.getYear() + 
                                       "  |  Semester: " + course.getSemester());
                courseInfoLabel.setVisible(true);
            } else {
                courseInfoLabel.setVisible(false);
            }
        });
                
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);
        
        formContainer.getChildren().addAll(
            courseLabel, courseComboBox, courseInfoLabel,
            groupNameLabel, groupNameField,
            professorLabel, professorComboBox,
            errorLabel
        );
                
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button nextButton = new Button("Next: Assign Students");
        nextButton.getStyleClass().add("enroll-student-button");
        nextButton.setPrefWidth(220);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("logout-button");
        cancelButton.setPrefWidth(150);
        
        buttonBox.getChildren().addAll(nextButton, cancelButton);
                
        nextButton.setOnAction(e -> {
            String selectedCourse = courseComboBox.getValue();
            String groupName = groupNameField.getText().trim();
            String selectedProfessor = professorComboBox.getValue();
            
            if (selectedCourse == null || groupName.isEmpty() || selectedProfessor == null) {
                errorLabel.setText("Please fill in all fields");
                errorLabel.setVisible(true);
                return;
            }
            
            Course course = courseMap.get(selectedCourse);
            String professorId = professorMap.get(selectedProfessor);
                        
            stage.setScene(createStudentAssignmentScene(stage, course, groupName, professorId));
        });
        
        cancelButton.setOnAction(e -> {
            stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
        });
        
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, formContainer, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");
        
        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(ClassManagement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }

    public static Scene createStudentAssignmentScene(Stage stage, Course course, 
            String groupName, String professorId) {
        
        VBox mainContainer = new VBox(25);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
                
        Label titleLabel = new Label("Assign Students to " + groupName);
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);");
        
        Label courseInfoLabel = new Label("Course: " + course.getCourse_code() + " - " + 
                course.getCourse_name() + " | " + course.getYear() + " " + course.getSemester());
        courseInfoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
                
        VBox studentListContainer = new VBox(15);
        studentListContainer.setStyle("-fx-background-color: #1e293b; -fx-padding: 30; " +
                "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 15; -fx-max-width: 800;");
        
        Label studentListTitle = new Label("Select Students:");
        studentListTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
                
        TextField searchField = new TextField();
        searchField.setPromptText("Search students by name or email...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-pref-width: 740; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; " +
                "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        
        VBox studentCheckboxContainer = new VBox(12);
        studentCheckboxContainer.setStyle("-fx-padding: 15 0;");
                
        StudentDAO studentDAO = new StudentDAO();
        List<Student> allStudents = studentDAO.get_all_students();
        Map<String, CheckBox> studentCheckboxMap = new HashMap<>();
                
        for (Student student : allStudents) {
            if (student.getName().equalsIgnoreCase("Admin")) {
                continue;
            }
            
            HBox studentRow = new HBox(15);
            studentRow.setAlignment(Pos.CENTER_LEFT);
            studentRow.setStyle("-fx-background-color: #0f172a; -fx-padding: 12; " +
                    "-fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; " +
                    "-fx-border-radius: 8;");
            
            CheckBox checkBox = new CheckBox();
            checkBox.setStyle("-fx-font-size: 14px;");
            studentCheckboxMap.put(student.getStudent_id(), checkBox);
            
            Label nameLabel = new Label(student.getName());
            nameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
            
            Label emailLabel = new Label(student.getEmail());
            emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            studentRow.getChildren().addAll(checkBox, nameLabel, spacer, emailLabel);
            studentCheckboxContainer.getChildren().add(studentRow);
        }
                
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String searchText = newVal.toLowerCase().trim();
            studentCheckboxContainer.getChildren().clear();
            
            for (Student student : allStudents) {
                if (student.getName().equalsIgnoreCase("Admin")) {
                    continue;
                }
                
                boolean matches = student.getName().toLowerCase().contains(searchText) ||
                                student.getEmail().toLowerCase().contains(searchText);
                
                if (matches) {
                    HBox studentRow = new HBox(15);
                    studentRow.setAlignment(Pos.CENTER_LEFT);
                    studentRow.setStyle("-fx-background-color: #0f172a; -fx-padding: 12; " +
                            "-fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; " +
                            "-fx-border-radius: 8;");
                    
                    CheckBox checkBox = studentCheckboxMap.get(student.getStudent_id());
                    
                    Label nameLabel = new Label(student.getName());
                    nameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
                    
                    Label emailLabel = new Label(student.getEmail());
                    emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    studentRow.getChildren().addAll(checkBox, nameLabel, spacer, emailLabel);
                    studentCheckboxContainer.getChildren().add(studentRow);
                }
            }
        });
        
        ScrollPane studentScrollPane = new ScrollPane(studentCheckboxContainer);
        studentScrollPane.setFitToWidth(true);
        studentScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        studentScrollPane.setMaxHeight(400);
                
        HBox selectionControls = new HBox(15);
        selectionControls.setAlignment(Pos.CENTER_LEFT);
        
        Button selectAllBtn = new Button("Select All");
        selectAllBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        
        Button deselectAllBtn = new Button("Deselect All");
        deselectAllBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        
        Label selectedCountLabel = new Label("Selected: 0 students");
        selectedCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-weight: 600;");
        
        selectionControls.getChildren().addAll(selectAllBtn, deselectAllBtn, selectedCountLabel);
        
        selectAllBtn.setOnAction(e -> {
            studentCheckboxMap.values().forEach(cb -> cb.setSelected(true));
            selectedCountLabel.setText("Selected: " + studentCheckboxMap.size() + " students");
        });
        
        deselectAllBtn.setOnAction(e -> {
            studentCheckboxMap.values().forEach(cb -> cb.setSelected(false));
            selectedCountLabel.setText("Selected: 0 students");
        });
                
        studentCheckboxMap.values().forEach(cb -> {
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                long count = studentCheckboxMap.values().stream().filter(CheckBox::isSelected).count();
                selectedCountLabel.setText("Selected: " + count + " students");
            });
        });
        
        studentListContainer.getChildren().addAll(studentListTitle, searchField, 
                selectionControls, studentScrollPane);
                
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button createButton = new Button("Create Class");
        createButton.getStyleClass().add("enroll-student-button");
        createButton.setPrefWidth(180);
        
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("logout-button");
        backButton.setPrefWidth(150);
        
        buttonBox.getChildren().addAll(createButton, backButton);
        
        createButton.setOnAction(e -> {
            List<String> selectedStudentIds = new ArrayList<>();
            for (Map.Entry<String, CheckBox> entry : studentCheckboxMap.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedStudentIds.add(entry.getKey());
                }
            }
            
            if (selectedStudentIds.isEmpty()) {
                CustomAlert.showWarning("No Students Selected", 
                        "Please select at least one student for this class.");
                return;
            }
            
            boolean success = createClassWithStudents(course, groupName, professorId, selectedStudentIds);
            
            if (success) {
                CustomAlert.showSuccess("Class Created", 
                        "Class " + groupName + " has been created successfully with " + 
                        selectedStudentIds.size() + " students!");
                stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
            } else {
                CustomAlert.showError("Creation Failed", 
                        "Failed to create class. Please try again.");
            }
        });
        
        backButton.setOnAction(e -> {
            stage.setScene(createNewClassScene(stage));
        });
        
        mainContainer.getChildren().addAll(titleLabel, courseInfoLabel, studentListContainer, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");
        
        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(ClassManagement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
        
    private static boolean createClassWithStudents(Course course, String groupName, 
            String professorId, List<String> studentIds) {
        
        String groupId = UUID.randomUUID().toString();
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
                        
            String groupSql = "INSERT INTO groups (group_id, group_name, course_code, professor_id, academic_year, term) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(groupSql)) {
                ps.setString(1, groupId);
                ps.setString(2, groupName);
                ps.setString(3, course.getCourse_id());
                ps.setString(4, professorId);
                ps.setString(5, course.getYear());      // Changed from setInt to setString
                ps.setString(6, "Term "+String.valueOf(course.getSemester()));
                ps.executeUpdate();
            }
                        
            String studentGroupSql = "INSERT INTO student_group (student_id, group_id, enrollment_date) VALUES (?, ?, CURRENT_DATE)";
            try (PreparedStatement ps = conn.prepareStatement(studentGroupSql)) {
                for (String studentId : studentIds) {
                    ps.setString(1, studentId);
                    ps.setString(2, groupId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
            System.out.println("Class created successfully: " + groupName + " with " + studentIds.size() + " students");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to create class: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
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
        
    public static Scene createManageClassScene(Stage stage, String groupId) {
        GroupDAO groupDAO = new GroupDAO();
        Group group = null;
        
        for (Group g : groupDAO.get_all_groups()) {
            if (g.getGroup_id().equals(groupId)) {
                group = g;
                break;
            }
        }
        
        if (group == null) {
            CustomAlert.showError("Error", "Class not found!");
            return Home.createHomeScene(Helper.loggedInUsername);
        }
        
        final Group currentGroup = group;
        
        VBox mainContainer = new VBox(25);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
                
        Label titleLabel = new Label("Manage Class: " + currentGroup.getGroup_name());
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);");
        
        Label courseInfoLabel = new Label("Course: " + currentGroup.getcourse_code() + " | " + 
                currentGroup.getAcademic_year() + " " + currentGroup.getTerm());
        courseInfoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
                
        HBox contentBox = new HBox(30);
        contentBox.setAlignment(Pos.TOP_CENTER);
                
        VBox enrolledColumn = createStudentColumn("Enrolled Students", groupId, true);
                
        VBox availableColumn = createStudentColumn("Available Students", groupId, false);
        
        contentBox.getChildren().addAll(enrolledColumn, availableColumn);
                
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button deleteClassButton = new Button("🗑 Delete Class");
        deleteClassButton.setStyle("-fx-background-color: #ef6060; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
        
        deleteClassButton.setOnMouseEntered(e -> {
            deleteClassButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: bold; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });
        
        deleteClassButton.setOnMouseExited(e -> {
            deleteClassButton.setStyle("-fx-background-color: #ef6060; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: bold;");
        });

        Button manageTAsButton = new Button("👥 Manage TAs");
        manageTAsButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");

        manageTAsButton.setOnMouseEntered(e -> {
            manageTAsButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: bold; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        manageTAsButton.setOnMouseExited(e -> {
            manageTAsButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                    "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                    "-fx-cursor: hand; -fx-font-weight: bold;");
        });

        manageTAsButton.setOnAction(e -> {
            Helper.stopCamera();
            stage.setScene(TAManagement.createManageTAsScene(stage, groupId, currentGroup.getGroup_name()));
        });

        Button backButton = new Button("Back to Home");
        backButton.getStyleClass().add("back-button");
        backButton.setPrefWidth(180);
        
        buttonBox.getChildren().addAll(manageTAsButton, deleteClassButton, backButton);
        
        deleteClassButton.setOnAction(e -> {
            boolean confirmed = CustomConfirmDialog.showDeleteClassConfirmation(
                currentGroup.getGroup_name(), 
                currentGroup.getcourse_code()
            );
            
            if (confirmed) {
                boolean success = deleteClass(groupId);
                if (success) {
                    CustomAlert.showSuccess("Class Deleted", 
                            "Class " + currentGroup.getGroup_name() + " has been permanently deleted.");
                    stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
                } else {
                    CustomAlert.showError("Deletion Failed", 
                            "Failed to delete the class. Please try again.");
                }
            }
        });
        
        backButton.setOnAction(e -> {
            stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
        });
        
        mainContainer.getChildren().addAll(titleLabel, courseInfoLabel, contentBox, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");
        
        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(ClassManagement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
    
    private static VBox createStudentColumn(String title, String groupId, boolean showEnrolled) {
        VBox column = new VBox(15);
        column.setStyle("-fx-background-color: #1e293b; -fx-padding: 25; " +
                "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 15; -fx-min-width: 450;");
        
        Label columnTitle = new Label(title);
        columnTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search students...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        
        VBox studentList = new VBox(10);
        studentList.setStyle("-fx-padding: 10 0;");
        
        StudentDAO studentDAO = new StudentDAO();
        Set<String> enrolledStudentIds = getEnrolledStudentIds(groupId);
        List<Student> allStudents = studentDAO.get_all_students();
        
        List<Student> displayStudents = new ArrayList<>();
        for (Student student : allStudents) {
            if (student.getName().equalsIgnoreCase("Admin")) continue;
            
            boolean isEnrolled = enrolledStudentIds.contains(student.getStudent_id());
            if ((showEnrolled && isEnrolled) || (!showEnrolled && !isEnrolled)) {
                displayStudents.add(student);
            }
        }
        
        for (Student student : displayStudents) {
            HBox studentRow = createStudentRow(student, groupId, showEnrolled);
            studentList.getChildren().add(studentRow);
        }
                
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String searchText = newVal.toLowerCase().trim();
            studentList.getChildren().clear();
            
            for (Student student : displayStudents) {
                boolean matches = student.getName().toLowerCase().contains(searchText) ||
                                student.getEmail().toLowerCase().contains(searchText);
                
                if (matches) {
                    HBox studentRow = createStudentRow(student, groupId, showEnrolled);
                    studentList.getChildren().add(studentRow);
                }
            }
        });
        
        ScrollPane scrollPane = new ScrollPane(studentList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setMaxHeight(500);
        
        Label countLabel = new Label(displayStudents.size() + " students");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
        
        column.getChildren().addAll(columnTitle, searchField, countLabel, scrollPane);
        
        return column;
    }
    
    private static HBox createStudentRow(Student student, String groupId, boolean isEnrolled) {
        HBox studentRow = new HBox(15);
        studentRow.setAlignment(Pos.CENTER_LEFT);
        studentRow.setStyle("-fx-background-color: #0f172a; -fx-padding: 12; " +
                "-fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; " +
                "-fx-border-radius: 8;");
        
        VBox infoBox = new VBox(3);
        
        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        Label emailLabel = new Label(student.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        infoBox.getChildren().addAll(nameLabel, emailLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button actionButton = new Button(isEnrolled ? "Remove" : "Add");
        actionButton.setStyle(
            "-fx-background-color: " + (isEnrolled ? "#ef4444" : "#10b981") + "; " +
            "-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 16; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        
        actionButton.setOnAction(e -> {
            boolean success;
            if (isEnrolled) {
                success = removeStudentFromGroup(student.getStudent_id(), groupId);
                if (success) {
                    CustomAlert.showSuccess("Student Removed", 
                            student.getName() + " has been removed from the class.");
                    Stage stage = (Stage) actionButton.getScene().getWindow();
                    stage.setScene(createManageClassScene(stage, groupId));
                }
            } else {
                success = addStudentToGroup(student.getStudent_id(), groupId);
                if (success) {
                    CustomAlert.showSuccess("Student Added", 
                            student.getName() + " has been added to the class.");
                    Stage stage = (Stage) actionButton.getScene().getWindow();
                    stage.setScene(createManageClassScene(stage, groupId));
                }
            }
            
            if (!success) {
                CustomAlert.showError("Operation Failed", 
                        "Failed to update student enrollment. Please try again.");
            }
        });
        
        studentRow.getChildren().addAll(infoBox, spacer, actionButton);
        
        return studentRow;
    }
    
    private static Set<String> getEnrolledStudentIds(String groupId) {
        Set<String> enrolledIds = new HashSet<>();
        String sql = "SELECT student_id FROM student_group WHERE group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                enrolledIds.add(rs.getString("student_id"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get enrolled students: " + e.getMessage());
        }
        
        return enrolledIds;
    }
    
    private static boolean addStudentToGroup(String studentId, String groupId) {
        String sql = "INSERT INTO student_group (student_id, group_id, enrollment_date) VALUES (?, ?, CURRENT_DATE)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, studentId);
            ps.setString(2, groupId);
            ps.executeUpdate();
            
            System.out.println("Student " + studentId + " added to group " + groupId + " with enrollment date");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to add student to group: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean removeStudentFromGroup(String studentId, String groupId) {
        String sql = "DELETE FROM student_group WHERE student_id = ? AND group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, studentId);
            ps.setString(2, groupId);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Student " + studentId + " removed from group " + groupId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to remove student from group: " + e.getMessage());
            return false;
        }
    }
        
    private static boolean deleteClass(String groupId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
                        
            String deleteAttendanceSql = "DELETE FROM attendanceRecords WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteAttendanceSql)) {
                ps.setString(1, groupId);
                int attendanceDeleted = ps.executeUpdate();
                System.out.println("Deleted " + attendanceDeleted + " attendance records");
            }
                        
            String deleteSessionsSql = "DELETE FROM attendanceSessions WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSessionsSql)) {
                ps.setString(1, groupId);
                int sessionsDeleted = ps.executeUpdate();
                System.out.println("Deleted " + sessionsDeleted + " attendance sessions");
            }
                        
            String deleteEnrollmentsSql = "DELETE FROM student_group WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteEnrollmentsSql)) {
                ps.setString(1, groupId);
                int enrollmentsDeleted = ps.executeUpdate();
                System.out.println("Deleted " + enrollmentsDeleted + " student enrollments");
            }
                        
            String deleteGroupSql = "DELETE FROM groups WHERE group_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteGroupSql)) {
                ps.setString(1, groupId);
                int groupDeleted = ps.executeUpdate();
                System.out.println("Deleted group: " + (groupDeleted > 0 ? "Success" : "Failed"));
            }
            
            conn.commit();
            System.out.println("Class deleted successfully: " + groupId);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to delete class: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
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
}