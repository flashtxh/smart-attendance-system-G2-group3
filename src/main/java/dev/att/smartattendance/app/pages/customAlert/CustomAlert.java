package dev.att.smartattendance.app.pages.customAlert;

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
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);
        
        // Main container - matching your dark blue theme
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50, 60, 50, 60));
        
        String iconColor;
        String iconText;
        String buttonColor;
        String buttonHoverColor;
        String borderColor;
        String glowColor;
        
        switch (type) {
            case SUCCESS:
                iconText = "✓";
                iconColor = "#10b981"; // Green
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #10b981 0%, #059669 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #059669 0%, #047857 100%)";
                borderColor = "#10b981";
                glowColor = "rgba(16, 185, 129, 0.4)";
                break;
            case ERROR:
                iconText = "✕";
                iconColor = "#ef4444"; // Red
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #dc2626 0%, #991b1b 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #b91c1c 0%, #7f1d1d 100%)";
                borderColor = "#ef4444";
                glowColor = "rgba(239, 68, 68, 0.4)";
                break;
            case WARNING:
                iconText = "⚠";
                iconColor = "#f59e0b"; // Orange
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #f59e0b 0%, #d97706 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #d97706 0%, #b45309 100%)";
                borderColor = "#f59e0b";
                glowColor = "rgba(245, 158, 11, 0.4)";
                break;
            case INFO:
            default:
                iconText = "ℹ";
                iconColor = "#3b82f6"; // Blue
                buttonColor = "linear-gradient(from 0% 0% to 100% 100%, #3b82f6 0%, #2563eb 100%)";
                buttonHoverColor = "linear-gradient(from 0% 0% to 100% 100%, #2563eb 0%, #1d4ed8 100%)";
                borderColor = "#3b82f6";
                glowColor = "rgba(59, 130, 246, 0.4)";
                break;
        }
        
        // Container styling matching your theme
        container.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1e293b 0%, #0f172a 100%);" +
            "-fx-background-radius: 25;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 25;" +
            "-fx-effect: dropshadow(gaussian, " + glowColor + ", 35, 0.7, 0, 10);"
        );
        
        // Icon with circular background
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle(
            "-fx-background-color: " + iconColor + ";" +
            "-fx-background-radius: 50;" +
            "-fx-min-width: 90;" +
            "-fx-min-height: 90;" +
            "-fx-max-width: 90;" +
            "-fx-max-height: 90;" +
            "-fx-effect: dropshadow(gaussian, " + iconColor + ", 20, 0.6, 0, 5);"
        );
        
        Label iconLabel = new Label(iconText);
        iconLabel.setStyle(
            "-fx-font-size: 50px;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        iconContainer.getChildren().add(iconLabel);
        
        // Title label with gradient
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, #60a5fa 0%, #a78bfa 100%);" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.3), 8, 0, 0, 3);"
        );
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Message label
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(480);
        messageLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: #94a3b8;" +
            "-fx-text-alignment: center;" +
            "-fx-font-weight: 500;" +
            "-fx-line-spacing: 2;"
        );
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        
        // OK button matching your theme
        Button okButton = new Button("OK");
        okButton.setPrefWidth(160);
        okButton.setPrefHeight(55);
        okButton.setStyle(
            "-fx-background-color: " + buttonColor + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + glowColor + ", 18, 0.6, 0, 6);"
        );
        
        String finalButtonHoverColor = buttonHoverColor;
        String finalGlowColor = glowColor;
        
        okButton.setOnMouseEntered(e -> {
            okButton.setStyle(
                "-fx-background-color: " + finalButtonHoverColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.08;" +
                "-fx-scale-y: 1.08;" +
                "-fx-effect: dropshadow(gaussian, " + finalGlowColor + ", 28, 0.8, 0, 12);"
            );
        });
        
        okButton.setOnMouseExited(e -> {
            okButton.setStyle(
                "-fx-background-color: " + buttonColor + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 15;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + finalGlowColor + ", 18, 0.6, 0, 6);"
            );
        });
        
        okButton.setOnAction(e -> dialog.close());
        
        // Add all elements to container
        container.getChildren().addAll(iconContainer, titleLabel, messageLabel, okButton);
        
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