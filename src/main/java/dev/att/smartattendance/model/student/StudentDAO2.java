package dev.att.smartattendance.model.student;

import dev.att.smartattendance.util.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO2 {
    
    /**
     * Insert a new student into the database
     */
    public void insert_student(String student_id, String name, String email) {
        String sql = "INSERT INTO students (student_id, name, email) VALUES (?, ?, ?)";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, student_id);
            ps.setString(2, name);
            ps.setString(3, email);

            ps.executeUpdate();
            System.out.println("✅ Student inserted successfully: " + name);
        } catch (SQLException e) {
            System.err.println("❌ Failed to insert student: " + e.getMessage());
        }
    }

    /**
     * Get a student by their ID
     */
    public Student get_student_by_id(String student_id) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, student_id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get student by ID: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get a student by their email
     */
    public Student get_student_by_email(String email) {
        String sql = "SELECT * FROM students WHERE email = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get student by email: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all students
     */
    public List<Student> get_all_students() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY name";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                students.add(new Student(
                    rs.getString("student_id"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get all students: " + e.getMessage());
        }
        
        return students;
    }

    /**
     * Update student information
     */
    public void update_student(String student_id, String name, String email) {
        String sql = "UPDATE students SET name = ?, email = ? WHERE student_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, student_id);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Student updated successfully: " + name);
            } else {
                System.out.println("⚠️ No student found with ID: " + student_id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to update student: " + e.getMessage());
        }
    }

    /**
     * Delete a student
     */
    public void delete_student(String student_id) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, student_id);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Student deleted successfully");
            } else {
                System.out.println("⚠️ No student found with ID: " + student_id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to delete student: " + e.getMessage());
        }
    }

    /**
     * Get students by group
     */
    public List<Student> get_students_by_group(String group_id) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.* FROM students s " +
                    "INNER JOIN student_group sg ON s.student_id = sg.student_id " +
                    "WHERE sg.group_id = ? ORDER BY s.name";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, group_id);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to get students by group: " + e.getMessage());
        }
        
        return students;
    }

    /**
     * Check if student exists
     */
    public boolean student_exists(String student_id) {
        String sql = "SELECT COUNT(*) FROM students WHERE student_id = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, student_id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to check student existence: " + e.getMessage());
        }
        
        return false;
    }
}