// Add this new class in a new file: CustomAlert.java
package dev.att.smartattendance.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomAlert {
    
    public enum AlertType {
        SUCCESS, ERROR, WARNING, INFO
    }
    
    public static void show(String title, String message, AlertType type) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle(title);
        
        // Main container - Dark theme matching your CSS
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40, 50, 40, 50));
        container.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1e293b 0%, #0f172a 100%);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: #3b82f6;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 30, 0, 0, 10);"
        );
        
        // Icon label (emoji style)
        Label iconLabel = new Label();
        iconLabel.setStyle("-fx-font-size: 56px;");
        
        String iconColor;
        String buttonColor;
        String buttonHoverColor;
        String borderColor;
        
        switch (type) {
            case SUCCESS:
                iconLabel.setText("✓");
                iconColor = "#10b981"; // Green
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #10b981 0%, #059669 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #059669 0%, #047857 100%)";
                borderColor = "#10b981";
                break;
            case ERROR:
                iconLabel.setText("✕");
                iconColor = "#ef4444"; // Red
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #dc2626 0%, #991b1b 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #b91c1c 0%, #7f1d1d 100%)";
                borderColor = "#ef4444";
                break;
            case WARNING:
                iconLabel.setText("⚠");
                iconColor = "#f59e0b"; // Orange
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #f59e0b 0%, #d97706 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #d97706 0%, #b45309 100%)";
                borderColor = "#f59e0b";
                break;
            case INFO:
            default:
                iconLabel.setText("ℹ");
                iconColor = "#3b82f6"; // Blue
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #3b82f6 0%, #2563eb 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #2563eb 0%, #1d4ed8 100%)";
                borderColor = "#3b82f6";
                break;
        }
        
        // Update container border color based on type
        container.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1e293b 0%, #0f172a 100%);" +
            "-fx-background-radius: 20;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 30, 0, 0, 10);"
        );
        
        iconLabel.setStyle(
            "-fx-font-size: 56px;" +
            "-fx-text-fill: " + iconColor + ";" +
            "-fx-effect: dropshadow(gaussian, " + iconColor + ", 15, 0.5, 0, 0);"
        );
        
        // Title label - matching your theme
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 5, 0, 0, 2);"
        );
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Message label - matching your secondary text color
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(450);
        messageLabel.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-text-fill: #94a3b8;" +
            "-fx-text-alignment: center;" +
            "-fx-font-weight: 500;"
        );
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        
        // OK button - matching your button styles
        Button okButton = new Button("OK");
        okButton.setPrefWidth(140);
        okButton.setPrefHeight(50);
        okButton.setStyle(
            "-fx-background-color: " + buttonColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 15, 0, 0, 5);"
        );
        
        String finalButtonHoverColor = buttonHoverColor;
        okButton.setOnMouseEntered(e -> {
            okButton.setStyle(
                "-fx-background-color: " + finalButtonHoverColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.08;" +
                "-fx-scale-y: 1.08;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.8), 25, 0, 0, 10);"
            );
        });
        
        okButton.setOnMouseExited(e -> {
            okButton.setStyle(
                "-fx-background-color: " + buttonColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 15, 0, 0, 5);"
            );
        });
        
        okButton.setOnAction(e -> dialog.close());
        
        // Add all elements to container
        container.getChildren().addAll(iconLabel, titleLabel, messageLabel, okButton);
        
        // Create scene with transparent background
        Scene scene = new Scene(container);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    // Convenience methods
    public static void showSuccess(String title, String message) {
        show(title, message, AlertType.SUCCESS);
    }
    
    public static void showError(String title, String message) {
        show(title, message, AlertType.ERROR);
    }
    
    public static void showWarning(String title, String message) {
        show(title, message, AlertType.WARNING);
    }
    
    public static void showInfo(String title, String message) {
        show(title, message, AlertType.INFO);
    }
}