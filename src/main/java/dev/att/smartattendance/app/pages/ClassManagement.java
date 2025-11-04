package dev.att.smartattendance.app.pages;

import java.util.ArrayList;
import java.util.HashMap;
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
        
        TextField courseSearchField = new TextField();
        courseSearchField.setPromptText("Search course by code or name...");
        courseSearchField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-pref-width: 400; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; " +
                "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        
        Label noResultsLabel = new Label("No courses found matching your search");
        noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 20; " +
                "-fx-font-style: italic;");
        noResultsLabel.setVisible(false);
        
        VBox courseResultsContainer = new VBox(8);
        courseResultsContainer.setStyle("-fx-background-color: #0f172a; -fx-padding: 10; " +
                "-fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; " +
                "-fx-border-radius: 8;");
        courseResultsContainer.getChildren().add(noResultsLabel);
        
        ScrollPane courseResultsScroll = new ScrollPane(courseResultsContainer);
        courseResultsScroll.setFitToWidth(true);
        courseResultsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        courseResultsScroll.setPrefHeight(250);
        courseResultsScroll.setMaxHeight(250);
        courseResultsScroll.setMinHeight(250);
        
        CourseDAO courseDAO = new CourseDAO();
        List<Course> allCourses = courseDAO.get_all_courses();
        allCourses.sort((c1, c2) -> c1.getCourse_code().compareToIgnoreCase(c2.getCourse_code()));
        
        final Course[] selectedCourse = {null};
        
        for (Course course : allCourses) {
            HBox resultRow = createCourseResultRow(course, courseSearchField, selectedCourse, courseLabel);
            courseResultsContainer.getChildren().add(resultRow);
        }
        
        Label courseInfoLabel = new Label("");
        courseInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #60a5fa; -fx-font-weight: 600; " +
                "-fx-padding: 10; -fx-background-color: rgba(59, 130, 246, 0.1); " +
                "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 1; -fx-border-radius: 8;");
        courseInfoLabel.setVisible(false);
        
        courseSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String searchText = newVal.toLowerCase().trim();
            courseResultsContainer.getChildren().clear();
            
            if (selectedCourse[0] != null && 
                newVal.equals(selectedCourse[0].getCourse_code() + " - " + selectedCourse[0].getCourse_name())) {
                courseInfoLabel.setText("Year: " + selectedCourse[0].getYear() + 
                                       "  |  Semester: " + selectedCourse[0].getSemester());
                courseInfoLabel.setVisible(true);
                // Change styling to show selection
                courseSearchField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-pref-width: 400; " +
                        "-fx-background-color: #0f172a; -fx-text-fill: #10b981; -fx-prompt-text-fill: #64748b; " +
                        "-fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 2; -fx-border-radius: 8; " +
                        "-fx-font-weight: bold;");
                return;
            } else {
                courseSearchField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-pref-width: 400; " +
                        "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; " +
                        "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            }
            
            if (searchText.isEmpty()) {
                noResultsLabel.setVisible(false);
                for (Course course : allCourses) {
                    HBox resultRow = createCourseResultRow(course, courseSearchField, selectedCourse, courseInfoLabel);
                    courseResultsContainer.getChildren().add(resultRow);
                }
                courseInfoLabel.setVisible(false);
                selectedCourse[0] = null;
                return;
            }
            
            boolean hasResults = false;
            for (Course course : allCourses) {
                boolean matches = course.getCourse_code().toLowerCase().contains(searchText) ||
                                course.getCourse_name().toLowerCase().contains(searchText);
                
                if (matches) {
                    hasResults = true;
                    HBox resultRow = createCourseResultRow(course, courseSearchField, selectedCourse, courseInfoLabel);
                    courseResultsContainer.getChildren().add(resultRow);
                }
            }
            
            if (!hasResults) {
                noResultsLabel.setVisible(true);
                courseResultsContainer.getChildren().add(noResultsLabel);
            } else {
                noResultsLabel.setVisible(false);
            }
            
            courseInfoLabel.setVisible(false);
            selectedCourse[0] = null;
        });
                
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
                
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);
        
        formContainer.getChildren().addAll(
            courseLabel, courseSearchField, courseResultsScroll, courseInfoLabel,
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
            String groupName = groupNameField.getText().trim();
            String selectedProfessor = professorComboBox.getValue();
            
            if (selectedCourse[0] == null || groupName.isEmpty() || selectedProfessor == null) {
                errorLabel.setText("Please fill in all fields");
                errorLabel.setVisible(true);
                return;
            }
            
            String professorId = professorMap.get(selectedProfessor);
                        
            stage.setScene(createStudentAssignmentScene(stage, selectedCourse[0], groupName, professorId));
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
    
    private static HBox createCourseResultRow(Course course, TextField searchField, 
            Course[] selectedCourse, Label infoLabel) {
        HBox resultRow = new HBox(10);
        resultRow.setAlignment(Pos.CENTER_LEFT);
        resultRow.setStyle("-fx-background-color: #1e293b; -fx-padding: 10; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        
        Label codeLabel = new Label(course.getCourse_code());
        codeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #60a5fa; -fx-font-weight: bold;");
        
        Label nameLabel = new Label(course.getCourse_name());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f1f5f9;");
        
        Label yearLabel = new Label(course.getYear() + " S" + course.getSemester());
        yearLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        resultRow.getChildren().addAll(codeLabel, nameLabel, spacer, yearLabel);
        
        resultRow.setOnMouseEntered(e -> {
            resultRow.setStyle("-fx-background-color: #334155; -fx-padding: 10; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
        });
        
        resultRow.setOnMouseExited(e -> {
            resultRow.setStyle("-fx-background-color: #1e293b; -fx-padding: 10; " +
                    "-fx-background-radius: 6; -fx-cursor: hand;");
        });
        
        resultRow.setOnMouseClicked(e -> {
            selectedCourse[0] = course;
            searchField.setText(course.getCourse_code() + " - " + course.getCourse_name());
            infoLabel.setText("Year: " + course.getYear() + "  |  Semester: " + course.getSemester());
            infoLabel.setVisible(true);
        });
        
        return resultRow;
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
        
        // Now uses GroupDAO method instead of inline SQL
        GroupDAO groupDAO = new GroupDAO();
        return groupDAO.create_class_with_students(
            groupId, 
            groupName, 
            course.getCourse_id(), 
            professorId, 
            course.getYear(), 
            "Term " + String.valueOf(course.getSemester()),
            studentIds
        );
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
        
        boolean isAdmin = Helper.loggedInUsername.equals("Admin");
        
        if (isAdmin) {
            Button backToCourseButton = new Button("← Back to Course");
            backToCourseButton.getStyleClass().add("back-button");
            backToCourseButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
            
            backToCourseButton.setOnAction(e -> {
                CourseDAO courseDAO = new CourseDAO();
                Course course = null;
                
                for (Course c : courseDAO.get_all_courses()) {
                    if (c.getCourse_id().equals(currentGroup.getcourse_code())) {
                        course = c;
                        break;
                    }
                }
                
                if (course != null) {
                    stage.setScene(Home.createCourseDetailScene(course, Helper.loggedInUsername, stage));
                } else {
                    stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
                }
            });
            
            HBox backToCourseBox = new HBox();
            backToCourseBox.setAlignment(Pos.CENTER_LEFT);
            backToCourseBox.getChildren().add(backToCourseButton);
            mainContainer.getChildren().add(backToCourseBox);
        }
      
        Label titleLabel = new Label("Manage Class: " + currentGroup.getGroup_name());
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);");
        
        CourseDAO courseDAO = new CourseDAO();
        Course course = null;
        
        for (Course c : courseDAO.get_all_courses()) {
            if (c.getCourse_id().equals(currentGroup.getcourse_code())) {
                course = c;
                break;
            }
        }

        Label courseInfoLabel;
        if (course != null) {
        courseInfoLabel = new Label("Course: " + course.getCourse_code() + " | " + 
                currentGroup.getAcademic_year() + " " + currentGroup.getTerm());
        } else {
            courseInfoLabel = new Label("Course: " + currentGroup.getcourse_code() + " | " + 
                currentGroup.getAcademic_year() + " " + currentGroup.getTerm());
        }
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
        GroupDAO groupDAO = new GroupDAO();
        return groupDAO.get_enrolled_student_ids(groupId);
    }
    
    private static boolean addStudentToGroup(String studentId, String groupId) {
        GroupDAO groupDAO = new GroupDAO();
        return groupDAO.add_student_to_group(studentId, groupId);
    }
    
    private static boolean removeStudentFromGroup(String studentId, String groupId) {
        GroupDAO groupDAO = new GroupDAO();
        return groupDAO.remove_student_from_group(studentId, groupId);
    }
        
    private static boolean deleteClass(String groupId) {
        GroupDAO groupDAO = new GroupDAO();
        return groupDAO.delete_class(groupId);
    }

    public static Scene createAddCourseScene(Stage stage) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Add New Course");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        
        Label subtitleLabel = new Label("Enter course information to create a new course");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #1e293b; -fx-padding: 40; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 20, 0, 0, 5);");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(600);
        
        // Course Code
        Label codeLabel = new Label("Course Code:");
        codeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField codeField = new TextField();
        codeField.setPromptText("e.g., CS101, MATH201");
        codeField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        codeField.setPrefWidth(500);
        
        // Course Name
        Label nameLabel = new Label("Course Name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Introduction to Computer Science");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        nameField.setPrefWidth(500);
        
        // Academic Year
        Label yearLabel = new Label("Academic Year:");
        yearLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        ComboBox<String> yearComboBox = new ComboBox<>();
        yearComboBox.setPromptText("Select year");
        yearComboBox.getItems().addAll("2024-2025", "2025-2026", "2026-2027", "2027-2028", "2028-2029");
        yearComboBox.setValue("2024-2025");
        yearComboBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 500; -fx-pref-height: 45; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-border-color: #3b82f6; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        // Semester
        Label semesterLabel = new Label("Semester:");
        semesterLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        ComboBox<Integer> semesterComboBox = new ComboBox<>();
        semesterComboBox.setPromptText("Select semester");
        semesterComboBox.getItems().addAll(1, 2, 3);
        semesterComboBox.setValue(1);
        semesterComboBox.setStyle("-fx-font-size: 14px; -fx-pref-width: 500; -fx-pref-height: 45; " +
                "-fx-background-color: #0f172a; -fx-text-fill: #f1f5f9; -fx-border-color: #3b82f6; " +
                "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);

        formContainer.getChildren().addAll(
            codeLabel, codeField,
            nameLabel, nameField,
            yearLabel, yearComboBox,
            semesterLabel, semesterComboBox,
            errorLabel
        );
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button createBtn = new Button("Create Course");
        createBtn.getStyleClass().add("enroll-student-button");
        createBtn.setPrefWidth(200);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("logout-button");
        cancelBtn.setPrefWidth(200);

        buttonBox.getChildren().addAll(createBtn, cancelBtn);
        
        createBtn.setOnAction(e -> {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String year = yearComboBox.getValue();
            Integer semester = semesterComboBox.getValue();

            if (code.isEmpty() || name.isEmpty()) {
                errorLabel.setText("Please fill in course code and name");
                errorLabel.setVisible(true);
                return;
            }
            
            if (year == null || semester == null) {
                errorLabel.setText("Please select year and semester");
                errorLabel.setVisible(true);
                return;
            }
            
            // Create new course
            String courseId = UUID.randomUUID().toString();
            CourseDAO courseDAO = new CourseDAO();
            
            try {
                courseDAO.insert_course(courseId, code, name, year, semester);
                CustomAlert.showSuccess("Course Created", 
                        "Course " + code + " - " + name + " has been created successfully!");
                stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
            } catch (Exception ex) {
                errorLabel.setText("Failed to create course. Please try again.");
                errorLabel.setVisible(true);
                System.err.println("Error creating course: " + ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> {
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

    public static Scene createNewClassForCourseScene(Stage stage, Course course) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
        
        Label titleLabel = new Label("Add Class to " + course.getCourse_code());
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
        
        // Course info (read-only display)
        Label courseInfoLabel = new Label("Course: " + course.getCourse_code() + " - " + course.getCourse_name());
        courseInfoLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #60a5fa; -fx-font-weight: 600; " +
                "-fx-padding: 10; -fx-background-color: rgba(59, 130, 246, 0.1); " +
                "-fx-background-radius: 8; -fx-border-color: #3b82f6; -fx-border-width: 1; -fx-border-radius: 8;");
        
        Label termInfoLabel = new Label("Year: " + course.getYear() + "  |  Semester: " + course.getSemester());
        termInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 0 10;");
        
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
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);
        
        formContainer.getChildren().addAll(
            courseInfoLabel, termInfoLabel,
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
            String groupName = groupNameField.getText().trim();
            String selectedProfessor = professorComboBox.getValue();
            
            if (groupName.isEmpty() || selectedProfessor == null) {
                errorLabel.setText("Please fill in all fields");
                errorLabel.setVisible(true);
                return;
            }
            
            String professorId = professorMap.get(selectedProfessor);
            
            stage.setScene(createStudentAssignmentScene(stage, course, groupName, professorId));
        });
        
        cancelButton.setOnAction(e -> {
            stage.setScene(Home.createCourseDetailScene(course, Helper.loggedInUsername, stage));
        });
        
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, formContainer, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");
        
        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(ClassManagement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }

    public static Scene createAddProfessorScene(Stage stage) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Add New Professor");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        
        Label subtitleLabel = new Label("Enter professor information to create a new account");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #1e293b; -fx-padding: 40; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 20, 0, 0, 5);");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(600);
        
        // Professor Name
        Label nameLabel = new Label("Professor Name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        nameField.setPrefWidth(500);
        
        // Professor Email
        Label emailLabel = new Label("Professor Email:");
        emailLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        emailField.setPrefWidth(500);
        
        // Password
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        passwordField.setPrefWidth(500);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);

        formContainer.getChildren().addAll(
            nameLabel, nameField,
            emailLabel, emailField,
            passwordLabel, passwordField,
            errorLabel
        );
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button createBtn = new Button("Create Professor");
        createBtn.getStyleClass().add("enroll-student-button");
        createBtn.setPrefWidth(200);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("logout-button");
        cancelBtn.setPrefWidth(200);

        buttonBox.getChildren().addAll(createBtn, cancelBtn);
        
        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please fill in all fields");
                errorLabel.setVisible(true);
                return;
            }
            
            if (!email.contains("@") || !email.contains(".")) {
                errorLabel.setText("Please enter a valid email address");
                errorLabel.setVisible(true);
                return;
            }
            
            if (isProfessorEmailExists(email)) {
                errorLabel.setText("Professor with this email already exists");
                errorLabel.setVisible(true);
                return;
            }
            
            String professorId = createNewProfessor(name, email, password);
            
            if (professorId == null) {
                errorLabel.setText("Failed to create professor account. Please try again.");
                errorLabel.setVisible(true);
                return;
            }
            
            // Reload credentials
            Login.initializeCredentials();
            
            CustomAlert.showSuccess("Professor Created", 
                    "Professor " + name + " has been created successfully!");
            stage.setScene(Home.createHomeScene(Helper.loggedInUsername));
        });

        cancelBtn.setOnAction(e -> {
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

    private static boolean isProfessorEmailExists(String email) {
        ProfessorDAO professorDAO = new ProfessorDAO();
        return professorDAO.email_exists(email);
    }

    private static String createNewProfessor(String name, String email, String password) {
        String professorId = UUID.randomUUID().toString();
        ProfessorDAO professorDAO = new ProfessorDAO();
        
        professorDAO.insert_professor(professorId, name, email, password);
        System.out.println("Professor created: " + name + " (" + email + ")");
        return professorId;
    }
}