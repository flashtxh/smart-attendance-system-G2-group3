package dev.att.smartattendance.app.pages;

import dev.att.smartattendance.app.Helper;
import dev.att.smartattendance.model.professor.Professor;
import dev.att.smartattendance.model.professor.ProfessorDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Login {

    public static void initializeCredentials() {
        Helper.userCredentials.clear();
        Helper.userCredentials.put("Admin", "admin");

        ProfessorDAO professorDAO = new ProfessorDAO();
        
        try {
            for (Professor prof : professorDAO.get_all_professors()) {
                Helper.userCredentials.put(prof.getEmail(), prof.getPassword());
            }
            System.out.println("Initialized " + Helper.userCredentials.size() + " user accounts (including professors)");
        } catch (Exception e) {
            System.err.println("Error loading professor credentials: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Initialized " + Helper.userCredentials.size() + " user accounts");
    }

    public static Scene createLoginScene(Stage stage) {
        // Main container with gradient background
        VBox mainContainer = new VBox();
        mainContainer.getStyleClass().add("login-container");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");
        
        // Login box (white card)
        VBox loginBox = new VBox(20);
        loginBox.getStyleClass().add("login-box");
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(50, 60, 50, 60));
        loginBox.setMaxWidth(450);
        loginBox.setStyle("-fx-background-color: white; " +
                         "-fx-background-radius: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 30, 0, 0, 10);");

        // Title
        Label titleLabel = new Label("Smart Attendance");
        titleLabel.getStyleClass().add("login-title");
        titleLabel.setStyle("-fx-font-size: 32px; " +
                           "-fx-font-weight: bold; " +
                           "-fx-text-fill: #1f2937;");

        // Subtitle
        Label subtitleLabel = new Label("Sign in to your account");
        subtitleLabel.getStyleClass().add("login-subtitle");
        subtitleLabel.setStyle("-fx-font-size: 14px; " +
                              "-fx-text-fill: #6b7280; " +
                              "-fx-padding: 0 0 10 0;");

        // Username field
        TextField usernameField = new TextField();
        usernameField.setPromptText("Email or Username");
        usernameField.getStyleClass().add("login-field");
        usernameField.setStyle("-fx-background-color: #f9fafb; " +
                              "-fx-border-color: #e5e7eb; " +
                              "-fx-border-width: 2; " +
                              "-fx-border-radius: 10; " +
                              "-fx-background-radius: 10; " +
                              "-fx-padding: 15 20; " +
                              "-fx-font-size: 14px; " +
                              "-fx-pref-width: 330; " +
                              "-fx-min-height: 50;");
        
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.setStyle("-fx-background-color: white; " +
                                      "-fx-border-color: #667eea; " +
                                      "-fx-border-width: 2; " +
                                      "-fx-border-radius: 10; " +
                                      "-fx-background-radius: 10; " +
                                      "-fx-padding: 15 20; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-pref-width: 330; " +
                                      "-fx-min-height: 50; " +
                                      "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.2), 8, 0, 0, 0);");
            } else {
                usernameField.setStyle("-fx-background-color: #f9fafb; " +
                                      "-fx-border-color: #e5e7eb; " +
                                      "-fx-border-width: 2; " +
                                      "-fx-border-radius: 10; " +
                                      "-fx-background-radius: 10; " +
                                      "-fx-padding: 15 20; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-pref-width: 330; " +
                                      "-fx-min-height: 50;");
            }
        });

        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-field");
        passwordField.setStyle("-fx-background-color: #f9fafb; " +
                              "-fx-border-color: #e5e7eb; " +
                              "-fx-border-width: 2; " +
                              "-fx-border-radius: 10; " +
                              "-fx-background-radius: 10; " +
                              "-fx-padding: 15 20; " +
                              "-fx-font-size: 14px; " +
                              "-fx-pref-width: 330; " +
                              "-fx-min-height: 50;");
        
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle("-fx-background-color: white; " +
                                      "-fx-border-color: #667eea; " +
                                      "-fx-border-width: 2; " +
                                      "-fx-border-radius: 10; " +
                                      "-fx-background-radius: 10; " +
                                      "-fx-padding: 15 20; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-pref-width: 330; " +
                                      "-fx-min-height: 50; " +
                                      "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.2), 8, 0, 0, 0);");
            } else {
                passwordField.setStyle("-fx-background-color: #f9fafb; " +
                                      "-fx-border-color: #e5e7eb; " +
                                      "-fx-border-width: 2; " +
                                      "-fx-border-radius: 10; " +
                                      "-fx-background-radius: 10; " +
                                      "-fx-padding: 15 20; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-pref-width: 330; " +
                                      "-fx-min-height: 50;");
            }
        });

        // Error message label
        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("login-error");
        messageLabel.setStyle("-fx-text-fill: #ef4444; " +
                             "-fx-font-size: 13px; " +
                             "-fx-padding: 0;");
        messageLabel.setVisible(false);

        // Login button
        Button loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("login-button");
        loginButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 15 40; " +
                            "-fx-background-radius: 10; " +
                            "-fx-cursor: hand; " +
                            "-fx-pref-width: 330; " +
                            "-fx-min-height: 50; " +
                            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 15, 0, 0, 5);");
        
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #5568d3 0%, #6b3f8f 100%); " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 15 40; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand; " +
                                "-fx-pref-width: 330; " +
                                "-fx-min-height: 50; " +
                                "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.6), 20, 0, 0, 8); " +
                                "-fx-scale-x: 1.02; " +
                                "-fx-scale-y: 1.02;");
        });
        
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%); " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 15 40; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand; " +
                                "-fx-pref-width: 330; " +
                                "-fx-min-height: 50; " +
                                "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.4), 15, 0, 0, 5);");
        });

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().strip();
            String password = passwordField.getText().strip();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("⚠ Please enter both username and password");
                messageLabel.setVisible(true);
                return;
            }

            if (Helper.userCredentials.containsKey(username) &&
                    Helper.userCredentials.get(username).equals(password)) {
                Helper.loggedInUsername = username;
                Helper.faceVerified = true;
                stage.setScene(Home.createHomeScene(username));
            } else {
                messageLabel.setText("⚠ Invalid username or password");
                messageLabel.setVisible(true);
                
                // Shake animation effect
                usernameField.setStyle(usernameField.getStyle() + "-fx-border-color: #ef4444;");
                passwordField.setStyle(passwordField.getStyle() + "-fx-border-color: #ef4444;");
            }
        });

        // Assemble login box
        loginBox.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            usernameField,
            passwordField,
            messageLabel,
            loginButton
        );

        mainContainer.getChildren().add(loginBox);

        Scene scene = new Scene(mainContainer, Helper.getScreenWidth(), Helper.getScreenHeight());
        scene.getStylesheets().add(Login.class.getResource("/css/styles.css").toExternalForm());
        
        return scene;
    }
}