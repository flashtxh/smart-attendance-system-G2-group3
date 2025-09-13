package dev.att.smartattendance.app;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class App extends Application {

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
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(400);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(400);

        Button loginButton = new Button("Login");
        Label messageLabel = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().strip();
            String password = passwordField.getText().strip();

            if(!username.isEmpty() && !password.isEmpty()) {
                stage.setScene(createHomeScene(username));
            } else {
                messageLabel.setText("Please enter username and password");
            }
        });

        VBox layout = new VBox(10, usernameField, passwordField, loginButton, messageLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-font-size:15px");

        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }

    private Scene createHomeScene(String username) {
        Label welcomeLabel = new Label("Welcome " + username + "!");
        VBox layout = new VBox(10, welcomeLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-font-size:40px;");

        return new Scene(layout, getScreenWidth(), getScreenHeight());
    }



    public static void main(String[] args) {
        launch();
    }
}