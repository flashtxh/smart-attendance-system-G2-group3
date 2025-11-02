package dev.att.smartattendance.model.studentGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class StudentGroupDAO {
    
    // Convenience method that auto-sets today's date
    public void insert_student_group(String student_id, String group_id) {
        insert_student_group(student_id, group_id, LocalDate.now().toString());
    }

    // Full method with enrollment_date parameter
    public void insert_student_group(String student_id, String group_id, String enrollment_date) {
        String sql = "INSERT INTO student_group (student_id, group_id, enrollment_date) VALUES (?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, student_id);
            ps.setString(2, group_id);
            ps.setString(3, enrollment_date);

            ps.executeUpdate();
            System.out.println("Student group inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert student group: " + e.getMessage());
        }
    }

    public List<StudentGroup> get_all_student_groups() {
        List<StudentGroup> student_groups = new ArrayList<>();
        String sql = "SELECT * FROM student_group";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                student_groups.add(new StudentGroup(
                    rs.getString("student_id"), 
                    rs.getString("group_id"),
                    rs.getString("enrollment_date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve student_groups: " + e.getMessage());
        }

        return student_groups;
    }

    public List<StudentGroup> get_students_in_group(String group_id) {
        List<StudentGroup> student_groups = new ArrayList<>();
        String sql = "SELECT * FROM student_group WHERE group_id = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, group_id);
            
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    student_groups.add(new StudentGroup(
                        rs.getString("student_id"), 
                        rs.getString("group_id"),
                        rs.getString("enrollment_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve students in group: " + e.getMessage());
        }

        return student_groups;
    }
}