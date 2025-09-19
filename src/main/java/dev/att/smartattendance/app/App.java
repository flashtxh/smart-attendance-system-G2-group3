package dev.att.smartattendance.app;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch();

        // For this to work, uncomment CropDemo and save some images first. Try saving around 8-10.
        // Afterwards, comment it out and uncomment RecognitionDemo to see if it works and detects.
        // After testing, you can delete the images from the person1 folder.
        // person2 folder contains an image of a random person in order for this code to work.
        
        // CropDemo.main(args);
        // RecognitionDemo.main(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Label label = new Label("Hello, JavaFX + Maven!");
        // Scene scene = new Scene(label, 400, 200);
        Scene loginScene = createLoginScene(primaryStage);

        primaryStage.setTitle("JavaFX App");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private double getScreenWidth() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getWidth() * 0.5;
    }

    private double getScreenHeight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        return screenBounds.getHeight() * 0.5;
    }

    private Scene createLoginScene(Stage stage) {
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
                messageLabel.setText("Please enter username and password");
            } else if (username.equals("admin") && password.equals("admin")) { // testing purposes
                stage.setScene(createHomeScene(username));
            } else {
                messageLabel.setText("Invalid username or password");
            }
        });


        VBox layout = new VBox(25, titleLabel, usernameField, passwordField, loginButton, messageLabel);
        layout.getStyleClass().add("vbox");

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());

        return scene;
    }



    private Scene createHomeScene(String username) {
        Label welcomeLabel = new Label("Welcome, " + username + "!");
        welcomeLabel.getStyleClass().add("welcome-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-logout");

        VBox layout = new VBox(20, welcomeLabel, logoutButton);
        layout.getStyleClass().add("home-layout");
        layout.setAlignment(Pos.CENTER);

        // When logout clicked, go back to login scene
        logoutButton.setOnAction(e -> {
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(createLoginScene(stage));
        });

        Scene scene = new Scene(layout, getScreenWidth(), getScreenHeight());
        scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());

        return scene;
    }

}