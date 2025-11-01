package dev.att.smartattendance.model.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dev.att.smartattendance.util.DatabaseManager;

public class StudentDAO {

    public void insert_student(String student_id, String name, String email) {
        String sql = "INSERT INTO students (student_id, name, email) VALUES (?, ?, ?)";

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
        String sql = "SELECT * FROM students WHERE email = ?";
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
        String sql = "SELECT * FROM students";

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
            System.err.println("Failed to retrieve students: " + e.getMessage());
        }

        return students;
    }
    
    // FIXED: Now actually filters by group_id using a JOIN!
    public List<Student> get_students_by_group(String group_id) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.name, s.email " +
                     "FROM students s " +
                     "INNER JOIN student_group sg ON s.student_id = sg.student_id " +
                     "WHERE sg.group_id = ? " +
                     "ORDER BY s.name";

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
                    students.add(new Student(student_id, name, email));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve students by group: " + e.getMessage());
        }
        
        return students;
    }
}