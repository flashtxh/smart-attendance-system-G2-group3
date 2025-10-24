package dev.att.smartattendance.model.student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class StudentDAO {

    public void insert_student(String student_id, String name, String email) {
        String sql = "insert into students (student_id, name, email) values (?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, student_id);
            ps.setString(2, name);
            ps.setString(3, email);

            ps.executeUpdate();
            System.out.println("Student inserted successfully");
        } catch (SQLException e) {
            System.err.println("Failed to insert student: " + e.getMessage());
        }
    }

    public Student get_student_by_email(String email) {
        String sql = "select * from students where email = ?";
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {
                return new Student(
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("email")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public List<Student> get_all_students() {
        List<Student> students = new ArrayList<>();
        String sql = "select * from students";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while(rs.next()) {
                students.add(new Student(
                    rs.getString("student_id"),
                    rs.getString("name"), 
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retreive students: " + e.getMessage());
        }

        return students;
    }
    
    public List<String> get_students_by_group(String group_id) {
        List<String> students = new ArrayList<>();
        String sql = "select student_id, name, email from students where group_id = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, group_id);
            try(ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    String student_id = rs.getString("student_id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    students.add(student_id + " - " + name + " (" + email + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve students: " + e.getMessage());
        }
        
        return students;
    }

}
