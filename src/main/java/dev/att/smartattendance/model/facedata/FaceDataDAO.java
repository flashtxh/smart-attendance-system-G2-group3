package dev.att.smartattendance.model.facedata;

import dev.att.smartattendance.util.DatabaseManager;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

public class FaceDataDAO {
    
    public void insert_face(String face_id, String student_id, String folder_path) {
        String sql = "insert into faces (face_id, student_id, folder_path) values (?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, face_id);
            ps.setString(2, student_id);
            ps.setString(3, folder_path);

            ps.executeUpdate();
            System.out.println("Face stored successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert face: " + e.getMessage());
        }
    }

    public List<Mat> get_faces_by_student_id(String student_id) {
        List<Mat> face_images = new ArrayList<>();
        String sql = "select folder_path from faces where student_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, student_id);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    String path = rs.getString("folder_path");
                    File folder = new File(path);

                    File[] files = folder.listFiles((dir, name) ->
                            name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

                    if (files != null) {
                        for (File imgFile : files) {
                            Mat img = Imgcodecs.imread(imgFile.getAbsolutePath());
                            if (!img.empty()) face_images.add(img);
                        }
                    }
                    
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retreive face data: " + e.getMessage());
        }

        return face_images;
    }
}
