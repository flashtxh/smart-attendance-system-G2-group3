package dev.att.smartattendance.model.student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    public List<Student> get_students_by_group(String group_id) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.student_id, s.name, s.email " +
                     "FROM students s " +
                     "INNER JOIN student_group sg ON s.student_id = sg.student_id " +
                     "WHERE sg.group_id = ? " +
                     "ORDER BY LOWER(s.name)";

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

    /**
     * Check if student email already exists
     */
    public boolean email_exists(String email) {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Assign student to multiple groups
     */
    public boolean assign_student_to_groups(String studentId, List<String> groupIds) {
        if (groupIds.isEmpty()) {
            return true;
        }
        
        String sql = "INSERT INTO student_group (student_id, group_id, enrollment_date) " +
                     "VALUES (?, ?, CURRENT_DATE)";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            for (String groupId : groupIds) {
                ps.setString(1, studentId);
                ps.setString(2, groupId);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("Student assigned to " + groupIds.size() + " groups");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to assign student to groups: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save attendance for a group
     */
    public boolean save_attendance(String groupId, Map<String, Boolean> attendanceMap) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            
            // Check if session exists
            String checkSessionSql = "SELECT COUNT(*) FROM attendanceSessions WHERE group_id = ? AND date = ?";
            boolean sessionExists = false;
            
            try (PreparedStatement checkPs = conn.prepareStatement(checkSessionSql)) {
                checkPs.setString(1, groupId);
                checkPs.setString(2, currentDate);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    sessionExists = true;
                }
            }
            
            // Create session if doesn't exist
            if (!sessionExists) {
                String sessionSql = "INSERT INTO attendanceSessions (group_id, date, start_time, end_time, session_type) " +
                                "VALUES (?, ?, NULL, NULL, NULL)";
                
                try (PreparedStatement sessionPs = conn.prepareStatement(sessionSql)) {
                    sessionPs.setString(1, groupId);
                    sessionPs.setString(2, currentDate);
                    sessionPs.executeUpdate();
                    System.out.println("Created new attendance session for " + groupId + " on " + currentDate);
                }
            }
            
            // Upsert attendance records
            String upsertRecordSql = "INSERT INTO attendanceRecords (group_id, date, student_id, status) " +
                                    "VALUES (?, ?, ?, ?) " +
                                    "ON CONFLICT(group_id, date, student_id) " +
                                    "DO UPDATE SET status = excluded.status";
            
            try (PreparedStatement recordPs = conn.prepareStatement(upsertRecordSql)) {
                for (Map.Entry<String, Boolean> entry : attendanceMap.entrySet()) {
                    String studentId = entry.getKey();
                    String status = entry.getValue() ? "Present" : "Absent";
                    
                    recordPs.setString(1, groupId);
                    recordPs.setString(2, currentDate);
                    recordPs.setString(3, studentId);
                    recordPs.setString(4, status);
                    
                    recordPs.addBatch();
                }
                recordPs.executeBatch();
            }
            
            conn.commit();
            System.out.println("Successfully saved attendance for " + attendanceMap.size() + " students");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Failed to save attendance: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Failed to rollback: " + ex.getMessage());
                }
            }
            return false;
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Load existing attendance for today
     */
    public Map<String, String> load_existing_attendance(String groupId) {
        Map<String, String> attendanceMap = new HashMap<>();
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        String sql = "SELECT student_id, status FROM attendanceRecords " +
                    "WHERE group_id = ? AND date = ?";
        
        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, groupId);
            ps.setString(2, currentDate);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String studentId = rs.getString("student_id");
                String status = rs.getString("status");
                attendanceMap.put(studentId, status);
            }
            
            if (!attendanceMap.isEmpty()) {
                System.out.println("Loaded existing attendance for " + attendanceMap.size() + 
                                 " students on " + currentDate);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to load existing attendance: " + e.getMessage());
        }
        
        return attendanceMap;
    }
}