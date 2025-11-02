package dev.att.smartattendance.app.pages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.app.customAlert.CustomAlert;
import dev.att.smartattendance.model.professor.Professor;
import dev.att.smartattendance.model.professor.ProfessorDAO;
import dev.att.smartattendance.util.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TAManagement {
    
    /**
     * Scene for managing TAs assigned to a specific group
     */
    public static Scene createManageTAsScene(Stage stage, String groupId, String groupName) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(40));
        
        Label titleLabel = new Label("Manage TAs for " + groupName);
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);");
        
        Label subtitleLabel = new Label("Assign Teaching Assistants to this class");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        
        HBox contentBox = new HBox(30);
        contentBox.setAlignment(Pos.TOP_CENTER);
        
        // Assigned TAs column
        VBox assignedColumn = createTAColumn("Assigned TAs", groupId, true);
        
        // Available TAs column
        VBox availableColumn = createTAColumn("Available TAs", groupId, false);
        
        contentBox.getChildren().addAll(assignedColumn, availableColumn);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button addNewTAButton = new Button("➕ Add New TA");
        addNewTAButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; " +
                "-fx-cursor: hand; -fx-font-weight: bold;");
        
        Button backButton = new Button("Back");
        backButton.getStyleClass().add("back-button");
        backButton.setPrefWidth(180);
        
        buttonBox.getChildren().addAll(addNewTAButton, backButton);
        
        addNewTAButton.setOnAction(e -> {
            stage.setScene(createAddTAScene(stage, groupId, groupName));
        });
        
        backButton.setOnAction(e -> {
            stage.setScene(ClassManagement.createManageClassScene(stage, groupId));
        });
        
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, contentBox, buttonBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");
        
        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(TAManagement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
    
    /**
     * Create a column showing either assigned or available TAs
     */
    private static VBox createTAColumn(String title, String groupId, boolean showAssigned) {
        VBox column = new VBox(15);
        column.setStyle("-fx-background-color: #1e293b; -fx-padding: 25; " +
                "-fx-background-radius: 15; -fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 15; -fx-min-width: 450;");
        
        Label columnTitle = new Label(title);
        columnTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1f5f9; -fx-font-weight: bold;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search TAs...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        
        VBox taList = new VBox(10);
        taList.setStyle("-fx-padding: 10 0;");
        
        ProfessorDAO professorDAO = new ProfessorDAO();
        Set<String> assignedTAIds = getAssignedTAIds(groupId);
        List<Professor> allTAs = professorDAO.get_all_tas();
        
        List<Professor> displayTAs = new ArrayList<>();
        for (Professor ta : allTAs) {
            boolean isAssigned = assignedTAIds.contains(ta.getProfessor_id());
            if ((showAssigned && isAssigned) || (!showAssigned && !isAssigned)) {
                displayTAs.add(ta);
            }
        }
        
        for (Professor ta : displayTAs) {
            HBox taRow = createTARow(ta, groupId, showAssigned);
            taList.getChildren().add(taRow);
        }
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String searchText = newVal.toLowerCase().trim();
            taList.getChildren().clear();
            
            for (Professor ta : displayTAs) {
                boolean matches = ta.getUsername().toLowerCase().contains(searchText) ||
                                ta.getEmail().toLowerCase().contains(searchText);
                
                if (matches) {
                    HBox taRow = createTARow(ta, groupId, showAssigned);
                    taList.getChildren().add(taRow);
                }
            }
        });
        
        ScrollPane scrollPane = new ScrollPane(taList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setMaxHeight(500);
        
        Label countLabel = new Label(displayTAs.size() + " TAs");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");
        
        column.getChildren().addAll(columnTitle, searchField, countLabel, scrollPane);
        
        return column;
    }
    
    /**
     * Create a row for a single TA
     */
    private static HBox createTARow(Professor ta, String groupId, boolean isAssigned) {
        HBox taRow = new HBox(15);
        taRow.setAlignment(Pos.CENTER_LEFT);
        taRow.setStyle("-fx-background-color: #0f172a; -fx-padding: 12; " +
                "-fx-background-radius: 8; -fx-border-color: #475569; -fx-border-width: 1; " +
                "-fx-border-radius: 8;");
        
        VBox infoBox = new VBox(3);
        
        Label nameLabel = new Label(ta.getUsername());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        Label emailLabel = new Label(ta.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        infoBox.getChildren().addAll(nameLabel, emailLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button actionButton = new Button(isAssigned ? "Remove" : "Assign");
        actionButton.setStyle(
            "-fx-background-color: " + (isAssigned ? "#ef4444" : "#10b981") + "; " +
            "-fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 6 16; " +
            "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-weight: bold;"
        );
        
        actionButton.setOnAction(e -> {
            boolean success;
            if (isAssigned) {
                success = removeTAFromGroup(ta.getProfessor_id(), groupId);
                if (success) {
                    CustomAlert.showSuccess("TA Removed", 
                            ta.getUsername() + " has been removed from this class.");
                    Stage stage = (Stage) actionButton.getScene().getWindow();
                    String groupName = getGroupName(groupId);
                    stage.setScene(createManageTAsScene(stage, groupId, groupName));
                }
            } else {
                success = assignTAToGroup(ta.getProfessor_id(), groupId);
                if (success) {
                    CustomAlert.showSuccess("TA Assigned", 
                            ta.getUsername() + " has been assigned to this class.");
                    Stage stage = (Stage) actionButton.getScene().getWindow();
                    String groupName = getGroupName(groupId);
                    stage.setScene(createManageTAsScene(stage, groupId, groupName));
                }
            }
            
            if (!success) {
                CustomAlert.showError("Operation Failed", 
                        "Failed to update TA assignment. Please try again.");
            }
        });
        
        taRow.getChildren().addAll(infoBox, spacer, actionButton);
        
        return taRow;
    }
    
    /**
     * Scene for adding a new TA to the system
     */
    public static Scene createAddTAScene(Stage stage, String groupId, String groupName) {
        VBox mainContainer = new VBox(30);
        mainContainer.setStyle("-fx-background-color: #0f172a;");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        
        Label titleLabel = new Label("Add New Teaching Assistant");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        
        Label subtitleLabel = new Label("Enter TA information to create new account");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
        
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #1e293b; -fx-padding: 40; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 20, 0, 0, 5);");
        formContainer.setAlignment(Pos.CENTER_LEFT);
        formContainer.setMaxWidth(500);
        
        Label nameLabel = new Label("TA Name:");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        nameField.setPrefWidth(400);
        
        Label emailLabel = new Label("TA Email:");
        emailLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        emailField.setPrefWidth(400);
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9; -fx-font-weight: 600;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-color: #0f172a; " +
                "-fx-text-fill: #f1f5f9; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
        passwordField.setPrefWidth(400);
        
        CheckBox assignNowCheckbox = new CheckBox("Assign to " + groupName + " immediately");
        assignNowCheckbox.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        assignNowCheckbox.setSelected(true);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444; -fx-font-weight: 600;");
        errorLabel.setVisible(false);

        formContainer.getChildren().addAll(nameLabel, nameField, emailLabel, emailField, 
                                          passwordLabel, passwordField, assignNowCheckbox, errorLabel);
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button createBtn = new Button("Create TA");
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
            
            if (isTAEmailExists(email)) {
                errorLabel.setText("TA with this email already exists");
                errorLabel.setVisible(true);
                return;
            }
            
            String taId = createNewTA(name, email, password);
            
            if (taId == null) {
                errorLabel.setText("Failed to create TA account. Please try again.");
                errorLabel.setVisible(true);
                return;
            }
            
            // Assign to group if checkbox is selected
            if (assignNowCheckbox.isSelected()) {
                assignTAToGroup(taId, groupId);
            }
            
            // Reload credentials
            Login.initializeCredentials();
            
            CustomAlert.showSuccess("TA Created", 
                    "Teaching Assistant " + name + " has been created successfully!");
            stage.setScene(createManageTAsScene(stage, groupId, groupName));
        });

        cancelBtn.setOnAction(e -> {
            stage.setScene(createManageTAsScene(stage, groupId, groupName));
        });

        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, formContainer, buttonBox);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");

        Scene scene = new Scene(scrollPane, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(TAManagement.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
    
    // Database helper methods
    
    private static Set<String> getAssignedTAIds(String groupId) {
        Set<String> assignedIds = new HashSet<>();
        String sql = "SELECT ta_id FROM ta_assignments WHERE group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                assignedIds.add(rs.getString("ta_id"));
            }
        } catch (SQLException e) {
            System.err.println("Failed to get assigned TAs: " + e.getMessage());
        }
        
        return assignedIds;
    }
    
    private static boolean assignTAToGroup(String taId, String groupId) {
        String sql = "INSERT INTO ta_assignments (ta_id, group_id) VALUES (?, ?)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ps.setString(2, groupId);
            ps.executeUpdate();
            
            System.out.println("TA " + taId + " assigned to group " + groupId);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to assign TA to group: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean removeTAFromGroup(String taId, String groupId) {
        String sql = "DELETE FROM ta_assignments WHERE ta_id = ? AND group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ps.setString(2, groupId);
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("TA " + taId + " removed from group " + groupId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Failed to remove TA from group: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean isTAEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM professors WHERE email = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }
    
    private static String createNewTA(String name, String email, String password) {
        String taId = UUID.randomUUID().toString();
        String sql = "INSERT INTO professors (professor_id, username, email, password, is_ta) VALUES (?, ?, ?, ?, 1)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, taId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, password);
            
            ps.executeUpdate();
            System.out.println("TA created successfully: " + name + " (" + email + ") with ID: " + taId);
            return taId;
            
        } catch (SQLException e) {
            System.err.println("Failed to create TA: " + e.getMessage());
            return null;
        }
    }
    
    private static String getGroupName(String groupId) {
        String sql = "SELECT group_name FROM groups WHERE group_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getString("group_name");
            }
        } catch (SQLException e) {
            System.err.println("Error getting group name: " + e.getMessage());
        }
        return "Unknown Group";
    }
}