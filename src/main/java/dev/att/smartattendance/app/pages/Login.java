package dev.att.smartattendance.app.pages;

import java.util.HashMap;
import java.util.Map;

import dev.att.smartattendance.app.Helper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Login {


    public static void initializeCredentials() {
        // sample credentials - in production, load from database
        Helper.userCredentials.put("Admin", "admin");

        // add more users as needed
        System.out.println("Initialized " + Helper.userCredentials.size() + " user accounts");
    }

    public static Scene createLoginScene(Stage stage) {
        Label titleLabel = new Label("Smart Attendance Login");
        titleLabel.getStyleClass().add("title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("text-field");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("password-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("error");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().strip();
            String password = passwordField.getText().strip();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password");
                return;
            }

            // check credentials
            if (Helper.userCredentials.containsKey(username) &&
                    Helper.userCredentials.get(username).equals(password)) {

                // check if user has face data enrolled
                // if (!personHistograms.containsKey(username)) {
                // messageLabel.setText("No face data found. Please enroll your face first.");
                // // redirect to enrollment
                // stage.setScene(createEnrollmentScene(stage, username));
                // } else {
                // // proceed to face verification
                // loggedInUsername = username;
                // faceVerified = true;
                // stage.setScene(createVerificationScene(stage, username));
                Helper.loggedInUsername = username;
                Helper.faceVerified = true;
                stage.setScene(Home.createHomeScene(username));
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });

        VBox layout = new VBox(25, titleLabel, usernameField, passwordField, loginButton,
                messageLabel);
        layout.getStyleClass().add("vbox");

        Scene scene = new Scene(layout, Helper.getScreenWidth(), Helper.getScreenHeight());
        // scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        return scene;
    }
}
