package dev.att.smartattendance.app.customAlert;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CustomConfirmDialog {
    
    private boolean confirmed = false;
    
    public static boolean show(String title, String message, String details) {
        CustomConfirmDialog dialog = new CustomConfirmDialog();
        return dialog.showDialog(title, message, details);
    }
    
    private boolean showDialog(String title, String message, String details) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setTitle(title);
        
        // Main container
        VBox container = new VBox(25);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40, 50, 40, 50));
        container.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1e293b 0%, #0f172a 100%);" +
            "-fx-background-radius: 25;" +
            "-fx-border-color: #ef4444;" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 25;" +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 35, 0.7, 0, 10);"
        );
        
        // Warning icon with animation effect
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle(
            "-fx-background-color: #ef4444;" +
            "-fx-background-radius: 50;" +
            "-fx-min-width: 90;" +
            "-fx-min-height: 90;" +
            "-fx-max-width: 90;" +
            "-fx-max-height: 90;" +
            "-fx-effect: dropshadow(gaussian, #ef4444, 20, 0.6, 0, 5);"
        );
        
        Label iconLabel = new Label("⚠");
        iconLabel.setStyle(
            "-fx-font-size: 50px;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;"
        );
        iconContainer.getChildren().add(iconLabel);
        
        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ef4444;" +
            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.3), 8, 0, 0, 3);"
        );
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Message label
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(500);
        messageLabel.setStyle(
            "-fx-font-size: 17px;" +
            "-fx-text-fill: #f1f5f9;" +
            "-fx-text-alignment: center;" +
            "-fx-font-weight: 600;" +
            "-fx-line-spacing: 2;"
        );
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Details box with warning background
        VBox detailsBox = new VBox(12);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPadding(new Insets(20, 25, 20, 25));
        detailsBox.setMaxWidth(500);
        detailsBox.setStyle(
            "-fx-background-color: rgba(239, 68, 68, 0.1);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #ef4444;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 12;"
        );
        
        Label detailsTitle = new Label("This action will:");
        detailsTitle.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-text-fill: #fca5a5;" +
            "-fx-font-weight: bold;"
        );
        
        String[] detailLines = details.split("\n");
        VBox detailsList = new VBox(8);
        for (String line : detailLines) {
            if (!line.trim().isEmpty()) {
                HBox detailItem = new HBox(10);
                detailItem.setAlignment(Pos.CENTER_LEFT);
                
                Label bullet = new Label("•");
                bullet.setStyle(
                    "-fx-font-size: 16px;" +
                    "-fx-text-fill: #ef4444;" +
                    "-fx-font-weight: bold;"
                );
                
                Label detailText = new Label(line.trim().replaceAll("^[•\\-]\\s*", ""));
                detailText.setWrapText(true);
                detailText.setMaxWidth(430);
                detailText.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #cbd5e1;" +
                    "-fx-line-spacing: 1.5;"
                );
                
                detailItem.getChildren().addAll(bullet, detailText);
                detailsList.getChildren().add(detailItem);
            }
        }
        
        detailsBox.getChildren().addAll(detailsTitle, detailsList);
        
        // Warning message
        Label warningLabel = new Label("⚠ This action CANNOT be undone!");
        warningLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: #fca5a5;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 15 20;" +
            "-fx-background-color: rgba(239, 68, 68, 0.15);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #ef4444;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;"
        );
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        // Cancel button (primary action)
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(180);
        cancelButton.setPrefHeight(50);
        cancelButton.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6 0%, #2563eb 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 15, 0.6, 0, 5);"
        );
        
        cancelButton.setOnMouseEntered(e -> {
            cancelButton.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2563eb 0%, #1d4ed8 100%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 20, 0.8, 0, 8);"
            );
        });
        
        cancelButton.setOnMouseExited(e -> {
            cancelButton.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #3b82f6 0%, #2563eb 100%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 15, 0.6, 0, 5);"
            );
        });
        
        // Delete button (destructive action)
        Button deleteButton = new Button("Delete Class");
        deleteButton.setPrefWidth(180);
        deleteButton.setPrefHeight(50);
        deleteButton.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #dc2626 0%, #991b1b 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.4), 15, 0.6, 0, 5);"
        );
        
        deleteButton.setOnMouseEntered(e -> {
            deleteButton.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #b91c1c 0%, #7f1d1d 100%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;" +
                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.6), 20, 0.8, 0, 8);"
            );
        });
        
        deleteButton.setOnMouseExited(e -> {
            deleteButton.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #dc2626 0%, #991b1b 100%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(220, 38, 38, 0.4), 15, 0.6, 0, 5);"
            );
        });
        
        cancelButton.setOnAction(e -> {
            confirmed = false;
            dialog.close();
        });
        
        deleteButton.setOnAction(e -> {
            confirmed = true;
            dialog.close();
        });
        
        buttonBox.getChildren().addAll(cancelButton, deleteButton);
        
        // Add all elements to container
        container.getChildren().addAll(
            iconContainer, 
            titleLabel, 
            messageLabel, 
            detailsBox, 
            warningLabel,
            buttonBox
        );
        
        // Create scene with transparent background
        Scene scene = new Scene(container);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        dialog.setScene(scene);
        dialog.showAndWait();
        
        return confirmed;
    }
    
    // Convenience method specifically for class deletion
    public static boolean showDeleteClassConfirmation(String className, String courseCode) {
        String title = "Delete Class?";
        String message = "Are you sure you want to permanently delete:\n" + 
                        className + " (" + courseCode + ")";
        String details;
        details = """
                  Remove all student enrollments from this class
                  Delete all attendance records for this class
                  Permanently delete the class from the system""";
        
        return show(title, message, details);
    }
}