package dev.att.smartattendance.model.course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class CourseDAO {
    public void insert_course(String course_id, String course_code, String course_name, String year, int semester) {
        String sql = "INSERT INTO courses (course_id, course_code, course_name, year, semester) VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, course_id);
            ps.setString(2, course_code);
            ps.setString(3, course_name);
            ps.setString(4, year);        // Changed from setInt to setString
            ps.setInt(5, semester);

            ps.executeUpdate();
            System.out.println("Course inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert course: " + e.getMessage());
        }
    }

    public List<Course> get_all_courses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                courses.add(new Course(
                    rs.getString("course_id"),     // Added course_id
                    rs.getString("course_code"), 
                    rs.getString("course_name"),
                    rs.getString("year"),          // Changed from getInt to getString
                    rs.getInt("semester")
                ));
            }
        } catch (SQLException e) {  
            System.err.println("Failed to retrieve courses: " + e.getMessage());
        }

        return courses;
    }

    public String getCourseCodeById(String course_id) {
        String sql = "SELECT course_code FROM courses WHERE course_id = ?";
        String courseCode = null;

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, course_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    courseCode = rs.getString("course_code");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch course code: " + e.getMessage());
        }

        return courseCode != null ? courseCode : "Unknown";
    }

}